package advanced;

import java.time.Year;
import java.util.Scanner;

public class _25_机器人活动区域 {

    static int M ;
    static int N ;

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

         M = sc.nextInt();
         N = sc.nextInt();
        int[][] datas = new int[M][N];

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
              datas[i][j] = sc.nextInt();
            }
        }

        int max = 0;
        for (int x = 0; x < M; x++) {
            for (int y = 0; y < N; y++) {
                int[][] record = new int[M][N];
                record[x][y] = 1;
                max = Math.max(max,move(x,y,datas,record));
            }
        }
        System.out.println(max);


    }

    /**
4 4
1 2 5 2
2 4 4 5
3 5 7 1
4 6 2 4
     * @param x
     * @param y
     * @param datas
     * @return
     */
    private static int move(int x, int y, int[][] datas,int[][] record) {

        int count = 1;
        //向右移动
        if(checkIndex(x,y+1) && canMove(x,y,x,y+1,datas) && record[x][y+1] !=1){
//            count++;
            record[x][y+1]= 1;
            count += move(x,y+1,datas,record);
        }
        //向左移动
        if(checkIndex(x,y-1) && canMove(x,y,x,y-1,datas) && record[x][y-1] !=1){

            record[x][y-1]= 1;
            count += move(x,y-1,datas,record);
        }
        //向上移动
        if(checkIndex(x-1,y) && canMove(x,y,x-1,y,datas) && record[x-1][y] !=1){
//            count++;
            record[x-1][y]= 1;
            count += move(x-1,y,datas,record);
        }
        //向下移动
        if(checkIndex(x+1,y) && canMove(x,y,x+1,y,datas) && record[x+1][y] !=1){
//            count++;
            record[x+1][y]= 1;
            count += move(x+1,y,datas,record);
        }

        return count;
    }

    private static boolean canMove(int x, int y, int x1, int y1,int[][] datas) {
        return Math.abs(datas[x][y] - datas[x1][y1]) <=1;
    }

    private static boolean checkIndex(int x, int y) {

        return 0<=x && x<=M -1 && 0<= y && y<= N-1;

    }
}
