package basic;

import java.util.Arrays;
import java.util.Scanner;

public class _08_分界线 {

        //-Xms32m -Xmx32m

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] newspaper = sc.nextLine().split(" ");
        String[] anonymousLetter = sc.nextLine().split(" ");

        boolean res = true;
        for (String anony : anonymousLetter) {

            for (int i = 0; i < newspaper.length; i++) {
                String news = newspaper[i];
                if (anony.length() == news.length() && checkWordAlign(news,anony)){
                    newspaper[i] = "";
                    break;
                }
                if (i == newspaper.length-1){
                    res = false;
                }
            }
            if (!res) {
                break;
            }
        }
        System.out.println(res);


    }

    private static boolean checkWordAlign(String news, String anony) {
        char[] newsChar = news.toCharArray();
        char[] anonyChar = news.toCharArray();

        Arrays.sort(newsChar);
        Arrays.sort(anonyChar);

        for (int i = 0; i < newsChar.length; i++) {
            if (newsChar[i] != anonyChar[i]) return false;
        }

        return true;

    }


}
