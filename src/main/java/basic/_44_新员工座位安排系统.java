package basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class _44_新员工座位安排系统 {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strings = sc.nextLine().split(" ");
        List<Integer> list = new ArrayList<>();
        int count = 0; //记录老员工的数量
        for(int i=0; i<strings.length; i++){
            if(strings[i].equals("1")){
                count ++;
                if(i == strings.length - 1){
                    list.add(count); //添加老员工的数量
                }
            }else if(strings[i].equals("0")){
                if(count != 0){
                    list.add(count); //添加老员工的数量
                    count = 0;
                }
                list.add(0); //空座位还是 0
            }else {
                if(count != 0){
                    list.add(count); //添加老员工的数量
                    count = 0;
                }
                list.add(-1); //2 记做-1
            }
        }

        int max = 0;
        if(list.size() != 1){ //只有一个
            for(int i=0; i<list.size(); i++){
                if(list.get(i) == 0){
                    int nums = 0;
                    if(i == 0){ //空座位在第一个
                        if(list.get(i+1) != -1){ //空座位右侧不是障碍物
                            nums = list.get(i + 1);
                        }
                    }else if(i == list.size() - 1){ //空座位在最后一个
                        if(list.get(i-1) != -1){ //空座位左侧不是障碍物
                            nums = list.get(i - 1);
                        }
                    }else {
                        if(list.get(i-1) != -1){ //空座位左侧不是障碍物
                            nums += list.get(i - 1);
                        }
                        if(list.get(i+1) != -1){ //空座位右侧不是障碍物
                            nums += list.get(i + 1);
                        }
                    }
                    max = Math.max( max, nums);
                }
            }
        }
        System.out.println(max);
    }

}
