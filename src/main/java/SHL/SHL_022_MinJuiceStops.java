package main.java.SHL;

import java.util.*;

public class SHL_022_MinJuiceStops {
/*
    English Version:

    Problem Description:

    John is walking from his home to school. There are N juice stalls along the road. The i-th stall is located at distance dist[i] from home and provides lit[i] units of energy juice. John starts with K units of energy. It takes 1 unit of energy to walk 1 unit of distance. When passing a stall, John can choose to stop and drink the juice, which immediately adds lit[i] to his energy. He can only stop at most once at each stall. John must maintain positive energy​ at all times while walking (when arriving at a point, his energy can be 0). Determine the minimum number of stalls​ John must stop at to reach school at distance D. If it's impossible, return -1.

    Input:

    First line: integer N (1 ≤ N ≤ 10^4)

    Second line: N integers dist[1..N] (1 ≤ dist[i] ≤ 10^9, strictly increasing)

    Third line: N integers lit[1..N] (1 ≤ lit[i] ≤ 10^9)

    Fourth line: integers D and K (1 ≤ D ≤ 10^9, 1 ≤ K ≤ 10^9)

    Output:

    A single integer: minimum stops needed, or -1 if impossible.

            Constraints:

            1 ≤ N ≤ 10^4

            1 ≤ dist[i] ≤ 10^9, strictly increasing

1 ≤ lit[i] ≤ 10^9

            1 ≤ D ≤ 10^9

            1 ≤ K ≤ 10^9

    Energy must remain > 0 while walking between points

    Example:

    Input:

    复制
4
        5 7 8 10
        2 3 1 5
        15 5

    Output:

    复制
3

    中文对照版本:

    问题描述:

    约翰从家走到学校。路边有N个果汁摊。第i个摊位距离家dist[i]，提供lit[i]单位的能量果汁。约翰初始有K单位能量。每走1单位距离消耗1单位能量。经过摊位时，
    约翰可以选择停下喝果汁，立即增加lit[i]能量。每个摊位最多停一次。约翰必须在行走时始终保持正能量（到达某点时能量可为0）。求到达距离D的学校所需的最少停靠摊位数量。如果无法到达，返回-1。

    输入:

    第一行：整数N (1 ≤ N ≤ 10^4)

    第二行：N个整数dist[1..N] (1 ≤ dist[i] ≤ 10^9, 严格递增)

    第三行：N个整数lit[1..N] (1 ≤ lit[i] ≤ 10^9)

    第四行：整数D和K (1 ≤ D ≤ 10^9, 1 ≤ K ≤ 10^9)

    输出:

    单个整数：最少需要的停靠次数，若不可能则输出-1。

    约束条件:

            1 ≤ N ≤ 10^4

            1 ≤ dist[i] ≤ 10^9, 严格递增

            1 ≤ lit[i] ≤ 10^9

            1 ≤ D ≤ 10^9

            1 ≤ K ≤ 10^9

    在两点间行走时能量必须保持>0

    示例:

    输入:

    复制
4
        5 7 8 10
        2 3 1 5
        15 5

    输出:

    复制
3

*/


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 读取输入
        int N = scanner.nextInt();

        JuiceStall[] stalls = new JuiceStall[N];

        // 读取距离
        for (int i = 0; i < N; i++) {
            stalls[i] = new JuiceStall(scanner.nextInt(), 0);
        }

        // 读取果汁量
        for (int i = 0; i < N; i++) {
            stalls[i].juice = scanner.nextInt();
        }

        int D = scanner.nextInt();  // 学校距离
        int K = scanner.nextInt();  // 初始能量

        // 按距离排序（题目说严格递增，但排序更安全）
        Arrays.sort(stalls, (a, b) -> Integer.compare(a.distance, b.distance));

        // 使用最大堆存储经过摊位的果汁量
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        int currentEnergy = K;
        int currentPos = 0;
        int stops = 0;
        boolean reachable = true;

