package advanced;

import java.util.Scanner;

public class _14_最优资源分配 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int M = sc.nextInt();
        int N = sc.nextInt();
        sc.nextLine();
        String string = sc.nextLine();
        int[] chip = new int[N]; //板卡上的芯片
        int conf; //配置所占容量
        for(int i=0; i<string.length(); i++){
            char c = string.charAt(i);
            if(c == 'A'){
                conf = 1;
            }else if(c == 'B'){
                conf = 2;
            }else {
                conf = 8;
            }
            for(int j=0; j<N; j++){
                int used = chip[j]; //芯片占用的情况
                if(M - used >= conf){ //芯片所剩容量大于等于配置所占容量
                    chip[j] += conf;
                    break;
                }
            }
        }
        for(int i=0; i<N; i++){
            StringBuffer sb = new StringBuffer();
            int used = chip[i]; //芯片占用的情况
            for(int j=0; j<M; j++){
                if(j<used){ //芯片占用的地方为 1，未占用为 0
                    sb.append("1");
                }else {
                    sb.append("0");
                }
            }
            System.out.println(sb);
        }
    }
}
