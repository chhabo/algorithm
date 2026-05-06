package advanced;

import java.util.Scanner;

public class _22_区块链文件转储系统 {

    public static void main1(String[] args) {
        Scanner sc = new Scanner(System.in);
        int M = sc.nextInt();
        sc.nextLine();
        String[] strings = sc.nextLine().split(" ");
        int left = 0; //滑窗左边界
        int right = 1; //滑窗右边界
        int count = Integer.valueOf(strings[0]); //连续文件大小之和
        int max = 0; //最大连续和
        while (max != M && (right < strings.length || count > max)){ //当 max==M 时直接图退出，当右边界到头且 count 小于 max 时退出
            if(count <= M){
                max = Math.max( max, count);
                if(right < strings.length){ //控制滑窗右边界
                    count += Integer.valueOf(strings[right]);
                    right ++;
                }
            }else {
                count -= Integer.valueOf(strings[left]);
                left ++;
            }
        }
        System.out.println(max);
    }


    public static void main(String[] args){
        int max = 0;
        Scanner sc = new Scanner(System.in);
        int M = sc.nextInt();
        sc.nextLine();
        String[] strings = sc.nextLine().split(" ");
        int[] ints = new int[strings.length];
        for (int i = 0; i < strings.length; i++) {
            ints[i] = Integer.parseInt(strings[i]);
        }

/*
1000
100 300 500 400 400 150 100
1000
100 500 400 150 500 100


*/

        for (int i = 0; i < ints.length; i++) {
            int sum = ints[i];
            if (sum>=M || max == M){
                max = M;
                break;
            }
            for (int j = i+1; j < ints.length; j++) {
                if (sum + ints[j] <=M){
                    sum += ints[j];
                }else {
                    max = Math.max(sum,max);
                }
            }
        }
        System.out.println(max);

    }



}
