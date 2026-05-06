package basic;

import java.util.Arrays;
import java.util.Scanner;

public class _36_学校的位置 {

    public static void main(String[] args) {
        /**
         * 5
         * 0 20 40 10 30
           0 10 20 30 40


         *
         *
         */

        Scanner sc = new Scanner(System.in);
        int num = sc.nextInt();
        int[] localtion = new int[num];

        for (int i = 0; i < num; i++) {
            localtion[i] = sc.nextInt();
        }
        Arrays.sort(localtion);

        int index =0;
        int min = Integer.MAX_VALUE;

        for (int i = 0; i < localtion.length; i++) {
            int sumlength =0;
            for (int j = 0; j < localtion.length; j++) {
                sumlength += Math.abs( localtion[i]-localtion[j]);
            }
            if (min>sumlength){
                min = sumlength;
                index = localtion[i];
            }else if (min == sumlength){
                if (localtion[i]< index){
                    index = localtion[i];
                }
            }

        }
        System.out.println(index);

    }
}
