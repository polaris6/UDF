/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package udtf;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDTF;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.io.Text;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * GenericUDTFBinlogTuple: this
 *
 */
@Description(name = "binlog_tuple",
        value = "_FUNC_(jsonStr, p1, p2, ..., pn) - like json_tuple, but it can be used to parse binlog. " +
                "All the input parameters and output column types are string.")

public class GenericUDTFBinlogTuple extends GenericUDTF {

    private static final Logger LOG = LoggerFactory.getLogger(GenericUDTFBinlogTuple.class.getName());

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    static {
        // Allows for unescaped ASCII control characters in JSON values
        JSON_FACTORY.enable(Feature.ALLOW_UNQUOTED_CONTROL_CHARS);
        // Enabled to accept quoting of all character backslash qooting mechanism
        JSON_FACTORY.enable(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
    }
    private static final ObjectMapper MAPPER = new ObjectMapper(JSON_FACTORY);
    private static final JavaType MAP_TYPE = TypeFactory.fromClass(Map.class);

    int numCols;    // number of output columns
    String[] paths; // array of path expressions, each of which corresponds to a column
    private transient Text[] retCols; // array of returned column values
    //object pool of non-null Text, avoid creating objects all the time
    private transient Text[] cols;
    private transient Object[] nullCols; // array of null column values
    private transient ObjectInspector[] inputOIs; // input ObjectInspectors
    boolean pathParsed = false;
    boolean seenErrors = false;

    private String columnName = "n";  // columns中每个json对象的字段名对应的key："n":"order_id"，通过get(columnName)拿到字段名
    private String columnValue = "v"; // columns中每个json对象的值对应的key："v":"35187716342120"，通过get(columnValue)拿到值
    private String columns = "columns."; // 代表内层字段的前缀
    // 包含所有外层字段名的数组
    private String[] externalColumns = {"xid", "binlog", "time", "canalTime", "db", "table", "event", "columns", "keys"};
    // 用于存"columns"对应jsonArray中所有的json对象，key是"n"对应的字段名，value是"v"对应的数值
    private Map<String, Object> columnMap = new HashMap<>();

    //An LRU cache using a linked hash map
    static class HashCache<K, V> extends LinkedHashMap<K, V> {

        private static final int CACHE_SIZE = 16;
        private static final int INIT_SIZE = 32;
        private static final float LOAD_FACTOR = 0.6f;

        HashCache() {
            super(INIT_SIZE, LOAD_FACTOR);
        }

        private static final long serialVersionUID = 1;

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > CACHE_SIZE;
        }

    }

    private transient Map<String, Object> jsonObjectCache;

    @Override
    public void close() throws HiveException {
    }

