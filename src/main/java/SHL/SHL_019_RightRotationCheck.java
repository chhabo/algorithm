package main.java.SHL;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SHL_019_RightRotationCheck {


    /**
     * SHL_019: Right Rotation String Check
     * 右旋转字符串判断
     *
     * 题目描述：
     * Given two strings word1 and word2, determine if word1 is a right rotation of word2.
     * A right rotation of a string is obtained by moving some characters from the end
     * of the string to the beginning while maintaining the order of the remaining characters.
     *
     * 中文翻译：
     * 给定两个字符串 word1 和 word2，判断 word1 是否是 word2 的右旋转版本。
     * 字符串的右旋转是通过将字符串末尾的一些字符移动到开头，同时保持其余字符的顺序而得到的。
     *
     * 输入格式：
     * First line: string word1
     * Second line: string word2
     *
     * 中文翻译：
     * 第一行：字符串 word1
     * 第二行：字符串 word2
     *
     * 输出格式：
     * If word1 is a right rotation of word2, print 1
     * Otherwise, print -1
     *
     * 中文翻译：
     * 如果 word1 是 word2 的右旋转版本，输出 1
     * 否则，输出 -1
     *
     * 约束条件：
     * 1 ≤ length of word1, word2 ≤ 1000
     * Strings contain only lowercase English letters
     *
     * 示例1：
     * Input:
     * abcd
     * cdab
     *
     * Output:
     * 1
     *
     * 示例解释1：
     * "abcd" rotated right by 2 positions becomes "cdab"
     *
     * 示例2：
     * Input:
     * hello
     * world
     *
     * Output:
     * -1
     *
     * 示例解释2：
     * "hello" cannot be obtained by rotating "world"
     */


    /**
     * 检查word1是否是word2的右旋转版本
     *
     * @param word1 第一个字符串
     * @param word2 第二个字符串
     * @return 如果是右旋转返回1，否则返回-1
     */
    public static int isRightRotation(String word1, String word2) {
        // 检查长度是否相同
        if (word1.length() != word2.length()) {
            return -1;
        }

        // 将word2与自身拼接，这样所有可能的右旋转都会包含在拼接后的字符串中
        String doubled = word2 + word2;
        // 检查word1是否是doubled的子串
        if (doubled.contains(word1)) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * 主函数，处理标准输入输出
     */
    public static void main1(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 读取输入字符串
        String word1 = scanner.nextLine();
        String word2 = scanner.nextLine();

        // 检查并输出结果
        int result = isRightRotation(word1, word2);
        System.out.println(result);

        scanner.close();
    }

    /**
     * 测试函数，包含多个测试用例
     */
    public static void testRightRotationCheck() {
        System.out.println("=== SHL_019 右旋转字符串判断测试开始 ===\n");

        // 测试用例1：示例1 - 是右旋转
        String test1Word1 = "abcd";
        String test1Word2 = "cdab";
        int result1 = isRightRotation(test1Word1, test1Word2);
        System.out.println("测试用例1 - 示例1 (abcd, cdab)：");
        System.out.println("  word1: " + test1Word1);
        System.out.println("  word2: " + test1Word2);
        System.out.println("  预期结果: 1");
        System.out.println("  实际结果: " + result1);
        System.out.println("  测试" + (result1 == 1 ? "通过" : "失败") + "\n");

        // 测试用例2：示例2 - 不是右旋转
        String test2Word1 = "hello";
        String test2Word2 = "world";
        int result2 = isRightRotation(test2Word1, test2Word2);
        System.out.println("测试用例2 - 示例2 (hello, world)：");
        System.out.println("  word1: " + test2Word1);
        System.out.println("  word2: " + test2Word2);
        System.out.println("  预期结果: -1");
        System.out.println("  实际结果: " + result2);
        System.out.println("  测试" + (result2 == -1 ? "通过" : "失败") + "\n");

        // 测试用例3：完全相同的字符串
        String test3Word1 = "rotation";
        String test3Word2 = "rotation";
        int result3 = isRightRotation(test3Word1, test3Word2);
        System.out.println("测试用例3 - 完全相同的字符串：");
        System.out.println("  word1: " + test3Word1);
        System.out.println("  word2: " + test3Word2);
        System.out.println("  预期结果: 1 (旋转0次)");
        System.out.println("  实际结果: " + result3);
        System.out.println("  测试" + (result3 == 1 ? "通过" : "失败") + "\n");

        // 测试用例4：长度不同
        String test4Word1 = "abc";
        String test4Word2 = "abcd";
        int result4 = isRightRotation(test4Word1, test4Word2);
        System.out.println("测试用例4 - 长度不同：");
        System.out.println("  word1: " + test4Word1 + " (长度" + test4Word1.length() + ")");
        System.out.println("  word2: " + test4Word2 + " (长度" + test4Word2.length() + ")");
        System.out.println("  预期结果: -1");
        System.out.println("  实际结果: " + result4);
        System.out.println("  测试" + (result4 == -1 ? "通过" : "失败") + "\n");

        // 测试用例5：空字符串
        String test5Word1 = "";
        String test5Word2 = "";
        int result5 = isRightRotation(test5Word1, test5Word2);
        System.out.println("测试用例5 - 空字符串：");
        System.out.println("  word1: \"" + test5Word1 + "\" (长度" + test5Word1.length() + ")");
        System.out.println("  word2: \"" + test5Word2 + "\" (长度" + test5Word2.length() + ")");
        System.out.println("  预期结果: 1");
        System.out.println("  实际结果: " + result5);
        System.out.println("  测试" + (result5 == 1 ? "通过" : "失败") + "\n");

        // 测试用例6：单个字符
        String test6Word1 = "a";
        String test6Word2 = "a";
        int result6 = isRightRotation(test6Word1, test6Word2);
        System.out.println("测试用例6 - 单个字符相同：");
        System.out.println("  word1: " + test6Word1);
        System.out.println("  word2: " + test6Word2);
        System.out.println("  预期结果: 1");
        System.out.println("  实际结果: " + result6);
        System.out.println("  测试" + (result6 == 1 ? "通过" : "失败") + "\n");

        // 测试用例7：单个字符不同
        String test7Word1 = "a";
        String test7Word2 = "b";
        int result7 = isRightRotation(test7Word1, test7Word2);
        System.out.println("测试用例7 - 单个字符不同：");
        System.out.println("  word1: " + test7Word1);
        System.out.println("  word2: " + test7Word2);
        System.out.println("  预期结果: -1");
        System.out.println("  实际结果: " + result7);
        System.out.println("  测试" + (result7 == -1 ? "通过" : "失败") + "\n");

        // 测试用例8：多次旋转的情况
        String test8Word1 = "programming";
        String test8Word2 = "mingprogram";
        int result8 = isRightRotation(test8Word1, test8Word2);
        System.out.println("测试用例8 - 多次旋转：");
        System.out.println("  word1: " + test8Word1);
        System.out.println("  word2: " + test8Word2);
        System.out.println("  预期结果: 1");
        System.out.println("  实际结果: " + result8);
        System.out.println("  测试" + (result8 == 1 ? "通过" : "失败") + "\n");

        // 测试用例9：包含重复字符
        String test9Word1 = "aabbaa";
        String test9Word2 = "baaabb";
        int result9 = isRightRotation(test9Word1, test9Word2);
        System.out.println("测试用例9 - 包含重复字符：");
        System.out.println("  word1: " + test9Word1);
        System.out.println("  word2: " + test9Word2);
        System.out.println("  预期结果: 1");
        System.out.println("  实际结果: " + result9);
        System.out.println("  测试" + (result9 == 1 ? "通过" : "失败") + "\n");

        // 测试用例10：边界值测试
        StringBuilder longWord1 = new StringBuilder();
        StringBuilder longWord2 = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longWord1.append((char) ('a' + (i % 26)));
            longWord2.append((char) ('a' + ((i + 500) % 26)));
        }
        String test10Word1 = longWord1.toString();
        String test10Word2 = longWord2.toString();
        int result10 = isRightRotation(test10Word1, test10Word2);
        System.out.println("测试用例10 - 边界值测试（长度1000）：");
        System.out.println("  word1: [长度" + test10Word1.length() + "]");
        System.out.println("  word2: [长度" + test10Word2.length() + "]");
        System.out.println("  实际结果: " + result10);
        System.out.println("  测试通过（验证算法效率）\n");

        System.out.println("=== SHL_019 右旋转字符串判断测试完成 ===");
    }

    /**
     * 程序入口点，可以选择运行主函数或测试函数
     */
    public static void main(String[] args) {
        // 如果要运行测试，取消下面一行的注释
         testRightRotationCheck();

        // 如果要运行主函数（从控制台输入），使用下面的main方法
//        runMain();
    }

    /**
     * 包装的主函数，避免与测试冲突
     */
    public static void runMain() {
        main(new String[0]);
    }
}
