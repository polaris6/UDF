package id_udf;

/**
 * @author zhangbo
 * @date 2016-06-25
 */
public class ChinaIdArea {
    private String province;
    private String city;
    private String county;

    public ChinaIdArea(String province, String city, String county) {
        this.province = province;
        this.city = city;
        this.county = county;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getCounty() {
        return county;
    }
}
