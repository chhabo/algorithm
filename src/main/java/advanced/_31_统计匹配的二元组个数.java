package advanced;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class _31_统计匹配的二元组个数 {
    public static void main(String[] args) {
        myImpl();
//        Scanner sc = new Scanner(System.in);
//        int M = sc.nextInt();
//        int N = sc.nextInt();
//        sc.nextLine();
//        String[] A = sc.nextLine().split(" ");
//        String[] B = sc.nextLine().split(" ");
//        Map<String, Integer> map = new HashMap<>();
//        for(int i=0; i<M; i++){
//            String a = A[i];
//            map.put( a, map.getOrDefault( a, 0) + 1);
//        }
//        int res = 0;
//        for(int i=0; i<N; i++){
//            String b = B[i];
//            if(map.containsKey(b)){
//                res += map.get(b);
//            }
//        }
//        System.out.println(res);
    }


    public static void myImpl (){
        Scanner sc = new Scanner(System.in);

        int M = sc.nextInt();
        int N = sc.nextInt();
        sc.nextLine();
        String[] A = sc.nextLine().split(" ");
        String[] B = sc.nextLine().split(" ");

        int count = 0;
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B.length; j++) {
                if(A[i].equals(B[j]) ){
                    count ++;
                }
            }
        }
        System.out.println(count);

    }

}
