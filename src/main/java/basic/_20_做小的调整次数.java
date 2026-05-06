package basic;

import sun.applet.Main;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Scanner;

public class _20_做小的调整次数 {
    public static void main(String[] arg) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        sc.nextLine();
        Deque<Integer> deque = new ArrayDeque<>();
        int index = 1; //删除的整数，相当于索引
        int res = 0; //变换的次数
        for (int i = 0; i < 2 * n; i++) {
            String[] strings = sc.nextLine().split(" ");
            if (strings.length == 1) { //只有一个字符 remove
                if (deque.peekFirst() != index) { //移除的数字不符合要求
                    Object[] cp = deque.toArray(); //队列转换为数组
                    Arrays.sort(cp); //数组进行升序排序
                    deque.clear(); //队列清空
                    for (int j = 0; j < cp.length; j++) {
                        deque.addLast((Integer) cp[j]); //排序后的数组加入队列
                    }
                    res++;
                }
                deque.pollFirst(); //移除第一个数
                index++; //数据索引加 1
            } else {
                int num = Integer.valueOf(strings[2]);
                if (strings[0].equals("head")) {
                    deque.addFirst(num); //头部添加
                } else {
                    deque.addLast(num); //尾部添加
                }
            }
        }
        System.out.println(res);
    }
}
