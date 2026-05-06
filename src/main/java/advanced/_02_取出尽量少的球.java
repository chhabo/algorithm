package advanced;

import java.util.Arrays;
import java.util.Scanner;

public class _02_取出尽量少的球 {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int SUM = sc.nextInt();
        int ballNumsLen = sc.nextInt();
        int[] ballNums = new int[ballNumsLen];
        int ballCount = 0; //球的总数
        for( int i=0; i<ballNumsLen; i++){
            ballNums[i] = sc.nextInt(); //各个管子中的球的个数
            ballCount += ballNums[i];
        }
        if(SUM < ballCount){ //球的总数大于 SUM 时需要处理
            int min = SUM/ballNumsLen; //maxCapacity 的最小值
            int max = Arrays.stream(ballNums).max().getAsInt(); //maxCapacity 的最大值
            int[] tempOut = new int[ballNumsLen]; //各个管子移除的球的个数数组（暂时存放）
            int[] ballOut = {}; //各个管子移除的球的个数数组
            for(int i=min; i<=max; i++){
                for(int j=0; j<ballNumsLen; j++){ //每个管子都需要移除 i 个球
                    tempOut[j] = ballNums[j] - i > 0 ? ballNums[j] - i : 0;
                }
                int outCount = Arrays.stream(tempOut).sum(); //取出的球的总数
                int remainCount = ballCount - outCount; //剩余的球的总数
                if(remainCount > SUM){ //剩下的球大于 SUM，则输出
                    break;
                }else {
                    ballOut = Arrays.copyOf( tempOut, tempOut.length);
                }
            }
            System.out.println(ballOut.length == 0 ? Arrays.toString(tempOut) :
                    Arrays.toString(ballOut));
        }else {
            System.out.println("[]");
        }
    }

}
