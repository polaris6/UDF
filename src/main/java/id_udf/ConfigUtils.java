package id_udf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbo
 * @date 2018-06-25
 * 用于读文件
 */
public class ConfigUtils {
    private static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static List<String> loadFile(String fileName) throws IOException {
        ArrayList<String> strings = new ArrayList<>();
        BufferedReader bufferedReader = null;
        try {
            //读jar包内部文件
            InputStream inputStream = ConfigUtils.class.getResourceAsStream(fileName);
            //读jar包外部文件，out.config是与jar包在同一文件目录下
            //InputStream inputStream = PhoneUtils.class.getResourceAsStream("out.config");
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty() && line == null || line.startsWith("#")) {
                    continue;
                }
                strings.add(line);
            }
        } catch (IOException e) {
            logger.error("loadFile {} error. error is {}.", fileName, e);
            throw e;
        } finally {
            bufferedReader.close();
        }

        return strings;
    }

    public static Map<String, ChinaIdArea> getIdCardMap() {
        String fileName = "/dim_country.config";
        Map<String, ChinaIdArea> map = new HashMap<>();
        try {
            List<String> list = loadFile(fileName);
            for (String line : list) {
                String[] results = line.split("\t", 4);
                map.put(results[0], new ChinaIdArea(results[1], results[2], results[3]));
            }
        } catch (IOException e) {
            logger.error("get china id card map error. error is {}.", e);
            return map;
        }

        return map;
    }
}
