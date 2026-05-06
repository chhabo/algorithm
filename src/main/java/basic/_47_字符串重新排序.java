package basic;

import java.util.*;

public class _47_字符串重新排序 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strings = sc.nextLine().split(" ");
        Map<String,Integer> map = new HashMap<>();
        for( int i=0; i<strings.length; i++){
            char[] chars = strings[i].toCharArray();
            Arrays.sort(chars); //对单子中的字母进行字典排序
            String temp = String.valueOf(chars);
            map.put( temp, map.getOrDefault( temp,0) + 1); //统计每个单词出现的次数
        }
        List<Map.Entry<String,Integer>> list = new ArrayList<>(map.entrySet());
        list.sort(( a, b)->{
            if(b.getValue() < a.getValue()){
                return -1; //按次数降序
            }else if(b.getValue() == a.getValue()){ //次数相同
                if(b.getKey().length() > a.getKey().length()){
                    return -1; //按字母长度升序
                }else if(b.getKey().length() == a.getKey().length()){ //字母长度相同
                    return a.getKey().compareTo(b.getKey()); //按字典序升序
                }
            }
            return 1;
        });
        String res = "";
        for(int i=0; i<list.size(); i++){
            for(int j=0;j<list.get(i).getValue();j++){
                res += list.get(i).getKey() + " ";
            }
        }
        System.out.println(res.substring(0, res.length()-1));
    }
}
