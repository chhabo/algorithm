package basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class _46_微服务的集成测试 {

    public static int[][] ints;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        ints = new int[n][n];
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                ints[i][j] = sc.nextInt();
            }
        }
        int k = sc.nextInt();
        System.out.println(handle( k-1)); //因为 k 是行数，在数组中的索引需要-1
    }

    /**
      4
      2 0 0 0
      0 3 0 0
      1 1 4 0
      1 1 1 5
      4
     * @param k
     * @return
     */
    public static Integer handle(int k){
        List<Integer> times = new ArrayList<>(); //所依赖的服务的启动时间集合
        for(int i=0; i<ints.length; i++){
            if(k != i && ints[k][i] == 1){ //需要剔除服务本身
                times.add(handle(i));
            }
        }
        int max = 0; //所依赖的服务的最长启动时间
        for(int time : times){
            max = Math.max( max, time);
        }
        return max + ints[k][k]; //需要加上本身的启动时间
    }
}
