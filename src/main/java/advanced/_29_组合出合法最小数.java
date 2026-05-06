package advanced;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class _29_组合出合法最小数 {


    public static List<Integer> list = new ArrayList<>(); //非 0 开头的数字
    public static List<Integer> listZero = new ArrayList<>(); //0 开头的数字
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strings = sc.nextLine().split(" ");
//        quanpailie(strings, 0, strings.length);
        for (int i = 0; i < strings.length; i++) {
            dfs(i,new ArrayList<String>(),strings);
        }
        if(list.size() == 0){ //说明都是以 0 开头的
            Collections.sort(listZero);
            System.out.println(listZero.get(0));
        }else {
            Collections.sort(list);
            System.out.println(list.get(0));
        }
    }
    public static void swap(String[] strings,int a,int b){
        String temp = strings[a];
        strings[a] = strings[b];
        strings[b] = temp;
    }
    /**
     * 经典全排列回溯 （不理解的可以强行背诵，用的地方很多）
     * @param strings 需要排列的数组
     * @param index 处理的索引
     * @param end 数组长度
     */
    public static void quanpailie(String[] strings, int index, int end) {
        if (index == end) { //所有的数据都遍历完成
            String res = "";
            for (int i = 0; i < strings.length; i++) {
                res += strings[i];
            }
            if (res.startsWith("0")) { //以 0 开头的需要分开放
                listZero.add(Integer.valueOf(res));
            } else {
                list.add(Integer.valueOf(res));
            }
        } else {
            for (int i = index; i < strings.length; i++) {
                swap(strings, i, index);
                quanpailie(strings, index + 1, end);
                swap(strings, i, index);
            }
        }
    }


    public static void dfs(int index , List<String> stringList,String[] strings){

        if (index == strings.length){
            StringBuffer sb = new StringBuffer();
            for (String s : stringList) {
                sb.append(s);
            }
            if (sb.toString().startsWith("0")) { //以 0 开头的需要分开放
                listZero.add(Integer.valueOf(sb.toString()));
            } else {
                list.add(Integer.valueOf(sb.toString()));
            }
            return;
        }


        for (int i = index; i < strings.length; i++) {
            stringList.add(strings[i]);
            System.out.println(stringList);

            dfs(i+1,stringList,strings);

            stringList.remove(stringList.size()-1);
            System.out.println("-------------------------------------");
        }





    }












}
