package advanced;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class _28_硬件产品销售方案 {

    public static int[] price; //物品价格数组
    public static int amount; //预算
    public static List<List<Integer>> resList = new ArrayList<>();
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        amount = sc.nextInt();
        sc.nextLine();
        String[] strings = sc.nextLine().replace("[","")
                .replace("]","")
                .split(",");
        price = new int[strings.length];
        for(int i=0; i<strings.length; i++){
            price[i] = Integer.valueOf(strings[i].trim());
        }
        dfs(0, 0, new ArrayList<>());
        System.out.println(resList);
    }
    /**
     * 通过递归求出所有的购买情况
     * @param index 物品价格索引
     * @param count 购买物品总价格
     * @param list 购买物品集合
     */
    public static void handle(int index, int count, List<Integer> list){
        if(amount <= count){ //物品总价格大于等于预算总退出
            List<Integer> tempList = new ArrayList<>(); //需要使用 tempList，否则会影响入参中的 list
            tempList.addAll(list);
            if(amount == count){ //只统计等于预算的情况
                resList.add(tempList);
            }
        }else {
            for(int i=index; i<price.length; i++){
                list.add(price[i]);
                System.out.println("index : "+index+" count : "+count+"  list : "+list);
                handle( i, count + price[i], list);
                list.remove(list.size()-1);
                System.out.println("--------------------------------------------------------");
            }
        }
    }


    public static void dfs(int index, int count, List<Integer> list){
        if (amount<= count){
            if (amount == count){
                List<Integer> temp = new ArrayList<>();
                temp.addAll(list);
                resList.add(temp);
            }

            return;
        }


        for (int i = index; i < price.length ; i++) {
            list.add(price[i] );
            dfs(i,count+price[i],list);
            list.remove(list.size()-1);
        }


    }



}
