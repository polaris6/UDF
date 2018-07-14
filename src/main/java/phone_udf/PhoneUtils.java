package phone_udf;

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
import java.util.regex.Pattern;

/**
 * @author zhangbo
 * @date 2018-07-10
 * 用于读文件
 */
public class PhoneUtils {
    private static Logger logger = LoggerFactory.getLogger(PhoneUtils.class);
    private static final Map<String, PhoneInfo> phoneInfoMap = getPhoneNumberMap();
    static String PHONE_NUMBER_REG = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$";
    public static final List<String> mobile = new ArrayList<String>(){      //中国移动号段
        {
            this.add("134");
            this.add("135");
            this.add("136");
            this.add("137");
            this.add("138");
            this.add("139");
            this.add("147");
            this.add("150");
            this.add("151");
            this.add("152");
            this.add("157");
            this.add("158");
            this.add("159");
            this.add("178");
            this.add("182");
            this.add("183");
            this.add("184");
            this.add("187");
            this.add("188");
            this.add("198");
            this.add("1703");
            this.add("1705");
            this.add("1706");
        }
    };
    public static final List<String> unicom = new ArrayList<String>(){      //中国联通号段
        {
            this.add("130");
            this.add("131");
            this.add("132");
            this.add("145");
            this.add("155");
            this.add("156");
            this.add("166");
            this.add("171");
            this.add("175");
            this.add("176");
            this.add("185");
            this.add("186");
            this.add("1704");
            this.add("1707");
            this.add("1708");
            this.add("1709");
        }
    };
    public static final List<String> telecom = new ArrayList<String>(){      //中国电信号段
        {
            this.add("133");
            this.add("149");
            this.add("153");
            this.add("173");
            this.add("177");
            this.add("180");
            this.add("181");
            this.add("189");
            this.add("199");
            this.add("1700");
            this.add("1701");
            this.add("1702");
        }
    };


    public static List<String> loadFile(String fileName) throws IOException {
        ArrayList<String> strings = new ArrayList<>();
        BufferedReader bufferedReader = null;

        try {
            //读jar包内部文件
            InputStream inputStream = PhoneUtils.class.getResourceAsStream(fileName);
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

    public static Map<String, PhoneInfo> getPhoneNumberMap() {
        String fileName = "/phone_number.config";
        Map<String, PhoneInfo> map = new HashMap<>();
        try {
            List<String> list = loadFile(fileName);
            for (String line : list) {
                String[] results = line.split(",", 6);
                map.put(results[0], new PhoneInfo(results[1], results[2], results[3], results[4], results[5]));
            }
        } catch (IOException e) {
            logger.error("get china id card map error. error is {}.", e);
            return map;
        }

        return map;
    }

    private static PhoneInfo getPhoneValue(String phoneNumber){
        if (phoneNumber == null) {
            return null;
        }
        //手机号只有11位
        if (phoneNumber.toString().length() != 11) {
            return null;
        }
        String phonePrefix = phoneNumber.toString().substring(0, 7);
        if (phoneInfoMap.containsKey(phonePrefix)) {
            return phoneInfoMap.get(phonePrefix);
        }
        return null;
    }

    public static String getProvince(String phoneNumber) {
        PhoneInfo phoneInfo = getPhoneValue(phoneNumber);
        if (phoneInfo != null) {
            return phoneInfo.getProvince();
        }
        return null;
    }

    public static String getProvinceID(String phoneNumber) {
        PhoneInfo phoneInfo = getPhoneValue(phoneNumber);
        if (phoneInfo != null) {
            return phoneInfo.getProvinceID();
        }
        return null;
    }

    public static String getCity(String phoneNumber) {
        PhoneInfo phoneInfo = getPhoneValue(phoneNumber);
        if (phoneInfo != null) {
            return phoneInfo.getCity();
        }
        return null;
    }

    public static String getCityID(String phoneNumber) {
        PhoneInfo phoneInfo = getPhoneValue(phoneNumber);
        if (phoneInfo != null) {
            return phoneInfo.getCityID();
        }
        return null;
    }

    public static String getOperator(String phoneNumber) {
        String prefix = null;
        if (!phoneNumber.substring(0, 3).equals("170")) {
            prefix = phoneNumber.substring(0, 3);
        } else
            prefix = phoneNumber.substring(0, 4);

        if (mobile.contains(prefix))
            return "中国移动";
        else if (unicom.contains(prefix))
            return "中国联通";
        else if (telecom.contains(prefix))
            return "中国电信";

        return null;
    }

    //获得Json串，包括全部信息
    public static String getJsonOfPhoneInfo(String phoneNumber) {

        Map<String, Object> map = new HashMap<>();
        map.put("isValidPhoneNumber", Pattern.matches(PHONE_NUMBER_REG, phoneNumber.toString()));
        map.put("operatorAndArea", PhoneUtils.getOperator(phoneNumber) + "|"
                + PhoneUtils.getProvinceID(phoneNumber) + "," + PhoneUtils.getProvince(phoneNumber) + "|"
                + PhoneUtils.getCityID(phoneNumber) + "," + PhoneUtils.getCity(phoneNumber));
        map.put("operator", PhoneUtils.getOperator(phoneNumber));
        map.put("province", PhoneUtils.getProvinceID(phoneNumber) + "," + PhoneUtils.getProvince(phoneNumber));
        map.put("city", PhoneUtils.getCityID(phoneNumber) + "," + PhoneUtils.getCity(phoneNumber));
        map.put("lastFourNumbers", phoneNumber.toString().substring(7, 11));

        String jsonString = "{";

        jsonString += "\"operatorAndArea\":\"" + map.get("operatorAndArea").toString() + "\","
                + "\"operator\":\"" + map.get("operator").toString() + "\","
                + "\"province\":\"" + map.get("province").toString() + "\","
                + "\"city\":\"" + map.get("city").toString() + "\","
                + "\"isValidPhoneNumber\":\"" + map.get("isValidPhoneNumber").toString() + "\","
                + "\"lastFourNumbers\":\"" + map.get("lastFourNumbers").toString() + "\"}";

        return jsonString;
    }
}
