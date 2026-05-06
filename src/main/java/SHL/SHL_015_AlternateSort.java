package main.java.SHL;


import java.util.*;


import java.util.Arrays;
        import java.util.Scanner;

/**
 * SHL_015: Alternate Sort 交替排序
 *
 * 题目描述：
 * Given an unsorted list of integers, you need to sort the list in ascending order
 * and then extract elements at alternate positions starting from the first element.
 *
 * 中文翻译：
 * 给定一个未排序的整数列表，你需要将列表按升序排序，然后从第一个元素开始提取交替位置的元素。
 *
 * 输入格式：
 * The first line contains an integer n, the size of the list.
 * The second line contains n space-separated integers, the elements of the list.
 *
 * 中文翻译：
 * 第一行包含一个整数 n，表示列表的大小。
 * 第二行包含 n 个用空格分隔的整数，表示列表的元素。
 *
 * 输出格式：
 * Output the alternate elements of the sorted list, separated by a single space.
 * The alternate elements are those at positions 0, 2, 4, ... (0-based indexing).
 *
 * 中文翻译：
 * 输出排序后列表的交替元素，用单个空格分隔。
 * 交替元素是指位置为 0, 2, 4, ... 的元素（基于0的索引）。
 *
 * 约束条件：
 * 1 ≤ n ≤ 10^6
 * -10^9 ≤ each element ≤ 10^9
 *
 * 示例：
 * Input:
 * 8
 * 3 5 1 5 9 10 2 6
 *
 * Output:
 * 1 3 5 9
 *
 * 示例解释：
 * 排序后的列表: [1, 2, 3, 5, 5, 6, 9, 10]
 * 交替位置元素(索引0,2,4,6): 1, 3, 5, 9
 */

public class SHL_015_AlternateSort {

    /**
     * 对数组进行排序并提取交替位置元素的算法
     *
     * @param arr 输入的整数数组
     * @return 排序后交替位置元素的字符串表示
     */
    public static String alternateSort(int[] arr) {
        // 1. 对数组进行升序排序
        Arrays.sort(arr);

        // 2. 构建结果字符串，提取偶数索引(0,2,4,...)的元素
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < arr.length; i += 2) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(arr[i]);
        }

        return result.toString();
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

        // 计算并输出结果
        String result = alternateSort(arr);
        System.out.println(result);

        scanner.close();
    }

    /**
     * 测试函数，包含多个测试用例
     */
    public static void testAlternateSort() {
        System.out.println("=== SHL_015 Alternate Sort 测试开始 ===\n");

        // 测试用例1：题目示例
        int[] test1 = {3, 5, 1, 5, 9, 10, 2, 6};
        String result1 = alternateSort(test1);
        System.out.println("测试用例1 - 题目示例：");
        System.out.println("  输入数组: [3, 5, 1, 5, 9, 10, 2, 6]");
        System.out.println("  预期输出: 1 3 5 9");
        System.out.println("  实际输出: " + result1);
        System.out.println("  测试" + (result1.equals("1 3 5 9") ? "通过" : "失败") + "\n");

        // 测试用例2：奇数个元素
        int[] test2 = {7, 3, 9, 1, 5};
        String result2 = alternateSort(test2);
        System.out.println("测试用例2 - 奇数个元素：");
        System.out.println("  输入数组: [7, 3, 9, 1, 5]");
        System.out.println("  排序后: [1, 3, 5, 7, 9]");
        System.out.println("  预期输出: 1 5 9");
        System.out.println("  实际输出: " + result2);
        System.out.println("  测试" + (result2.equals("1 5 9") ? "通过" : "失败") + "\n");

        // 测试用例3：单个元素
        int[] test3 = {42};
        String result3 = alternateSort(test3);
        System.out.println("测试用例3 - 单个元素：");
        System.out.println("  输入数组: [42]");
        System.out.println("  预期输出: 42");
        System.out.println("  实际输出: " + result3);
        System.out.println("  测试" + (result3.equals("42") ? "通过" : "失败") + "\n");

        // 测试用例4：已排序数组
        int[] test4 = {1, 2, 3, 4, 5, 6};
        String result4 = alternateSort(test4);
        System.out.println("测试用例4 - 已排序数组：");
        System.out.println("  输入数组: [1, 2, 3, 4, 5, 6]");
        System.out.println("  预期输出: 1 3 5");
        System.out.println("  实际输出: " + result4);
        System.out.println("  测试" + (result4.equals("1 3 5") ? "通过" : "失败") + "\n");

        // 测试用例5：包含负数和重复元素
        int[] test5 = {-5, 0, -3, 7, -5, 2, 2};
        String result5 = alternateSort(test5);
        System.out.println("测试用例5 - 包含负数和重复元素：");
        System.out.println("  输入数组: [-5, 0, -3, 7, -5, 2, 2]");
        System.out.println("  排序后: [-5, -5, -3, 0, 2, 2, 7]");
        System.out.println("  预期输出: -5 -3 2");
        System.out.println("  实际输出: " + result5);
        System.out.println("  测试" + (result5.equals("-5 -3 2") ? "通过" : "失败") + "\n");

        // 测试用例6：所有元素相同
        int[] test6 = {8, 8, 8, 8, 8};
        String result6 = alternateSort(test6);
        System.out.println("测试用例6 - 所有元素相同：");
        System.out.println("  输入数组: [8, 8, 8, 8, 8]");
        System.out.println("  预期输出: 8 8 8");
        System.out.println("  实际输出: " + result6);
        System.out.println("  测试" + (result6.equals("8 8 8") ? "通过" : "失败") + "\n");

        // 测试用例7：大数值测试
        int[] test7 = {1000000000, -1000000000, 0, 999999999, -999999999};
        String result7 = alternateSort(test7);
        System.out.println("测试用例7 - 大数值测试：");
        System.out.println("  输入数组: [1000000000, -1000000000, 0, 999999999, -999999999]");
        System.out.println("  排序后: [-1000000000, -999999999, 0, 999999999, 1000000000]");
        System.out.println("  预期输出: -1000000000 0 1000000000");
        System.out.println("  实际输出: " + result7);
        System.out.println("  测试" + (result7.equals("-1000000000 0 1000000000") ? "通过" : "失败") + "\n");

        System.out.println("=== SHL_015 Alternate Sort 测试完成 ===");
    }

    /**
     * 程序入口点，可以选择运行主函数或测试函数
     */
    public static void main(String[] args) {
        // 如果要运行测试，取消下面一行的注释
         testAlternateSort();

        // 如果要运行主函数（从控制台输入），使用下面的main方法
//        runMain();
    }

    /**
     * 包装的主函数，避免与测试冲突
     */
    public static void runMain() {
        main1(new String[0]);
    }
}
