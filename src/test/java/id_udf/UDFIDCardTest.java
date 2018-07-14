package id_udf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class UDFIDCardTest {

    UDFIDCard id = new UDFIDCard();

    @Test
    public void areaTest(){

        String idCard = "371323199401117258";
        String idCardOnlyProvince = "347321196703181128";

        assertEquals("areaNotEquals","370000,山东省|371300,临沂市|371323,沂水县", id.evaluate(idCard, 1));
        System.out.println("expected :370000,山东省|371300,临沂市|371323,沂水县" + "  actual :" + id.evaluate(idCard, 1));

        assertEquals("areaNotEquals","340000,安徽省|347300,null|347321,null", id.evaluate(idCardOnlyProvince, 1));
        System.out.println("expected :340000,安徽省|347300,null|347321,null" + "  actual :" + id.evaluate(idCardOnlyProvince, 1));
    }

    @Test
    public void genderTest(){

        String idCard = "371323199401117258";

        assertEquals("genderNotEquals","M", id.evaluate(idCard, 2));
        System.out.println("expected :M" + "  actual :" + id.evaluate(idCard, 2));
    }

    @Test
    public void zodiacTest(){

        String idCard = "371323199401117258";

        assertEquals("zodiacNotEquals","摩羯座", id.evaluate(idCard, 3));
        System.out.println("expected :摩羯座" + "  actual :" + id.evaluate(idCard, 3));
    }

    @Test
    public void birthdayTest(){

        String idCard = "371323199401117258";

        assertEquals("birthdayNotEquals","19940111", id.evaluate(idCard, 4));
        System.out.println("expected :19940111" + "  actual :" + id.evaluate(idCard, 4));
    }

    @Test
    public void ageTest(){
        String idCard = "371323199401117258";

        assertEquals("ageNotEquals","24",id.evaluate(idCard, 5));
        System.out.println("expected :24" + "  actual :" + id.evaluate(idCard, 5));
    }

    @Test
    public void convertIdTo18Test(){

        String idCard_15 = "347321670318112";  //15位身份证号
        String idCard_18 = "371323199401117258";  //18位身份证号

        assertEquals("covertIdTo18_NotEquals","347321196703181128", id.evaluate(idCard_15, 6));
        System.out.println("expected :347321196703181128" + "  actual :" + id.evaluate(idCard_15, 6));


        assertEquals("covertIdTo18_NotEquals","371323199401117258", id.evaluate(idCard_18, 6));
        System.out.println("expected :371323199401117258" + "  actual :" + id.evaluate(idCard_18, 6));
    }

    @Test
    public void validTest(){

        String trueIdCard = "410801199201042176";        //正确的身份证号
        //错误的身份证号：校验码错误，省编码错误，年份错误，月份错误，日错误，前17位存在非数字
        String[] falseIdCard = {"410801199201042171","660901198810082577","441600187511042139",
                "370402198213080791","230207198906314319","37132X199401117258"};

        assertEquals("true", id.evaluate(trueIdCard, 7));
        System.out.println(trueIdCard + " -- expected :true" + "  actual :" + id.evaluate(trueIdCard, 7));

        for(String message : falseIdCard) {
            assertEquals("-99", id.evaluate(message, 7));
            System.out.println("expected :-99" + "  actual :" + id.evaluate(message, 7));
        }
    }

    @Test
    public void ss(){
        String trueIdCard = "110101199003076878";
        System.out.println(id.evaluate(trueIdCard));
    }
}