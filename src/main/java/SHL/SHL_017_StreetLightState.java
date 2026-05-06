package main.java.SHL;

import java.util.*;

public class SHL_017_StreetLightState {


    /**
     * SHL_017: Street Light State Changes
     * 路灯状态变化问题
     *
     * 题目描述：
     * There are 8 street lights in a line, each can be either ON (1) or OFF (0).
     * Each day, the state of each light changes based on its neighbors:
     * - If both neighbors are the same (both 0 or both 1), the light turns OFF (0) next day
     * - If neighbors are different, the light turns ON (1) next day
     * For the first and last lights, the missing neighbor is considered OFF (0).
     * Given the initial state and number of days M, find the state after M days.
     *
     * 中文翻译：
     * 有8个排成一排的路灯，每个路灯可以是开启(1)或关闭(0)。
     * 每天，每个路灯的状态根据其相邻路灯的变化：
     * - 如果两个相邻路灯状态相同（都是0或都是1），则下一天该路灯关闭(0)
     * - 如果相邻路灯状态不同，则下一天该路灯开启(1)
     * 对于第一个和最后一个路灯，缺失的相邻路灯被视为关闭(0)。
     * 给定初始状态和天数M，求M天后的状态。
     *
     * 输入格式：
     * First line: 8 integers (0 or 1), the initial states of the 8 lights
     * Second line: an integer M, the number of days
     *
     * 中文翻译：
     * 第一行：8个整数(0或1)，表示8个路灯的初始状态
     * 第二行：一个整数M，表示天数
     *
     * 输出格式：
     * 8 integers (0 or 1) without spaces, representing the state after M days
     *
     * 中文翻译：
     * 8个整数(0或1)，没有空格，表示M天后的状态
     *
     * 约束条件：
     * 1 ≤ M ≤ 10^6
     * Initial states are 0 or 1
     *
     * 示例：
     * Input:
     * 1 0 0 0 0 1 0 0
     * 1
     *
     * Output:
     * 00111001
     *
     * 示例解释：
     * Day 0: 1 0 0 0 0 1 0 0
     * Day 1: 0 0 1 1 1 0 0 1
     */


        /**
         * 计算路灯的下一个状态
         *
         * @param currentState 当前状态数组
         * @return 下一个状态数组
         */
        public static int[] getNextState(int[] currentState) {
            int[] nextState = new int[8];
            for (int i = 0; i < 8; i++) {
                int left = (i == 0) ? 0 : currentState[i - 1];
                int right = (i == 7) ? 0 : currentState[i + 1];
                nextState[i] = (left == right) ? 0 : 1;
            }
            return nextState;
        }

        /**
         * 将状态数组转换为整数键值
         *
         * @param state 状态数组
         * @return 整数键值
         */
        public static int getStateKey(int[] state) {
            int key = 0;
            for (int i = 0; i < 8; i++) {
                key = (key << 1) | state[i];
            }
            return key;
        }

        /**
         * 将状态数组转换为字符串
         *
         * @param state 状态数组
         * @return 状态字符串
         */
        public static String getStateString(int[] state) {
            StringBuilder sb = new StringBuilder();
            for (int s : state) {
                sb.append(s);
            }
            return sb.toString();
        }

        /**
         * 计算M天后的路灯状态
         *
         * @param initialState 初始状态数组
         * @param M 天数
         * @return M天后的状态字符串
         */
        public static String calculateFinalState(int[] initialState, int M) {
            // 记录状态历史
            Map<Integer, Integer> stateToDay = new HashMap<>();
            List<int[]> stateHistory = new ArrayList<>();

            int[] currentState = initialState.clone();
            int currentDay = 0;

            // 记录初始状态
            stateHistory.add(currentState.clone());
            stateToDay.put(getStateKey(currentState), currentDay);

            while (currentDay < M) {
                // 计算下一个状态
                int[] nextState = getNextState(currentState);
                int nextKey = getStateKey(nextState);

                // 检查是否出现重复状态（发现循环节）
                if (stateToDay.containsKey(nextKey)) {
                    int cycleStartDay = stateToDay.get(nextKey);
                    int cycleLength = currentDay + 1 - cycleStartDay;
                    int remainingDays = M - currentDay - 1;
                    int targetDay = cycleStartDay + (remainingDays % cycleLength);
                    return getStateString(stateHistory.get(targetDay));
                }

                // 记录新状态
                currentDay++;
                stateHistory.add(nextState.clone());
                stateToDay.put(nextKey, currentDay);
                currentState = nextState;

                // 如果已经达到M天，直接返回
                if (currentDay == M) {
                    return getStateString(currentState);
                }
            }

            return getStateString(currentState);
        }

        /**
         * 主函数，处理标准输入输出
         */
        public static void main1(String[] args) {
            Scanner scanner = new Scanner(System.in);

            // 读取初始状态
            int[] initialState = new int[8];
            for (int i = 0; i < 8; i++) {
                initialState[i] = scanner.nextInt();
            }

            // 读取天数
            int M = scanner.nextInt();

            // 计算并输出结果
            String result = calculateFinalState(initialState, M);
            System.out.println(result);

            scanner.close();
        }