    //输入输出参数：输入参数ObjectInspector与输出参数StructObjectInspector
    @Override
    public StructObjectInspector initialize(ObjectInspector[] args)
            throws UDFArgumentException {

        inputOIs = args;
        numCols = args.length - 1;
        jsonObjectCache = new HashCache<>();

        if (numCols < 1) {
            throw new UDFArgumentException("binlog_tuple() takes at least two arguments: " +
                    "the json string and a path expression");
        }

        for (int i = 0; i < args.length; ++i) {
            if (args[i].getCategory() != ObjectInspector.Category.PRIMITIVE ||
                    !args[i].getTypeName().equals(serdeConstants.STRING_TYPE_NAME)) {
                throw new UDFArgumentException("binlog_tuple()'s arguments have to be string type");
            }
        }

        seenErrors = false;
        pathParsed = false;
        paths = new String[numCols];
        cols = new Text[numCols];
        retCols = new Text[numCols];
        nullCols = new Object[numCols];

        for (int i = 0; i < numCols; ++i) {
            cols[i] = new Text();
            retCols[i] = cols[i];
            nullCols[i] = null;
        }

        // construct output object inspector
        ArrayList<String> fieldNames = new ArrayList<String>(numCols);
        ArrayList<ObjectInspector> fieldOIs = new ArrayList<ObjectInspector>(numCols);
        for (int i = 0; i < numCols; ++i) {
            // column name can be anything since it will be named by UDTF as clause
            fieldNames.add("c" + i);
            // all returned type will be Text
            fieldOIs.add(PrimitiveObjectInspectorFactory.writableStringObjectInspector);
        }
        return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, fieldOIs);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(Object[] o) throws HiveException {

        if (o[0] == null) {
            forward(nullCols);
            return;
        }
        // get the path expression for the 1st row only  只用对表中第一行数据进行下面的if操作，往paths里存字段名，之后每行都不用再操作
        // paths数组用于存字段名，外层字段直接存，内层字段都加上columns.前缀
        if (!pathParsed) {
            for (int i = 0;i < numCols; ++i) {
                // 如果externalColumns数组包含传入的这个列名，即是外层字段，不用变
                // inputOIs[1]~inputOIs[numCols] 和 o[1]~o[numCols] 存的是字段名
                if(Arrays.asList(externalColumns).contains(o[i + 1].toString()))
                    paths[i] = ((StringObjectInspector) inputOIs[i + 1]).getPrimitiveJavaObject(o[i + 1]);
                else{
                    // 如果传入的列名包含columns.前缀，直接转成String传进去
                    if(o[i + 1].toString().contains(columns))
                        paths[i] = o[i + 1].toString();
                    // 如果传入的列名不包含columns.前缀，在列名前加上columns.前缀，转成String穿进去
                    else
                        paths[i] = columns + o[i + 1].toString();
                }
            }
            pathParsed = true;
        }

        //inputOIs[0]和o[0]存的是json字符串
        String jsonStr = ((StringObjectInspector) inputOIs[0]).getPrimitiveJavaObject(o[0]);
        if (jsonStr == null) {
            forward(nullCols);
            return;
        }
        try {
            Object jsonObj = jsonObjectCache.get(jsonStr);
            if (jsonObj == null) {
                try {
                    jsonObj = MAPPER.readValue(jsonStr, MAP_TYPE);
                } catch (Exception e) {
                    reportInvalidJson(jsonStr);
                    forward(nullCols);
                    return;
                }
                jsonObjectCache.put(jsonStr, jsonObj);
            }

            if (!(jsonObj instanceof Map)) {
                reportInvalidJson(jsonStr);
                forward(nullCols);
                return;
            }

            // columnObject为"columns"对应的jsonArray
            Object columnObject = ((Map<String, Object>)jsonObj).get("columns");

            // columnMap中key是每个json串的"n"对应的字段名，value是"v"对应的数值
            if(columnObject != null) {
                 for (Object obj : (List<Object>) columnObject)
                     columnMap.put(((Map<String, Object>) obj).get(columnName).toString(), ((Map<String, Object>) obj).get(columnValue));
            }

            for (int i = 0; i < numCols; ++i) {
                if (retCols[i] == null) {
                    retCols[i] = cols[i]; // use the object pool rather than creating a new object
                }
                Object extractObject = null;
                // 如果字段不包含columns.前缀，即外层字段，直接get
                if(!paths[i].contains(columns))
                    extractObject = ((Map<String, Object>)jsonObj).get(paths[i]);
                // 内层字段先用空字符替代"columns."，之后从columnMap中get
                else{
                    extractObject = columnMap.get(paths[i].replace(columns,""));
                }
                // 如果extractObject是Map或List的一个实例，比如"columns"对应的value，通过writeValueAsString方法存
                if (extractObject instanceof Map || extractObject instanceof List) {
                    retCols[i].set(MAPPER.writeValueAsString(extractObject));
                } else if (extractObject != null) {
                    retCols[i].set(extractObject.toString());
                } else {
                    retCols[i] = null;
                }
            }
            forward(retCols);
            return;
        } catch (Throwable e) {
            LOG.error("JSON parsing/evaluation exception" + e);
            System.out.println("exception: " + e);
            forward(nullCols);
        }
    }

    @Override
    public String toString() {
        return "binlog_tuple";
    }

    private void reportInvalidJson(String jsonStr) {
        if (!seenErrors) {
            LOG.error("The input is not a valid JSON string: " + jsonStr +
                    ". Skipping such error messages in the future.");
            seenErrors = true;
        }
    }
}

/*
json示例：json为表中的一个列名
json:
{
    "xid": "18994885768",
    "binlog": "492634100@dd-bin.010697",
    "time": 1526864759000,
    "canalTime": 1526864759764,
    "db": "dos_order",
    "table": "d_order_base_0531",
    "event": "d",
    "columns": [
        {
            "n": "xid",
            "t": "decimal(10,6)",
            "v": "119.052460",
            "null": false
        },
        {
            "n": "order_id",
            "t": "bigint(20) unsigned",
            "v": "17600362962233",
            "null": false
        },
        {
            "n": "passenger_id",
            "t": "bigint(20)",
            "v": "2890046907708",
            "null": false
        }
    ],
    "keys": ["order_id"]
}
 */