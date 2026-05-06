package advanced;

import java.util.Scanner;

public class _06_分奖金 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        sc.nextLine();
        String[] strings = sc.nextLine().split(" ");
        int[] ints = new int[n];
        for(int i=0; i<n; i++){
            ints[i] = Integer.valueOf(strings[i]);
        }
        for(int i=0; i<n; i++){
            for(int j=i+1; j<n; j++){
                if(ints[i] < ints[j]){ //碰到比自己大数字才进行处理
                    ints[i] = (ints[j] - ints[i]) * (i+1);
                    break;
                }
            }
        }
        String res = "";
        for(int i : ints){
            res += i + " ";
        }
        System.out.println(res.substring(0, res.length()-1));
    }

}
