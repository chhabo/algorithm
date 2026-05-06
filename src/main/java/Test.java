package main.java;

import java.util.ArrayList;
import java.util.List;

public class Test {


    public static void main(String[] args) {
        int[] a = new int[]{1,2,3};
    }

    public List<Integer> spiralOrder(int[][] matrix) {
        List<Integer> res = new ArrayList<>();
        if (matrix == null || matrix.length == 0) return res;
        int t = 0, b = matrix.length - 1, l = 0, r = matrix[0].length - 1;
        while (t <= b && l <= r) {
            for (int i = l; i <= r; res.add(matrix[t][i++])) ; t++;
            for (int i = t; i <= b; res.add(matrix[i++][r])) ; r--;
            if (t > b) break;
            for (int i = r; i >= l; res.add(matrix[b][i--])) ; b--;
            if (l > r) break;
            for (int i = b; i >= t; res.add(matrix[i--][l])) ; l++;
        }
        return res;
    }
}
