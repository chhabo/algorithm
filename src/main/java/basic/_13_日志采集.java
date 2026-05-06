package basic;

import java.util.Scanner;

public class _13_日志采集 {


    public static int[] ints;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strs = sc.nextLine().split(" ");
        ints = new int[strs.length];
        for (int i = 0; i < strs.length; i++) {
            ints[i] = Integer.valueOf(strs[i]);
        }
        int count = ints[0]; //日志总数
        int max = ints[0]; //最大积分
        if (ints[0] >= 100)
        {
            System.out.println(100);
        }
        else
        {
            for (int i = 1; i < strs.length; i++) {
                int log = Integer.valueOf(strs[i]); //当前日志数
                count += log; //需要计算日志总数
                if (count >= 100) {
                    max = Math.max(max, 100 - jisuan(i));
                    break;
                } else {
                    max = Math.max(max, count - jisuan(i));
                }
            }
            System.out.println(max);
        }
    }

    /**
     * 求 index 前日志扣的分数
     *
     * @param index 当前日志索引
     * @return 需要扣除的分数
     */
    public static int jisuan(int index) {
        int core = 0;
        for (int i = 0; i < index; i++) {
            core += ints[i] * (index - i);
        }
        return core;
    }

}
