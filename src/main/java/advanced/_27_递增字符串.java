package advanced;

import java.util.Scanner;

public class _27_递增字符串 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine();
        int BinA = 0;
        int lastAIndex = str.lastIndexOf("A");
        for(int i=0; i<lastAIndex; i++){
            if(str.charAt(i) == 'B'){
                BinA ++;
            }
        }
        int AinB = 0;
        int firstBIndex = str.indexOf("B");
        for(int i=firstBIndex+1; i<str.length(); i++){
            if(str.charAt(i) == 'A'){
                AinB ++;
            }
        }
        System.out.println(Math.min( BinA, AinB));
    }

}
