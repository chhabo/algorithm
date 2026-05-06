package main.java.SHL;

import java.util.Scanner;

public class SHL_004 {

    /*题目ID: SHL_004

    英文题目 (English Description):

    You are given a digit (0-9) and a positive integer (0 ≤ n ≤ 99,999,999). Count how many times the digit appears in the integer.

    中文翻译 (Chinese Description):

    给定一个数字（0-9）和一个正整数（0 ≤ n ≤ 99,999,999）。统计该数字在整数中出现的次数。

    输入格式 (Input Format):

    第一行：一个数字 needle（0-9）

    第二行：一个正整数 haystack（0 ≤ haystack ≤ 99,999,999）

    输出格式 (Output Format):

    一个整数，表示 needle在 haystack中出现的次数

    约束条件 (Constraints):

            0 ≤ needle ≤ 9

            0 ≤ haystack ≤ 99,999,999

    示例1 (Example 1):

    复制
    输入 (Input):
            2
            123228

    输出 (Output):
            3

    解释：数字2在123228中出现了3次。

    示例2 (Example 2):

    复制
    输入 (Input):
            7
            12345678

    输出 (Output):
            1

    解释：数字7在12345678中出现了1次。*/

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int h = sc.nextInt();

        String hStr = String.valueOf(h);
        char numChar = (char)(n + '0');
        char[] hArr = hStr.toCharArray();

        int count = 0;
        for(int i = 0; i < hArr.length; i ++){

            if (numChar == hArr[i]) {
                count++;
            }
        }
        System.out.print(count);

    }
    public static void main1(String[] args) {

        Scanner sc = new Scanner(System.in);
        int num = sc.nextInt();
        int haystack = sc.nextInt();

        String haystackStr = String.valueOf(haystack);
        char numChar = (char) (num + '0');

        int count = 0;
        char[] haystackChars = haystackStr.toCharArray();
        for (char haystackChar : haystackChars) {
            if (numChar == haystackChar){
                count++;
            }

        }
        System.out.println(count);
        sc.close();
    }

}
