package advanced;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class _05_寻找符合要求的最长子串 {


    public static void main1(String[] args) {
        Scanner sc = new Scanner(System.in);
        char c = sc.nextLine().charAt(0);
        String s = sc.nextLine();
        int left = 0; //子串的起始位置
        int max = 0;
/**
 D
 ABC123

 D
 ABACA123D

 * key：字符
 * value：字符出现的次数
 */
        Map<Character, Integer> map = new HashMap<>();
        for(int i=0; i<s.length(); i++){
            char temp = s.charAt(i);
            if(temp == c){
                map.clear(); //指定字符之前的数据需要清空
                left = i + 1; //指定字符本身不做计数，所以需要+1
                continue;
            }
            map.put( temp, map.getOrDefault(temp, 0) + 1);
            while (map.get(temp) == 3){ //字符出现第二次之前的字符都需要删除
                char rmStr = s.charAt(left);
                left ++;
                map.put( rmStr, map.get(rmStr) - 1); //起始位置后移，字符个数也需要-1
            }
            max = Math.max( max, i - left + 1);
        }
        System.out.println(max);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        char c = sc.nextLine().charAt(0);
        String s  = sc.nextLine();
        char[] chars = s.toCharArray();

/*
D
ABC123

D
ABACA123D
 */
//



        Map<Character,Integer> cacheMap = new HashMap<>();
        int max = 0;
        for (int i = 0; i < chars.length; i++) {
            max = Math.max(getMaxLength(i,chars,c,cacheMap),max);

        }
        System.out.println(max);




    }

    private static int getMaxLength(int begin,char[] chars,char c,Map<Character,Integer> cacheMap) {
        int max =0;

        while (begin < chars.length -1){
            int count = 0;
            for (int i = begin; i < chars.length; i++) {
                if (chars[i] == c){
                    begin = i+1;
                    break;
                }
                int nums = cacheMap.getOrDefault(chars[i], 0) + 1;
                if (nums>=3){
                    begin = i + 1;
                    cacheMap.clear();
                    break;
                }
                cacheMap.put(chars[i],nums);
                count ++;
            }
            begin++;
            max = Math.max(count,max);

        }

        return max;

    }


}
