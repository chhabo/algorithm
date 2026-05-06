package main.java.SHL;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SHL_002 {
/*    题目ID: SHL_002

    英文题目:

    You are given a permutation of size N.
    A permutation is a sequence containing each element from 0 to N-1 exactly once.
    For each element, you need to output the index of the value in the original array.
    In other words, for each i from 0 to N-1, output the index where value i is located in the given permutation.

    中文翻译:

    给定一个大小为 N 的排列。排列是一个序列，其中包含从 0 到 N-1 的每个元素恰好一次。对于每个元素，您需要输出该值在原数组中的索引。换句话说，对于每个从 0 到 N-1 的 i，输出给定排列中值 i 所在的位置索引。

    输入格式:

    第一行包含一个整数 N (1 ≤ N ≤ 10⁵)，排列的大小

    第二行包含 N 个空格分隔的整数，表示排列的元素（每个元素是 0 到 N-1 之间的唯一整数）

    输出格式:

    输出一行，包含 N 个空格分隔的整数，其中第 i 个整数（0-based）表示值 i 在原排列中的索引

    约束条件:

            1 ≤ N ≤ 10⁵

    排列中的元素是 0 到 N-1 的唯一整数

    示例:

    输入：

        5
        4 2 3 0 1

    输出：

        3 4 1 2 0

    解释：

    值0在索引3

    值1在索引4

    值2在索引1

    值3在索引2

    值4在索引0*/

    public static int test(){
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i++){
            list.add(sc.nextInt());
        }
        int[] result = new int[n];

        for (int i = 0; i < n; i++){
            result[list.get(i)] = i;
        }

        for(int i = 0; i < n; i++){
            System.out.print(result[i]);
        }
        return 0;
    }



    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();

        List<Integer> list = new ArrayList<>(n);
        for(int i = 0; i < n ; i++){
            list.add(sc.nextInt());
        }

        int[] result = new int[n];

        for(int i = 0; i< n; i++){
            result[list.get(i)] = i;
        }

        for (int i=0; i < n; i++){
            System.out.print(result[i] + " ");
        }

    }


}
