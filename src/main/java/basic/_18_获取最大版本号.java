package basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class _18_获取最大版本号 {



    public static void main(String[] args) {
        myImpl();

//        Scanner sc = new Scanner(System.in);
//        String strings1 =sc.nextLine();
//        String strings2 =sc.nextLine();
//        String[] arrA = strings1.replace("-",".").split("\\.");
//        String[] arrB = strings2.replace("-",".").split("\\.");
//        String[] mvnA = new String[]{"","","",""};
//        for(int i=0; i<arrA.length; i++){
//            mvnA[i] = arrA[i];
//        }
//        String[] mvnB = new String[]{"","","",""};
//        for(int i=0; i<arrB.length; i++){
//            mvnB[i] = arrB[i];
//        }
//        int res = 1;
//        for(int i=0; i<4; i++){
//            if(i == 3 || (i == 2 && (mvnA[i].equals("") || mvnB[i].equals("")))){
//                res = mvnA[i].compareTo(mvnB[i]) < 0 ? 2 : 1;
//                break;
//            }
//            int a = Integer.valueOf(mvnA[i]);
//            int b = Integer.valueOf(mvnB[i]);
//            if(a > b){
//                res = 1;
//                break;
//            }else if(a < b){
//                res = 2;
//                break;
//            }
//        }
//        System.out.println(res == 1 ? strings1 : strings2);
    }


    public static void myImpl(){
        Scanner sc = new Scanner(System.in);
        String version1 = sc.nextLine();
        String version2 =  sc.nextLine();
        String[] arrA = version1.replace("-",".").split("\\.");
        String[] arrB = version2.replace("-",".").split("\\.");
        String[] mvnA = new String[]{"","","",""};
        for(int i=0; i<arrA.length; i++){
            mvnA[i] = arrA[i];
        }
        String[] mvnB = new String[]{"","","",""};
        for(int i=0; i<arrB.length; i++){
            mvnB[i] = arrB[i];
        }

        List<String[]> list = new ArrayList<>();
        list.add(arrA);
        list.add(arrB);

        list.sort((a,b)->{
            if (Integer.parseInt(a[0]) > Integer.parseInt(b[0])){
                return -1;
            }else if (Integer.parseInt(a[1]) > Integer.parseInt(b[1])){
                return -1;
            }else if (a[2] != "" && b[2] != "" && Integer.parseInt(a[2]) > Integer.parseInt(b[2])){
                return -1;
            }else if (a[2] == "" || b[2] == "" ){
                return b[2].compareTo(a[2]);
            }else {
                return b[3].compareTo(a[3]);
            }


        });
        System.out.println(list.get(0));

    }

//1.2.11-S2
//1.3.11-S13



















}
