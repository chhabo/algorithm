package main.java.SHL;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SHL_007 {
   /* 题目ID: SHL_007

    英文题目 (English Description):

    You are given two lists of positive integers of different lengths. Write an algorithm to count the number of elements that are present in only one of the lists.

    中文翻译 (Chinese Description):

    给定两个长度不同的正整数列表。编写一个算法来计算仅存在于其中一个列表中的元素数量。

    输入格式 (Input Format):

    第一行：一个整数 listinput1_size，表示第一个列表的大小 N

    第二行：N 个用空格分隔的正整数，表示第一个列表的元素

    第三行：一个整数 listinput2_size，表示第二个列表的大小 M

    第四行：M 个用空格分隔的正整数，表示第二个列表的元素

    输出格式 (Output Format):

    一个正整数，表示仅存在于其中一个列表中的元素的总数量

    约束条件 (Constraints):

            1 ≤ N, M ≤ 10⁵（基于常见约束，图片中可能未明确，但合理假设）

    列表中的元素为正整数，范围在 1 到 10⁹ 之间（常见假设）

    列表可能包含重复元素

    示例 (Example):

    复制
    输入 (Input):
            11
            1 1 2 3 4 5 5 7 6 9 10
            10
            11 12 13 4 5 6 7 18 19 20

    输出 (Output):
            12

    示例解释 (Explanation):

    仅存在于一个列表中的元素是：1, 1, 2, 3, 9, 10, 11, 12, 13, 18, 19, 20。注意，元素 4, 5, 6, 7 同时出现在两个列表中，因此不计入。总数为 12。*/


    public static void main1(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 读取第一个列表
        int n = scanner.nextInt();
        Map<Integer, Integer> freq1 = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int num = scanner.nextInt();
            freq1.put(num, freq1.getOrDefault(num, 0) + 1);
        }

        // 读取第二个列表
        int m = scanner.nextInt();
        Map<Integer, Integer> freq2 = new HashMap<>();
        for (int i = 0; i < m; i++) {
            int num = scanner.nextInt();
            freq2.put(num, freq2.getOrDefault(num, 0) + 1);
        }

        // 计算仅存在于一个列表中的元素数量
        int count = 0;

        // 检查第一个列表中的元素
        for (Map.Entry<Integer, Integer> entry : freq1.entrySet()) {
            int num = entry.getKey();
            int count1 = entry.getValue();
            if (!freq2.containsKey(num)) {
                count += count1; // 该数字只存在于第一个列表
            }
        }

        // 检查第二个列表中的元素
        for (Map.Entry<Integer, Integer> entry : freq2.entrySet()) {
            int num = entry.getKey();
            int count2 = entry.getValue();
            if (!freq1.containsKey(num)) {
                count += count2; // 该数字只存在于第二个列表
            }
        }

        System.out.println(count);
        scanner.close();
    }



    public static void main(String[] args) {
        // 测试用例1：示例输入
        test(11, new int[]{1, 1, 2, 3, 4, 5, 5, 7, 6, 9, 10},
                10, new int[]{11, 12, 13, 4, 5, 6, 7, 18, 19, 20}, 12, "示例测试");

        // 测试用例2：无共同元素
        test(3, new int[]{1, 2, 3},
                3, new int[]{4, 5, 6}, 6, "无共同元素");

        // 测试用例3：完全相同元素
        test(3, new int[]{1, 2, 3},
                3, new int[]{1, 2, 3}, 0, "完全相同元素");

        // 测试用例4：部分重叠
        test(4, new int[]{1, 2, 2, 3},3, new int[]{2, 3, 4}, 2, "部分重叠，有重复元素");

        // 测试用例5：第一个列表为空
        test(0, new int[]{},
                3, new int[]{1, 2, 3}, 3, "第一个列表为空");

        // 测试用例6：第二个列表为空
        test(3, new int[]{1, 2, 3},
                0, new int[]{}, 3, "第二个列表为空");

        // 测试用例7：大列表测试
        test(5, new int[]{10, 20, 30, 40, 50},
                5, new int[]{10, 20, 30, 60, 70}, 4, "大列表测试");
    }

    private static void test(int n, int[] list1, int m, int[] list2, int expected, String testName) {
        // 构建频率映射
        Map<Integer, Integer> freq1 = new HashMap<>();
        for (int num : list1) {
            freq1.put(num, freq1.getOrDefault(num, 0) + 1);
        }

        Map<Integer, Integer> freq2 = new HashMap<>();
        for (int num : list2) {
            freq2.put(num, freq2.getOrDefault(num, 0) + 1);
        }

        int count = 0;
        for (Map.Entry<Integer, Integer> entry : freq1.entrySet()) {
            int num = entry.getKey();
            int count1 = entry.getValue();
            if (!freq2.containsKey(num)) {
                count += count1;
            }
        }

        for (Map.Entry<Integer, Integer> entry : freq2.entrySet()) {
            int num = entry.getKey();
            int count2 = entry.getValue();
            if (!freq1.containsKey(num)) {
                count += count2;
            }
        }

        if (count == expected) {
            System.out.println("✓ " + testName + " 通过");
        } else {
            System.out.println("✗ " + testName + " 失败: 期望 " + expected + ", 实际 " + count);
        }
    }




}
