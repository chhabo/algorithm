package basic;

import java.util.Scanner;

public class _45_光伏场地建设 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int row = sc.nextInt(); //长
        int col = sc.nextInt(); //宽
        int width = sc.nextInt(); //电站边长
        int pow = sc.nextInt(); //最低要求的发电量
        int[][] region = new int[row][col]; //区域
        for(int i=0; i<row; i++){
            for(int j=0; j<col; j++){
                region[i][j] = sc.nextInt();
            }
        }
        // 2 5 2 6
        // 1 3 4 5 8
        // 2 3 6 7 1
        // 2 3 4 5 6
        int res = 0;
        for(int i=0; i <= row - width; i++){
            for(int j=0; j <= col - width; j++){
                int count = 0; //电站区域的发电量
                for(int l=i; l < i + width; l++){
                    for(int m=j; m < j + width; m++){
                        count += region[l][m];
                    }
                }
                if(count >= pow){ //发电量大于等于最低要求的发电量则满足
                    res ++;
                }
            }
        }
        System.out.println(res);
    }

}
