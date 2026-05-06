package basic;

import java.util.Scanner;

public class _33_相同数字的积木游戏 {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);


        final int nums = sc.nextInt();
        int[] numInts = new int[nums];
        for (int i = 0; i < nums; i++) {
            numInts[i] = sc.nextInt();
        }

        // 1 2 3 4 3 6 7

        int max = -1;
        for (int i = 0; i < numInts.length; i++) {
            for (int j = numInts.length -1; j > i; j--) {
                if (numInts[i]== numInts[j]){
                    max = Math.max(max,j-i);
                    break;
                }
            }
        }
        System.out.println(max);


    }
}
