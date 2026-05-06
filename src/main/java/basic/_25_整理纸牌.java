package basic;

import java.util.*;

public class _25_整理纸牌 {
    public static void main2(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] strings = sc.nextLine().split(" ");
/**
 * 使用 map 来放置牌
 * key：car 的点数
 * value：同一点数的张数
 */
        Map<Integer, Integer> map = new HashMap<>();
        for(String string : strings){
            Integer i = Integer.valueOf(string);
            map.put( i, map.getOrDefault( i, 0) + 1);
        }
        /**
         * map 转化为 list 进行排序
         * 1、张数多的排在前面
         * 2、张数一样的，点数多的排在前面
         */
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((a,b) -> {
            if(b.getValue() == a.getValue()){
                return b.getKey() - a.getKey();
            }
            return b.getValue() - a.getValue();
        });
        StringBuffer sb = new StringBuffer();
        List<Integer> chaiFen = new ArrayList<>(); //需要拆分的点数（有可能是 2 个所以需要用到集合）
        for(int i=0; i<list.size(); i++){
            Map.Entry<Integer, Integer> temp = list.get(i);
            int carNum = temp.getKey();
            int carCount = temp.getValue();
/**
 * 当碰到前面是三张，此牌也是三张的时候，需要进行拆分了
 * （因为葫芦仅次于炸弹，所以要紧葫芦后面的对子凑最大）
 */
            if(i > 0 && list.get(i-1).getValue() == 3 && carCount == 3){
                chaiFen.add(carNum);
                carCount = 2;
                temp.setValue(2);
            }else if(carCount == 1 && chaiFen.size() != 0){ //到了单张的时候，需要注意拆分的牌了
                for (int k=0; k<chaiFen.size(); k++){
                    if(chaiFen.get(k) > carNum){ //当拆分中的牌大于此牌时，先安排拆分的牌
                        sb.append(chaiFen.get(k) + " ");
                        chaiFen.remove(k); //用过的牌需要移除
                        k--; //移除后索引往前移一位
                    }
                }
            }
            for(int j=0; j<carCount; j++){
                sb.append(carNum + " ");
            }
        }
        if(chaiFen.size() != 0){ //拆分的牌还有时需要继续用完
            for(int i : chaiFen){
                sb.append(i + " ");
            }
        }
        System.out.println(sb.substring(0, sb.length()-1));
    }



    public static void main1(String[] args) {
        Scanner sc = new Scanner(System.in);
        String n = sc.nextLine();
        String[] nList = n.split(" ");
        List<Integer> num3 = new ArrayList<>();
        List<Integer> num2 = new ArrayList<>();
        List<Integer> num1 = new ArrayList<>();
        List<Integer> result = new ArrayList<>();
        int[] intList = new int[14];
        for(String a:nList){
            if(!num1.contains(Integer.valueOf(a))) {
                num1.add(Integer.valueOf(a));
            }
            intList[Integer.valueOf(a)] = intList[Integer.valueOf(a)]+1;
            if(intList[Integer.valueOf(a)] >= 4){
                result.add(Integer.valueOf(a));
                result.add(Integer.valueOf(a));
                result.add(Integer.valueOf(a));
                result.add(Integer.valueOf(a));
                intList[Integer.valueOf(a)] -= 4;
                num3.remove(Integer.valueOf(a));
                num2.remove(Integer.valueOf(a));
                num1.remove(Integer.valueOf(a));
            }
            if(intList[Integer.valueOf(a)] == 3){
                num3.add(Integer.valueOf(a));
                num2.remove(Integer.valueOf(a));
                num1.remove(Integer.valueOf(a));
            }
            if(intList[Integer.valueOf(a)] == 2){
                num2.add(Integer.valueOf(a));
                num1.remove(Integer.valueOf(a));
            }
        }
        Collections.sort(result, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2-o1;
            }
        });
        Collections.sort(num3, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2-o1;
            }
        });
        Collections.sort(num2);
        while (num3.size()>1){
            result.add(num3.get(0));
            result.add(num3.get(0));
            result.add(num3.get(0));
            if(num2.size()<=0||num3.get(1)>num2.get(num2.size()-1)){
                result.add(num3.get(1));
                result.add(num3.get(1));
                num1.add(num3.get(1));
                num3.remove(0);
                num3.remove(0);
            }else{
                result.add(num2.get(num2.size()-1));
                result.add(num2.get(num2.size()-1));
                num3.remove(0);
                num2.remove(num2.size()-1);
            }

        }
        while(num3.size()>0&&num2.size()>0){
            result.add(num3.get(0));
            result.add(num3.get(0));
            result.add(num3.get(0));
            result.add(num2.get(0));
            result.add(num2.get(0));
            num3.remove(0);
            num2.remove(0);
        }
        if(num3.size() == 1){
            result.add(num3.get(0));
            result.add(num3.get(0));
            result.add(num3.get(0));
        }
        if(num2.size()>0) {
            for(int i=num2.size()-1;i>=0;i--){
                result.add(num2.get(i));
                result.add(num2.get(i));
            }
        }

        if(num1.size()>0){
            Collections.sort(num1, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    return o2-o1;
                }
            });
            result.addAll(num1);
        }
        for(int i=0;i<result.size();i++){
            System.out.print(result.get(i));
            System.out.print(" ");
        }
    }


    public static void main(String[] args) {
//        List<Integer> list = new ArrayList<>();
//
//
////
////
////            1 2 3 2 5 1 4 5 7 2
//    //     2 | 21 | 231 | 5231 | 52314 | 572314
//        Scanner sc = new Scanner(System.in);
//
//        int nodeNum = sc.nextInt();
//        int headVal = sc.nextInt();
//        list.add(headVal);
//        for (int i = 1; i < nodeNum; i++) {
//           int  value = sc.nextInt();
//           int index = sc.nextInt();
//           if (list.size()<index){
//               list.add(list.size(),value);
//           }else {
//               list.add(index-1,value);
//           }
//        }
//
//        int delVal = sc.nextInt();
//        List<Integer> resList = new ArrayList<>();
//
//        System.out.println();
//        Iterator<Integer> iterator = list.iterator();
//        while (iterator.hasNext()){
//            Integer val = iterator.next();
//            System.out.print(val+ " ");
//            if (!val.equals(delVal)){
//                resList.add(val);
//            }
//        }
//
//        System.out.println();
//        for (Integer integer : resList) {
//            System.out.print(integer+ " ");
//
//        }
//        System.out.println();
//        sc.close();
//
//
//
//

        Scanner in = new Scanner(System.in);
        while (in.hasNextInt()) {
            int num = in.nextInt();
            LinkedList<Integer> list = new LinkedList<>();
            list.add(in.nextInt());
            for (int i = 1; i < num; i++) {
                int newNum = in.nextInt();
                int index = list.indexOf(in.nextInt());
                list.add(index + 1, newNum);
            }

            list.remove((Integer) (in.nextInt())); // remove 是重载方法，将其转为 Integer，强制使用删除指定对象的方法
            for (int i : list) {
                System.out.print(i + " ");
            }
        }
    }




}
