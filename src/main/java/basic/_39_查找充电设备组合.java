package basic;

import java.util.Arrays;
import java.util.Scanner;

public class _39_查找充电设备组合 {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt();

        int[] p = new int[n];
        for (int i = 0; i < n; i++) {
            p[i] = sc.nextInt();
        }
        Arrays.sort(p);
        int max = sc.nextInt();
        int max_p = 0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] > max) {
                break;
            }
            max_p = Math.max(getMaxP(p, i, max), max_p);
        }
        System.out.println(max_p);

    }
//  50 20 20 60
//  90

    /**
     * 4
     * 50 20 20 60
     * 90
     *
     * @param p
     * @param i
     * @param max
     * @return
     */
    private static int getMaxP(int[] p, int i, int max) {
        int maxP = p[i];

        for (int j = i + 1; j < p.length; j++) {
            if (maxP + p[j] < max) {
                maxP += p[j];
            } else if (maxP + p[j] == max) {
                return max;
            } else {
                break;
            }
        }
        return maxP;
    }


    public static int p_max;
    public static int max;

    public static void main1(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        sc.nextLine();
        String[] strs = sc.nextLine().split(" ");
        p_max = sc.nextInt();
        int[] ints = Arrays.stream(strs).mapToInt(Integer::parseInt).toArray(); //字符串数组转化为正数数组
        int count = Arrays.stream(ints).sum(); //求出数组的总和
        if (count <= p_max) {
            System.out.println(count); //总和小于等于 p_max 则直接输出总和
        } else {
            handle(ints, 0, 0);
            System.out.println(max);
        }
    }

    /**
     * 经典的 m 个数中求 n 个数的算法（强烈建议大家记住这个算法，很多地方可以套
     * 用）
     *
     * @param ints  正数数组
     * @param sum   功率和
     * @param index 索引
     */
    public static void handle(int[] ints, int sum, int index) {
        if (sum >= p_max) {
            if (sum == p_max) {
                max = sum;
            } else {
                max = Math.max(max, sum - ints[index - 1]);
            }
        } else {
            for (int i = index; i < ints.length; i++) {
                sum += ints[i];
                handle(ints, sum, i + 1);
                sum -= ints[i];
            }


        }
    }
}