        // 遍历所有摊位
        for (JuiceStall stall : stalls) {
            int distanceToStall = stall.distance - currentPos;

            // 如果能量不够走到下一个摊位，从堆中取最大果汁补充
            while (currentEnergy < distanceToStall && !maxHeap.isEmpty()) {
                currentEnergy += maxHeap.poll();
                stops++;
            }

            // 如果还是不够，无法到达
            if (currentEnergy < distanceToStall) {
                reachable = false;
                break;
            }

            // 走到摊位，消耗能量
            currentEnergy -= distanceToStall;
            currentPos = stall.distance;

            // 将当前摊位的果汁加入堆（路过但未停）
            maxHeap.offer(stall.juice);
        }

        // 处理从最后一个摊位到学校的距离
        if (reachable) {
            int distanceToSchool = D - currentPos;

            while (currentEnergy < distanceToSchool && !maxHeap.isEmpty()) {
                currentEnergy += maxHeap.poll();
                stops++;
            }

            if (currentEnergy < distanceToSchool) {
                reachable = false;
            }
        }

        // 输出结果
        System.out.print(reachable ? stops : -1);

        scanner.close();
    }




}


 class TestMinJuiceStops {
    public static void main(String[] args) {

        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Collections.reverseOrder());

        maxHeap.offer(1);
        maxHeap.offer(2);
        maxHeap.offer(6);
        maxHeap.offer(4);
        maxHeap.offer(5);
        while (!maxHeap.isEmpty()){
            System.out.println(maxHeap.poll());
        }

        // 测试示例输入
        testExample();

        // 测试无法到达的情况
        testUnreachable();

        // 测试无需停靠的情况
        testNoStopNeeded();
    }

    private static void testExample() {
        System.out.println("=== 测试示例输入 ===");
        String input = "4\n5 7 8 10\n2 3 1 5\n15 5\n";
        System.out.println("输入:\n" + input);
        System.out.print("期望输出: 3");

        // 模拟输入
        java.io.ByteArrayInputStream in =
                new java.io.ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        // 保存原System.out
        java.io.PrintStream originalOut = System.out;

        try {
            // 重定向输出
            java.io.ByteArrayOutputStream out =
                    new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(out));

            // 运行主程序
            SHL_022_MinJuiceStops.main(new String[]{});

            String output = out.toString().trim();
            System.out.println("\n实际输出: " + output);
            System.out.println("测试结果: " +
                    (output.equals("3") ? "✓ 通过" : "✗ 失败"));
        } finally {
            System.setOut(originalOut);
        }
        System.out.println();
    }

    private static void testUnreachable() {
        System.out.println("=== 测试无法到达的情况 ===");
        String input = "2\n5 10\n1 1\n20 1\n";
        System.out.println("输入:\n" + input);
        System.out.print("期望输出: -1");

        java.io.ByteArrayInputStream in =
                new java.io.ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        java.io.PrintStream originalOut = System.out;

        try {
            java.io.ByteArrayOutputStream out =
                    new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(out));

            SHL_022_MinJuiceStops.main(new String[]{});

            String output = out.toString().trim();
            System.out.println("\n实际输出: " + output);
            System.out.println("测试结果: " +
                    (output.equals("-1") ? "✓ 通过" : "✗ 失败"));
        } finally {
            System.setOut(originalOut);
        }
        System.out.println();
    }

    private static void testNoStopNeeded() {
        System.out.println("=== 测试无需停靠的情况 ===");
        String input = "3\n5 10 15\n2 2 2\n20 20\n";
        System.out.println("输入:\n" + input);
        System.out.print("期望输出: 0");

        java.io.ByteArrayInputStream in =
                new java.io.ByteArrayInputStream(input.getBytes());
        System.setIn(in);

        java.io.PrintStream originalOut = System.out;

        try {
            java.io.ByteArrayOutputStream out =
                    new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(out));

            SHL_022_MinJuiceStops.main(new String[]{});

            String output = out.toString().trim();
            System.out.println("\n实际输出: " + output);
            System.out.println("测试结果: " +
                    (output.equals("0") ? "✓ 通过" : "✗ 失败"));
        } finally {
            System.setOut(originalOut);
        }
        System.out.println();
    }
}

class JuiceStall {
    int distance;
    int juice;

    public JuiceStall(int distance, int juice) {
        this.distance = distance;
        this.juice = juice;
    }
}