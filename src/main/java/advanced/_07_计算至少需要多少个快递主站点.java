package advanced;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class _07_计算至少需要多少个快递主站点 {


//    [0,1]
//
//    0 1 2 3
//

    public static int[][] s;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int N = sc.nextInt();
        s = new int[N][N];
        for(int i=0; i<N; i++){
            for(int j=0; j<N; j++){
                s[i][j] = sc.nextInt();
            }
        }
        /**
         4
         1 1 1 1
         1 1 1 0
         1 1 1 0
         1 0 0 1
         4
         1 1 0 0
         1 1 0 0
         0 0 1 0
         0 0 0 1
         */

        Set<Integer> set = new HashSet<>(); //已经有连通的站点
        int res = 0; //主站点数
        for(int i=0; i<N; i++){
            if(set.contains(i)){ //站点已经有连通，无需再处理
                continue;
            }
            Set<Integer> temp = new HashSet<>(); //相互连通的站点集合
            temp.add(i);
            handle( temp, i);
            set.addAll(temp); //已经连通的站点放在 set 里面用来判断后续是否需要处理
            res ++;
        }
        System.out.println(res);
    }
    /**
     * 寻找跟 n 站点相连通的站点
     * @param set 相互连通的站点集合
     * @param n 站点编号
     */
    public static void handle(Set<Integer> set, int n) {
        for (int i = 0; i < s.length; i++) {
            if (set.contains(i)) { //已经连通的站点无需再判断
                continue;
            }
            if (n != i && s[n][i] == 1) { //值等于 1 说明两站点连通（n==i 就是本站点）
                set.add(i);
                handle(set, i);
            }
        }
    }

    }
