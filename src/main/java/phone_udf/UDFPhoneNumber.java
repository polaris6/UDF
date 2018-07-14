package phone_udf;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;

import java.util.regex.Pattern;

/**
 * @author zhangbo
 * @date 2018-07-10
 */

@Description(name = "get_phone_info",
        value = "_FUNC1_('phoneNumber') - " + "All the input parameters and output column types are string."
                + "_FUNC2_('phoneNumber',index) - " + "index is from 1 to 3,output column type is string")

public class UDFPhoneNumber extends UDF {

    String PHONE_NUMBER_REG = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\\d{8}$";

    public String evaluate(String phoneNumber, int index){

        if(valid(phoneNumber)) {
            String output = new String();

            if (index == 1) {
                output = PhoneUtils.getOperator(phoneNumber) + "|"
                        + PhoneUtils.getProvinceID(phoneNumber) + "," + PhoneUtils.getProvince(phoneNumber) + "|"
                        + PhoneUtils.getCityID(phoneNumber) + "," + PhoneUtils.getCity(phoneNumber);
            } else if (index == 2){
                output = PhoneUtils.getOperator(phoneNumber);
            } else if (index == 3){
                output = PhoneUtils.getProvinceID(phoneNumber) + "," + PhoneUtils.getProvince(phoneNumber);
            } else if (index == 4){
                output = PhoneUtils.getCityID(phoneNumber) + "," + PhoneUtils.getCity(phoneNumber);
            }
            else if (index == 5) {
                output = "true";
            } else if (index == 6) {
                output = phoneNumber.toString().substring(7, 11);
            }

            return output;
        }

        return "-99";
    }

    public String evaluate(String phoneNumber){
        if(valid(phoneNumber)){
            return PhoneUtils.getJsonOfPhoneInfo(phoneNumber);
        }
        return "-99";
    }

    public boolean valid(String phoneNumber){
        if(phoneNumber == null || !Pattern.matches(PHONE_NUMBER_REG, phoneNumber.toString()))
            return false;

        return true;
    }
}
