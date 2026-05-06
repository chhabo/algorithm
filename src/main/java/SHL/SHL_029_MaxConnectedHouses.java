package main.java.SHL;

import java.*;
import java.io.*;
import java.util.Scanner;

public class SHL_029_MaxConnectedHouses {

/*
    English Version:

    Problem Description:

    You are given a grid of size rows x cols, where each cell represents either a house (value 1) or empty land (value 0). A group of houses forms a connected region if they are adjacent to each other horizontally or vertically (not diagonally). The area of a region is the number of houses in that region.

    You need to find the maximum area of a connected region of houses in the grid.

    Input:

    First line: two integers rows and cols, representing the number of rows and columns of the grid.

    Next rows lines: each line contains cols space-separated integers (0 or 1).

    Output:

    Print a single integer representing the maximum area of a connected region of houses.

    Constraints:

            1 ≤ rows, cols ≤ 100

    Grid values are either 0 or 1

    Example:

    Input:

    复制
4 5
        1 1 0 0 0
        0 1 0 0 1
        1 0 0 1 1
        0 0 0 0 0

    Output:

    复制
4

    Chinese Version:

    问题描述:

    给定一个大小为 rows x cols 的网格，其中每个单元格表示房屋（值1）或空地（值0）。如果房屋在水平或垂直方向（不包括对角线）相邻，则它们形成一个连通区域。一个区域的面积是该区域中房屋的数量。

    你需要在网格中找出最大的连通房屋区域的面积。

    输入:

    第一行：两个整数 rows 和 cols，表示网格的行数和列数。

    接下来 rows 行：每行包含 cols 个空格分隔的整数（0 或 1）。

    输出:

    打印一个整数，表示最大的连通房屋区域的面积。

    约束条件:

            1 ≤ rows, cols ≤ 100

    网格值只能是 0 或 1

    示例:

    输入:

    复制
4 5
        1 1 0 0 0
        0 1 0 0 1
        1 0 0 1 1
        0 0 0 0 0

    输出:

    复制
4


 */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Read grid dimensions
        int rows = scanner.nextInt();
        int cols = scanner.nextInt();

        // Read the grid
        int[][] grid = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = scanner.nextInt();
            }
        }

        // Find maximum area
        int maxArea = findMaxConnectedArea(grid, rows, cols);

        // Output result
        System.out.println(maxArea);

        scanner.close();
    }

    private static int findMaxConnectedArea(int[][] grid, int rows, int cols) {
        if (rows == 0 || cols == 0) {
            return 0;
        }

        // Create a visited array to track visited cells
        boolean[][] visited = new boolean[rows][cols];
        int maxArea = 0;

        // Traverse the grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // If cell contains a house and hasn't been visited
                if (grid[i][j] == 1 && !visited[i][j]) {
                    // Calculate area of this connected region
                    int area = dfs(grid, i, j, rows, cols, visited);
                    // Update maximum area
                    maxArea = Math.max(maxArea, area);
                }
            }
        }

        return maxArea;
    }

    private static int dfs(int[][] grid, int i, int j, int rows, int cols, boolean[][] visited) {
        // Check bounds, visited status, and if it's a house
        if (i < 0 || i >= rows || j < 0 || j >= cols ||
                grid[i][j] == 0 || visited[i][j]) {
            return 0;
        }

        // Mark current cell as visited
        visited[i][j] = true;

        int area = 1; // Current house

        // Explore all 4 directions: up, down, left, right
        area += dfs(grid, i - 1, j, rows, cols, visited); // up
        area += dfs(grid, i + 1, j, rows, cols, visited); // down
        area += dfs(grid, i, j - 1, rows, cols, visited); // left
        area += dfs(grid, i, j + 1, rows, cols, visited); // right

        return area;
    }
}


class TestMaxConnectedHouses {
    public static void main(String[] args) throws IOException {
        // Test case 1: Example from problem
        test("Test 1: Example",
                "4 5\n" +
                        "1 1 0 0 0\n" +
                        "0 1 0 0 1\n" +
                        "1 0 0 1 1\n" +
                        "0 0 0 0 0\n",
                "3");

        // Test case 2: Single house
        test("Test 2: Single house",
                "3 3\n" +
                        "0 0 0\n" +
                        "0 1 0\n" +
                        "0 0 0\n",
                "1");

        // Test case 3: All houses connected
        test("Test 3: All houses connected",
                "3 3\n" +
                        "1 1 1\n" +
                        "1 1 1\n" +
                        "1 1 1\n",
                "9");

        // Test case 4: No houses
        test("Test 4: No houses",
                "2 2\n" +
                        "0 0\n" +
                        "0 0\n",
                "0");

        // Test case 5: Multiple separate regions
        test("Test 5: Multiple separate regions",
                "4 4\n" +
                        "1 1 0 0\n" +
                        "1 1 0 1\n" +
                        "0 0 1 1\n" +
                        "0 0 1 1\n",
                "5");

        // Test case 6: Long connected region
        test("Test 6: Long connected region",
                "5 5\n" +
                        "1 1 0 0 0\n" +
                        "0 1 1 0 0\n" +
                        "0 0 1 1 0\n" +
                        "0 0 0 1 1\n" +
                        "0 0 0 0 1\n",
                "9");
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
            SHL_029_MaxConnectedHouses.main(new String[]{});

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