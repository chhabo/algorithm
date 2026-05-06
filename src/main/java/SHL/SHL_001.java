package main.java.SHL;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SHL_001 {

    public int findMaxNum(){

        Scanner sc = new Scanner(System.in);
        int listLen = sc.nextInt();
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < listLen; i++){
            list.add(sc.nextInt());
        }
        int K = sc.nextInt();
        int count = 0;
        for (Integer num : list){
            if (num<K){
                count++;
            }
        }

        sc.close();
        return count;
    }


   /* 题目ID: SHL_001



    英文题目 (English Description):

    You are given a list of integers and an integer K.
    Write an algorithm to find the number of elements in the list that are strictly less than K.

    中文题目 (Chinese Description):

    给定一个整数列表和一个整数 K。编写一个算法，找出列表中严格小于 K 的元素数量。

    输入格式 (Input Format):

    第一行是一个整数 N，表示列表中元素的个数。

    第二行包含 N 个以空格分隔的整数，表示列表中的元素。

    第三行是一个整数 K。

    输出格式 (Output Format):

    输出一个整数，表示列表中严格小于 K 的元素个数。

    约束条件 (Constraints):

            -10⁹ ≤ K ≤ 10⁹

            -10⁹ ≤ 列表中的每个元素 ≤ 10⁹

    示例 (Example):

    复制
    输入 (Input):
            7
            1 7 4 5 6 3 2
            5

    输出 (Output):
            4*/
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int count = 0;
        int n  = scanner.nextInt();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            list.add(scanner.nextInt());
        }
        int k = scanner.nextInt();
        for (Integer num : list) {
            if (k > num){
                count++;
            }
        }
        System.out.println(count);
        scanner.close();
    }
}
