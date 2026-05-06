package main.java.SHL;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SHL_025 {
/*
    English Version:

    Problem Description:

    Given a binary string of length N, find the maximum signal length. A signal is defined as a continuous segment of the same characters ('0' or '1'). The maximum signal length is the length of the longest continuous segment of the same characters that does NOT start at the beginning or end at the end of the string. In other words, we need to find the longest contiguous subsegment of identical characters that is entirely within the interior of the string (not touching either end).

    Write an algorithm to compute the maximum signal length.

            Input:

    First line: an integer N (length of the string).

    Second line: a binary string of length N.

    Output:

    Print a single integer: the maximum signal length.

    Constraints:

    The string consists only of '0's and '1's.

            Example 1:

    Input:

    复制
6
        101000

    Output:

    复制
1

    Explanation: The binary string is "101000". The continuous segments are: "1" (length 1), "0" (length 1), "1" (length 1), "000" (length 3). The segments that are not at the beginning or end are "0" and "1" (the second and third segments). Their lengths are 1 and 1, so the maximum is 1.

    Example 2:

    Input:

    复制
9
        101111110

    Output:

    复制
6

    Explanation: The binary string is "101111110". The continuous segments are: "1" (length 1), "0" (length 1), "111111" (length 6), "0" (length 1). The segments that are not at the beginning or end are "0" (the second segment) and "111111" (the third segment). Their lengths are 1 and 6, so the maximum is 6.

    中文对照版本：

    问题描述：

    给定一个长度为N的二进制字符串，找到最大信号长度。信号被定义为相同字符（'0'或'1'）的连续段。最大信号长度是指不是从字符串开头开始且不在字符串结尾结束的最长相同字符连续段的长度。换句话说，我们需要找到完全在字符串内部（不接触任一端）的相同字符的最长连续子段。

    编写一个算法来计算最大信号长度。

    输入：

    第一行：一个整数N（字符串长度）。

    第二行：一个长度为N的二进制字符串。

    输出：

    输出一个整数：最大信号长度。

    约束条件：

    字符串仅由'0'和'1'组成。

    示例1：

    输入：

    复制
6
        101000

    输出：

    复制
1

    解释：二进制字符串是"101000"。连续段为："1"（长度1）、"0"（长度1）、"1"（长度1）、"000"（长度3）。不在开头或结尾的段是"0"和"1"（第二和第三段）。它们的长度是1和1，所以最大值是1。

    示例2：

    输入：

    复制
9
        101111110

    输出：

    复制
6

    解释：二进制字符串是"101111110"。连续段为："1"（长度1）、"0"（长度1）、"111111"（长度6）、"0"（长度1）。不在开头或结尾的段是"0"（第二段）和"111111"（第三段）。它们的长度是1和6，所以最大值是6。

    */

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);

            // 读取输入
            int N = Integer.parseInt(scanner.nextLine());
            String binaryString = scanner.nextLine();

            // 计算最大信号长度
            int maxSignalLength = findMaxSignalLength(binaryString, N);

            // 输出结果
            System.out.println(maxSignalLength);
            scanner.close();
        }

        private static int findMaxSignalLength(String binaryString, int N) {
            if (N <= 2) {
                // 如果字符串长度小于等于2，没有内部段，返回0
                return 0;
            }

            // 存储连续段的长度
            List<Integer> segmentLengths = new ArrayList<>();

            // 遍历字符串，统计连续段
            int currentLength = 1;
            for (int i = 1; i < N; i++) {
                if (binaryString.charAt(i) == binaryString.charAt(i-1)) {
                    currentLength++;
                } else {
                    // 当前段结束，记录长度
                    segmentLengths.add(currentLength);
                    currentLength = 1; // 开始新的段
                }
            }
            // 记录最后一个段
            segmentLengths.add(currentLength);

            // 如果段数小于等于2，没有内部段，返回0
            if (segmentLengths.size() <= 2) {
                return 0;
            }

            // 排除第一个和最后一个段，找到中间段的最大长度
            int maxLength = 0;
            for (int i = 1; i < segmentLengths.size() - 1; i++) {
                maxLength = Math.max(maxLength, segmentLengths.get(i));
            }

            return maxLength;
        }
    }


 class TestMaxSignalLength {
    public static void main(String[] args) throws IOException {
        // 测试用例1：示例1
        System.out.println("测试用例1：示例1 (期望输出: 1)");
        runTest("6\n101000\n", 1);

        // 测试用例2：示例2
        System.out.println("\n测试用例2：示例2 (期望输出: 6)");
        runTest("9\n101111110\n", 6);

        // 测试用例3：全相同字符
        System.out.println("\n测试用例3：全相同字符 (期望输出: 0)");
        runTest("5\n11111\n", 0);

        // 测试用例4：交替字符
        System.out.println("\n测试用例4：交替字符 (期望输出: 0)");
        runTest("5\n10101\n", 1);

        // 测试用例5：中间有长段
        System.out.println("\n测试用例5：中间有长段 (期望输出: 8)");
        runTest("12\n100111111110\n", 8);

        // 测试用例6：长度小于等于2
        System.out.println("\n测试用例6：长度小于等于2 (期望输出: 0)");
        runTest("2\n10\n", 0);
        runTest("1\n1\n", 0);
        runTest("0\n\n", 0);
    }

    private static void runTest(String input, int expected) {
        // 保存原始输入输出
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        try {
            // 设置测试输入
            ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
            System.setIn(in);

            // 捕获输出
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(out);
            System.setOut(printStream);

            // 运行主程序
            SHL_025.main(new String[]{});

            // 获取输出并转换为整数
            String output = out.toString().trim();
            int result;
            try {
                result = Integer.parseInt(output);
            } catch (NumberFormatException e) {
                System.setOut(originalOut);
                System.out.println("输出不是整数: " + output);
                return;
            }

            // 恢复原始输出
            System.setOut(originalOut);
            System.setIn(originalIn);

            // 检查结果
            if (result == expected) {
                System.out.println("✓ 通过! 输出: " + result);
            } else {
                System.out.println("✗ 失败! 期望: " + expected + ", 实际: " + result);
            }

        } catch (Exception e) {
            // 恢复原始输出
            System.setOut(originalOut);
            System.setIn(originalIn);
            System.out.println("✗ 测试异常: " + e.getMessage());
        }
    }
}