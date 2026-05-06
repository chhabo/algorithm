package advanced;

import java.util.Scanner;

public class _32_狼羊过河 {

    public static int min = Integer.MAX_VALUE;
    public static int countY; //羊的总数
    public static int countL; //狼的总数
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int m = sc.nextInt();
        int n = sc.nextInt();
        int x = sc.nextInt();
        countY = m;
        countL = n;
        guohe( m, n, x,0);
        if(m + n <= x){ //一趟能运完
            System.out.println(1);
        }else if(min == Integer.MAX_VALUE){
            System.out.println(0);
        }else {
            System.out.println(min);
        }
    }
    /**
     *
     * @param m 岸边的羊的个数
     * @param n 岸边的狼的个数
     * @param x 船的承重
     * @param count 过河的次数
     */
    public static void guohe(int m, int n,int x,int count){
        if(m + n <= x){ //剩下的能一次运完
            min = Math.min( min, count+1);
        }else {
            for(int i=0; i<=m; i++){ //过河的羊的个数
                for(int j=0; j<=n; j++){ //过河的狼的个数
                    if((i+j == 0) || (i+j >x)){ //在船上的狼羊总数需要大于 0 且小于等于 x
                        continue;
                    }
                    if(m-i != 0 && m - i <= n - j){ //剩下的羊在不为 0 的情况下必须大于狼
                        continue;
                    }
                    if(countY-(m-i) != 0 && (countY-(m-i)) <= (countL-(n-j))){ //对岸的羊在不为 0 的情况下必须要大于狼
                        continue;
                    }
                    guohe(m-i,n-j, x,count+1);
                }
            }
        }
    }


}
