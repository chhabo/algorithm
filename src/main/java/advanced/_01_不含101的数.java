package advanced;

import java.util.Scanner;

public class _01_不含101的数 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int l = sc.nextInt();
        int r = sc.nextInt();
        int res = r - l + 1;
        for(int i=l; i<=r; i++){
            String binary = Integer.toBinaryString(i);
            if(binary.contains("101")){
                res --;
            }
        }
        System.out.println(res);
    }
}
