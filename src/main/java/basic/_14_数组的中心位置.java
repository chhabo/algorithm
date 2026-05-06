package basic;

import java.util.Arrays;
import java.util.Scanner;

public class _14_数组的中心位置 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strings = sc.nextLine().split(" ");
        int len = strings.length;
        int[] nums = Arrays.stream(strings).mapToInt(Integer::parseInt).toArray();
        int[] leftProduct = new int[len]; //左侧前缀积
        leftProduct[0] = nums[0];
        for(int i=1; i<len; i++){
            leftProduct[i] = leftProduct[i-1] * nums[i];
        }
        int[] rightProduct = new int[len]; //右侧前缀积
        rightProduct[len-1] = nums[len-1];
        for(int i=len-2; i>=0; i--){
            rightProduct[i] = rightProduct[i+1] * nums[i];
        }
        int res = -1;
        if(rightProduct[1] == 1)
        {
            //根据题意，0 的左侧积为 1,如果右侧积也为 1 则中心位置为 0
            res = 0;
        }
        else
        {
            for(int i=1; i<len; i++){
                if(i == len-1){
                    if(leftProduct[i-2] == 1){
                        res = len - 1; //根据题意，len-1 的右侧积为 1,如果左侧积也为 1则中心位置为 len-1
                    }
                }else if(leftProduct[i-1] == rightProduct[i+1]){ //左侧积等于右侧积
                    res = i;
                    break;
                }
            }
        }
        System.out.println(res);
    }


}
