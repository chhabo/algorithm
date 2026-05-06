package main.java.SHL;

import javafx.beans.binding.When;

import java.util.*;

public class SHL_018 {

/*
    题目ID: SHL_018

    英文题目 (English Description):

    Shortest Job First (SJF) is a system for scheduling task requests. Each task request is characterized by its request time (i.e., the time at which the task is submitted to the system) and its duration (i.e., the time needed to complete the task). When the SJF system completes a task, it selects the task with the smallest duration to execute next. If multiple tasks have the same smallest duration, SJF selects the task with the earliest request time. The waiting time for a task is the difference between the request time and the actual start time (i.e., the time that it spends waiting for the system to execute it). You may assume that the tasks arrive in such frequency that the system executes tasks constantly and is never idle. Given a list of request times and duration times, calculate the average task waiting time when scheduled using the Shortest Job First (SJF) algorithm.

    中文翻译 (Chinese Description):

    最短作业优先（SJF）是一种任务请求调度系统。每个任务请求由其请求时间（即任务提交到系统的时间）和持续时间（即完成任务所需的时间）来表征。当SJF系统完成一个任务时，
    它会选择持续时间最短的任务作为下一个执行的任务。如果多个任务具有相同的最短持续时间，SJF会选择请求时间最早的任务。任务的等待时间是其请求时间与实际开始时间之间的差值（即任务等待系统执行所花费的时间）。
    您可以假设任务到达的频率使得系统不断执行任务并且从不空闲。给定请求时间和持续时间列表，计算使用最短作业优先（SJF）算法调度时的平均任务等待时间。

    输入格式 (Input Format):

    第一行：一个正整数N，表示任务的数量

    第二行：N个用空格分隔的整数，表示任务的请求时间 req[1], req[2], ..., req[N]

    第三行：N个用空格分隔的整数，表示任务的持续时间 dur[1], dur[2], ..., dur[N]

    输出格式 (Output Format):

    一个实数，表示平均任务等待时间，使用非抢占式SJF调度算法计算。输出保留两位小数。

    约束条件 (Constraints):

            0 ≤ req[i] < 100

            0 < dur[i] < 100

            0 ≤ i < N

    注意：请求时间和持续时间已按请求时间的升序排列。

    示例 (Example):

    输入：

    复制
4
        0 2 4 5
        7 4 1 4

    输出：

    复制
4.00

    示例解释 (Explanation):

    按照SJF调度：

    时间0：任务1到达，持续时间7，执行

    时间2：任务2到达，持续时间4

    时间4：任务3到达，持续时间1

    时间5：任务4到达，持续时间4

    时间7：任务1完成，队列中任务3持续时间最短（1），执行任务3

            任务3等待时间 = 7 - 4 = 3

    时间8：任务3完成，队列中任务2和4持续时间相同（4），选择请求时间更早的任务2

            任务2等待时间 = 8 - 2 = 6

    时间12：任务2完成，执行任务4

            任务4等待时间 = 12 - 5 = 7

    时间16：任务4完成

            总等待时间 = 0 + 6 + 3 + 7 = 16

    平均等待时间 = 16 / 4 = 4.00*/


    /**
     * 任务类，包含请求时间和持续时间
     */
    static class Task {
        int id;          // 任务ID
        int requestTime; // 请求时间
        int duration;    // 持续时间

        public Task(int id, int requestTime, int duration) {
            this.id = id;
            this.requestTime = requestTime;
            this.duration = duration;
        }

        @Override
        public String toString() {
            return "Task{" + "id=" + id + ", req=" + requestTime + ", dur=" + duration + '}';
        }
    }

    /**
     * 计算SJF调度下的平均等待时间
     *
     * @param N 任务数量
     * @param requestTimes 请求时间数组
     * @param durationTimes 持续时间数组
     * @return 平均等待时间
     */
    public static double calculateAverageWaitingTime(int N, int[] requestTimes, int[] durationTimes) {
        // 创建任务列表
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            tasks.add(new Task(i, requestTimes[i], durationTimes[i]));
        }

        // 按请求时间排序（题目说已排序，但这里再确认）
        tasks.sort(Comparator.comparingInt(a -> a.requestTime));

        // 优先队列，按持续时间排序，持续时间相同则按请求时间排序
        PriorityQueue<Task> pq = new PriorityQueue<>((a, b) -> {
            if (a.duration != b.duration) {
                return Integer.compare(a.duration, b.duration);
            }
            return Integer.compare(a.requestTime, b.requestTime);
        });

