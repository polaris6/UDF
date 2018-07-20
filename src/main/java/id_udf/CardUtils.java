package id_udf;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author zhangbo
 * @date 2018-06-25
 */
public class CardUtils {
    private static final Map<String, ChinaIdArea> chinaIdAreaMap = ConfigUtils.getIdCardMap();
    private static int[] weight = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};    //十七位数字本体码权重
    private static char[] validate = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};    //mod11,对应校验码字符值
    private static final Date LEGAL_DATE_BEFORE = new Date("1900/01/01");
    private static final Date LEGAL_DATE_AFTER = new Date();

    private static ChinaIdArea getCardValue(String idCard) {

        if (idCard == null) {
            return null;
        }
        int cardLength = idCard.length();
        //身份证只有15位或18位
        if (cardLength != 15 && cardLength != 18) {
            return null;
        }
        String cardPrefix = idCard.substring(0, 6);
        if (chinaIdAreaMap.containsKey(cardPrefix)) {
            return chinaIdAreaMap.get(cardPrefix);
        }
/**
 //如果chinaIdAreaMap不包含这个身份证的前六位，判断包不包含前四位，定位到市
 for(String code : chinaIdAreaMap.keySet()){
 if(code.substring(0,4).equals(cardPrefix.substring(0,4)))
 return chinaIdAreaMap.get(cardPrefix.substring(0,4) + "00");
 }

 //如果chinaIdAreaMap不包含这个身份证的前六位和前四位，判断包不包含前两位，定位到省
 for(String code : chinaIdAreaMap.keySet()){
 if(code.substring(0,2).equals(cardPrefix.substring(0,2)))
 return chinaIdAreaMap.get(cardPrefix.substring(0,2) + "0000");
 }
 */

        return null;
    }

    //获得身份证省份
    public static String getIdCardProvince(String idCard) {
        idCard = idCard.substring(0,2) + "0000" + idCard.substring(6,idCard.length());
        ChinaIdArea chinaIdArea = getCardValue(idCard);
        if (chinaIdArea != null) {
            return chinaIdArea.getProvince();
        }
        return null;
    }

    //获得身份证市
    public static String getIdCardCity(String idCard) {
        idCard = idCard.substring(0,4) + "00" + idCard.substring(6,idCard.length());
        ChinaIdArea chinaIdArea = getCardValue(idCard);
        if (chinaIdArea != null) {
            return chinaIdArea.getCity();
        }
        return null;
    }

    //获得身份证县
    public static String getIdCardArea(String idCard) {
        ChinaIdArea chinaIdArea = getCardValue(idCard);
        if (chinaIdArea != null) {
            return chinaIdArea.getArea();
        }
        return null;
    }

    //获得生日
    public static String getBirthday(String idCard) {
        if (isValidIdCard(idCard)) {
            int cardLength = idCard.length();
            if (cardLength == 15) {
                return "19" + idCard.substring(6, 12);
            } else {
                return idCard.substring(6, 14);
            }
        }
        return null;
    }

    //获得性别
    public static String getGender(String idCard) {
        if (isValidIdCard(idCard)) {
            int cardLength = idCard.length();
            int genderValue;
            if (cardLength == 15) {
                genderValue = idCard.charAt(14) - '0';
            } else {
                genderValue = idCard.charAt(16) - '0';
            }
            if (genderValue % 2 == 0) {
                return "F";
            } else {
                return "M";
            }
        }
        return null;
    }

    //获得年龄
    public static int getAge(int birthday) {
        Calendar c = Calendar.getInstance();
        String y = "" + c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);
        int today = Integer.parseInt(y + (m < 10 ? "0" + m : m) + (d < 10 ? "0" + d : d));
        if (today - birthday < 10000)
            return 0;
        return (today - birthday) / 10000;
    }

    //获得星座
    public static String getZodiac(int month, int day) {
        String[] zodiacArray = {"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座"};
        int[] splitDay = {19, 18, 20, 20, 20, 21, 22, 22, 22, 22, 21, 21}; // 两个星座分割日
        int index = month;
        // 所查询日期在分割日之前，索引-1，否则不变
        if (day <= splitDay[month - 1]) {
            index = index - 1;
        } else if (month == 12) {
            index = 0;
        }
        // 返回索引指向的星座string
        return zodiacArray[index];
    }

    /**
     * 判断是否是正确的身份证号，如果是15位先转成18位判断
     */
    public static boolean isValidIdCard(String idCard) {
        boolean flag = false;
        if (!idCard.isEmpty() && idCard != null) {
            int cardLength = idCard.length();
            if(cardLength != 15 && cardLength != 18)
                return flag;
            if(cardLength == 15) {
                if(!idCard.matches("^[0-9]+$"))
                    return flag;
                idCard = CardConvert.convertIdcarBy15bit(idCard);
            }
            flag = isValidate18Idcard(idCard);
        }
        return flag;
    }

    //获得Json串，包括全部信息
    public static String getJsonOfChinaIdCard(String idCard) {

        Map<String, Object> map = new HashMap<>();
        map.put("isValidIDCard",isValidIdCard(idCard));
        String id = idCard;
        if (CardConvert.is15Idcard(id))
            id = CardConvert.convertIdcarBy15bit(id);
        map.put("idCard",id);
        map.put("area", idCard.substring(0, 2) + "0000" + "," + CardUtils.getIdCardProvince(idCard.toString())
                + "|" + idCard.substring(0, 4) + "00" + "," + CardUtils.getIdCardCity(idCard.toString())
                + "|" + idCard.substring(0, 6) + "," + CardUtils.getIdCardArea(idCard.toString()));
        map.put("birthday",getBirthday(id));
        int month = Integer.parseInt(CardUtils.getBirthday(id.toString()).substring(4,6));
        int day;
        if(CardUtils.getBirthday(id.toString()).charAt(6) == '0')
            day = CardUtils.getBirthday(id.toString()).charAt(7) - '0';
        else
            day = Integer.parseInt(CardUtils.getBirthday(id.toString()).substring(6,8));
        map.put("zodiac",getZodiac(month, day));
        map.put("gender", getGender(id));
        map.put("age",getAge(Integer.parseInt(getBirthday(id))));

        String jsonString = "{";

        jsonString += "\"area\":\"" + map.get("area").toString() + "\","
                + "\"gender\":\"" + map.get("gender").toString() + "\","
                + "\"zodiac\":\"" + map.get("zodiac").toString() + "\","
                + "\"birthday\":\"" + map.get("birthday").toString() + "\","
                + "\"age\":\"" + map.get("age").toString() + "\","
                + "\"idCard\":\"" + map.get("idCard").toString() + "\","
                + "\"isValidIDCard\":\"" + map.get("isValidIDCard").toString() + "\"}";

        return jsonString;
    }

    /**
     * 判断18位身份证号的正确性
     * @param idCard
     * @return
     */
    public static boolean isValidate18Idcard(String idCard) {
        // 非18位为假
        if (idCard.length() != 18) {
            return false;
        }
        // 获取前17位
        String idcard17 = idCard.substring(0, 17);

        // 前17位是否全都为数字
        if (idcard17.matches("^[0-9]+$")) {
            String provinceid = idCard.substring(0, 2);
            String birthday = idCard.substring(6, 14);
            int year = Integer.parseInt(idCard.substring(8, 10));   //取year后两位
            int month = Integer.parseInt(idCard.substring(10, 12));
            int day = Integer.parseInt(idCard.substring(12, 14));

            // 判断是否为合法的省份
            boolean flag = false;
            for (String id : CardConvert.cityCode) {
                if (id.equals(provinceid)) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return false;
            }

            // 该身份证出生日期在当前日期之后为假，在1900年之前为假
            Date birthdate = null;
            try {
                birthdate = new SimpleDateFormat("yyyyMMdd").parse(birthday);
                if (birthdate.before(LEGAL_DATE_BEFORE) || birthdate.after(LEGAL_DATE_AFTER)) {
                    return false;
                }
            } catch (ParseException e) {
                return false;
            }

            // 判断是否为合法的年份
            GregorianCalendar curDay = new GregorianCalendar();
            int curYear = curDay.get(Calendar.YEAR);
            int year2bit = Integer.parseInt(String.valueOf(curYear).substring(2));

            // 判断该年份的两位表示法，小于30的和大于当前年份的，为假
            if ((year < 30 && year > year2bit)) {
                return false;
            }

            // 判断是否为合法的月份
            if (month < 1 || month > 12) {
                return false;
            }

            // 判断是否为合法的日期
            boolean mflag = false;
            curDay.setTime(birthdate); // 将该身份证的出生日期赋于对象curDay
            switch (month) {
                case 1:
                case 3:
                case 5:
                case 7:
                case 8:
                case 10:
                case 12:
                    mflag = (day >= 1 && day <= 31);
                    break;
                case 2: // 公历的2月非闰年有28天,闰年的2月是29天。
                    if (curDay.isLeapYear(curDay.get(Calendar.YEAR))) {
                        mflag = (day >= 1 && day <= 29);
                    } else {
                        mflag = (day >= 1 && day <= 28);
                    }
                    break;
                case 4:
                case 6:
                case 9:
                case 11:
                    mflag = (day >= 1 && day <= 30);
                    break;
            }
            if (!mflag) {
                return false;
            }
        }else if(!idcard17.matches("^[0-9]+$"))
            return false;

        //判断第18位校验码是否正确
        char validateCode = getValidateCode(idcard17);
        if (validateCode != idCard.charAt(17)) {
            return false;
        }

        return true;
    }


    /**
     * 获取正确的校验码(第18位)
     */
    private static char getValidateCode(String card17) {
        int sum = 0, mode = 0;
        for (int i = 0; i < card17.length(); i++) {
            sum = sum + (card17.charAt(i) - 48) * weight[i];
        }
        mode = sum % 11;
        return validate[mode];
    }
}
