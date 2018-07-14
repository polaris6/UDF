package id_udf;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

/**
 * @author zhangbo
 * @date 2018-06-25
 */

@Description(name = "get_idcard_info",
        value = "_FUNC1_('idCard') - " + "All the input parameters and output column types are string."
                + "_FUNC2_('idCard',index) - " + "index is from 1 to 8,output column type is string")


public class UDFIDCard extends UDF{

    public String evaluate(String idCard){
        //先判断身份证号是否合法
        if(valid(idCard))
            return CardUtils.getJsonOfChinaIdCard(idCard.toString());
        else
            return "-99";
    }


    public String evaluate(String idCard, int index) {

        String output = new String();

        if(valid(idCard)) {
            /**
             * 1、获取省市县
             */
            if (index == 1)
                output = idCard.substring(0, 2) + "0000" + "," + CardUtils.getIdCardProvince(idCard.toString())
                        + "|" + idCard.substring(0, 4) + "00" + "," + CardUtils.getIdCardCity(idCard.toString())
                        + "|" + idCard.substring(0, 6) + "," + CardUtils.getIdCardArea(idCard.toString());

            /**
             * 2、获取性别
             */
            else if (index == 2)
                output = CardUtils.getGender(idCard.toString());

            /**
             * 3、获取星座
             */
            else if (index == 3) {
                int month = Integer.parseInt(CardUtils.getBirthday(idCard.toString()).substring(4, 6));
                int day;
                if (CardUtils.getBirthday(idCard.toString()).charAt(6) == '0')
                    day = CardUtils.getBirthday(idCard.toString()).charAt(7) - '0';
                else
                    day = Integer.parseInt(CardUtils.getBirthday(idCard.toString()).substring(6, 8));
                output = CardUtils.getZodiac(month, day);
            }

            /**
             * 4、获取生日
             */
            else if (index == 4)
                output = CardUtils.getBirthday(idCard.toString());

            /**
             * 5、获取年龄
             */
            else if (index == 5) {
                String birthday = CardUtils.getBirthday(idCard.toString());
                output = CardUtils.getAge(Integer.parseInt(birthday)) + "";
            }

            /**
             * 6、如果输入15位，转成18位返回，否则直接返回18位
             */
            else if (index == 6) {
                if (CardConvert.is15Idcard(idCard))
                    output = CardConvert.convertIdcarBy15bit(idCard);
                else
                    output = idCard;
            }

            /**
             * 7、验证身份证是否有效
             */
            else if (index == 7) {
                output = "true";
            }

            return output;
        }else
            return "-99";
    }


    public boolean valid(String idCard){
        if(idCard == null || !CardUtils.isValidIdCard(idCard.toString()))
            return false;
        else
            return true;
    }
}
