package basic;

import java.util.*;

public class _09_关联端口组合 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int M = sc.nextInt();
        if(M>10){
            System.out.println("[[]]");
        }else {
            List<Set<String>> list = new ArrayList<>();
            sc.nextLine();
            for(int i=0; i<M; i++){
                String[] strings = sc.nextLine().split(",");
                if (strings.length>100) {
                    System.out.println("[[]]");
                    break;
                }
                Set<String> set = new HashSet<>();
                if(strings.length == 1){ //只有一个元素直接添加
                    set.add(strings[0]);
                    list.add(set);
                }else {
                    for(String str : strings){
                        set.add(str); //数组转化为 set 集合
                    }
                    list.add(set);
                    merge( set, list, list.size()-1);
                }
            }
            System.out.println(list);
        }
    }
    /**
     * 合并关联端口组合
     * @param set 进行合并的端口组合
     * @param list 端口组合集合
     * @param index 需要进行合并的组合索引
     */
    public static void merge(Set<String> set, List<Set<String>> list, int index){
        for(int i=0; i<list.size(); i++){
            if(i == index){
                continue;
            }
            Set<String> setIndex = list.get(i); //当前索引的 set 集合
            Set<String> setTemp = new HashSet<>();
            setTemp.addAll(set);
            setTemp.retainAll(setIndex); //求出 set 和 setIndex 的交集
            if(setTemp.size()>=2){ //交集大于等于 2 则合并
                set.addAll(setIndex); //set 与 setIndex 合并
                int beforeIndex = index > i ? i : index; //求出排在前面的 index
                int afterIndex = beforeIndex == index ? i : index; //靠后的位置
                list.remove(afterIndex); //合并的两个 set 进行删除，先删除后面的一个
                list.remove(beforeIndex);
                list.add( beforeIndex, set); //将新的 set 放在前面位置
                merge( set, list, beforeIndex); //将新建的组合再进行判断合并
            }
        }
    }
}
