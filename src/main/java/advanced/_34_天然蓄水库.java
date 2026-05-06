package advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class _34_天然蓄水库 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strs = sc.nextLine().split(" ");
        int res = 0;
        int indexLeft = 0; //最大蓄水池左边界
        int indexRight = 0; //最大蓄水池右边界
        for(int i=indexRight; i<strs.length-2; i++){
            int left = Integer.valueOf(strs[i]); //左侧边界
            int right = Integer.valueOf(strs[i+1]); //右侧边界
            if(left <= right){ //左边界小于等于右边界，不能形成蓄水池
                continue;
            }
            List<Integer> list = new ArrayList<>(); //山脉集合
            list.add(left);
            list.add(right);
            boolean isSuccess = false; //是否形成蓄水池
            int index = 0; //蓄水池右边界索引
            for(int j = i+2; j<strs.length; j++){
                int height = Integer.valueOf(strs[j]); //此时山脉高度
                list.add(height);
                if(height > right){ //大于右边界时，形成新的右边界
                    right = height;
                    index = list.size() - 1;
                    isSuccess = true;
                }
                if(height >= left){ //右边界大于左边界直接跳出
                    break;
                }
            }
            if(isSuccess){
                int height = Math.min( list.get(0), list.get(index)); //求出蓄水池边界
                int count = 0; //蓄水池面积
                for(int k=1; k<index; k++){
                    count += (height - list.get(k));
                }
                res = Math.max( res, count); //求出蓄水池面积最大值
                if(res == count){
                    indexLeft = i; //左边界索引
                    indexRight = index + i; //右边界索引
                }
            }
        }
        if(res == 0){
            System.out.println(0);
        }else {
            System.out.println(indexLeft + " " + indexRight + ":" + res);
        }
    }
}