        int currentTime = 0;
        int totalWaitingTime = 0;
        int index = 0; // 指向下一个要加入队列的任务

        while (index < N || !pq.isEmpty()) {
            // 将所有在当前时间或之前到达的任务加入优先队列
            while (index < N && tasks.get(index).requestTime <= currentTime) {
                pq.offer(tasks.get(index));
                index++;
            }

            // 如果队列为空，说明当前没有任务到达，跳到下一个任务的到达时间
            if (pq.isEmpty()) {
                if (index < N) {
                    currentTime = tasks.get(index).requestTime;
                    continue;
                } else {
                    break;
                }
            }

            // 执行队列中持续时间最短的任务
            Task currentTask = pq.poll();

            // 计算等待时间
            int waitingTime = currentTime - currentTask.requestTime;
            if (waitingTime < 0) waitingTime = 0; // 不应该发生，但安全起见

            totalWaitingTime += waitingTime;

            // 更新当前时间
            currentTime += currentTask.duration;
        }

        // 计算平均等待时间
        return (double) totalWaitingTime / N;
    }
    //0,2,3,4
    //7,4,1,4

    /**
     * 主函数，处理标准输入输出
     */
    public static void main1(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 读取任务数量
        int N = scanner.nextInt();

        // 读取请求时间
        int[] requestTimes = new int[N];
        for (int i = 0; i < N; i++) {
            requestTimes[i] = scanner.nextInt();
        }

        // 读取持续时间
        int[] durationTimes = new int[N];
        for (int i = 0; i < N; i++) {
            durationTimes[i] = scanner.nextInt();
        }

        // 计算平均等待时间
        double avgWaitingTime = calculateAverageWaitingTime(N, requestTimes, durationTimes);

        // 输出结果，保留两位小数
        System.out.printf("%.2f%n", avgWaitingTime);

        scanner.close();
    }

    /**
     * 测试函数，包含多个测试用例
     */
    public static void testSJFScheduler() {
        System.out.println("=== SHL_018 最短作业优先调度算法测试开始 ===\n");

        // 测试用例1：题目示例
        int N1 = 4;
        int[] req1 = {0, 2, 4, 5};
        int[] dur1 = {7, 4, 1, 4};
        double result1 = calculateAverageWaitingTime(N1, req1, dur1);
        System.out.println("测试用例1 - 题目示例：");
        System.out.println("  任务数: " + N1);
        System.out.println("  请求时间: " + Arrays.toString(req1));
        System.out.println("  持续时间: " + Arrays.toString(dur1));
        System.out.println("  预期平均等待时间: 4.00");
        System.out.printf("  实际平均等待时间: %.2f%n", result1);
        System.out.println("  测试" + (Math.abs(result1 - 4.00) < 0.01 ? "通过" : "失败") + "\n");

        // 测试用例2：所有任务同时到达
        int N2 = 3;
        int[] req2 = {0, 0, 0};
        int[] dur2 = {5, 2, 3};
        double result2 = calculateAverageWaitingTime(N2, req2, dur2);
        System.out.println("测试用例2 - 所有任务同时到达：");
        System.out.println("  任务数: " + N2);
        System.out.println("  请求时间: " + Arrays.toString(req2));
        System.out.println("  持续时间: " + Arrays.toString(dur2));
        // 计算：先执行持续时间2，等待0；再执行持续时间3，等待2；再执行持续时间5，等待5
        // 总等待 = 0 + 2 + 5 = 7，平均 = 7/3 ≈ 2.33
        System.out.println("  预期平均等待时间: 2.33");
        System.out.printf("  实际平均等待时间: %.2f%n", result2);
        System.out.println("  测试" + (Math.abs(result2 - 7.0/3) < 0.01 ? "通过" : "失败") + "\n");

        // 测试用例3：任务按顺序到达，持续时间递增
        int N3 = 4;
        int[] req3 = {0, 1, 2, 3};
        int[] dur3 = {1, 2, 3, 4};
        double result3 = calculateAverageWaitingTime(N3, req3, dur3);
        System.out.println("测试用例3 - 持续时间递增：");
        System.out.println("  任务数: " + N3);
        System.out.println("  请求时间: " + Arrays.toString(req3));
        System.out.println("  持续时间: " + Arrays.toString(dur3));
        // 计算：时间0执行任务1(持续1)，等待0；时间1执行任务2(持续2)，等待0；时间3执行任务3(持续3)，等待1；时间6执行任务4(持续4)，等待3
        // 总等待 = 0 + 0 + 1 + 3 = 4，平均 = 1.00
        System.out.println("  预期平均等待时间: 1.00");
        System.out.printf("  实际平均等待时间: %.2f%n", result3);
        System.out.println("  测试" + (Math.abs(result3 - 1.00) < 0.01 ? "通过" : "失败") + "\n");

        // 测试用例4：任务按顺序到达，持续时间递减
        int N4 = 4;
        int[] req4 = {0, 1, 2, 3};
        int[] dur4 = {4, 3, 2, 1};
        double result4 = calculateAverageWaitingTime(N4, req4, dur4);
        System.out.println("测试用例4 - 持续时间递减：");
        System.out.println("  任务数: " + N4);
        System.out.println("  请求时间: " + Arrays.toString(req4));
        System.out.println("  持续时间: " + Arrays.toString(dur4));
        // 计算：时间0执行任务1(持续4)，等待0；时间1任务2到达(持续3)，时间2任务3到达(持续2)，时间3任务4到达(持续1)
        // 时间4任务1完成，队列中任务4持续时间最短(1)，执行任务4，等待1
        // 时间5任务4完成，队列中任务3持续时间最短(2)，执行任务3，等待3
        // 时间7任务3完成，执行任务2(持续3)，等待6
        // 总等待 = 0 + 6 + 3 + 1 = 10，平均 = 2.50
        System.out.println("  预期平均等待时间: 2.50");
        System.out.printf("  实际平均等待时间: %.2f%n", result4);
        System.out.println("  测试" + (Math.abs(result4 - 2.50) < 0.01 ? "通过" : "失败") + "\n");

        // 测试用例5：持续时间相同，请求时间不同
        int N5 = 3;
        int[] req5 = {0, 2, 4};
        int[] dur5 = {3, 3, 3};
        double result5 = calculateAverageWaitingTime(N5, req5, dur5);
        System.out.println("测试用例5 - 持续时间相同：");
        System.out.println("  任务数: " + N5);
        System.out.println("  请求时间: " + Arrays.toString(req5));
        System.out.println("  持续时间: " + Arrays.toString(dur5));
        // 计算：时间0执行任务1(持续3)，等待0；时间2任务2到达，时间3任务1完成，执行任务2(请求时间最早)，等待1
        // 时间4任务3到达，时间6任务2完成，执行任务3，等待2
        // 总等待 = 0 + 1 + 2 = 3，平均 = 1.00
        System.out.println("  预期平均等待时间: 1.00");
        System.out.printf("  实际平均等待时间: %.2f%n", result5);
        System.out.println("  测试" + (Math.abs(result5 - 1.00) < 0.01 ? "通过" : "失败") + "\n");

        // 测试用例6：单个任务
        int N6 = 1;
        int[] req6 = {5};
        int[] dur6 = {10};
        double result6 = calculateAverageWaitingTime(N6, req6, dur6);
        System.out.println("测试用例6 - 单个任务：");
        System.out.println("  任务数: " + N6);
        System.out.println("  请求时间: " + Arrays.toString(req6));
        System.out.println("  持续时间: " + Arrays.toString(dur6));
        System.out.println("  预期平均等待时间: 0.00");
        System.out.printf("  实际平均等待时间: %.2f%n", result6);
        System.out.println("  测试" + (Math.abs(result6 - 0.00) < 0.01 ? "通过" : "失败") + "\n");

        // 测试用例7：大间隔到达
        int N7 = 3;
        int[] req7 = {0, 10, 20};
        int[] dur7 = {5, 5, 5};
        double result7 = calculateAverageWaitingTime(N7, req7, dur7);
        System.out.println("测试用例7 - 大间隔到达：");
        System.out.println("  任务数: " + N7);
        System.out.println("  请求时间: " + Arrays.toString(req7));
        System.out.println("  持续时间: " + Arrays.toString(dur7));
        // 计算：每个任务到达时系统空闲，等待时间为0
        System.out.println("  预期平均等待时间: 0.00");
        System.out.printf("  实际平均等待时间: %.2f%n", result7);
        System.out.println("  测试" + (Math.abs(result7 - 0.00) < 0.01 ? "通过" : "失败") + "\n");

        System.out.println("=== SHL_018 最短作业优先调度算法测试完成 ===");
    }

    /**
     * 程序入口点，可以选择运行主函数或测试函数
     */
    public static void main(String[] args) {
        // 如果要运行测试，取消下面一行的注释
         testSJFScheduler();

        // 如果要运行主函数（从控制台输入），使用下面的main方法
//        runMain();
    }

    /**
     * 包装的主函数，避免与测试冲突
     */
    public static void runMain() {
        main(new String[0]);
    }
}
