package main.java.SHL;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SHL_026 {
/*
    English Version:

    Problem Description:

    Given a tree with N nodes and R edges (R = N-1), we need to find the edge that yields the maximum toll revenue. The toll revenue of an edge (u,v) is defined as the number of pairs of nodes (x,y) such that the unique path from x to y must pass through this edge. In other words, if we remove the edge (u,v), the tree splits into two components of sizes s and N-s; then the revenue is s * (N-s).

    We need to output the edge (with nodes sorted in ascending order) that gives the maximum revenue. If multiple edges give the same maximum revenue, output the one with the smallest lexicographic order (i.e., smallest u, then smallest v).

    Input:

    First line: two integers N and R (where R = N-1).

    Next R lines: each line contains two integers u and v, representing an edge.

    Output:

    Two space-separated integers (smaller first) representing the edge with maximum revenue.

    Constraints:

            2 ≤ N ≤ 10^5

            1 ≤ u, v ≤ N

    The input is a valid tree.

    Example:

    Input:

    复制
5 4
        1 2
        2 3
        3 4
        4 5

    Output:

    复制
2 3

    Chinese Version:

    问题描述:

    给定一棵有N个节点、R条边（R = N-1）的树，我们需要找到产生最大通行费收入的边。边(u,v)的通行费收入定义为：必须经过该边的节点对(x,y)的数量。换句话说，如果我们移除边(u,v)，树会被分成大小分别为s和N-s的两个连通分量；那么收入就是s * (N-s)。

    我们需要输出产生最大收入的边（节点按升序排列）。如果有多条边产生相同的最大收入，则输出字典序最小的边（即u最小，然后v最小）。

    输入:

    第一行：两个整数N和R（其中R = N-1）。

    接下来R行：每行包含两个整数u和v，表示一条边。

    输出:

    两个空格分隔的整数（较小的在前），表示具有最大收入的边。

    约束条件:

            2 ≤ N ≤ 10^5

            1 ≤ u, v ≤ N

    输入是一棵有效的树。

    示例:

    输入:

    复制
5 4
        1 2
        2 3
        3 4
        4 5

    输出:

    复制
2 3*/


        private static List<Integer>[] graph;
        private static int[] subtreeSize;
        private static int n;
        private static long maxRevenue;
        private static int resultU, resultV;

        public static void main(String[] args) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter pw = new PrintWriter(System.out);

            // Read input
            StringTokenizer st = new StringTokenizer(br.readLine());
            n = Integer.parseInt(st.nextToken());
            int r = Integer.parseInt(st.nextToken());

            // Initialize adjacency list
            graph = new ArrayList[n + 1];
            for (int i = 1; i <= n; i++) {
                graph[i] = new ArrayList<>();
            }

            // Read edges
            for (int i = 0; i < r; i++) {
                st = new StringTokenizer(br.readLine());
                int u = Integer.parseInt(st.nextToken());
                int v = Integer.parseInt(st.nextToken());
                graph[u].add(v);
                graph[v].add(u);
            }

            // Initialize arrays
            subtreeSize = new int[n + 1];
            maxRevenue = -1;
            resultU = 0;
            resultV = 0;

            // Perform DFS to calculate subtree sizes
            dfs(1, 0);

            // Ensure output is in sorted order
            if (resultU > resultV) {
                int temp = resultU;
                resultU = resultV;
                resultV = temp;
            }

            pw.println(resultU + " " + resultV);
            pw.flush();
        }

        private static void dfs(int node, int parent) {
            subtreeSize[node] = 1;  // Count the node itself

            for (int neighbor : graph[node]) {
                if (neighbor == parent) {
                    continue;  // Skip the parent node
                }

                dfs(neighbor, node);
                subtreeSize[node] += subtreeSize[neighbor];

                // For the edge (node, neighbor), calculate revenue
                // Revenue = size of component containing neighbor * (n - that size)
                long revenue = (long) subtreeSize[neighbor] * (n - subtreeSize[neighbor]);

                // Get the edge in sorted order
                int u = Math.min(node, neighbor);
                int v = Math.max(node, neighbor);

                // Update maximum revenue and result
                if (revenue > maxRevenue) {
                    maxRevenue = revenue;
                    resultU = u;
                    resultV = v;
                } else if (revenue == maxRevenue) {
                    // If revenue is equal, choose lexicographically smaller edge
                    if (u < resultU || (u == resultU && v < resultV)) {
                        resultU = u;
                        resultV = v;
                    }
                }
            }
        }
    }



class TestMaxTollRevenue {
    public static void main(String[] args) throws IOException {
        // Test case 1: Example from problem
        System.out.println("=== Test Case 1: Example ===");
        test("5 4\n1 2\n2 3\n3 4\n4 5\n", "2 3");

        // Test case 2: Star tree
        System.out.println("\n=== Test Case 2: Star tree ===");
        test("6 5\n1 2\n1 3\n1 4\n1 5\n1 6\n", "1 2");

        // Test case 3: Balanced binary tree
        System.out.println("\n=== Test Case 3: Balanced binary tree ===");
        test("7 6\n1 2\n1 3\n2 4\n2 5\n3 6\n3 7\n", "2 4");

        // Test case 4: Simple two nodes
        System.out.println("\n=== Test Case 4: Two nodes ===");
        test("2 1\n1 2\n", "1 2");

        // Test case 5: Path graph
        System.out.println("\n=== Test Case 5: Path graph ===");
        test("6 5\n1 2\n2 3\n3 4\n4 5\n5 6\n", "3 4");
    }

    private static void test(String input, String expectedOutput) {
        // Save original System.in and System.out
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        try {
            // Set up test input
            ByteArrayInputStream testInput = new ByteArrayInputStream(input.getBytes());
            System.setIn(testInput);

            // Set up output capture
            ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
            PrintStream testOutput = new PrintStream(outputCapture);
            System.setOut(testOutput);

            // Run the main program
            SHL_026.main(new String[]{});

            // Get the program output
            String actualOutput = outputCapture.toString().trim();

            // Restore original System.in and System.out
            System.setIn(originalIn);
            System.setOut(originalOut);

            // Check the result
            if (actualOutput.equals(expectedOutput)) {
                System.out.println("✓ PASSED! Output: " + actualOutput);
            } else {
                System.out.println("✗ FAILED! Expected: " + expectedOutput + ", Got: " + actualOutput);
            }
        } catch (Exception e) {
            // Restore original System.in and System.out in case of exception
            System.setIn(originalIn);
            System.setOut(originalOut);
            System.out.println("✗ Exception occurred: " + e.getMessage());
        }
    }
}