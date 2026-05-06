package basic;

import java.util.*;

public class _06_打印文件 {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = Integer.parseInt(sc.nextLine());

        Map<String,List<int[]>> map = new HashMap<>();
        int count = 0;
        for (int i = 0; i < n; i++) {
            String[] split = sc.nextLine().split(" ");
            if ("IN".equals(split[0])){
                int[] inInts = new int[2];
                inInts[0] = Integer.parseInt(split[2]);
                inInts[1] = ++ count;
                List<int[]> inList = new ArrayList<>();
                if (map.containsKey(split[1])){
                    inList = map.get(split[1]);
                }
                inList.add(inInts);
                map.put(split[1],inList);

            }else if ("OUT".equals(split[0])){
                if (!map.containsKey(split[1])){
                    System.out.println("NULL");
                    continue;
                }
                List<int[]> outList = map.get(split[1]);
                if (outList.size() ==0) {
                    System.out.println("NULL");
                }
                else {
                    outList.sort((a,b)->b[0]-a[0]);
                    System.out.println(outList.get(0)[1]);
                    outList.remove(0);
                }
            }

        }


    }
}
