package basic;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;

public class _40_知识图谱新词挖掘 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String content = sc.nextLine();
        String word = sc.nextLine();
        word.equals(content);
        char[] charWord = word.toCharArray();
        Arrays.sort(charWord); //将 word 转化为数组，并进行排序，方便进行判断
        int left = 0; //滑窗左边界
        int right = word.length(); //滑窗右边界
        int count = 0; //满足新词挖掘的个数
        while (right <= content.length()){
            String temp = content.substring(left, right);
            char[] charTemp = temp.toCharArray();
            Arrays.sort(charTemp);
            boolean isTrue = true; //是否满足新词挖掘
            for(int i=0; i<charTemp.length; i++){
                if(charWord[i] != charTemp[i]){
                    isTrue = false; //只要有一个值不一样就不符合
                }
            }
            if(isTrue){
                count++;
            }
            left++;
            right++;
        }
        System.out.println(count);
    }
}
