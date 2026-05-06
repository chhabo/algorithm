package basic;

import com.sun.media.sound.SoftTuning;
import jdk.nashorn.internal.runtime.linker.LinkerCallSite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class _35_预定酒店 {


    public static void main(String[] args) {
        /**
         10 5 6
         1 2 3 4 5 6 7 8 9 10
         5 4 3 2 1 0 1 2 3 4

         10 5 6
         1 2 3 4 5 6 7 8 9 10


         */

        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int k = sc.nextInt();
        int x = sc.nextInt();

        List<int[]> list = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            int[] ints =new int[2];
            ints[0] = sc.nextInt(); //价格
            ints[1] = Math.abs(x-ints[0]) ;// 价格差
            list.add(ints);
        }
        list.sort((a,b)->{
            if (a[1]<b[1]){
                return -1;
            }else if (a[1] ==b[1]){
                return a[0]-b[0];
            }
            return 0;
        });


        int[] resInt = new int[k];
        for (int i = 0; i < k; i++) {
            resInt[i] = list.get(i)[0];
        }
        Arrays.sort(resInt);

        for (int i = 0; i < resInt.length; i++) {
            System.out.print(resInt[i]+ " ");

        }
        System.out.println();





    }
}
