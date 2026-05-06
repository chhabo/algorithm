package advanced;

import java.util.Scanner;

public class _24_字母组合 {

    public static String noContain;
    public static char[][] digits = {
            {'a','b','c'}, {'d','e','f'}, {'g','h','i'}, {'j','k','l'}, {'m','n','o'}, {'p','q','r'}, {'s','t'}, {'u','v'}, {'w','x'}, {'y','z'}
    };
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String numStr = sc.nextLine();
        noContain = sc.nextLine(); //不包含的字符串
        StringBuffer res = new StringBuffer();

        handle(0, numStr, "",res);
        System.out.println(res);
    }
    /**
     * 回溯法求出所有可能的字母组合，并筛选出符合要求的（去除不能出现的字符）
     * @param index 字母索引
     * @param numStr 输入的数字字符串
     * @param temp 字母组合
     */
    public static void handle(int index, String numStr, String temp,StringBuffer res ){
        if(temp.length() == numStr.length()){ //字符串的长度等于数字字符串的长度说明已经遍历完成
            if(!temp.contains(noContain)){ //字符串不包含 noContain 的进行拼接
                res.append(temp + ",");
            }
        }else {
            int digitsIndex = numStr.charAt(index) - '0'; //字母的索引
            char[] chars = digits[digitsIndex];
            for( int i=0; i<chars.length; i++){
                handle(index + 1, numStr, temp + chars[i],res);
            }
        }
    }
}
