package work;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class templet {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        ArrayList<String> fields = new ArrayList<>();
        String n = "";
        // 把字段名存入数组，>10因为空格和换行也占字符
        while((n = br.readLine()).length() > 10)
            fields.add(n.split("`")[1]);
        //nvl(fields);
        //normal(fields);
        //insert(fields);
        diff(fields);
    }

    // 非空记录数比较
    public static void nvl(ArrayList<String> fields){
        // 打印结果
        for(String s : fields) {
            System.out.println("  sum(case when nvl(a." + s + ", '" + s + "') = nvl(b." + s + ", '" + s + "') " +
                    "then 1 else 0 end) as " + s + "_equals,");
            System.out.println("  sum(case when nvl(a." + s + ", '" + s + "') != nvl(b." + s + ", '" + s + "') " +
                    "then 1 else 0 end) as " + s + "_not_equals,");
        }
    }

    // 正常值比较
    public static void normal(ArrayList<String> fields){
        for(String s : fields) {
            System.out.println("  sum(case when\n" +
                    "  (case when a." + s + " not in ('',-99,-999,-9999,-10000,'0000-00-00 00:00:00','9999-99-99 99:99:99') and a." + s + " is not null then a." + s + " else 'i1' end) =\n" +
                    "  (case when b." + s + " not in ('',-99,-999,-9999,-10000,'0000-00-00 00:00:00','9999-99-99 99:99:99') and b." + s + " is not null then b." + s + " else 'i2' end)\n" +
                    "  then 1 else 0 end) as normal_" + s + "_equals,");
            System.out.println("  sum(case when\n" +
                    "  (case when a." + s + " not in ('',-99,-999,-9999,-10000,'0000-00-00 00:00:00','9999-99-99 99:99:99') and a." + s + " is not null then a." + s + " else 'i1' end) !=\n" +
                    "  (case when b." + s + " not in ('',-99,-999,-9999,-10000,'0000-00-00 00:00:00','9999-99-99 99:99:99') and b." + s + " is not null then b." + s + " else 'i1' end)\n" +
                    "  then 1 else 0 end) as normal_" + s + "_not_equals,");
        }
    }

    // insert字段
    public static void insert(ArrayList<String> fields){
        for(String s : fields)
            System.out.println(s + ",");
    }

    // 差异查看字段
    public static void diff(ArrayList<String> fields){
        for(String s : fields){
            System.out.println("a." + s + " a_" + s + ",");
            System.out.println("b." + s + " b_" + s + ",");
        }
    }
}
