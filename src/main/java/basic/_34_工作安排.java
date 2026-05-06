package basic;

import java.util.Scanner;

public class _34_工作安排 {

    public static void main1(String[] args) {
        Scanner sc = new Scanner(System.in);
        int T = sc.nextInt();
        int n = sc.nextInt();
        int[] times = new int[n];
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            times[i] = sc.nextInt();
            values[i] = sc.nextInt();
        }
        int res = handle(T, times, values);
        System.out.println(res);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int T = sc.nextInt();
        int n = sc.nextInt();
        int[] times = new int[n];
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            times[i] = sc.nextInt();
            values[i] = sc.nextInt();
        }
        int max = 0;
        for (int i = 0; i < n; i++) {
            max = Math.max(getMaxIncome(times,values,i,T,values[0]),max);
        }
        System.out.println(max);
    }
    /**
     * 动态规划
     * @param T 工作时长
     * @param times 任务工作数组
     * @param values 任务报酬数组
     * @return
     */
    public static int handle(int T, int[] times, int[] values){
        int[] dp = new int[T+1]; //相当于各个时间所能做的最大报酬
        for(int i=0; i<times.length; i++){
            for(int j=T; j>=times[i]; j--){
                dp[j] = Math.max(dp[j], dp[j-times[i]] + values[i]);
            }
        }
        return dp[T];
    }
//    20 20 20
    //10 20 5
    public static int getMaxIncome(int[] times, int[] values,int i,int T,int value){

        for (int j = 0; j < times.length; j++) {
            if (i == j){
                continue;
            }
            if (times[j] + times[i] <T){
                getMaxIncome(times,values,j,T,value+values[i]);
            }else {
                return value +values[j];
            }
        }
        return value;
    }

/**
40 3
20 10
20 20
20 5
 *
 */








}
