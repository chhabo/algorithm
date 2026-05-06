package basic;

import java.util.*;

public class _05_单词倒叙 {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String str = sc.nextLine();

        String[] split = str.split(" ");

        String resStr = "";

        for (int i = 0; i < split.length; i++) {
            resStr += resver(split[i])+" ";

        }
        System.out.println(resStr);

    }

    private static String resver(String word) {
        StringBuffer res = new StringBuffer();
        Map<Integer,String>  map = new HashMap<>();
        for (int i = word.length()-1; i >= 0; i--) {

            char c = word.charAt(i);
            String strC = String.valueOf(c);

            if (isSpecialChar(strC)){
                map.put(i,strC);
            }else {
                res.append(c);
            }

        }
        if (map.size() != 0){
            for (Integer integer : map.keySet()) {
                res.insert(integer,map.get(integer));
            }
        }

        return res.toString();
    }

    private static boolean isSpecialChar(String strC) {

        return strC.equals(",") || strC.equals(".") || strC.equals("?");

    }


}
