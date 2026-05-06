package basic;

import java.util.Arrays;
import java.util.Scanner;

public class _32_租车骑绿道 {


    /**
     * 3 4
     * 3 2 2 1
     *
     * 3
     *
     *
     *
     *
     * @param args
     */
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        final String[] split =sc.nextLine().split(" ");
        int cycleWeight = Integer.valueOf(split[0]);
        int peopleNum = Integer.valueOf(split[1]);

        String[] weigths = sc.nextLine().split(" ");

        int[] weigthsInts = new int[peopleNum];
        for (int i = 0; i < weigths.length; i++) {
            weigthsInts[i] = Integer.parseInt(weigths[i]);
        }

        int res = 0;
        Arrays.sort(weigthsInts);
//                * 3 4
//                * 3 2 2 1
//                *
        for (int i = weigthsInts.length-1; i > 0; i--) {
            if (weigthsInts[i] == 0) continue;

            if (weigthsInts[i] == cycleWeight){
                res++;
                continue;
            }
            for (int j = i-1; j >=0 ; j--) {
                if (weigthsInts[j]!=0 && weigthsInts[i] + weigthsInts[j] <=cycleWeight){
                    weigthsInts[j] =0;
                    break;
                }
            }
            res++;

        }


        System.out.println(res);


    }


}
