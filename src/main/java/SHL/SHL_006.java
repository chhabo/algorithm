package main.java.SHL;

import java.util.*;

public class SHL_006 {

   /* 题目ID: SHL_006

    英文题目 (English Description):

    You are given N products in a supermarket, each with a price. Find the number of pairs of products where the absolute difference between their prices is exactly K. Note that each product can be in at most one pair, and (a, b) and (b, a) are considered the same pair.

            中文翻译 (Chinese Description):

    超市中有 N 个产品，每个产品有一个价格。请找出有多少对产品，它们价格之差的绝对值恰好等于 K。注意每个产品最多只能在一个对中出现，并且 (a, b) 和 (b, a) 被视为相同的对。

    输入格式 (Input Format):

    第一行：一个整数 N (1 ≤ N ≤ 10⁵)，产品的数量

    第二行：N 个用空格分隔的整数，表示每个产品的价格 (1 ≤ 价格 ≤ 10⁹)

    第三行：一个整数 K (0 ≤ K ≤ 10⁹)，目标价格差

    输出格式 (Output Format):

    一个整数，表示价格差恰好为 K 的产品对的数量

    约束条件 (Constraints):

            1 ≤ N ≤ 10⁵

            1 ≤ 价格 ≤ 10⁹

            0 ≤ K ≤ 10⁹

    示例1 (Example 1):

    复制
    输入 (Input):
            5
            1 5 3 4 2
            2

    输出 (Output):
            3

    解释：价格差为2的产品对是 (1,3), (2,4), (3,5)。注意 (5,3) 与 (3,5) 是同一个对。

    示例2 (Example 2):

    复制
    输入 (Input):
            6
            1 2 3 4 5 6
            0

    输出 (Output):
            0

    解释：当 K=0 时，需要找价格相同的产品对，但所有价格都不同，所以没有符合条件的对。

    示例3 (Example 3):

    复制
    输入 (Input):
            4
            1 1 2 2
            0

    输出 (Output):
            2

    解释：当 K=0 时，第一个1和第二个1可以配对，第一个2和第二个2可以配对，共2对。*/

    public static void main1(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int N = scanner.nextInt();
        int[] prices = new int[N];
        for (int i = 0; i < N; i++) {
            prices[i] = scanner.nextInt();
        }
        int K = scanner.nextInt();

        // 统计价格频率
        Map<Integer, Integer> freq = new HashMap<>();
        for (int price : prices) {
            freq.put(price, freq.getOrDefault(price, 0) + 1);
        }

        long pairs = 0;

        if (K == 0) {
            // K=0的情况：计算相同价格的组合数
            for (int count : freq.values()) {
                pairs += (long) count * (count - 1) / 2;
            }
        } else {
            // K≠0的情况：只检查price + K，避免重复
            for (int price : freq.keySet()) {
                int target = price + K;
                if (freq.containsKey(target)) {
                    pairs += (long) freq.get(price) * freq.get(target);
                }
            }
        }

        System.out.println(pairs);
        scanner.close();
    }

    public static void main(String[] args) {
        // 测试用例1：示例1
        test(new int[]{1, 5, 3, 4, 2}, 2, 3, "示例1测试");

        // 测试用例2：示例2
        test(new int[]{1, 2, 3, 4, 5, 6}, 0, 0, "示例2测试（K=0，无重复）");

        // 测试用例3：示例3
        test(new int[]{1, 1, 2, 2}, 0, 2, "示例3测试（K=0，有重复）");

        // 测试用例4：K≠0，有多个相同价格
        test(new int[]{1, 1, 2, 2, 3, 3}, 1, 8, "K=1，多个相同价格");

        // 测试用例5：K较大，无匹配
        test(new int[]{1, 2, 3, 4, 5}, 10, 0, "K较大，无匹配");

        // 测试用例6：单个元素
        test(new int[]{5}, 0, 0, "单个元素，K=0");

        // 测试用例7：所有元素相同，K≠0
        test(new int[]{7, 7, 7, 7}, 3, 0, "所有元素相同，K≠0");

        // 测试用例8：边界值测试
        test(new int[]{1, 1000000000}, 999999999, 1, "边界值测试");

        // 测试用例9：多个匹配
        test(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, 1, 9, "连续数字，K=1");

        // 测试用例10：负K（根据约束K≥0，但测试异常情况）
        test(new int[]{1, 2, 3}, -1, 0, "K为负数");
    }


    private static void test(int[] prices, int K, long expected, String testName) {
        // 统计价格频率
        Map<Integer, Integer> freq = new HashMap<>();
        for (int price : prices) {
            freq.put(price, freq.getOrDefault(price, 0) + 1);
        }

        long result = 0;

        if (K == 0) {
            // K=0的情况
            for (int count : freq.values()) {
                result += (long) count * (count - 1) / 2;
            }
        } else if (K > 0) {
            // K>0的情况
            for (int price : freq.keySet()) {
                int target = price + K;
                if (freq.containsKey(target)) {
                    result += (long) freq.get(price) * freq.get(target);
                }
            }
        }
        // K<0的情况，根据题目约束K≥0，所以不处理

        if (result == expected) {
            System.out.println("✓ " + testName + " 通过");
        } else {
            System.out.println("✗ " + testName + " 失败: 期望 " + expected + ", 实际 " + result);
        }
    }


}
