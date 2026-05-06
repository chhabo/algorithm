package basic;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;

public class _02_木板 {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String[] split = sc.nextLine().split(",");
        int n = Integer.parseInt(split[0]);
        int m = Integer.parseInt(split[1]);

        int[] nums = new int[n];
        String[] split2 = sc.nextLine().split(",");
        for (int i = 0; i < split2.length; i++) {
            nums[i] = Integer.parseInt(split2[i]);
        }
        Arrays.sort(nums);

        while (m>0){
            m--; //木材一段一段的截
            for(int i=1; i<n; i++){
                if(nums[i] > nums[i-1]){
                    nums[i-1] ++; //碰到比后面的短的就加一截
                    break;
                }
                if(i == n-1){
                    nums[i] ++; //所有木材一样长则在最后一根加一截
                }
            }
        }
        System.out.println(nums[0]);

    }

}
