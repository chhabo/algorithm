package main.java.SHL;

import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;

public class SHL_011 {


    static class Employee implements Comparable<Employee> {
        int id;
        int efficiency;

        public Employee(int id, int efficiency) {
            this.id = id;
            this.efficiency = efficiency;
        }

        @Override
        public int compareTo(Employee other) {
            if (this.efficiency != other.efficiency) {
                return Integer.compare(this.efficiency, other.efficiency);
            }
            return Integer.compare(this.id, other.id);
        }
    }


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 读取员工数量
        int N = scanner.nextInt();

        // 读取员工效率
        int[] efficiencies = new int[N + 1]; // 1-based索引
        for (int i = 1; i <= N; i++) {
            efficiencies[i] = scanner.nextInt();
        }

        // 读取团队数量（未使用）
        int T = scanner.nextInt();

        // 读取团队ID（未使用）
        for (int i = 1; i <= N; i++) {
            scanner.nextInt();
        }

        // 读取天数
        int D = scanner.nextInt();

        // 读取每天参数数量（固定为2）
        int P = scanner.nextInt();

        // 读取每天的操作
        int[][] operations = new int[D][2];
        for (int i = 0; i < D; i++) {
            operations[i][0] = scanner.nextInt(); // 开除的员工ID
            operations[i][1] = scanner.nextInt(); // 辞职人数K
        }

        // 初始化在职员工集合（按效率升序，效率相同按ID升序）
        TreeSet<Employee> employees = new TreeSet<>();
        long totalReputation = 0;

        for (int i = 1; i <= N; i++) {
            employees.add(new Employee(i, efficiencies[i]));
            totalReputation += efficiencies[i];
        }

        // 存储每天的结果
        long[] dailyReputation = new long[D];

        // 处理每天的操作
        for (int day = 0; day < D; day++) {
            int fireId = operations[day][0];
            int K = operations[day][1];

            // 步骤1：开除指定员工
            Employee toFire = new Employee(fireId, efficiencies[fireId]);
            if (employees.contains(toFire)) {
                employees.remove(toFire);
                totalReputation -= efficiencies[fireId];
            }

            // 步骤2：K个效率最低的员工辞职
            Iterator<Employee> iterator = employees.iterator();
            int resigned = 0;

            while (iterator.hasNext() && resigned < K) {
                Employee lowest = iterator.next();
                iterator.remove(); // 从TreeSet中删除
                totalReputation -= lowest.efficiency;
                resigned++;
            }

            // 记录当天的声誉
            dailyReputation[day] = totalReputation;
        }

        // 输出结果
        for (int i = 0; i < D; i++) {
            System.out.print(dailyReputation[i]);
            if (i < D - 1) {
                System.out.print(" ");
            }
        }

        scanner.close();
    }

}
