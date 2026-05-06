package main.java.SHL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SHL_008 {

/*
    题干（Problem Statement）

    中文：大学邀请了 N位校友参加晚宴。餐桌是圆形的。学校给每位校友分配了从 1 到 N的邀请 ID。每位校友恰好喜欢一位其他校友，且只有能坐在喜欢的人旁边才会参加晚宴。你需要规划座位安排，编写算法找出最多有多少位校友能参加晚宴。

    English：A University has invited Nalumni to a dinner. The dinner table is circular in shape. The university has assigned each alumnus an invitation ID from 1 to N. Each alumnus likes exactly one fellow alumnus and will attend the dinner only if he/she can be seated next to that person. You are asked to plan the seating arrangement. Write an algorithm to find the maximum number of alumni who will attend the dinner.

            输入（Input）

    中文：第一行是一个整数 personal_id_size，代表校友数量 N。第二行是 N个空格分隔的整数，表示第 i位校友喜欢的人的 ID。

    English：The first line of the input consists of an integer personal_id_size, representing the number of alumni (N). The second line consists of Nspace-separated integers representing the ID of the person whom the i-th alumnus likes.

            输出（Output）

    中文：打印一个整数，代表能参加晚宴的最大校友数。

    English：Print an integer representing the maximum number of alumni who can attend the dinner.

    注意事项（Note）

    中文：一位校友可以被多位校友喜欢。

    English：One alumnus can be liked by more than one alumnus.

    约束（Constraints）

    中文：1≤personal_id_size≤10
            5
            ；每位校友喜欢的 ID 满足 1≤喜欢的 ID≤personal_id_size。

    English：1≤personal_id_size≤10
            5
    ; 1≤The ID of the person each alumnus likes≤personal_id_size.

            示例（Example）

    中文：

    输入：

    复制
4
        2 3 4 1

    输出：

    复制
4

    解释：第 1 位校友喜欢 ID 为 2 的人；第 2 位喜欢 ID 为 3 的人；第 3 位喜欢 ID 为 4 的人；第 4 位喜欢 ID 为 1 的人。最多 4 位校友可以围坐在圆桌旁，排列方式为 1-2-3-4-（循环）。

    English：

    Input:

    复制
4
        2 3 4 1

    Output:

    复制
4

    Explanation:

    The 1st alumnus likes the person with ID 2; the 2nd likes the person with ID 3; the 3rd likes the person with ID 4; the 4th likes the person with ID 1. A maximum of 4 alumni can be seated around the circular table in the following manner: 1-2-3-4-(cyclic).

    编程要求与说明

    编程语言：Java

    主类名：必须为 Solution

    JDK 版本：1.8

    调试限制：可使用 System.out.println()调试，但语法/运行时错误时会失效*/


    public static void main1(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int[] likes = new int[n + 1];  // 1-based索引

        for (int i = 1; i <= n; i++) {
            likes[i] = scanner.nextInt();
        }

        // visited数组记录每个校友的状态
        // 0: 未访问
        // 1: 正在访问（在当前路径中）
        // 2: 已访问完成
        int[] visited = new int[n + 1];
        int maxAlumni = 0;

        for (int i = 1; i <= n; i++) {
            if (visited[i] == 0) {  // 只处理未访问的校友
                // 从校友i开始，沿着"喜欢"关系走
                int current = i;
                List<Integer> path = new ArrayList<>();  // 记录当前路径
                Map<Integer, Integer> position = new HashMap<>();  // 记录每个校友在路径中的位置

                // 沿着"喜欢"链前进
                while (true) {
                    if (visited[current] == 1) {
                        // 发现环！current已经在当前路径中
                        int cycleStartIndex = position.get(current);
                        int cycleLength = path.size() - cycleStartIndex;
                        maxAlumni = Math.max(maxAlumni, cycleLength);
                        break;
                    }

                    if (visited[current] == 2) {
                        // 当前校友已经访问过，但不在当前路径中
                        // 说明不会形成新环
                        break;
                    }

                    // 标记为正在访问，记录位置
                    visited[current] = 1;
                    position.put(current, path.size());
                    path.add(current);

                    // 移动到当前校友喜欢的人
                    current = likes[current];
                }

                // 将当前路径中所有校友标记为已访问完成
                for (int alum : path) {
                    visited[alum] = 2;
                }
            }
        }

        System.out.println(maxAlumni);
    }

    public static void main(String[] args) {
        // 测试用例1：示例输入
        test(4, new int[]{2, 3, 4, 1}, 4, "示例测试");

        // 测试用例2：单个自环
        test(1, new int[]{1}, 1, "单个自环");

        // 测试用例3：两个校友互相喜欢
        test(2, new int[]{2, 1}, 2, "互相喜欢");

        // 测试用例4：多个环
        test(5, new int[]{2, 3, 1, 5, 4}, 3, "多个环（最大环长度3）");

        // 测试用例5：链状结构，没有环
        test(3, new int[]{2, 3, 1}, 3, "完整环");

        // 测试用例6：部分校友形成环
        test(6, new int[]{2, 3, 4, 2, 6, 5}, 3, "部分校友形成环");

        // 测试用例7：所有校友喜欢同一个人
        test(5, new int[]{2, 2, 2, 2, 2}, 2, "所有喜欢同一个人（最大环长度2）");

        // 测试用例8：大N测试
        test(100000, generateLargeInput(100000), 50000, "大N测试");
    }

    // 生成大N测试数据：形成多个长度为2的环
    private static int[] generateLargeInput(int N) {
        int[] likes = new int[N];
        for (int i = 0; i < N; i++) {
            if (i % 2 == 0) {
                likes[i] = i + 2; // 偶数喜欢下一个奇数
            } else {
                likes[i] = i; // 奇数喜欢前一个偶数
            }
        }
        // 处理边界
        if (N % 2 == 1) {
            likes[N-1] = 1;
        }
        return likes;
    }

    private static void test(int N, int[] likesArray, int expected, String testName) {
        // 将0-based数组转为1-based
        int[] likes = new int[N + 1];
        for (int i = 1; i <= N; i++) {
            likes[i] = likesArray[i-1];
        }

        int[] visited = new int[N + 1];
        int maxAlumni = 0;

        for (int i = 1; i <= N; i++) {
            if (visited[i] == 0) {
                int current = i;
                List<Integer> path = new ArrayList<>();
                Map<Integer, Integer> position = new HashMap<>();

                while (true) {
                    if (visited[current] == 1) {
                        int cycleStartIndex = position.get(current);
                        int cycleLength = path.size() - cycleStartIndex;
                        maxAlumni = Math.max(maxAlumni, cycleLength);
                        break;
                    }

                    if (visited[current] == 2) {
                        break;
                    }

                    visited[current] = 1;
                    position.put(current, path.size());
                    path.add(current);

                    current = likes[current];
                }

                for (int alum : path) {
                    visited[alum] = 2;
                }
            }
        }

        if (maxAlumni == expected) {
            System.out.println("✓ " + testName + " 通过");
        } else {
            System.out.println("✗ " + testName + " 失败: 期望 " + expected + ", 实际 " + maxAlumni);
        }
    }

}
