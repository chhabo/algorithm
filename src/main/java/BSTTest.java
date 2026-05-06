package main.java;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;


import java.util.*;

/**
 * 二叉搜索树中第K小的元素 - 完整测试程序
 */
public class BSTTest {

    // 树节点定义
    static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;
        TreeNode() {}
        TreeNode(int val) { this.val = val; }
        TreeNode(int val, TreeNode left, TreeNode right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }
    }

    // Solution类,包含kthSmallest方法
    static class Solution {
        /**
         * 非递归中序遍历查找第k小元素
         */
        public int kthSmallest(TreeNode root, int k) {
            Deque<TreeNode> stack = new ArrayDeque<TreeNode>();
            while (root != null || !stack.isEmpty()) {
                while (root != null) {
                    stack.push(root);
                    root = root.left;
                }
                root = stack.pop();
                --k;
                if (k == 0) {
                    break;
                }
                root = root.right;
            }
            return root.val;
        }

        /**
         * 递归版本（备选方案）
         */
        private int count = 0;
        private int result = 0;

        public int kthSmallestRecursive(TreeNode root, int k) {
            count = k;
            inorderTraversal(root);
            return result;
        }

        private void inorderTraversal(TreeNode node) {
            if (node == null) return;

            inorderTraversal(node.left);

            count--;
            if (count == 0) {
                result = node.val;
                return;
            }

            inorderTraversal(node.right);
        }
    }

    /**
     * 辅助方法：根据数组构建二叉树
     * 数组表示：层次遍历,null表示空节点
     */
    public static TreeNode buildTree(Integer[] arr) {
        if (arr == null || arr.length == 0 || arr[0] == null) {
            return null;
        }

        TreeNode root = new TreeNode(arr[0]);
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        int i = 1;
        while (!queue.isEmpty() && i < arr.length) {
            TreeNode current = queue.poll();

            // 处理左子节点
            if (i < arr.length && arr[i] != null) {
                current.left = new TreeNode(arr[i]);
                queue.offer(current.left);
            }
            i++;

            // 处理右子节点
            if (i < arr.length && arr[i] != null) {
                current.right = new TreeNode(arr[i]);
                queue.offer(current.right);
            }
            i++;
        }

        return root;
    }

    /**
     * 中序遍历二叉树,用于验证树结构
     */
    public static void inorderTraversal(TreeNode root) {
        if (root == null) return;
        inorderTraversal(root.left);
        System.out.print(root.val + " ");
        inorderTraversal(root.right);
    }

    /**
     * 主方法：运行测试用例
     */
    /**
     * 主方法：运行测试用例
     */
    public static void main(String[] args) {
        System.out.println("=== 二叉搜索树中第K小的元素测试 ===\n");
        Solution solution = new Solution();

        // 测试用例1：题目示例
        System.out.println("测试用例1: root = [3,1,4,null,2], k = 1");
        Integer[] arr1 = {3, 1, 4, null, 2};
        TreeNode root1 = buildTree(arr1);
        System.out.print("中序遍历结果: ");
        inorderTraversal(root1);
        System.out.println("\n期望输出: 1");
        System.out.println("实际输出: " + solution.kthSmallest(root1, 1));
        System.out.println("---");

        // 测试用例2：题目示例
        System.out.println("\n测试用例2: root = [5,3,6,2,4,null,null,1], k = 3");
        Integer[] arr2 = {5, 3, 6, 2, 4, null, null, 1};
        TreeNode root2 = buildTree(arr2);
        System.out.print("中序遍历结果: ");
        inorderTraversal(root2);
        System.out.println("\n期望输出: 3");
        System.out.println("实际输出: " + solution.kthSmallest(root2, 3));
        System.out.println("---");

        // 测试用例3：单节点树
        System.out.println("\n测试用例3: root = [10], k = 1");
        Integer[] arr3 = {10};
        TreeNode root3 = buildTree(arr3);
        System.out.println("期望输出: 10");
        System.out.println("实际输出: " + solution.kthSmallest(root3, 1));
        System.out.println("---");

        // 测试用例4：完全左斜树
        System.out.println("\n测试用例4: 左斜树, root = [5,4,null,3,null,2,null,1], k = 2");
        Integer[] arr4 = {5, 4, null, 3, null, 2, null, 1};
        TreeNode root4 = buildTree(arr4);
        System.out.print("中序遍历结果: ");
        inorderTraversal(root4);
        System.out.println("\n期望输出: 2");
        System.out.println("实际输出: " + solution.kthSmallest(root4, 2));
        System.out.println("---");

        // 测试用例5：完全右斜树
        System.out.println("\n测试用例5: 右斜树, root = [1,null,2,null,3,null,4], k = 3");
        Integer[] arr5 = {1, null, 2, null, 3, null, 4};
        TreeNode root5 = buildTree(arr5);
        System.out.print("中序遍历结果: ");
        inorderTraversal(root5);
        System.out.println("\n期望输出: 3");
        System.out.println("实际输出: " + solution.kthSmallest(root5, 3));
        System.out.println("---");

        // 测试用例6：边界测试 - k等于节点总数
        System.out.println("\n测试用例6: root = [5,3,6,2,4,null,null,1], k = 6 (最大值)");
        System.out.println("期望输出: 6");
        System.out.println("实际输出: " + solution.kthSmallest(root2, 6));
        System.out.println("---");

        // 测试递归版本
        System.out.println("\n=== 递归版本测试 ===");
        System.out.println("测试用例1: root = [3,1,4,null,2], k = 1");
        System.out.println("期望输出: 1");
        System.out.println("实际输出: " + solution.kthSmallestRecursive(root1, 1));
        System.out.println("---");

        System.out.println("\n测试用例2: root = [5,3,6,2,4,null,null,1], k = 3");
        System.out.println("期望输出: 3");
        System.out.println("实际输出: " + solution.kthSmallestRecursive(root2, 3));
        System.out.println("---");

        // 测试调试功能：查看栈的变化
        System.out.println("\n=== 栈变化调试（k=3的步骤）===");
        System.out.println("树结构: [5,3,6,2,4,null,null,1]");
        System.out.println("中序遍历: 1 2 3 4 5 6");
        System.out.println("查找第3小的元素:");


        Integer[] arr = {1,2,3,4,5,6,7,8,9};

        // 手动模拟栈变化
        TreeNode debugRoot = buildTree(arr);
        int debugK = 3;
        Deque<TreeNode> stack = new ArrayDeque<>();

        
        List<Integer> list = new ArrayList<>();
        System.out.println("\n步骤演示:");
        int step = 1;
        while (debugRoot != null || !stack.isEmpty()) {
            System.out.println("\n步骤" + step + ":");
//Integer[] arr2 = {5, 3, 6, 2, 4, null, null, 1};
            // 深入最左路径
            while (debugRoot != null) {
                stack.push(debugRoot);
                System.out.println("  将节点 " + debugRoot.val + " 压入栈中");
                debugRoot = debugRoot.left;
            }

            // 弹出栈顶节点
            debugRoot = stack.pop();
            System.out.println("  弹出节点 " + debugRoot.val + " (栈顶)");
            list.add(debugRoot.val);
            debugK--;
            System.out.println("  k减1,当前k=" + debugK);

//            if (debugK == 0) {
//                System.out.println("  k=0,找到第3小的元素: " + debugRoot.val);
//                break;
//            }

            debugRoot = debugRoot.right;
            if (debugRoot != null) {
                System.out.println("  转向右子节点: " + debugRoot.val);
            } else {
                System.out.println("  右子节点为空,继续弹出栈顶");
            }

            step++;
        }
        System.out.println(list);
    }

}