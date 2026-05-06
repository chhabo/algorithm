package main.java.SHL;

import java.util.*;

public class SHL_020_FrequencySort {

    /**
     * SHL_020: Sort by Frequency
     * 按频率排序
     *
     * 题目描述：
     * Given a list of positive integers, sort the list in descending order of frequency.
     * If two numbers have the same frequency, they should appear in the same order
     * as they appear in the original list (i.e., maintain their relative order).
     *
     * 中文翻译：
     * 给定一个正整数列表，按照频率降序对列表进行排序。
     * 如果两个数字具有相同的频率，它们应按照原始列表中的顺序出现（即保持它们的相对顺序）。
     *
     * 输入格式：
     * First line: an integer n, the size of the list
     * Second line: n space-separated positive integers, the elements of the list
     *
     * 中文翻译：
     * 第一行：一个整数 n，表示列表的大小
     * 第二行：n 个用空格分隔的正整数，表示列表的元素
     *
     * 输出格式：
     * n space-separated integers, the sorted list
     *
     * 中文翻译：
     * n 个用空格分隔的整数，表示排序后的列表
     *
     * 约束条件：
     * 1 ≤ n ≤ 10^5
     * 1 ≤ each element ≤ 10^9
     *
     * 示例：
     * Input:
     * 10
     * 4 5 6 5 4 3 2 4 5 6
     *
     * Output:
     * 4 4 4 5 5 5 6 6 3 2
     *
     * 示例解释：
     * 频率统计：
     *   4出现3次，5出现3次，6出现2次，3出现1次，2出现1次
     * 按频率降序排序：
     *   频率3: 4和5 → 保持原始顺序，4在前，5在后
     *   频率2: 6
     *   频率1: 3和2 → 保持原始顺序，3在前，2在后
     * 最终结果: 4 4 4 5 5 5 6 6 3 2
     */


    /**
     * 按频率降序排序，频率相同时保持原顺序
     *
     * @param arr 输入数组
     * @return 排序后的数组
     */
    public static int[] sortByFrequency(int[] arr) {
        int n = arr.length;
        if (n == 0) {
            return new int[0];
        }

        // 1. 统计每个元素的频率
        Map<Integer, Integer> frequency = new HashMap<>();
        for (int num : arr) {
            frequency.put(num, frequency.getOrDefault(num, 0) + 1);
        }

        // 2. 记录每个元素首次出现的位置
        Map<Integer, Integer> firstOccurrence = new HashMap<>();
        for (int i = 0; i < n; i++) {
            if (!firstOccurrence.containsKey(arr[i])) {
                firstOccurrence.put(arr[i], i);
            }
        }

        // 3. 将数组转换为Integer列表以便排序
        Integer[] result = new Integer[n];
        for (int i = 0; i < n; i++) {
            result[i] = arr[i];
        }

        // 4. 自定义排序：先按频率降序，频率相同时按首次出现位置升序
        Arrays.sort(result, (a, b) -> {
            int freqA = frequency.get(a);
            int freqB = frequency.get(b);

            if (freqA != freqB) {
                return Integer.compare(freqB, freqA); // 降序
            } else {
                return Integer.compare(firstOccurrence.get(a), firstOccurrence.get(b)); // 升序
            }
        });

        // 5. 转换回int数组
        int[] sorted = new int[n];
        for (int i = 0; i < n; i++) {
            sorted[i] = result[i];
        }

        return sorted;
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

        // 按频率排序
        int[] result = sortByFrequency(arr);

        // 输出结果
        for (int i = 0; i < result.length; i++) {
            System.out.print(result[i]);
            if (i < result.length - 1) {
                System.out.print(" ");
            }
        }

        scanner.close();
    }

    /**
     * 测试函数
     */
    public static void testFrequencySort() {
        System.out.println("=== SHL_020 按频率排序测试开始 ===");

        // 测试用例1：题目示例
        int[] test1 = {4, 5, 6, 5, 4, 3, 2, 4, 5, 6};
        int[] result1 = sortByFrequency(test1);
        int[] expected1 = {4, 4, 4, 5, 5, 5, 6, 6, 3, 2};
        System.out.println("测试用例1 - 题目示例: " +
                (Arrays.equals(result1, expected1) ? "通过" : "失败"));

        // 测试用例2：所有元素都不同
        int[] test2 = {1, 2, 3, 4, 5};
        int[] result2 = sortByFrequency(test2);
        int[] expected2 = {1, 2, 3, 4, 5}; // 频率相同，保持原顺序
        System.out.println("测试用例2 - 所有元素都不同: " +
                (Arrays.equals(result2, expected2) ? "通过" : "失败"));

        // 测试用例3：所有元素都相同
        int[] test3 = {7, 7, 7, 7};
        int[] result3 = sortByFrequency(test3);
        int[] expected3 = {7, 7, 7, 7};
        System.out.println("测试用例3 - 所有元素都相同: " +
                (Arrays.equals(result3, expected3) ? "通过" : "失败"));

        // 测试用例4：频率相同，测试顺序保持
        int[] test4 = {3, 1, 2, 1, 3, 2};
        int[] result4 = sortByFrequency(test4);
        int[] expected4 = {3, 3, 1, 1, 2, 2}; // 3和1频率相同，3在前；2频率相同但出现晚
        System.out.println("测试用例4 - 频率相同保持顺序: " +
                (Arrays.equals(result4, expected4) ? "通过" : "失败"));

        // 测试用例5：单个元素
        int[] test5 = {42};
        int[] result5 = sortByFrequency(test5);
        int[] expected5 = {42};
        System.out.println("测试用例5 - 单个元素: " +
                (Arrays.equals(result5, expected5) ? "通过" : "失败"));

        // 测试用例6：空数组
        int[] test6 = {};
        int[] result6 = sortByFrequency(test6);
        int[] expected6 = {};
        System.out.println("测试用例6 - 空数组: " +
                (Arrays.equals(result6, expected6) ? "通过" : "失败"));

        // 测试用例7：复杂情况
        int[] test7 = {1, 2, 3, 2, 1, 4, 4, 4, 5};
        int[] result7 = sortByFrequency(test7);
        int[] expected7 = {4, 4, 4, 1, 1, 2, 2, 3, 5};
        System.out.println("测试用例7 - 复杂情况: " +
                (Arrays.equals(result7, expected7) ? "通过" : "失败"));

        // 测试用例8：大数字测试
        int[] test8 = {1000000000, 999999999, 1000000000, 888888888};
        int[] result8 = sortByFrequency(test8);
        int[] expected8 = {1000000000, 1000000000, 999999999, 888888888};
        System.out.println("测试用例8 - 大数字测试: " +
                (Arrays.equals(result8, expected8) ? "通过" : "失败"));

        System.out.println("=== SHL_020 按频率排序测试完成 ===");
    }

    /**
     * 程序入口
     */
    public static void main(String[] args) {
        // 运行测试
        testFrequencySort();

        // 如需运行主函数，取消下面的注释
        // runMain();
    }

    /**
     * 运行主函数
     */
    public static void runMain() {
        main(new String[0]);
    }
}