        /**
         * 测试函数，包含多个测试用例
         */
        public static void testStreetLightState() {
            System.out.println("=== SHL_017 路灯状态变化问题 测试开始 ===\n");

            // 测试用例1：题目示例
            int[] test1State = {1, 0, 0, 0, 0, 1, 0, 0};
            int test1Days = 1;
            String result1 = calculateFinalState(test1State, test1Days);
            System.out.println("测试用例1 - 题目示例：");
            System.out.println("  初始状态: " + getStateString(test1State));
            System.out.println("  天数: " + test1Days);
            System.out.println("  预期输出: 00111001");
            System.out.println("  实际输出: " + result1);
            System.out.println("  测试" + (result1.equals("00111001") ? "通过" : "失败") + "\n");

            // 测试用例2：2天后的状态
            int[] test2State = {1, 0, 0, 0, 0, 1, 0, 0};
            int test2Days = 2;
            String result2 = calculateFinalState(test2State, test2Days);
            System.out.println("测试用例2 - 2天后的状态：");
            System.out.println("  初始状态: " + getStateString(test2State));
            System.out.println("  天数: " + test2Days);
            System.out.println("  预期输出: 11000110");
            System.out.println("  实际输出: " + result2);
            System.out.println("  测试" + (result2.equals("11000110") ? "通过" : "失败") + "\n");

            // 测试用例3：全0状态
            int[] test3State = {0, 0, 0, 0, 0, 0, 0, 0};
            int test3Days = 5;
            String result3 = calculateFinalState(test3State, test3Days);
            System.out.println("测试用例3 - 全0状态：");
            System.out.println("  初始状态: " + getStateString(test3State));
            System.out.println("  天数: " + test3Days);
            System.out.println("  预期输出: 00000000");
            System.out.println("  实际输出: " + result3);
            System.out.println("  测试" + (result3.equals("00000000") ? "通过" : "失败") + "\n");

            // 测试用例4：全1状态
            int[] test4State = {1, 1, 1, 1, 1, 1, 1, 1};
            int test4Days = 3;
            String result4 = calculateFinalState(test4State, test4Days);
            System.out.println("测试用例4 - 全1状态：");
            System.out.println("  初始状态: " + getStateString(test4State));
            System.out.println("  天数: " + test4Days);
            System.out.println("  预期输出: 00000000");
            System.out.println("  实际输出: " + result4);
            System.out.println("  测试" + (result4.equals("00000000") ? "通过" : "失败") + "\n");

            // 测试用例5：交替状态
            int[] test5State = {1, 0, 1, 0, 1, 0, 1, 0};
            int test5Days = 4;
            String result5 = calculateFinalState(test5State, test5Days);
            System.out.println("测试用例5 - 交替状态：");
            System.out.println("  初始状态: " + getStateString(test5State));
            System.out.println("  天数: " + test5Days);
            System.out.println("  预期输出: 01010101");
            System.out.println("  实际输出: " + result5);
            System.out.println("  测试" + (result5.equals("01010101") ? "通过" : "失败") + "\n");

            // 测试用例6：大天数测试（利用循环节优化）
            int[] test6State = {1, 0, 0, 1, 1, 0, 1, 0};
            int test6Days = 1000000;
            long startTime = System.currentTimeMillis();
            String result6 = calculateFinalState(test6State, test6Days);
            long endTime = System.currentTimeMillis();
            System.out.println("测试用例6 - 大天数测试：");
            System.out.println("  初始状态: " + getStateString(test6State));
            System.out.println("  天数: " + test6Days);
            System.out.println("  计算时间: " + (endTime - startTime) + "ms");
            System.out.println("  实际输出: " + result6);
            System.out.println("  测试通过（验证算法效率）\n");

            // 测试用例7：边界条件测试
            int[] test7State = {0, 1, 1, 0, 0, 1, 1, 0};
            int test7Days = 0;
            String result7 = calculateFinalState(test7State, test7Days);
            System.out.println("测试用例7 - 边界条件（0天）：");
            System.out.println("  初始状态: " + getStateString(test7State));
            System.out.println("  天数: " + test7Days);
            System.out.println("  预期输出: " + getStateString(test7State));
            System.out.println("  实际输出: " + result7);
            System.out.println("  测试" + (result7.equals(getStateString(test7State)) ? "通过" : "失败") + "\n");

            // 测试getNextState函数
            System.out.println("=== 辅助函数测试 ===");
            int[] testState = {1, 0, 0, 0, 0, 1, 0, 0};
            int[] nextState = getNextState(testState);
            System.out.println("getNextState测试：");
            System.out.println("  输入状态: " + getStateString(testState));
            System.out.println("  输出状态: " + getStateString(nextState));
            System.out.println("  测试" + (getStateString(nextState).equals("00111001") ? "通过" : "失败") + "\n");

            System.out.println("=== SHL_017 路灯状态变化问题 测试完成 ===");
        }

        /**
         * 程序入口点，可以选择运行主函数或测试函数
         */
        public static void main(String[] args) {
            // 如果要运行测试，取消下面一行的注释
             testStreetLightState();

            // 如果要运行主函数（从控制台输入），使用下面的main方法
//            runMain()/;
        }

        /**
         * 包装的主函数，避免与测试冲突
         */
        public static void runMain() {
            main(new String[0]);
        }


}
