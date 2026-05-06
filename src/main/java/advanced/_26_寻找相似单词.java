package advanced;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class _26_寻找相似单词 {



    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int N = sc.nextInt();
        sc.nextLine();
        List<String> list = new ArrayList<>();
        for(int i=0; i<N; i++){
            list.add(sc.nextLine());
        }
        String X = sc.nextLine();
        String res = "";
        for(String Y : list){
            if(Y.length() == X.length() && isSimilar( X, Y)){ //长度相同才能相似
                res += Y + " ";
            }
        }
        System.out.println(res.length() == 0 ? "null" : res.substring(0, res.length()-1));
    }
    /**
     * 将两个字符串转换为字符数组
     * 排序后比较字符是否都相同
     * 都相同则相似
     * 有一个不相同则不相似
     * @param X
     * @param Y * @return
     */
    public static boolean isSimilar(String X, String Y){
        char[] charX = X.toCharArray();
        char[] charY = Y.toCharArray();
        Arrays.sort(charX);
        Arrays.sort(charY);
        boolean res = true;
        for(int i=0; i<charX.length; i++){
            if(charX[i] != charY[i]){ //有一个字符不相同就不相似
                res = false;
                break;
            }
        }
        return res;
    }
}
