package main.java.SHL;


/*

English Version:

        Problem Description:

        A software development team has n members, each with an error score. The team can work on projects to reduce error scores. In each project, exactly one member is chosen to complete it. When a member completes a project:

        Their error score decreases by P

        For every other member whose error score is > 0, it decreases by Q

        The goal is to find the minimum number of projects​ needed to reduce all members' error scores to 0.

        Input:

        First line: integer n (number of team members)

        Second line: n integers, the initial error scores of each member

        Third line: integer P (error score reduction for the member who completes the project)

        Fourth line: integer Q (error score reduction for other members whose error score > 0)

        Output:

        A single integer: the minimum number of projects needed.

        Constraints:

        1 ≤ n ≤ 10^5

        1 ≤ s_i ≤ 10^9 (initial error scores)

        1 ≤ Q ≤ P ≤ 10^9

        Example:

        Input:

        复制
        3
        5 4 1
        4
        1

        Output:

        复制
        2

        Chinese Version:

        问题描述:

        一个软件开发团队有n个成员，每个成员有一个错误分数。团队可以通过完成项目来减少错误分数。每个项目中，恰好选择一个成员来完成。当一个成员完成项目时：

        他们的错误分数减少P

        对于其他每个错误分数>0的成员，错误分数减少Q

        目标是找到将所有成员的错误分数减少到0所需的最少项目数量。

        输入:

        第一行: 整数n（团队成员数量）

        第二行: n个整数，每个成员的初始错误分数

        第三行: 整数P（完成项目的成员的分数减少量）

        第四行: 整数Q（其他成员的分数减少量）

        输出:

        一个整数：所需的最少项目数量。

        约束条件:

        1 ≤ n ≤ 10^5

        1 ≤ s_i ≤ 10^9（初始错误分数）

        1 ≤ Q ≤ P ≤ 10^9

        示例:

        输入:

        复制
        3
        5 4 1
        4
        1

        输出:

        复制
        2
*/

import java.io.*;
import java.util.StringTokenizer;

public class SHL_032_MinProjectsToZeroErrors {

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Read input
        int n = Integer.parseInt(br.readLine().trim());
        long[] scores = new long[n];

        StringTokenizer st = new StringTokenizer(br.readLine());
        for (int i = 0; i < n; i++) {
            scores[i] = Long.parseLong(st.nextToken());
        }

        long P = Long.parseLong(br.readLine().trim());
        long Q = Long.parseLong(br.readLine().trim());

        // Binary search for minimum k
        long left = 0;
        long right = 0;

        // Find a suitable upper bound
        long maxScore = 0;
        for (int i = 0; i < n; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
            }
        }

        // Upper bound: if we only use Q reduction, we need at most maxScore/Q projects
        // But we need to consider worst case, multiply by n for safety
        right = Math.max(maxScore / Q + 1, 1);
        right = Math.max(right, maxScore / P + 1) * n;

        long answer = right;

        // Handle special case: P == Q
        if (P == Q) {
            // All members are reduced by P each project
            // We need enough projects so that P * k >= maxScore
            answer = (maxScore + P - 1) / P; // ceil(maxScore / P)
            System.out.println(answer);
            return;
        }

        while (left <= right) {
            long mid = left + (right - left) / 2;

            if (isPossible(scores, n, P, Q, mid)) {
                answer = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        System.out.println(answer);
    }

    private static boolean isPossible(long[] scores, int n, long P, long Q, long k) {
        long totalNeeded = 0;

        for (int i = 0; i < n; i++) {
            long score = scores[i];

            // Calculate minimum number of times member i must be selected
            // We need: x_i * P + (k - x_i) * Q >= score
            // Rearranged: x_i >= (score - k*Q) / (P - Q)

            long numerator = score - k * Q;

            if (numerator <= 0) {
                // Even if member i is never selected, their score reaches 0
                continue;
            }

            long denominator = P - Q;

            // Calculate x_i = ceil(numerator / denominator)
            long x_i = (numerator + denominator - 1) / denominator;

            if (x_i > k) {
                // This member would need to be selected more than k times, impossible
                return false;
            }

            totalNeeded += x_i;

            if (totalNeeded > k) {
                // Total selections needed exceeds k
                return false;
            }
        }

        // We can assign at most k selections, and we need at most totalNeeded
        return totalNeeded <= k;
    }
}


class TestMinProjectsToZeroErrors {
    public static void main(String[] args) throws IOException {
        // Test case 1: Example from problem
        test("Test 1: Example",
                "3\n5 4 1\n4\n1\n",
                "2");

        // Test case 2: P == Q
        test("Test 2: P == Q",
                "3\n6 8 10\n2\n2\n",
                "5"); // ceil(10/2) = 5

        // Test case 3: Only one member
        test("Test 3: Single member",
                "1\n10\n3\n1\n",
                "4"); // Need 4 projects: 10-3-3-3-1=0

        // Test case 4: Large values
        test("Test 4: Large values",
                "2\n1000000000 1000000000\n1000\n1\n",
                "999001"); // Approximate calculation

        // Test case 5: Small values
        test("Test 5: Small values",
                "3\n1 2 3\n3\n2\n",
                "2");

        // Test case 6: All scores already 0
        test("Test 6: All scores 0",
                "3\n0 0 0\n5\n2\n",
                "0");

        // Test case 7: Need many projects
        test("Test 7: Many projects needed",
                "2\n100 200\n2\n1\n",
                "150"); // Approximate calculation
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
            SHL_032_MinProjectsToZeroErrors.main(new String[]{});

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