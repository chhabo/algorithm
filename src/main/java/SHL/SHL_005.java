package main.java.SHL;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.*;
public class SHL_005 {
/*
    题目ID: SHL_005

    英文题目 (English Description):

    Emerson is playing with binary strings.
    He can reverse any substring of a string in one step.
    He wants to transform the initial string into the target string using the minimum number of steps.
     Help him find the minimum number of steps required, or output -1 if it's impossible.

    中文翻译 (Chinese Description):

    Emerson 正在玩二进制字符串。他可以在一步操作中反转字符串的任意子串。他希望将初始字符串转换为目标字符串，并且使用最少的步骤数。
    请帮助他找到所需的最少步骤数，如果不可能则输出 -1。

    输入格式 (Input Format):

    第一行：初始字符串 str1（仅包含字符 '0' 和 '1'）

    第二行：目标字符串 str2（仅包含字符 '0' 和 '1'）

    输出格式 (Output Format):

    一个整数，表示从 str1 转换到 str2 所需的最少步骤数。如果不可能，则输出 -1。

    约束条件 (Constraints):

            1 ≤ 字符串长度 ≤ 30

    字符串只包含字符 '0' 和 '1'

    示例1 (Example 1):

    复制
    输入 (Input):
            1010
            0101

    输出 (Output):
            2

    解释：第一步反转整个字符串得到 0101，但可能需要更多步骤。实际最少步骤为 2（例如，先反转前两个字符得到 0110，再反转后两个字符得到 0101）。

    示例2 (Example 2):

    复制
    输入 (Input):
            1111
            1111

    输出 (Output):
            -1

    解释：初始字符串和目标字符串已经相同，但由于每一步反转必须生成新字符串，因此无法通过任何步骤转换，输出 -1。

    示例3 (Example 3):

    复制
    输入 (Input):
            1100
            0011

    输出 (Output):
            1

    解释：直接反转整个字符串即可从 1100 得到 0011，只需要 1 步。*/


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String str1 = scanner.nextLine().trim();
        String str2 = scanner.nextLine().trim();

        // 检查字符组成是否相同
        if (str1.length() != str2.length()) {
            System.out.println(-1);
            return;
        }

        int countZero1 = 0, countZero2 = 0;
        for (int i = 0; i < str1.length(); i++) {
            if (str1.charAt(i) == '0') countZero1++;
            if (str2.charAt(i) == '0') countZero2++;
        }
        if (countZero1 != countZero2) {
            System.out.println(-1);
            return;
        }

        // 如果已经相同，由于每一步必须生成新字符串，返回 -1
        if (str1.equals(str2)) {
            System.out.println(-1);
            return;
        }

        // 使用BFS寻找最少步骤
        int result = bfs(str1, str2);
        System.out.println(result);

        scanner.close();
    }
    private static int bfs(String start, String target) {
        int n = start.length();
        Queue<String> queue = new LinkedList<>();
        Map<String, Integer> visited = new HashMap<>(); // 记录每个字符串的步骤数

        queue.offer(start);
        visited.put(start, 0);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            int steps = visited.get(current);
            System.out.println("------------------------");
            System.out.println("current -> "+ current);
            System.out.println("------------------------");
            // 尝试所有可能的子串反转
            for (int len = 2; len <= n; len++) { // 子串长度至少为2
                for (int i = 0; i <= n - len; i++) {
                    // 反转子串 current[i : i+len]
                    int right = i + len - 1;
                    String reversed = reverseSubstring(current, i, right);
                    System.out.println("reversed ["+i +"," + right+"] -> " + reversed);
                    // 如果找到目标
                    if (reversed.equals(target)) {
                        return steps + 1;
                    }

                    // 如果未访问过，加入队列
                    if (!visited.containsKey(reversed)) {
                        visited.put(reversed, steps + 1);
                        queue.offer(reversed);
                    }
                }
            }
        }

        return -1; // 无法到达
    }

    private static String reverseSubstring(String s, int left, int right) {
        char[] chars = s.toCharArray();
        while (left < right) {
            char temp = chars[left];
            chars[left] = chars[right];
            chars[right] = temp;
            left++;
            right--;
        }
        return new String(chars);
    }
}
