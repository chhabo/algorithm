package basic;

import java.util.*;

public class _31_优秀学员统计 {


public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    int N = sc.nextInt();
/**
 * 使用 map 来放置员工打卡情况
 * key：员工工号
 * value：int[]员工打卡情况
 * int[0]：第一次打开时间
 * int[1]：总打卡数
 */

    Map<Integer, int[]> map = new HashMap<>();
    int[] dayInts = new int[30]; //30 天每天的打卡人数
    for(int i=0; i<30; i++){
        dayInts[i] = sc.nextInt();
    }
    for(int i=0; i<30; i++){
        for(int j=0; j<dayInts[i]; j++){ //用 30 天每天的打卡人数控制输入
            int input = sc.nextInt();
            if(input > N-1){ //输入值大于员工总数（工号从 0 开始）
                continue;
            }
            int[] temp = new int[2];
            if(map.containsKey(input)){
                temp = map.get(input);
                ++ temp[1];
            }else {
                temp[0] = i;
                temp[1] = 1;
            }
            map.put(input, temp);
        }
    }
    List<Map.Entry<Integer, int[]>> mapList = new ArrayList<>(map.entrySet()); //将 map转化为 list 进行排序
    mapList.sort((a,b) -> {
        if(b.getValue()[1] < a.getValue()[1]){ //天数多的靠前
            return -1;
        }else if(b.getValue()[1] == a.getValue()[1]){
            if(b.getValue()[0] > a.getValue()[0]){ //天数相同的第一次打卡时间靠前
                return -1;
            }
        }
        return 1;
    });
    String res = "";
    for(int i=0; i< (mapList.size() < 5 ? mapList.size() : 5);i++){ //小于 5 的取所有，大于 5 的只取 5
        res += mapList.get(i).getKey() + " ";
    }
    System.out.println(res.substring(0,res.length()-1));
}

}
