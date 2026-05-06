package main.java.SHL;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SHL_016 {


    /**
     * SHL_016: Count Elements Less Than K
     * 统计小于K的元素个数
     *
     * 题目描述：
     * Given a list of integers and an integer K, count the number of elements
     * in the list that are strictly less than K.
     *
     * 中文翻译：
     * 给定一个整数列表和一个整数K，统计列表中严格小于K的元素个数。
     *
     * 输入格式：
     * First line: an integer N, the size of the list
     * Second line: N space-separated integers, the elements of the list
     * Third line: an integer K
     *
     * 中文翻译：
     * 第一行：一个整数N，表示列表的大小
     * 第二行：N个用空格分隔的整数，表示列表的元素
     * 第三行：一个整数K
     *
     * 输出格式：
     * A single integer, the count of elements strictly less than K
     *
     * 中文翻译：
     * 一个整数，表示严格小于K的元素个数
     *
     * 约束条件：
     * 1 ≤ N ≤ 10^5
     * -10^9 ≤ each element ≤ 10^9
     * -10^9 ≤ K ≤ 10^9
     *
     * 示例：
     * Input:
     * 7
     * 1 7 4 5 6 3 2
     * 5
     *
     * Output:
     * 4
     *
     * 示例解释：
     * Elements strictly less than 5 are: 1, 4, 3, 2 → total 4 elements
     * 严格小于5的元素是：1, 4, 3, 2 → 总共4个元素
     */



        /**
         * 统计数组中严格小于k的元素个数
         *
         * @param arr 整数数组
         * @param k 比较值
         * @return 严格小于k的元素个数
         */
        public static int countElementsLessThanK(int[] arr, int k) {
            int count = 0;
            for (int num : arr) {
                if (num < k) {
                    count++;
                }
            }
            return count;
        }

        /**
         * 主函数，处理标准输入输出
         */
        public static void main1(String[] args) {
            Scanner scanner = new Scanner(System.in);

            // 读取数组大小
            int n = scanner.nextInt();

            // 读取数组元素
            int[] arr = new int[n];
            for (int i = 0; i < n; i++) {
                arr[i] = scanner.nextInt();
            }

            // 读取比较值K
            int k = scanner.nextInt();

            // 统计并输出结果
            int result = countElementsLessThanK(arr, k);
            System.out.println(result);

            scanner.close();
        }

        /**
         * 测试函数，包含多个测试用例
         */
        public static void testCountElementsLessThanK() {
            System.out.println("=== SHL_016 统计小于K的元素个数 测试开始 ===\n");

            // 测试用例1：题目示例
            int[] test1 = {1, 7, 4, 5, 6, 3, 2};
            int result1 = countElementsLessThanK(test1, 5);
            System.out.println("测试用例1 - 题目示例：");
            System.out.println("  数组: [1, 7, 4, 5, 6, 3, 2]");
            System.out.println("  K = 5");
            System.out.println("  预期结果: 4");
            System.out.println("  实际结果: " + result1);
            System.out.println("  测试" + (result1 == 4 ? "通过" : "失败") + "\n");

            // 测试用例2：所有元素都小于K
            int[] test2 = {1, 2, 3, 4, 5};
            int result2 = countElementsLessThanK(test2, 10);
            System.out.println("测试用例2 - 所有元素都小于K：");
            System.out.println("  数组: [1, 2, 3, 4, 5]");
            System.out.println("  K = 10");
            System.out.println("  预期结果: 5");
            System.out.println("  实际结果: " + result2);
            System.out.println("  测试" + (result2 == 5 ? "通过" : "失败") + "\n");

            // 测试用例3：没有元素小于K
            int[] test3 = {5, 6, 7, 8, 9};
            int result3 = countElementsLessThanK(test3, 1);
            System.out.println("测试用例3 - 没有元素小于K：");
            System.out.println("  数组: [5, 6, 7, 8, 9]");
            System.out.println("  K = 1");
            System.out.println("  预期结果: 0");
            System.out.println("  实际结果: " + result3);
            System.out.println("  测试" + (result3 == 0 ? "通过" : "失败") + "\n");

            // 测试用例4：包含负数和零
            int[] test4 = {-5, -1, 0, 3, 7};
            int result4 = countElementsLessThanK(test4, 0);
            System.out.println("测试用例4 - 包含负数和零：");
            System.out.println("  数组: [-5, -1, 0, 3, 7]");
            System.out.println("  K = 0");
            System.out.println("  预期结果: 2 (-5, -1)");
            System.out.println("  实际结果: " + result4);
            System.out.println("  测试" + (result4 == 2 ? "通过" : "失败") + "\n");

            // 测试用例5：空数组
            int[] test5 = {};
            int result5 = countElementsLessThanK(test5, 5);
            System.out.println("测试用例5 - 空数组：");
            System.out.println("  数组: []");
            System.out.println("  K = 5");
            System.out.println("  预期结果: 0");
            System.out.println("  实际结果: " + result5);
            System.out.println("  测试" + (result5 == 0 ? "通过" : "失败") + "\n");

            // 测试用例6：K为负数
            int[] test6 = {-10, -5, 0, 5, 10};
            int result6 = countElementsLessThanK(test6, -3);
            System.out.println("测试用例6 - K为负数：");
            System.out.println("  数组: [-10, -5, 0, 5, 10]");
            System.out.println("  K = -3");
            System.out.println("  预期结果: 2 (-10, -5)");
            System.out.println("  实际结果: " + result6);
            System.out.println("  测试" + (result6 == 2 ? "通过" : "失败") + "\n");

            // 测试用例7：边界值测试
            int[] test7 = {-1000000000, 0, 1000000000};
            int result7 = countElementsLessThanK(test7, 1000000000);
            System.out.println("测试用例7 - 边界值测试：");
            System.out.println("  数组: [-1000000000, 0, 1000000000]");
            System.out.println("  K = 1000000000");
            System.out.println("  预期结果: 2 (-1000000000, 0)");
            System.out.println("  实际结果: " + result7);
            System.out.println("  测试" + (result7 == 2 ? "通过" : "失败") + "\n");

            // 测试用例8：重复元素
            int[] test8 = {2, 2, 2, 2, 2};
            int result8 = countElementsLessThanK(test8, 3);
            System.out.println("测试用例8 - 重复元素：");
            System.out.println("  数组: [2, 2, 2, 2, 2]");
            System.out.println("  K = 3");
            System.out.println("  预期结果: 5");
            System.out.println("  实际结果: " + result8);
            System.out.println("  测试" + (result8 == 5 ? "通过" : "失败") + "\n");

            System.out.println("=== SHL_016 统计小于K的元素个数 测试完成 ===");
        }

        /**
         * 程序入口点，可以选择运行主函数或测试函数
         */
        public static void main(String[] args) {
            // 如果要运行测试，取消下面一行的注释
            // testCountElementsLessThanK();

            // 如果要运行主函数（从控制台输入），使用下面的main方法
            runMain();
        }

        /**
         * 包装的主函数，避免与测试冲突
         */
        public static void runMain() {
            main(new String[0]);
        }
    }
