package basic;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class _41_静态代码扫描服务 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int M = sc.nextInt();
        sc.nextLine();
        String[] strings = sc.nextLine().split(" ");
        String[] strings1 = sc.nextLine().split(" ");
        Map<Integer,Integer> timesMap = new HashMap<>(); //扫描次数
        Map<Integer,Integer> priceMap = new HashMap<>(); //扫描价格
        for(int i=0; i<strings.length; i++){
            int num = Integer.valueOf(strings[i]);
            int price = Integer.valueOf(strings1[i]);
            timesMap.put( num, timesMap.getOrDefault( num, 0) + 1);
            priceMap.put( num, price);
        }
        int res = 0;
        for(Map.Entry<Integer,Integer> map : timesMap.entrySet()){
            int key = map.getKey(); //扫描文件
            int times = map.getValue(); //扫描次数
            int price = priceMap.get(key); //扫描价格
            int cache = price + M; //使用缓存的总价格
            int noCache = times * price; //不适用缓存的总价格
            if(cache < noCache){ //那个价格便宜使用哪个
                res += cache;
            }else {
                res += noCache;
            }
        }
        System.out.println(res);
    }
}
