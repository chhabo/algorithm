package basic;

import java.util.Scanner;

public class _28_最左侧冗余覆盖子串 {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String n1 = sc.nextLine();
        String n2 = sc.nextLine();
        int k = sc.nextInt();
        int res = -1;
        int strLen = n1.length() + k;
        for (int i = 0; i < n2.length() - strLen + 1; i++) {
            char[] chars = n2.substring(i, i + strLen).toCharArray();
            if (qiuzichuan(n1, chars)) {
                res = i;
                break;
            }
        }
        System.out.println(res);
    }

    public static boolean qiuzichuan(String s1, char[] chars) {
        int count = 0;
        for (int i = 0; i < s1.length(); i++) {
            for (int j = 0; j < chars.length; j++) {
                if (s1.charAt(i) == chars[j]) {
                    chars[j] = ' ';
                    count++;
                    break;
                }
            }
        }
        return count == s1.length();
    }

}
