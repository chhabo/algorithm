package advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class _08_基站维修工程师 {

    public static int min = Integer.MAX_VALUE;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        sc.nextLine();
        int[][] distance = new int[n][n]; //路程转化为二维数组
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                distance[i][j] = sc.nextInt();
            }
        }
        for(int i=1; i<distance.length; i++){
            List<Integer> stepList = new ArrayList<>();
            stepList.add(i); //从基站 2 开始走
            handle( distance, i, stepList, distance[0][i]);
        }
        System.out.println(min);
    }
    /**

     3
     0 2 1
     1 0 2
     2 1 0

     * @param ints 各基站路程的二维数组
     * @param index 达到的基站
     * @param step 路过的基站
     * @param sum 走过的路程
     */
    public static void handle(int[][] ints, int index, List<Integer> step, int sum){
        if(step.size() + 1 == ints.length){ //走完所有的基站，准备返回基站 1
            min = Math.min( min, sum + ints[index][0]); //记得加上基站 1 的路程
        } else {
            for(int i=1; i<ints.length; i++){
                if(step.contains(i)){ //已经走过的基站不需要再走
                    continue;
                }
                step.add(i); //走过的基站

                handle(ints, i, step, sum + ints[index][i]); //index 到达的基站，i 下次去的基站
                System.out.println("-------------------------");
                System.out.println(step);
                step.remove(step.size()-1); //刚走过的基站需要剔除以便进行下一个循环
                System.out.println(step);
            }
        }
    }
}
