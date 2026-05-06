package basic;

import java.util.Scanner;

public class _07_对称字符串 {

//    public static void main(String[] args) {
//        Scanner sc = new Scanner(System.in);
//        int T = sc.nextInt();
//        for(int i=0; i<T; i++){
//            int n = sc.nextInt();
//            int k = sc.nextInt();
//            long start = System.currentTimeMillis();
//            StringBuffer string = doProcess(n);
//            String res = string.charAt(k) == 'R' ? "red" : "blue";
//            System.out.println(res);
//            long end = System.currentTimeMillis();
//            System.out.println("cost time : "+(end-start)/1000 + " s");
//        }
//    }
//    /**
//     * 第 n 行字符串
//     * @param n 字符串行数
//     * @return
//     */
//    public static StringBuffer doProcess(int n){
//        if(n == 1){ //当 n 为 1 时输出 R
//            return new StringBuffer("R");
//        }
//        StringBuffer str = doProcess(n-1); //当 n 大于等于 2 时需要对字符进行反转处理
//        StringBuffer reverseStr = new StringBuffer();
//        for(int i=0; i<str.length(); i++){
//            char c = str.charAt(i);
//            if(c == 'R'){
//                reverseStr.append('B');
//            }else {
//                reverseStr.append('R');
//            }
//        }
//        return reverseStr.append(str);
//    }


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int T = sc.nextInt();
        for(int i=0; i<T; i++){
            int n = sc.nextInt();
            long k = sc.nextLong();
            long charNum = (long) Math.pow(2, n - 1); //第 n 行的总字符个数
            long start = System.currentTimeMillis();

            int re = doProcess(charNum, k, 0);
            if (re % 2 == 0) {
                System.out.println("red"); //翻转次数为 2 的倍数
            } else {
                System.out.println("blue");
            }
            long end = System.currentTimeMillis();
            System.out.println("cost time : "+(end-start)/1000 + " s");

        }
    }
    /**
     * 翻转次数
     * @param count 总字符的个数
     * @param cur 字符的索引
     * @param reverse 翻转次数
     * @return
     */
    public static int doProcess(long count, long cur, int reverse){
        if (count == 1) {
            return reverse;
        }
        long half = count / 2;
        if (cur < half) { //小于半数说明是经过翻转的部分
            reverse ++;
            return doProcess(half, cur, reverse);
        } else {
            return doProcess(half, cur - half, reverse);
        }
    }

}
