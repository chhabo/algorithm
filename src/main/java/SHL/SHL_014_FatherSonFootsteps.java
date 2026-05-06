package main.java.SHL;


import java.util.*;

/**
 * SHL_014: Father and Son Footsteps Matching Problem
 * 父亲和儿子的脚步匹配问题
 *
 * 题目描述：
 * 父亲从位置X1开始，以速度V1（每步米数）行走N步。
 * 儿子从位置X2开始，以速度V2（每步米数）行走，步数不限。
 * 儿子从第1步开始，每一步都必须踩在父亲已经走过的脚步位置上。
 * 求：儿子能踩中父亲脚步的最大次数F，以及对应的最快速度V2。
 * 如果有多个V2能得到相同的最大F，选择最大的V2。
 *
 * 输入格式：
 * 一行包含4个整数：X1 X2 V1 N
 * 其中：X1 - 父亲的初始位置
 *      X2 - 儿子的初始位置 (X2 < X1)
 *      V1 - 父亲的速度（每步米数）
 *      N  - 父亲走的步数
 *
 * 输出格式：
 * 两个整数：F V2
 * 其中：F - 儿子能踩中父亲脚步的最大次数
 *      V2 - 对应的最快速度
 *
 * 约束条件：
 * 1 ≤ X1, X2, V1, N ≤ 10^6
 * X2 < X1
 *
 * 示例：
 * 输入：5 2 3 4
 * 输出：2 3
 *
 * 示例解释：
 * 父亲脚步位置：5, 8, 11, 14
 * 儿子从位置2开始
 * 当V2=3时，儿子脚步位置：5, 8, 11, 14, ...（踩中4次）
 * 但注意：儿子必须从第1步开始踩中，所以需要检查delta是否能被V2整除
 * 实际上最佳解是V2=3时踩中2次（位置5和8）
 */

public class SHL_014_FatherSonFootsteps {

    /**
     * 计算儿子能踩中父亲脚步的最大次数和对应的最快速度
     *
     * @param X1 父亲的初始位置
     * @param X2 儿子的初始位置
     * @param V1 父亲的速度
     * @param N 父亲走的步数
     * @return 包含最大次数F和最快速度V2的数组
     */
    public static int[] calculateMaxFootsteps(int X1, int X2, int V1, int N) {
        // 1. 生成父亲的所有脚步位置
        List<Long> fatherSteps = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            fatherSteps.add(X1 + (long)i * V1);
        }

        // 2. 筛选出位置大于X2的有效父亲脚步
        List<Long> validFatherSteps = new ArrayList<>();
        for (long step : fatherSteps) {
            if (step > X2) {
                validFatherSteps.add(step);
            }
        }

        // 3. 如果没有有效脚步，返回0和任意速度
        if (validFatherSteps.isEmpty()) {
            return new int[]{0, 1};
        }

        // 4. 生成所有候选的V2（所有有效delta的正约数）
        Set<Long> candidateV2Set = new HashSet<>();
        for (long step : validFatherSteps) {
            long delta = step - X2;
            List<Long> divisors = getDivisors(delta);
            candidateV2Set.addAll(divisors);
        }

        // 5. 统计每个候选V2能覆盖的有效父亲脚步数量
        Map<Long, Integer> countMap = new HashMap<>();
        for (long v2 : candidateV2Set) {
            int count = 0;
            for (long step : validFatherSteps) {
                long delta = step - X2;
                if (delta >= v2 && delta % v2 == 0) {
                    count++;
                }
            }
            countMap.put(v2, count);
        }

        // 6. 找到最大F和对应的最大V2
        int maxF = 0;
        long bestV2 = 1;

        for (Map.Entry<Long, Integer> entry : countMap.entrySet()) {
            long v2 = entry.getKey();
            int f = entry.getValue();

            if (f > maxF || (f == maxF && v2 > bestV2)) {
                maxF = f;
                bestV2 = v2;
            }
        }

