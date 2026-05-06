package basic;

import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class _23_任务总执行时长 {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strings = sc.nextLine().split(",");
        int aTime = Integer.valueOf(strings[0]);
        int bTime = Integer.valueOf(strings[1]);
        int num = Integer.valueOf(strings[2]);
        Set<Integer> total = new TreeSet<>();
        int res;
        for (int i = 0; i <= num; i++) {
            res = aTime * (num - i) + i * bTime;
            total.add(res);
        }
        System.out.println(total);
    }


    static int num;
    static Set<Integer> resList = new TreeSet<>();
    public static void main1(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strings = sc.nextLine().split(",");
        int[] tasks = new int[2];
        tasks[0] = Integer.valueOf(strings[0]); //taskA
        tasks[1] = Integer.valueOf(strings[1]); //taskB
        num = Integer.valueOf(strings[2]);
        handle( tasks,0,0,0);
        System.out.println(resList);
    }
    /**
     *
     * @param tasks taskA 和 taskB 的数组
     * @param count 已执行任务的个数
     * @param times 执行任务的时长
     * @param index 执行任务的索引（0：taskA，1：taskB）
     */
    public static void handle(int[] tasks, int count, int times, int index){
        if(count == num){
            resList.add(times);
        }else {
            for(int i = index; i<2; i++){
                times += tasks[i];
                index = resList.size() == 0 ? 0 : 1;
                handle( tasks, count + 1, times, index);
                times -= tasks[0];
            }
        }
    }


}
