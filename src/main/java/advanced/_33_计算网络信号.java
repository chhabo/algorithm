package advanced;

import java.util.Scanner;

public class _33_计算网络信号 {


    public static int i;
    public static int j;
    public static int[][] signals; //网格地图二维数组
    public static int signalX; //信号源横坐标
    public static int signalY; //信号源纵坐标
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int m = sc.nextInt();
        int n = sc.nextInt();
        signals = new int[m][n];
        for( int i=0; i<m; i++){
            for( int j=0; j<n; j++){
                signals[i][j] = sc.nextInt();
                if(signals[i][j] != 0 && signals[i][j] != -1){ //信号
                    signalX = i;
                    signalY = j;
                }
            }
        }
        i = sc.nextInt();
        j = sc.nextInt();
        handle(signalX, signalY, signals[signalX][signalY]);
        System.out.println(signals[i][j]);
    }
    /**
     *
     * @param x 网格地图横坐标
     * @param y 网格地图纵坐标
     * @param signal 信号值
     */
    public static void handle(int x, int y, int signal){
        if(x == i && y == j){ //达到所求位置
            return;
        }
        if(signal == 0 || signal == -1){ //无信号进行传播
            return;
        }
        if(x < i){
            if(signals[x+1][y] != -1){ //下一个位置如果是阻隔则无需进行操作
                signals[x+1][y] = Math.max(signals[x+1][y], signal - 1);
            }
            handle( x+1, y, signals[x+1][y]);
        }
        if(y < j){
            if(signals[x][y+1] != -1){ //下一个位置如果是阻隔则无需进行操作
                signals[x][y+1] = Math.max(signals[x][y+1], signal - 1);
            }
            handle( x, y+1, signals[x][y+1]);
        }
        if(x > i){
            if(signals[x-1][y] != -1){ //下一个位置如果是阻隔则无需进行操作
                signals[x-1][y] = Math.max(signals[x-1][y], signal - 1);
            }
            handle( x-1, y, signals[x-1][y]);
        }
        if(y > j){
            if(signals[x][y-1] != -1){ //下一个位置如果是阻隔则无需进行操作
                signals[x][y-1] = Math.max(signals[x][y-1], signal - 1);
            }
            handle( x, y-1, signals[x][y-1]);
        }
    }
}
