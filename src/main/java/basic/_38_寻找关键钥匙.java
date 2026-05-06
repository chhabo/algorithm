package basic;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class _38_寻找关键钥匙 {


    public static void main(String[] args) {
        Scanner sc =new Scanner(System.in);

        String k = sc.nextLine();
        String[] boxs = sc.nextLine().split(" ");

        String[] resBoxs = new String[boxs.length];

        for (int i = 0; i < resBoxs.length; i++) {

            char[] chars = boxs[i].toLowerCase(Locale.ROOT).toCharArray();
            StringBuffer sb = new StringBuffer();
            for (int j = 0; j < chars.length; j++) {
                if (chars[j] >='a' && chars[j]<='z') {
                    sb.append(chars[j]);
                }
            }
            if (sb.length() == 0){
                sb.append(" ");
            }
            char[] charArray = sb.toString().toCharArray();
            Arrays.sort(charArray);
            resBoxs[i] = String.valueOf(charArray);
        }
        final char[] charArray = k.toCharArray();
        Arrays.sort(charArray);
        k = String.valueOf(charArray);

        int res =-1;

        for (int i = 0; i < resBoxs.length; i++) {
            if (k.equals(resBoxs[i])){
                res = i+1;
                break;
            }

        }
        System.out.println(res);


    }
}
