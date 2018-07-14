package phone_udf;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class UDFPhoneNumberTest {

    UDFPhoneNumber pn = new UDFPhoneNumber();

    @Test
    public void operatorAllTest(){
        assertEquals("中国移动|440000,广东省|440500,汕头市",pn.evaluate("13502726538",1));
        System.out.println(pn.evaluate("13502726538", 1));
    }

    @Test
    public void operatorTest(){
        assertEquals("中国移动",pn.evaluate("13502726538",2));
        System.out.println(pn.evaluate("13502726538", 2));
    }

    @Test
    public void proTest(){
        assertEquals("440000,广东省",pn.evaluate("13502726538",3));
        System.out.println(pn.evaluate("13502726538", 3));
    }

    @Test
    public void cityTest(){
        assertEquals("440500,汕头市",pn.evaluate("13502726538",4));
        System.out.println(pn.evaluate("13502726538", 4));
    }

    @Test
    public void isValidTest(){
        assertEquals("true",pn.evaluate("13502726538",5));
        System.out.println(pn.evaluate("13502726538", 5));
    }

    @Test
    public void lastFourNumbersTest(){
        assertEquals("6538",pn.evaluate("13502726538",6));
        System.out.println(pn.evaluate("13502726538", 6));
    }

    @Test
    public void jsonTest(){
        //assertEquals("{\"operator\":\"中国移动|440000,广东省|440500,汕头市\",\"isValidPhoneNumber\":\"true\",\"lastFourNumbers\":\"6538\"}",
             //   pn.evaluate("13502726538"));
        System.out.println(pn.evaluate("13502726538"));
    }
}
