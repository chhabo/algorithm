package basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class _19_寻找链表中间节点 {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String[] inputFirst = sc.nextLine().split(" ");
        int n = Integer.parseInt(inputFirst[1]);
        List<String[]> list = new ArrayList<>();
        int dataF = -1;
        String nexS = "";
        for (int i = 0; i < n; i++) {
            String[] inp = sc.nextLine().split(" ");
            if (inp[0].equals(inputFirst[0])) {
                dataF = Integer.valueOf(inp[1]); //头部 data
                nexS = inp[2]; //下一个节点的 addr
            } else {
                list.add(inp);
            }
        }


        Node first = new Node(inputFirst[0], dataF, new Node(nexS, 0, new Node())); //头部节点
        int num = n / 2 + 1; //中间节点的索引
        Node node = first;
        int index = 0;
        while (list.size() != n - num) { //num 个元素已经取完了
            for (int i = 0; i < list.size(); i++) {
                String[] strings = list.get(i);
                if (strings[0].equals(node.next.addr)) {
                    int data = Integer.valueOf(strings[1]);
                    node.next = new Node(strings[0], data, new Node(strings[2], 0, new Node()));
                    node = node.next;
                    index = i;
                    break;
                }
            }
            list.remove(index); //删除已经加入链表的元素
        }
        System.out.println(node.data);
    }
}

class Node {
    public String addr;
    public int data;
    public Node next;

    public Node() {
        super();
    }

    public Node(String addr, int data, Node next) {
        this.addr = addr;
        this.data = data;
        this.next = next;
    }


}
