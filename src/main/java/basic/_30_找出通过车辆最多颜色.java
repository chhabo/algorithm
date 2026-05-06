package basic;

import java.util.Arrays;
import java.util.Scanner;

public class _30_找出通过车辆最多颜色 {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strs = sc.nextLine().split(" ");
        int n = sc.nextInt();

        int max = 0;
        for (int i = 0; i < strs.length -n +1; i++) {
            int[] ints = new int[3];
            for (int j = i; j < i+n; j++) {
                int index = Integer.valueOf(strs[j]);
                ints[index] ++;
            }
            int maxCount = Arrays.stream(ints).max().getAsInt();
            max = Math.max(maxCount,max);

        }
        System.out.println(max);
    }

}
