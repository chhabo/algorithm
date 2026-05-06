package basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class _37_寻找密码 {

    // b eredderd bw bww bwwl bwwlm bwwln
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strs = sc.nextLine().split(" ");
        List<String> list = new ArrayList<>();
        for(int i=0; i<strs.length; i++){
            list.add(strs[i]); //将字符数组转化为 list，方便使用 contains 函数
        }
        Collections.sort(list , (a, b) -> b.length() - a.length());

        String res = "";
        List<String> result = new ArrayList<>();
        int resLength = 0;

        for (int i = 0; i < list.size(); i++) {
            boolean iscontanin = true;
            for (int length = list.get(i).length(); length > 0; length--) {
                String subStr = list.get(i).substring(0,length);
                if (!list.contains(subStr)){
                    iscontanin = false;
                    break;
                }

            }

            if (iscontanin && resLength<=list.get(i).length()){
                result.add(list.get(i));
                resLength = list.get(i).length();

            } else if (iscontanin && resLength>list.get(i).length()){
                break;
            }



        }
        if (result.size() != 0){
            result.sort((a,b)-> b.compareTo(a));
            System.out.println(result.get(0));
        }else {
            System.out.println("NULL");
        }


    }
}
