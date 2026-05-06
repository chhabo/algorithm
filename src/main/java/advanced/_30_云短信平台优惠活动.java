package advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class _30_云短信平台优惠活动 {

    public static int max = 0;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int M = sc.nextInt();
        sc.nextLine();
        String[] P = sc.nextLine().split(" ");
        combine(P, M, new ArrayList<>(), 0);
        System.out.println(max);
    }
    /**
     * M 中取 N 个数的算法
     * @param strings 出售价表
     * @param n 剩余的预算
     * @param list 充值获得的短信条数的集合
     * @param index 出售价表的索引
     */
    public static void combine(String[] strings, int n, List<Integer> list, int index){
        if(n == 0){ //当预算等于 0，退出循环
            int count = 0;
            for(int i=0; i<list.size(); i++){
                count += list.get(i);
            }
            max = Math.max(max, count);
        }else {
            for(int i=index; i<strings.length; i++){
                int x = Integer.valueOf(strings[i]);
                list.add(x);
                combine(strings,n - (i + 1), list, i+1);
                list.remove(list.size()-1);
            }
        }
    }
}
