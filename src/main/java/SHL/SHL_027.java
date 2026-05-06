package main.java.SHL;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class SHL_027 {


/*

    English Version:

    Problem Description:

    Write a program to remove all the vowels from the given string.

    Given a string, you need to remove all the vowels (both lowercase and uppercase) from the string and print the resulting string.

    Input:

    A single line containing a string.

    Output:

    Print the string after removing all the vowels.

    Constraints:

    The string contains only English alphabets (both uppercase and lowercase).

    The length of the string is at most 1000.

    Example:

    Input:

    复制
            MynameisAnthony

    Output:

    复制
            Mynmsnthny

    中文对照版本：

    问题描述：

    编写一个程序，从给定字符串中移除所有元音字母。

    给定一个字符串，你需要从该字符串中移除所有元音字母（包括小写和大写），并打印结果字符串。

    输入：

    一行包含一个字符串。

    输出：

    打印移除所有元音后的字符串。

    约束条件：

    字符串仅包含英文字母（包括大写和小写）。

    字符串长度最多为1000。

    示例：

    输入：

    复制
            MynameisAnthony

    输出：

    复制
            Mynmsnthny*/


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 读取输入字符串
        String input = scanner.nextLine().trim();

        // 定义元音字母集合（包含大小写）
        Set<Character> vowels = new HashSet<>();
        vowels.add('a');
        vowels.add('e');
        vowels.add('i');
        vowels.add('o');
        vowels.add('u');
        vowels.add('A');
        vowels.add('E');
        vowels.add('I');
        vowels.add('O');
        vowels.add('U');

        // 使用StringBuilder构建结果字符串
        StringBuilder result = new StringBuilder();

        // 遍历输入字符串的每个字符
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            // 如果字符不是元音，则添加到结果中
            if (!vowels.contains(c)) {
                result.append(c);
            }
        }

        // 输出结果
        System.out.println(result.toString());

        scanner.close();
    }
}


class TestRemoveVowels {
    public static void main(String[] args) throws IOException {
        // 测试用例1: 示例输入
        test("测试用例1: 示例输入", "MynameisAnthony", "Mynmsnthny");

        // 测试用例2: 全是大写元音
        test("测试用例2: 全是大写元音", "AEIOU", "");

        // 测试用例3: 全是小写元音
        test("测试用例3: 全是小写元音", "aeiou", "");

        // 测试用例4: 混合大小写
        test("测试用例4: 混合大小写", "Hello World", "Hll Wrld");

        // 测试用例5: 没有元音
        test("测试用例5: 没有元音", "BCDFG", "BCDFG");

        // 测试用例6: 空字符串
        test("测试用例6: 空字符串", "", "");

        // 测试用例7: 只有一个字符
        test("测试用例7: 只有一个字符(a)", "a", "");
        test("测试用例7: 只有一个字符(b)", "b", "b");
    }

    private static void test(String testName, String input, String expected) {
        System.out.println("\n" + testName);
        System.out.println("输入: \"" + input + "\"");
        System.out.println("期望输出: \"" + expected + "\"");

        // 保存原始输入输出
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        try {
            // 设置测试输入
            ByteArrayInputStream in = new ByteArrayInputStream((input + "\n").getBytes());
            System.setIn(in);

            // 捕获输出
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(out);
            System.setOut(printStream);

            // 运行主程序
            SHL_027.main(new String[]{});

            // 获取输出（去除换行符）
            String output = out.toString().trim();

            // 恢复原始输出
            System.setOut(originalOut);
            System.setIn(originalIn);

            // 检查结果
            if (output.equals(expected)) {
                System.out.println("✓ 通过! 输出: \"" + output + "\"");
            } else {
                System.out.println("✗ 失败! 期望: \"" + expected + "\", 实际: \"" + output + "\"");
            }

        } catch (Exception e) {
            // 恢复原始输出
            System.setOut(originalOut);
            System.setIn(originalIn);
            System.out.println("✗ 测试异常: " + e.getMessage());
        }
    }
}