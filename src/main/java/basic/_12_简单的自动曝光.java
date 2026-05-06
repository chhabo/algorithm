package basic;

import java.util.Scanner;

public class _12_简单的自动曝光 {

    public static void main(String[] args) {
        myImpl();
//        Scanner sc = new Scanner(System.in);
//        String[] strs = sc.nextLine().split(" ");
//        int len = strs.length;
//        double count = 0; //输入值总和
//        for (int i = 0; i < len; i++) {
//            int num = Integer.valueOf(strs[i]);
//            count += num;
//        }
//        double averages = count / len - 128; //平均差
//        int res = (int) Math.round(averages); //对平均差进行四舍五入
//        int lessThenZeroNum = 0; //newImg 小于 0 的个数
//        int lessThenZero = 0; //newImg 小于 0 的与 0 的差值的和
//        for (int i = 0; i < len; i++) {
//            int num = Integer.valueOf(strs[i]);
//            if (num - res < 0) { //考虑到 newImg 的值小于 0
//                lessThenZeroNum++;
//                lessThenZero += num - res;
//            }
//        }
//        res -= lessThenZero / (len - lessThenZeroNum);
//        System.out.println(0 - res); //取反
    }


    public static void myImpl() {
        Scanner sc = new Scanner(System.in);
        String[] imgs = sc.nextLine().split(" ");

        int begin = -128;
        int end = 128;

        int[] newImgs = new int[imgs.length];
        long sum = 0;
        for (int i = 0; i < imgs.length; i++) {
            int in = Integer.parseInt(imgs[i]);
            in = in < 0 ? 0 : in > 255 ? 255 : in;
            newImgs[i] = in;
            sum += in;
        }

        int res = 0;
        double minAvg = Integer.MAX_VALUE;
        while (end >= begin){
            double avg = (double)(begin * imgs.length + sum) / 4 - 128;
            avg = Math.abs(avg);
            if (minAvg - avg>0) {
                minAvg = avg;
                System.out.println(minAvg);
                res = begin;
            }
            begin++;

        }

        System.out.println(res);

    }


}
