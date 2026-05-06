package main.java.SHL;
import java.io.*;
import java.util.*;
public class SHL_031_EvenBeforeOdd {

/*
    English Version:

    Problem Description:

    Given a list of integers, rearrange the list so that all even numbers come before all odd numbers, while maintaining the relative order​ of even numbers among themselves and the relative order​ of odd numbers among themselves.

    Input:

    The first line contains an integer N, the number of elements in the list.

    The second line contains N space-separated integers.

    Output:

    Print the rearranged list as space-separated integers.

    Constraints:

            1 ≤ N ≤ 10⁵

    The integers are within the typical integer range.

    Example:

    Input:

    复制
5
        1 2 3 4 5

    Output:

    复制
2 4 1 3 5

    Chinese Version:

    问题描述:

    给定一个整数列表，重新排列列表使得所有偶数出现在所有奇数之前，同时保持偶数之间的相对顺序和奇数之间的相对顺序不变。

    输入:

    第一行包含一个整数 N，表示列表中元素的数量。

    第二行包含 N 个空格分隔的整数。

    输出:

    打印重新排列后的列表，以空格分隔整数。

    约束条件:

            1 ≤ N ≤ 10⁵

    整数在典型整数范围内

    示例:

    输入:

    复制
5
        1 2 3 4 5

    输出:

    复制
2 4 1 3 5

    */

        public static void main(String[] args) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter pw = new PrintWriter(System.out);

            // Read input
            int N = Integer.parseInt(br.readLine().trim());
            String[] tokens = br.readLine().trim().split("\\s+");

            // Use ArrayLists to maintain relative order
            List<Integer> evens = new ArrayList<>();
            List<Integer> odds = new ArrayList<>();

            for (int i = 0; i < N; i++) {
                int num = Integer.parseInt(tokens[i]);
                if (num % 2 == 0) {
                    evens.add(num);
                } else {
                    odds.add(num);
                }
            }

            // Combine evens first, then odds
            List<Integer> result = new ArrayList<>();
            result.addAll(evens);
            result.addAll(odds);

            // Output result
            for (int i = 0; i < result.size(); i++) {
                pw.print(result.get(i));
                if (i < result.size() - 1) {
                    pw.print(" ");
                }
            }
            pw.println();
            pw.flush();
        }
    }

class TestEvenBeforeOdd {
    public static void main(String[] args) throws IOException {
        // Test case 1: Example from problem
        test("Test 1: Example",
                "5\n1 2 3 4 5\n",
                "2 4 1 3 5");

        // Test case 2: All even numbers
        test("Test 2: All even",
                "4\n2 4 6 8\n",
                "2 4 6 8");

        // Test case 3: All odd numbers
        test("Test 3: All odd",
                "4\n1 3 5 7\n",
                "1 3 5 7");

        // Test case 4: Already in correct order
        test("Test 4: Already sorted",
                "6\n2 4 6 1 3 5\n",
                "2 4 6 1 3 5");

        // Test case 5: Mixed with zero
        test("Test 5: With zero",
                "6\n0 1 2 3 4 5\n",
                "0 2 4 1 3 5");

        // Test case 6: Large input
        test("Test 6: Large input",
                "10\n9 8 7 6 5 4 3 2 1 0\n",
                "8 6 4 2 0 9 7 5 3 1");

        // Test case 7: Single element
        test("Test 7: Single even",
                "1\n2\n",
                "2");

        test("Test 7: Single odd",
                "1\n3\n",
                "3");
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
            SHL_031_EvenBeforeOdd.main(new String[]{});

            // Get the output
            String output = out.toString().trim();

            // Restore original input/output
            System.setIn(originalIn);
            System.setOut(originalOut);

            // Check result
            if (output.equals(expected)) {
                System.out.println(testName + ": ✓ PASSED");
            } else {
                System.out.println(testName + ": ✗ FAILED (expected: \"" + expected +
                        "\", got: \"" + output + "\")");
            }
        } catch (Exception e) {
            // Restore in case of exception
            System.setIn(originalIn);
            System.setOut(originalOut);
            System.out.println(testName + ": ERROR - " + e.getMessage());
        }
    }
}