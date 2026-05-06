package main.java.SHL;

import java.io.IOException;
import java.util.*;

public class SHL_009 {
  /*  题目ID: SHL_009

    英文题目 (English Description):

    Bob needs to send a secret code S to his boss. He designs a method to encrypt the code using two key values N and M. The formula that he uses to develop the encrypted code is shown below:

            (((S^N % 10)^M) % 1000000007)

    Write an algorithm to help Bob encrypt the code.

            中文翻译 (Chinese Description):

    Bob需要向他的老板发送一个秘密代码S。他设计了一种使用两个密钥值N和M来加密代码的方法。他用于生成加密代码的公式如下所示：

            (((S^N % 10)^M) % 1000000007)

    请编写一个算法来帮助Bob加密代码。

    输入格式 (Input Format):

    第一行：一个整数 secretCode，代表秘密代码 (S)。

    第二行：一个整数 firstKey，代表第一个密钥值 (N)。

    第三行：一个整数 secondKey，代表第二个密钥值 (M)。

    输出格式 (Output Format):

    输出一个整数，代表Bob加密后的代码。

    约束条件 (Constraints):

            1 ≤ secretCode ≤ 10⁹

            0 ≤ firstKey, secondKey ≤ 1000000007

    示例 (Example):

    复制
    输入 (Input):
            2
            3
            4

    输出 (Output):
            7

    示例解释 (Explanation):

    首先计算 S^N % 10= 2^3 % 10= 8 % 10= 8

    然后计算 8^M= 8^4= 4096

    最后计算 4096 % 1000000007= 4096

    注意：根据公式 (((S^N % 10)^M) % 1000000007)和给定的示例输入，计算过程应为 (((2^3 % 10)^4) % 1000000007)= ((8 % 10)^4) % 1000000007= (8^4) % 1000000007= 4096 % 1000000007= 4096。因此示例输出应为 4096。原题描述中的示例输出 7疑似有误或对应其他计算顺序。我们将严格按题目公式实现。*/


    // 快速幂模运算方法 (a^b mod m)
    private static long modPow(long base, long exponent, long modulus) {
        if (modulus == 1) return 0;
        long result = 1;
        base = base % modulus;

        while (exponent > 0) {
            // 如果当前指数位为1，将当前底数乘入结果
            if ((exponent & 1) == 1) {
                result = (result * base) % modulus;
            }
            // 底数平方，指数右移一位
            base = (base * base) % modulus;
            exponent >>= 1;
        }
        return result;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 读取输入
        long S = scanner.nextLong(); // 秘密代码
        long N = scanner.nextLong(); // 第一个密钥
        long M = scanner.nextLong(); // 第二个密钥

        // 第一步：计算 S^N % 10
        long step1 = modPow(S, N, 10);

        // 第二步：计算 (step1)^M % 1000000007
        long step2 = modPow(step1, M, 1000000007);

        // 输出结果
        System.out.println(step2);

        scanner.close();
    }



}
