package basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class _01_猜字谜 {


    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        String[] mimian = sc.nextLine().split(",");
        String[] midi = sc.nextLine().split(",");
        List<String> resList = new ArrayList<>();
        for(int i=0; i<mimian.length; i++){
            String mm = mimian[i]; //谜面
            boolean isFound = false; //是否存在谜底
            for(int j=0; j<midi.length; j++){
                String md = midi[j]; //谜底
                if(bianxu(mm, md)){
                    resList.add(md);
                    isFound = true;
                }else if(quchong(mm, md)){
                    resList.add(md);
                    isFound = true;
                }
            }
            if(!isFound){
                resList.add("not found");
            }
        }
        String res = "";
        for(int i=0;i<resList.size();i++){
            res += resList.get(i) + ",";
        }
        System.out.println(res.substring(0,res.length()-1));
    }
    /**
     * 变序后谜面和谜底是否一致
     * @param miman
     * @param midi * @return
     */
    public static boolean bianxu(String miman, String midi){
        String[] mmStrs = miman.split("");
        Arrays.sort(mmStrs);
        String[] midiStrs = midi.split("");
        Arrays.sort(midiStrs);
        if(Arrays.equals(mmStrs, midiStrs)){
            return true;
        }
        return false;
    }
    /**
     * 去重后谜面与谜底是否一致
     * @param miman
     * @param midi * @return
     */
    public static boolean quchong(String miman, String midi){
        List<Character> mmList = new ArrayList<>();
        for(int i=0; i<miman.length(); i++){
            char c = miman.charAt(i);
            if(!mmList.contains(c)){
                mmList.add(c);
            }
        }
        List<Character> midiList = new ArrayList<>();
        for(int i=0; i<midi.length(); i++){
            char c = midi.charAt(i);
            if(!midiList.contains(c)){
                midiList.add(c);
            }
        }
        if(mmList.equals(midiList)){
            return true;
        }
        return false;
    }

}
