package main.java.SHL;

import java.io.*;
import java.util.Scanner;

public class SHL_030_PerfectSquareBills {
/*

    English Version:

    Problem Description:

    A restaurant wants to reward customers whose bill amounts are perfect square numbers. Given the bill amounts of N customers, count how many customers have bill amounts that are perfect squares (a number that is the square of an integer).

    Input:

    The first line contains an integer N, representing the number of customers.

    The second line contains N space-separated integers, representing the bill amounts of the customers.

    Output:

    Print a single integer representing the number of customers whose bill amounts are perfect squares.

            Constraints:

            1 ≤ N ≤ 10^6

            0 ≤ bill amounts ≤ 10^6

    Example:

    Input:

    复制
6
        25 77 54 81 48 34

    Output:

    复制
2

    Explanation: 25 is 5^2, 81 is 9^2, so there are 2 perfect square bills.

    Chinese Version:

    问题描述:

    一家餐厅想要奖励那些账单金额是完全平方数的顾客。给定N个顾客的账单金额，统计有多少个顾客的账单金额是完全平方数（一个可以表示为整数平方的数）。

    输入:

    第一行包含一个整数N，表示顾客数量。

    第二行包含N个空格分隔的整数，表示顾客的账单金额。

    输出:

    打印一个整数，表示账单金额是完全平方数的顾客数量。

    约束条件:

            1 ≤ N ≤ 10^6

            0 ≤ 账单金额 ≤ 10^6

    示例:

    输入:

    复制
6
        25 77 54 81 48 34

    输出:

    复制
2

    解释: 25是5^2，81是9^2，所以有2个完全平方数账单。*/


        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);

            // Read number of customers
            int N = scanner.nextInt();

            int count = 0;

            // Check each bill
            for (int i = 0; i < N; i++) {
                int bill = scanner.nextInt();
                if (isPerfectSquare(bill)) {
                    count++;
                }
            }

            System.out.println(count);
            scanner.close();
        }

        // Function to check if a number is a perfect square
        private static boolean isPerfectSquare(int n) {
            if (n < 0) {
                return false; // 账单金额不可能是负数，但保持健壮性
            }

            // 使用整数平方根计算
            int sqrt = (int) Math.sqrt(n);

            // 检查平方是否等于原数
            return sqrt * sqrt == n;
        }
    }

class TestPerfectSquareBills {
    public static void main(String[] args) throws IOException {
        // Test case 1: Example from problem
        test("Test 1: Example",
                "6\n25 77 54 81 48 34\n",
                "2");

        // Test case 2: All perfect squares
        test("Test 2: All perfect squares",
                "5\n0 1 4 9 16\n",
                "5");

        // Test case 3: No perfect squares
        test("Test 3: No perfect squares",
                "4\n2 3 5 6\n",
                "0");

        // Test case 4: Boundary test - 1,000,000
        test("Test 4: Boundary test (1,000,000)",
                "1\n1000000\n",
                "1"); // 1000^2 = 1,000,000

        // Test case 5: Perfect squares at boundaries
        test("Test 5: Perfect squares at boundaries",
                "3\n0 1 1000000\n",
                "3");

        // Test case 6: Large input with mixed numbers
        test("Test 6: Large input with mixed",
                "10\n0 1 2 4 5 9 10 16 25 30\n",
                "6"); // 0, 1, 4, 9, 16, 25
    }

    private static void test(String testName, String input, String expected) {
        // Save original input/output
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        try {
            // Set up test input
            ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
            System.setIn(in);

            // Capture output
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(out);
            System.setOut(printStream);

            // Run the main program
            SHL_030_PerfectSquareBills.main(new String[]{});

            // Get the output
            String output = out.toString().trim();

            // Restore original input/output
            System.setIn(originalIn);
            System.setOut(originalOut);

            // Check result
            if (output.equals(expected)) {
                System.out.println(testName + ": ✓ PASSED (output: " + output + ")");
            } else {
                System.out.println(testName + ": ✗ FAILED (expected: " + expected +
                        ", got: " + output + ")");
            }
        } catch (Exception e) {
            // Restore in case of exception
            System.setIn(originalIn);
            System.setOut(originalOut);
            System.out.println(testName + ": ERROR - " + e.getMessage());
        }
    }
}