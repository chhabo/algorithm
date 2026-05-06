package basic;

import java.util.Scanner;

public class _10_货币单位换算 {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int N = sc.nextInt();
        int res = 0;
        sc.nextLine();
        for(int i=0;i<N;i++){
            res += getRes(sc.nextLine());
        }
        System.out.println(res);
    }
    public static double getRes(String str){
        String temp = "";
        double count = 0;
        for(int i=0; i<str.length(); i++){
            char c = str.charAt(i);
            if(Character.isDigit(c)){
                temp += c;
            }else {
                if(temp == ""){ //temp 为空，说明已经计算过了，直接跳过。用来避免四位及以上字符的简写货币（cents）
                    continue;
                }
                count += huansuan( temp, c);
                i += 2; //因为货币的简写至少为 3 位，所以可以跳两位，缩减时间
                temp = "";
            }
        }
        return count;
    }
    public static double huansuan(String numStr, char str){
        double count = 0;
        int num = Integer.valueOf(numStr);
        if(str == 'C'){
            count = num * 100;
        }else if(str == 'J'){
            count = (double) num * 10000 / 1825;
        }else if(str == 'H'){
            count = (double) num * 10000 / 123;
        }else if(str == 'E'){
            count = (double) num * 10000 / 14;
        }else if(str == 'G'){
            count = (double) num * 10000 / 12;
        }else if(str == 'f'){
            count = num * 1;
        }else if(str == 's'){
            count = (double) num * 100 / 1825;
        }else if(str == 'c'){
            count = (double) num * 100 / 123;
        }else if(str == 'e'){
            count = (double) num * 100 / 14;
        }else if(str == 'p'){
            count = (double) num * 100 / 12;
        }
        return count;
    }


}
