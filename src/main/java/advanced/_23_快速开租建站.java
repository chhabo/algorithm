package advanced;

import java.util.*;

public class _23_快速开租建站 {

    /**
     * key：服务器
     * value：需要依赖 key 的服务器
     */
    public static Map<Integer, Set<Integer>> map = new HashMap<>();
    public static int res = 1;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int taskNum = sc.nextInt();
        int relationsNum = sc.nextInt();
        int[] taskInts = new int[taskNum]; //用来记录需要依赖的服务
        for(int i=0; i<relationsNum; i++){
            Set<Integer> inputList;
            int k = sc.nextInt();
            int v = sc.nextInt();
            if(map.containsKey(k)){
                inputList = map.get(k);
            }else {
                inputList = new HashSet<>();
            }
            inputList.add(v);
            map.put( k, inputList);
            taskInts[v]++;
        }
        Set<Integer> taskStart = new HashSet<>(); //已经部署的服务
        Set<Integer> taskWait = new HashSet<>(); //等待部署的服务
        for(int i=0; i<taskNum; i++){
            if(taskInts[i] == 0){
                taskStart.add(i); //不需要依赖的服务直接部署
            }else {
                taskWait.add(i); //需要依赖的服务等待部署
            }
        }
        handle( taskStart, taskWait);
        System.out.println(res);
    }
    /**
     * 1、先求出依赖已经部署的服务的服务
     * 2、从步骤 1 中的集合移除还需要其他依赖的服务
     * 3、根据步骤 2 求得的集合更新 taskStart 和 taskWait * @param taskStart 已经部署的服务
     * @param taskWait 等待部署的服务
     */
    public static void handle( Set<Integer> taskStart, Set<Integer> taskWait){
        if(taskWait.size() != 0){ //还有服务等待部署
            Set<Integer> temp = new HashSet<>(); //接下来可以部署的服务
            for(int i : taskStart){
                if(map.containsKey(i)){
                    temp.addAll(map.get(i)); //依赖已经部署的服务的服务
                }
            }
            for(int i : map.keySet()){
                if(!taskStart.contains(i)){
                    temp.removeAll(map.get(i)); //移除那些还需要依赖的服务
                }
            }
            taskStart.addAll(temp);
            taskWait.removeAll(temp);
            res ++; //部署过一次 ++
            handle( taskStart, taskWait);
        }
    }

}
