package main.java.SHL;

import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;

import java.util.*;

public class SHL_012 {
/*
    题目ID: SHL_012

    英文题目 (English Description):

    There are N bus routes in a city, each operating between two stops. Each stop has a unique ID. Buses only travel between the given stops, and the distance between consecutive stops is 1 unit. Multiple buses may cover overlapping or adjacent segments of the route. Calculate the total distance covered by all bus routes, ensuring that overlapping segments are not counted multiple times.

            中文翻译 (Chinese Description):

    一个城市中有N条公交车路线，每条路线在两个站点之间运行。每个站点有唯一的ID。公交车仅在给定的站点之间运行，相邻站点之间的距离为1个单位。多条公交车的路线可能重叠或相邻。计算所有公交车路线覆盖的总距离，确保重叠的路段不被重复计算。

    输入格式 (Input Format):

    第一行：两个整数 N 和 M，其中 N 是公交车数量，M 是常量（通常为2，表示每个区间有起点和终点）

    接下来N行：每行包含两个整数 start 和 end，表示公交车运行的起始站点ID和结束站点ID

    输出格式 (Output Format):

    一个整数，表示所有公交车路线覆盖的总距离

    约束条件 (Constraints):

            1 ≤ N ≤ 10⁵

            0 ≤ start < end ≤ 10⁹

    M = 2

    示例 (Example):

    复制
    输入 (Input):
            4 2
            1 3
            2 5
            7 9
            8 10

    输出 (Output):
            7

    示例解释 (Explanation):

    区间[1,3]和[2,5]重叠，合并为[1,5]，距离为4

    区间[7,9]和[8,10]重叠，合并为[7,10]，距离为3

            总距离 = 4 + 3 = 7*/


    /**
     * 计算所有公交车行驶区间的总覆盖距离
     *
     * 算法思路：
     * 1. 读取所有公交车的行驶区间
     * 2. 按区间起点进行排序
     * 3. 合并所有重叠或相邻的区间
     * 4. 计算合并后各区间长度之和
     *
     * 时间复杂度：O(n log n)，主要来自排序操作
     * 空间复杂度：O(n)，用于存储区间
     */
    public static int calculateTotalDistance(List<int[]> intervals) {
        if (intervals == null || intervals.isEmpty()) {
            return 0;
        }

        // 1. 按区间起点进行排序
        intervals.sort((a, b) -> Integer.compare(a[0], b[0]));

        // 2. 合并区间
        List<int[]> merged = new ArrayList<>();
        for (int[] interval : intervals) {
            int start = interval[0];
            int end = interval[1];

            if (merged.isEmpty()) {
                merged.add(new int[]{start, end});
            } else {
                int[] last = merged.get(merged.size() - 1);
                int lastStart = last[0];
                int lastEnd = last[1];

                // 如果当前区间起点 <= 上一个区间终点，则合并
                if (start <= lastEnd) {
                    // 合并时取最大的终点
                    last[1] = Math.max(lastEnd, end);
                } else {
                    merged.add(new int[]{start, end});
                }
            }
        }

        // 3. 计算总距离
        int totalDistance = 0;
        for (int[] interval : merged) {
            totalDistance += (interval[1] - interval[0]);
        }

        return totalDistance;
    }

    /**
     * 主函数，包含测试用例
     */
    public static void main(String[] args) {
        System.out.println("=== SHL_012 公交车总行驶距离计算测试 ===\n");

        // 测试用例1：示例测试
        List<int[]> test1 = new ArrayList<>();
        test1.add(new int[]{1, 3});
        test1.add(new int[]{2, 5});
        test1.add(new int[]{7, 9});
        test1.add(new int[]{8, 10});
        int result1 = calculateTotalDistance(test1);
        System.out.println("测试用例1 - 不连续区间：");
        System.out.println("  区间: [1,3], [2,5], [7,9], [8,10]");
        System.out.println("  预期结果: 7 (合并后: [1,5]长度4 + [7,10]长度3)");
        System.out.println("  实际结果: " + result1);
        System.out.println("  测试" + (result1 == 7 ? "通过" : "失败") + "\n");

        // 测试用例2：完全重叠区间
        List<int[]> test2 = new ArrayList<>();
        test2.add(new int[]{1, 5});
        test2.add(new int[]{2, 4});
        test2.add(new int[]{3, 6});
        int result2 = calculateTotalDistance(test2);
        System.out.println("测试用例2 - 完全重叠区间：");
        System.out.println("  区间: [1,5], [2,4], [3,6]");
        System.out.println("  预期结果: 5 (合并后: [1,6]长度5)");
        System.out.println("  实际结果: " + result2);
        System.out.println("  测试" + (result2 == 5 ? "通过" : "失败") + "\n");

        // 测试用例3：相邻区间
        List<int[]> test3 = new ArrayList<>();
        test3.add(new int[]{1, 3});
        test3.add(new int[]{3, 5});
        test3.add(new int[]{5, 7});
        int result3 = calculateTotalDistance(test3);
        System.out.println("测试用例3 - 相邻区间：");
        System.out.println("  区间: [1,3], [3,5], [5,7]");
        System.out.println("  预期结果: 6 (合并后: [1,7]长度6)");
        System.out.println("  实际结果: " + result3);
        System.out.println("  测试" + (result3 == 6 ? "通过" : "失败") + "\n");

        // 测试用例4：完全分离区间
        List<int[]> test4 = new ArrayList<>();
        test4.add(new int[]{1, 2});
        test4.add(new int[]{4, 6});
        test4.add(new int[]{8, 10});
        int result4 = calculateTotalDistance(test4);
        System.out.println("测试用例4 - 完全分离区间：");
        System.out.println("  区间: [1,2], [4,6], [8,10]");
        System.out.println("  预期结果: 5 (1+2+2)");
        System.out.println("  实际结果: " + result4);
        System.out.println("  测试" + (result4 == 5 ? "通过" : "失败") + "\n");

        // 测试用例5：单区间
        List<int[]> test5 = new ArrayList<>();
        test5.add(new int[]{5, 10});
        int result5 = calculateTotalDistance(test5);
        System.out.println("测试用例5 - 单区间：");
        System.out.println("  区间: [5,10]");
        System.out.println("  预期结果: 5");
        System.out.println("  实际结果: " + result5);
        System.out.println("  测试" + (result5 == 5 ? "通过" : "失败") + "\n");

        // 测试用例6：空列表
        List<int[]> test6 = new ArrayList<>();
        int result6 = calculateTotalDistance(test6);
        System.out.println("测试用例6 - 空列表：");
        System.out.println("  区间: []");
        System.out.println("  预期结果: 0");
        System.out.println("  实际结果: " + result6);
        System.out.println("  测试" + (result6 == 0 ? "通过" : "失败") + "\n");

        // 测试用例7：随机测试
        List<int[]> test7 = new ArrayList<>();
        test7.add(new int[]{1, 4});
        test7.add(new int[]{0, 2});
        test7.add(new int[]{3, 6});
        test7.add(new int[]{7, 9});
        int result7 = calculateTotalDistance(test7);
        System.out.println("测试用例7 - 随机测试：");
        System.out.println("  区间: [1,4], [0,2], [3,6], [7,9]");
        System.out.println("  预期结果: 8 (合并后: [0,6]长度6 + [7,9]长度2)");
        System.out.println("  实际结果: " + result7);
        System.out.println("  测试" + (result7 == 8 ? "通过" : "失败"));

        System.out.println("\n=== 所有测试完成 ===");
    }
}
