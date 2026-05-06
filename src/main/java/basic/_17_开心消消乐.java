package basic;

import java.util.Scanner;

public class _17_开心消消乐 {


    public static int[][] juzhen;
    public static int N;
    public static int M;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        N = sc.nextInt();
        M = sc.nextInt();
        juzhen = new int[N][M];
        for(int i=0; i<N; i++){
            for(int j=0; j<M; j++){
                juzhen[i][j] = sc.nextInt();
            }
        }
        int res = 0;
        for(int i=0; i<N; i++){
            for(int j=0; j<M; j++){
                if(juzhen[i][j] == 1){
                    clearHappy( i, j);
                    res ++;
                }
            }
        }
        System.out.println(res);
    }
/**
 3 3
 1 0 1
 0 1 0
 1 0 1
 *
 */
    public static void clearHappy( int x, int y){
        //正上
        if (checkExists(x-1,y) && juzhen[x-1][y]==1){
            juzhen[x-1][y] = 0;
            clearHappy(x-1,y);
        }
        //正下
        if (checkExists(x+1,y) && juzhen[x+1][y]==1){
            juzhen[x+1][y] = 0;
            clearHappy(x+1,y);
        }
        //正左
        if (checkExists(x,y-1) && juzhen[x][y-1]==1){
            juzhen[x][y-1] = 0;
            clearHappy(x,y-1);
        }
        //正右
        if (checkExists(x,y+1) && juzhen[x][y+1]==1){
            juzhen[x][y+1] = 0;
            clearHappy(x,y+1);
        }
        //左上
        if (checkExists(x-1,y-1) && juzhen[x-1][y-1]==1){
            juzhen[x-1][y-1] = 0;
            clearHappy(x-1,y-1);
        }
        //左下
        if (checkExists(x+1,y-1) && juzhen[x+1][y-1]==1){
            juzhen[x+1][y-1] = 0;
            clearHappy(x+1,y-1);
        }
        //右上
        if (checkExists(x-1,y+1) && juzhen[x-1][y+1]==1){
            juzhen[x-1][y+1] = 0;
            clearHappy(x-1,y+1);
        }

        //右下
        if (checkExists(x+1,y+1) && juzhen[x+1][y+1]==1){
            juzhen[x+1][y+1] = 0;
            clearHappy(x+1,y+1);
        }



    }

    private static boolean checkExists(int x, int y) {
        boolean b = (0 <= x && x <= N - 1 ) && (0<= y && y <= M - 1);
        return b;
    }




}
