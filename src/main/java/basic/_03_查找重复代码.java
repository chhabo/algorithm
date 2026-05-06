package basic;

import java.util.Scanner;

public class _03_查找重复代码 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String text1 = sc.nextLine();
        String text2 = sc.nextLine();

        String minStr = text1.length() > text2.length()? text2:text1;
        String maxStr = minStr.equals(text1)? text2:text1;

        String resStr = "";
        for (int i = 0; i < minStr.length() - 1; i++) {
            for (int j = i+1; j < minStr.length(); j++) {
                String temp = minStr.substring(i,j);
                if (maxStr.contains(temp) && temp.length() > resStr.length()){
                    resStr = temp;
                }
            }

        }
        System.out.println(resStr);
    }

}
