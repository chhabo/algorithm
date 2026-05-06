package basic;

import java.util.*;

public class _15_通信误码 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        /**
         * key：误码
         * value：误码频率
         */
        Map<Integer,Integer> map = new HashMap<>();
        List<Integer> inputList = new ArrayList<>(); //通信误码列表
        int max = 0; //误码次数的最大值
        for(int i=0; i<n; i++) {
            int errorCode = sc.nextInt();
            inputList.add(errorCode);
            map.put( errorCode, map.getOrDefault( errorCode,0) + 1);
            max = Math.max( max, map.get(errorCode));
        }
        List<Integer> maxList = new ArrayList<>(); //频率最高的误码集合
        for(Map.Entry<Integer,Integer> entry : map.entrySet()){
            if(entry.getValue() == max){
                maxList.add(entry.getKey());
            }
        }
        int min = Integer.MAX_VALUE;
        for(int i=0; i<maxList.size(); i++){
            int maxCode = maxList.get(i);
            int start = inputList.indexOf(maxCode); //误码的第一个索引
            int end = inputList.lastIndexOf(maxCode); //误码的最后一个索引
            min = Math.min( min, end - start + 1); //误码区间
        }
        System.out.println(min);
    }

}
