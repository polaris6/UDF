package phone_udf;

/**
 * @author zhangbo
 * @date 2016-07-10
 */
public class PhoneInfo {

    private String operator;
    private String cityID;
    private String city;
    private String provinceID;
    private String province;


    public PhoneInfo(String operator, String cityID, String city, String provinceID, String province) {
        this.operator = operator;
        this.cityID = cityID;
        this.city = city;
        this.provinceID = provinceID;
        this.province = province;
    }

    public String getProvince() {
        return province;
    }

    public String getProvinceID() {
        return provinceID;
    }

    public String getCity() {
        return city;
    }

    public String getCityID() {
        return cityID;
    }

    public String getOperator() {
        return operator;
    }
}
