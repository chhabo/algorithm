package basic;

import java.util.Scanner;

public class _29_最多提取子串数目 {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        char[] charsA = sc.nextLine().toCharArray();
        char[] charsB = sc.nextLine().toCharArray();
        System.out.println(getNums(charsA,charsB));


         
    }

    /**
     *
     * ababcecfdc  A
     * abc         B
     *
     * @param charsA
     * @param charsB
     */
    private static int getNums(char[] charsA, char[] charsB) {

        boolean match = true;
        int index = 0;
        for (int i = 0; i < charsB.length; i++) {
            boolean charMath = false;

            for (int j = 0; j < charsA.length; j++) {
                if (charsA[j] == charsB[i] && (index<j || index == 0)){
                    index = j;
                    charMath = true;
                    charsA[j] = ' ';
                    break;
                }
            }
            if (!charMath){
                match = false;
                break;
            }
        }
        if (match){
            return 1 + getNums(charsA,charsB);
        }
        return 0;





    }
}
