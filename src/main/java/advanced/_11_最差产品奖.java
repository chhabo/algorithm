package advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class _11_最差产品奖 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int M = sc.nextInt();
        sc.nextLine();
        String[] strs = sc.nextLine().split(",");
        List<Integer> list = new ArrayList<>();
        for(int i=0; i<strs.length - M + 1; i++){
            int min = Integer.MAX_VALUE;
            for(int j=i; j<i + M; j++){
                min = Math.min(min, Integer.valueOf(strs[j]));
            }
            list.add(min);
        }
        System.out.println(list);
    }
}
