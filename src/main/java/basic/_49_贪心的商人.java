package basic;

import java.util.Scanner;

public class _49_贪心的商人 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int number = sc.nextInt();
        int days = sc.nextInt();
        int[] item = new int[number];
        for(int i=0; i<number; i++){
            item[i] = sc.nextInt();
        }
        int[][] item_price = new int[number][days];
        for(int i=0; i<number; i++){
            for(int j=0; j<days; j++){
                item_price[i][j] = sc.nextInt();
            }
        }

        /**
          3
          3
          4 5 6
          1 2 3
          4 3 2
          1 5 3
         */
        int res = 0;
        for(int i=0; i<number; i++){
            for(int j=0; j<days-1; j++){
                int purchase = item_price[i][j]; //进价
                int selling = item_price[i][j+1]; //卖价
                if(selling > purchase){ //当卖价大于进价才有利润
                    res += (selling - purchase) * item[i]; //还需要乘以其数量
                }
            }
        }
        System.out.println(res);
    }
}
