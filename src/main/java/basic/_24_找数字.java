package basic;

import java.io.PrintStream;
import java.util.*;

public class _24_找数字 {

    public static Map<String,Integer> map = new HashMap<>();

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int row = sc.nextInt();
        int col = sc.nextInt();

        int [][] nums = new int[row][col];
        int [][] res = new int[row][col];
        List<List<Integer>> resList = new ArrayList<>();
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                nums[i][j] = sc.nextInt();
            }
        }

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                getMinnestLength(nums,i,j,row,col);
            }
        }

//        key : i-j
        for (int i = 0; i < row; i++) {
            List<Integer> rows = new ArrayList<>();
            for (int j = 0; j < col; j++) {
                String key = i + "-" + j;
                int length = map.getOrDefault(key,-1);
                rows.add(length);
            }
            resList.add(rows);
        }
        System.out.println(resList);


    }

    /**
 3
 5
 0 3 5 4 2
 2 5 7 8 3
 2 5 4 2 4

     * @param nums
     * @param x
     * @param y
     * @param row
     * @param col
     * @return
     */

    private static int getMinnestLength(int[][] nums,int x,int y,int row,int col) {
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int temp = nums[i][j];
                if (!(x == i && j == y) && temp == nums[x][y]){
                    int length = Math.abs(i - x) + Math.abs(j - y);
                    String key = x + "-" + y;
                    if (map.containsKey(key)) {
                        if (map.get(key) > length) {
                            map.put(key,length);
                        }
                    }else {
                        map.put(key,length);
                    }
                }

            }
        }
        return 0;
    }
}
