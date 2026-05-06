package basic;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class _26_箱子之形摆放 {



    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strings = sc.nextLine().split(" ");
        String box = strings[0];
        int n = Integer.valueOf(strings[1]);
        Map<Integer, StringBuffer> map = new HashMap<>();

        for(int i=0; i<box.length(); i++){
            int index ;

            if (i/n%2 == 0){
                index = i%n; //用索引取余来确定箱子的位置c
            } else {
                index = n-1-i%n;
            }
            char c = box.charAt(i);
            if(map.containsKey(index)){
                map.get(index).append(c);
            }else {
                map.put(index, new StringBuffer().append(c));
            }
        //map.put(index, map.getOrDefault(index, new StringBuffer()).append(c));
        }
        for(StringBuffer sb : map.values()){
            System.out.println(sb);
        }




    }

















}