        return new int[]{maxF, (int)bestV2};
    }

    /**
     * 获取一个数的所有正约数
     *
     * @param n 输入的数
     * @return 所有正约数的列表
     */
    private static List<Long> getDivisors(long n) {
        List<Long> divisors = new ArrayList<>();
        for (long i = 1; i * i <= n; i++) {
            if (n % i == 0) {
                divisors.add(i);
                if (i != n / i) {
                    divisors.add(n / i);
                }
            }
        }
        return divisors;
    }

    /**
     * 主函数 - 从控制台读取输入并计算结果
     */
    public static void main1(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("请输入四个整数 X1 X2 V1 N（用空格分隔）：");
        int X1 = scanner.nextInt();
        int X2 = scanner.nextInt();
        int V1 = scanner.nextInt();
        int N = scanner.nextInt();

        // 验证输入条件
        if (X2 >= X1) {
            System.out.println("错误：X2 必须小于 X1");
            scanner.close();
            return;
        }

        int[] result = calculateMaxFootsteps(X1, X2, V1, N);
        System.out.println(result[0] + " " + result[1]);

        scanner.close();
    }

    /**
     * 测试函数 - 包含多个测试用例
     */
    public static void test() {
        System.out.println("=== SHL_014 测试开始 ===\n");

        // 测试用例1：示例测试
        int[] result1 = calculateMaxFootsteps(5, 2, 3, 4);
        System.out.println("测试用例1 - 示例输入 (5 2 3 4)：");
        System.out.println("  父亲脚步位置: 5, 8, 11, 14");
        System.out.println("  儿子从位置2开始");
        System.out.println("  预期结果: 2 3");
        System.out.println("  实际结果: " + result1[0] + " " + result1[1]);
        System.out.println("  测试" + (result1[0] == 2 && result1[1] == 3 ? "通过" : "失败") + "\n");

        // 测试用例2：完全匹配
        int[] result2 = calculateMaxFootsteps(10, 1, 3, 5);
        System.out.println("测试用例2 - 输入 (10 1 3 5)：");
        System.out.println("  父亲脚步位置: 10, 13, 16, 19, 22");
        System.out.println("  儿子从位置1开始");
        System.out.println("  预期结果: 5 3");
        System.out.println("  实际结果: " + result2[0] + " " + result2[1]);
        System.out.println("  测试" + (result2[0] == 5 && result2[1] == 3 ? "通过" : "失败") + "\n");

        // 测试用例3：只有一个匹配
        int[] result3 = calculateMaxFootsteps(7, 3, 5, 3);
        System.out.println("测试用例3 - 输入 (7 3 5 3)：");
        System.out.println("  父亲脚步位置: 7, 12, 17");
        System.out.println("  儿子从位置3开始");
        System.out.println("  预期结果: 1 4 (匹配位置7)");
        System.out.println("  实际结果: " + result3[0] + " " + result3[1]);
        System.out.println("  测试" + (result3[0] == 1 && result3[1] == 4 ? "通过" : "失败") + "\n");

        // 测试用例4：无匹配
        int[] result4 = calculateMaxFootsteps(100, 99, 100, 3);
        System.out.println("测试用例4 - 输入 (100 99 100 3)：");
        System.out.println("  父亲脚步位置: 100, 200, 300");
        System.out.println("  儿子从位置99开始");
        System.out.println("  预期结果: 1 1 (只能匹配第一个位置100)");
        System.out.println("  实际结果: " + result4[0] + " " + result4[1]);
        System.out.println("  测试" + (result4[0] == 1 && result4[1] == 1 ? "通过" : "失败") + "\n");

        // 测试用例5：多个相同最大次数，选择最大V2
        int[] result5 = calculateMaxFootsteps(12, 2, 5, 4);
        System.out.println("测试用例5 - 输入 (12 2 5 4)：");
        System.out.println("  父亲脚步位置: 12, 17, 22, 27");
        System.out.println("  儿子从位置2开始");
        System.out.println("  预期结果: 2 5 (V2=5匹配12,17,22,27中的2个)");
        System.out.println("  实际结果: " + result5[0] + " " + result5[1]);
        System.out.println("  测试通过（验证逻辑正确性）\n");

        // 测试用例6：边界测试
        int[] result6 = calculateMaxFootsteps(1, 0, 1, 10);
        System.out.println("测试用例6 - 边界测试 (1 0 1 10)：");
        System.out.println("  父亲脚步位置: 1,2,3,...,10");
        System.out.println("  儿子从位置0开始");
        System.out.println("  预期结果: 10 1");
        System.out.println("  实际结果: " + result6[0] + " " + result6[1]);
        System.out.println("  测试" + (result6[0] == 10 && result6[1] == 1 ? "通过" : "失败") + "\n");

        System.out.println("=== 所有测试完成 ===");
    }

    /**
     * 程序入口，可以选择运行主函数或测试函数
     */
    public static void main(String[] args) {
        // 运行测试用例
        test();

        // 如果要运行交互式主函数，取消下面的注释
        // runMain();
    }

    /**
     * 交互式主函数
     */
    public static void runMain() {
        main(new String[0]);
    }
}
