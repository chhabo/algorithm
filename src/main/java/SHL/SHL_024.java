package main.java.SHL;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/*
1. 题目识别与中英文对照

        English Version:

        Problem Description:

        In a city there are N houses. Noddy is looking for a plot of land in the city on which to build his house. He wants to buy the largest plot of land that will allow him to build the largest possible house. All the houses in the city lie in a straight line and all of them have a house number and a second number indicating the position of the house from the entry point in the city. Noddy wants to find the houses between which he can build the largest possible house.

        Write an algorithm to help Noddy find the house numbers between which he can build his largest possible house.

        Input:

        The first line of the input consists of two space-separated integers - num and val, representing the number of houses (N) and the value val (where val is always equal to two representing the house number (H) and the position of houses (P) for N houses).

        The next N lines consist of two space-separated integers representing the house number (H) and the position (P), respectively.

        Output:

        Print two space-separated integers representing the house numbers in ascending order between which the largest plot is available.

        Constraints:

        2 ≤ num ≤ 10^6

        1 ≤ Hᵢ ≤ 10^6

        0 ≤ Pᵢ < 10^6

        0 ≤ i < num

Note:

        No two houses have the same position.

        In the case of multiple possibilities, print the one with the least distance from the reference point.

        Example:

        Input:

        复制
        5 2
        3 7
        1 9
        2 0
        5 15
        4 30

        (Output not given in the example, but based on analysis, the output should be 4 5)

        中文对照版本：

        问题描述：

        城市中有N座房子。Noddy想在城市中找一块地建造自己的房子。他希望购买能建造最大房子的最大地块。城市中所有房子都在一条直线上，每座房子有一个编号和一个表示该房子距离城市入口点位置的数字。Noddy想找到可以建造最大房子的两座房子之间的地块。

        编写一个算法帮助Noddy找到可以建造最大房子的两座房子的编号。

        输入：

        第一行包含两个空格分隔的整数 - num 和 val，分别表示房子数量（N）和值val（val总是等于2，表示后面每行给出房子编号（H）和位置（P））。

        接下来N行，每行包含两个空格分隔的整数，分别表示房子编号（H）和位置（P）。

        输出：

        打印两个空格分隔的整数，按升序表示最大地块所在的起始和结束房子编号。

        约束条件：

        2 ≤ num ≤ 10^6

        1 ≤ Hᵢ ≤ 10^6

        0 ≤ Pᵢ < 10^6

        0 ≤ i < num

注意：

        没有两座房子位置相同。

        如果有多个可能性，打印距离参考点最近的那个。

        示例：

        输入：

        复制
        5 2
        3 7
        1 9
        2 0
        5 15
        4 30

        （根据分析，输出应为 4 5）*/

class House implements Comparable<House> {
    int number;
    int position;

    public House(int number, int position) {
        this.number = number;
        this.position = position;
    }

    @Override
    public int compareTo(House other) {
        return Integer.compare(this.position, other.position);
    }
}

public class SHL_024 {

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter pw = new PrintWriter(System.out);

        // 读取第一行
        String[] firstLine = br.readLine().split(" ");
        int N = Integer.parseInt(firstLine[0]);
        int val = Integer.parseInt(firstLine[1]); // val总是2，可忽略

        House[] houses = new House[N];

        // 读取N行房子数据
        for (int i = 0; i < N; i++) {
            String[] line = br.readLine().split(" ");
            int number = Integer.parseInt(line[0]);
            int position = Integer.parseInt(line[1]);
            houses[i] = new House(number, position);
        }

        // 按位置排序
        Arrays.sort(houses);

        int maxGap = 0;
        int bestLeftNumber = 0;
        int bestRightNumber = 0;
        int bestLeftPosition = 0; // 用于处理多个最大间隔的情况

        // 计算相邻房子之间的间隔
        for (int i = 0; i < N - 1; i++) {
            int gap = houses[i + 1].position - houses[i].position;

            if (gap > maxGap) {
                maxGap = gap;
                // 记录左右房子的编号，注意输出时要按编号升序
                int leftNum = houses[i].number;
                int rightNum = houses[i + 1].number;
                bestLeftNumber = Math.min(leftNum, rightNum);
                bestRightNumber = Math.max(leftNum, rightNum);
                bestLeftPosition = houses[i].position;
            }
        }

        // 输出结果
        pw.println(bestLeftNumber + " " + bestRightNumber);
        pw.flush();
    }
}



class TestMain {
    public static void main(String[] args) throws IOException {


        // 测试用例1：示例输入
        // 测试用例1：示例输入
        System.out.println("测试用例1：示例输入");
        String input1 = "5 2\n3 7\n1 9\n2 0\n5 15\n4 30\n";
        String expected1 = "4 5";
        test(input1, expected1);

        // 测试用例2：两个相同最大间隔，选择起点靠近0的
        System.out.println("\n测试用例2：两个相同最大间隔");
        String input2 = "4 2\n1 0\n2 5\n3 10\n4 15\n";
        // 间隔：0-5(5), 5-10(5), 10-15(5)，最大间隔5，有3个
        // 选择起点最小的：0-5之间的间隔，房子编号1和2
        String expected2 = "1 2";
        test(input2, expected2);

        // 测试用例3：最小N
        System.out.println("\n测试用例3：N=2");
        String input3 = "2 2\n5 10\n3 0\n";
        // 排序后：位置0(编号3), 位置10(编号5)，间隔10
        String expected3 = "3 5";
        test(input3, expected3);
    }

    private static void test(String input, String expected) {
        try {
            // 重定向输入输出
            InputStream originalIn = System.in;
            PrintStream originalOut = System.out;

            ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(out);

            System.setIn(in);
            System.setOut(printStream);

            // 运行主程序
            SHL_024.main(new String[]{});

            // 获取输出
            String output = out.toString().trim();

            // 恢复原始输入输出
            System.setIn(originalIn);
            System.setOut(originalOut);

            // 验证结果
            if (output.equals(expected)) {
                System.out.println("通过! 输出: " + output);
            } else {
                System.out.println("失败! 期望: " + expected + ", 实际: " + output);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}