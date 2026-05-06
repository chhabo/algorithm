package main.java.SHL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class SHL_021_TreeMaxLeafProductPath {


    /**
     * SHL_021: Maximum Product Path Between Two Leaves in a Tree
     * 树中叶子到叶子的最大乘积路径
     * <p>
     * 题目描述：
     * Arya is attempting to solve a math problem. In this problem, she is given a tree with N nodes,
     * indexed from 1 to N where the root node is indexed as 1. Each node of the tree has a defined value.
     * She wants to trace a path from one leaf to another leaf in such way that will award her the maximum
     * score for that path. The score of a path is defined as the product of node values along the path.
     * <p>
     * 中文翻译：
     * Arya正在尝试解决一个数学问题。在这个问题中，她得到一棵有N个节点的树，
     * 节点从1到N编号，根节点编号为1。树的每个节点都有一个定义的值。
     * 她想从一片叶子到另一片叶子追踪一条路径，以获得该路径的最大分数。
     * 路径的分数定义为路径上节点值的乘积。
     * <p>
     * 输入格式：
     * The first line of the input consists of an integer N, representing the number of nodes in the tree.
     * The second line consists of N space-separated integers representing the value of each node in the tree.
     * The next N-1 lines consist of two space-separated integers a and b, representing the indices of the
     * starting node and ending node of an edge of the tree.
     * <p>
     * 中文翻译：
     * 第一行：一个整数N，表示树中节点的数量
     * 第二行：N个用空格分隔的整数，表示树中每个节点的值
     * 接下来的N-1行：每行包含两个用空格分隔的整数a和b，表示树的一条边的起始节点和结束节点
     * <p>
     * 输出格式：
     * Print an integer representing the maximum possible score
     * <p>
     * 中文翻译：
     * 输出一个整数，表示最大可能的分数
     * <p>
     * 约束条件：
     * 1 ≤ N ≤ 10^4
     * -10^3 ≤ node value ≤ 10^3
     * <p>
     * 示例：
     * Input:
     * 4
     * -1 2 3 2
     * 1 2
     * 1 3
     * 3 4
     * <p>
     * Output:
     * -12
     * <p>
     * 示例解释：
     * 从叶子节点2到叶子节点4只有一条路径：2→1→3→4
     * 路径分数 = 2 * (-1) * 3 * 2 = -12
     */
/*
    private static List<Integer>[] tree;
    private static int[] values;
    private static long answer = Long.MIN_VALUE;

    *//**
     * DP类，存储从当前节点到叶子的最大和最小乘积路径
     *//*
    private static class DP {
        long max; // 从当前节点到叶子的最大乘积（包含当前节点）
        long min; // 从当前节点到叶子的最小乘积（包含当前节点）

        DP(long max, long min) {
            this.max = max;
            this.min = min;
        }
    }

    *//**
     * 深度优先搜索计算每个节点的DP值
     *//*
    private static DP dfs(int currentNode, int parentNode) {
        // 初始化当前节点的DP值
        long currentMax = values[currentNode];
        long currentMin = values[currentNode];

        // 用于合并子节点路径的变量
        Long childMax1 = null; // 子节点中最大值的最大值
        Long childMin1 = null; // 子节点中最大值的最大值
        Long childMax2 = null; // 子节点中最小值的最大值
        Long childMin2 = null; // 子节点中最小值的最大值

        // 遍历所有相邻节点
        for (int neighbor : tree[currentNode]) {
            if (neighbor == parentNode) continue;

            DP childDP = dfs(neighbor, currentNode);

            // 合并路径：计算经过当前节点的两条子路径的组合
            if (childMax1 != null) {
                // 8种可能的组合
                long[] candidates = {
                        childMax1 * values[currentNode] * childDP.max,
                        childMin1 * values[currentNode] * childDP.max,
                        childMax1 * values[currentNode] * childDP.min,
                        childMin1 * values[currentNode] * childDP.min,
                        childMax2 * values[currentNode] * childDP.max,
                        childMin2 * values[currentNode] * childDP.max,
                        childMax2 * values[currentNode] * childDP.min,
                        childMin2 * values[currentNode] * childDP.min
                };

                // 更新全局答案
                for (long candidate : candidates) {
                    answer = Math.max(answer, candidate);
                }
            }

            // 更新合并变量
            if (childMax1 == null) {
                childMax1 = childDP.max;
                childMin1 = childDP.max;
                childMax2 = childDP.min;
                childMin2 = childDP.min;
            } else {
                childMax1 = Math.max(childMax1, childDP.max);
                childMin1 = Math.min(childMin1, childDP.max);
                childMax2 = Math.max(childMax2, childDP.min);
                childMin2 = Math.min(childMin2, childDP.min);
            }

            // 更新当前节点的DP值
            currentMax = Math.max(currentMax, (long) values[currentNode] * childDP.max);
            currentMax = Math.max(currentMax, (long) values[currentNode] * childDP.min);
            currentMin = Math.min(currentMin, (long) values[currentNode] * childDP.max);
            currentMin = Math.min(currentMin, (long) values[currentNode] * childDP.min);
        }

        return new DP(currentMax, currentMin);
    }

    *//**
     * 计算树中叶子到叶子的最大乘积路径
     *//*
    public static long calculateMaxLeafProductPath(int n, int[] nodeValues, int[][] edges) {
        // 初始化全局变量
        answer = Long.MIN_VALUE;
        values = new int[n + 1];
        tree = new ArrayList[n + 1];

        // 复制节点值
        for (int i = 1; i <= n; i++) {
            values[i] = nodeValues[i - 1];
            tree[i] = new ArrayList<>();
        }

        // 构建树
        for (int[] edge : edges) {
            int a = edge[0];
            int b = edge[1];
            tree[a].add(b);
            tree[b].add(a);
        }

        // 特殊处理：只有两个节点的情况
        if (n == 2) {
            return (long) values[1] * values[2];
        }

        // 从根节点开始DFS
        dfs(1, 0);

        return answer;
    }

    *//**
     * 主函数，处理标准输入输出
     *//*
    public static void main1(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // 读取节点数量
        int N = Integer.parseInt(br.readLine());

        // 读取节点值
        int[] nodeValues = new int[N];
        String[] valueStrs = br.readLine().split(" ");
        for (int i = 0; i < N; i++) {
            nodeValues[i] = Integer.parseInt(valueStrs[i]);
        }

        // 读取边
        int[][] edges = new int[N - 1][2];
        for (int i = 0; i < N - 1; i++) {
            String[] edgeStrs = br.readLine().split(" ");
            edges[i][0] = Integer.parseInt(edgeStrs[0]);
            edges[i][1] = Integer.parseInt(edgeStrs[1]);
        }

        // 计算并输出结果
        long result = calculateMaxLeafProductPath(N, nodeValues, edges);
        System.out.println(result);
    }

    *//**
     * 测试函数
     *//*
    public static void testTreeMaxLeafProductPath() {
        System.out.println("=== SHL_021 树中叶子到叶子的最大乘积路径测试开始 ===");

        // 测试用例1：题目示例
        int n1 = 4;
        int[] values1 = {-1, 2, 3, 2};
        int[][] edges1 = {{1, 2}, {1, 3}, {3, 4}};
        long result1 = calculateMaxLeafProductPath(n1, values1, edges1);
        System.out.println("测试用例1 - 题目示例: " + (result1 == -12 ? "通过" : "失败"));

        // 测试用例2：所有节点值都为正
        int n2 = 5;
        int[] values2 = {2, 3, 4, 1, 5};
        int[][] edges2 = {{1, 2}, {1, 3}, {2, 4}, {2, 5}};
        long result2 = calculateMaxLeafProductPath(n2, values2, edges2);
        // 路径: 4->2->1->3 = 2*3*4*1 = 24
        // 路径: 5->2->1->3 = 2*3*4*5 = 120
        System.out.println("测试用例2 - 所有节点值都为正: " + (result2 == 120 ? "通过" : "失败"));

        // 测试用例3：包含负值
        int n3 = 6;
        int[] values3 = {-2, 3, -4, 5, -1, 2};
        int[][] edges3 = {{1, 2}, {1, 3}, {2, 4}, {2, 5}, {3, 6}};
        long result3 = calculateMaxLeafProductPath(n3, values3, edges3);
        System.out.println("测试用例3 - 包含负值: 结果 = " + result3);

        // 测试用例4：只有两个节点
        int n4 = 2;
        int[] values4 = {-3, 4};
        int[][] edges4 = {{1, 2}};
        long result4 = calculateMaxLeafProductPath(n4, values4, edges4);
        System.out.println("测试用例4 - 只有两个节点: " + (result4 == -12 ? "通过" : "失败"));

        // 测试用例5：星形树
        int n5 = 5;
        int[] values5 = {1, -2, 3, -4, 5};
        int[][] edges5 = {{1, 2}, {1, 3}, {1, 4}, {1, 5}};
        long result5 = calculateMaxLeafProductPath(n5, values5, edges5);
        // 叶子到叶子路径: 2->1->3 = 1*(-2)*3 = -6
        // 叶子到叶子路径: 2->1->4 = 1*(-2)*(-4) = 8
        // 叶子到叶子路径: 2->1->5 = 1*(-2)*5 = -10
        // 叶子到叶子路径: 3->1->4 = 1*3*(-4) = -12
        // 叶子到叶子路径: 3->1->5 = 1*3*5 = 15
        // 叶子到叶子路径: 4->1->5 = 1*(-4)*5 = -20
        System.out.println("测试用例5 - 星形树: " + (result5 == 15 ? "通过" : "失败"));

        // 测试用例6：链式树
        int n6 = 4;
        int[] values6 = {-1, -2, -3, -4};
        int[][] edges6 = {{1, 2}, {2, 3}, {3, 4}};
        long result6 = calculateMaxLeafProductPath(n6, values6, edges6);
        // 唯一路径: 1->2->3->4 = (-1)*(-2)*(-3)*(-4) = 24
        System.out.println("测试用例6 - 链式树: " + (result6 == 24 ? "通过" : "失败"));

        // 测试用例7：包含零值
        int n7 = 5;
        int[] values7 = {2, 0, -3, 4, 1};
        int[][] edges7 = {{1, 2}, {1, 3}, {2, 4}, {2, 5}};
        long result7 = calculateMaxLeafProductPath(n7, values7, edges7);
        System.out.println("测试用例7 - 包含零值: 结果 = " + result7);

        System.out.println("=== SHL_021 测试完成 ===");
    }

    *//**
     * 程序入口
     *//*
    public static void main(String[] args) {
        // 运行测试函数
        testTreeMaxLeafProductPath();

        // 如果要运行主函数，取消下面的注释
        // runMain();
    }

    *//**
     * 运行主函数
     *//*
    public static void runMain() {
        main(new String[0]);
    }*/


    /**
     * 树节点类
     */
    static class TreeNode {
        int id;                 // 节点ID
        int value;              // 节点值
        List<TreeNode> children; // 子节点列表
        TreeNode parent;        // 父节点（可选，用于构建树）

        public TreeNode(int id, int value) {
            this.id = id;
            this.value = value;
            this.children = new ArrayList<>();
            this.parent = null;
        }

        public void addChild(TreeNode child) {
            child.parent = this;
            this.children.add(child);
        }

        @Override
        public String toString() {
            return "TreeNode{id=" + id + ", value=" + value +
                    ", children=" + children.size() + "}";
        }
    }

    /**
     * 树类
     */
    static class NodeTree {
        private int nodeCount;               // 节点数量
        private TreeNode root;               // 根节点
        private TreeNode[] nodes;            // 节点数组
        private long maxProduct;             // 最大乘积

        /**
         * 构造函数
         */
        public NodeTree(int nodeCount, int[] values) {
            this.nodeCount = nodeCount;
            this.nodes = new TreeNode[nodeCount + 1];
            this.maxProduct = Long.MIN_VALUE;

            // 创建节点
            for (int i = 1; i <= nodeCount; i++) {
                nodes[i] = new TreeNode(i, values[i-1]);
            }

            // 设置根节点为1
            this.root = nodes[1];
        }

        /**
         * 添加边
         */
        public void addEdge(int a, int b) {
            // 注意：这里只是记录邻接关系，不建立父子关系
            // 父子关系在buildTree方法中建立
        }

        /**
         * 构建树（从根节点开始，建立父子关系）
         */
        public void buildTreeFromEdges(List<int[]> edges) {
            // 构建邻接表
            List<Integer>[] adj = new ArrayList[nodeCount + 1];
            for (int i = 1; i <= nodeCount; i++) {
                adj[i] = new ArrayList<>();
            }

            for (int[] edge : edges) {
                int a = edge[0];
                int b = edge[1];
                adj[a].add(b);
                adj[b].add(a);
            }

            // 从根节点开始构建树
            boolean[] visited = new boolean[nodeCount + 1];
            buildTreeDFS(root, adj, visited);
        }

        /**
         * DFS构建树
         */
        private void buildTreeDFS(TreeNode currentNode, List<Integer>[] adj, boolean[] visited) {
            visited[currentNode.id] = true;

            for (int neighborId : adj[currentNode.id]) {
                if (!visited[neighborId]) {
                    TreeNode neighborNode = nodes[neighborId];
                    currentNode.addChild(neighborNode);
                    buildTreeDFS(neighborNode, adj, visited);
                }
            }
        }

        /**
         * DP结果类
         */
        private static class DPResult {
            long max;  // 从当前节点到叶子的最大乘积（包含当前节点）
            long min;  // 从当前节点到叶子的最小乘积（包含当前节点）

            DPResult(long max, long min) {
                this.max = max;
                this.min = min;
            }
        }

        /**
         * 计算最大乘积路径
         */
        public long calculateMaxProductPath() {
            maxProduct = Long.MIN_VALUE;
            dfs(root);
            return maxProduct;
        }

        /**
         * DFS计算每个节点的DP值
         */
        private DPResult dfs(TreeNode node) {
            // 初始化当前节点的DP值
            long currentMax = node.value;
            long currentMin = node.value;

            // 用于合并子节点路径的变量
            Long childMax1 = null; // 子节点中最大值的最大值
            Long childMin1 = null; // 子节点中最大值的最大值
            Long childMax2 = null; // 子节点中最小值的最大值
            Long childMin2 = null; // 子节点中最小值的最大值

            // 遍历所有子节点
            for (TreeNode child : node.children) {
                DPResult childResult = dfs(child);

                // 合并路径：计算经过当前节点的两条子路径的组合
                if (childMax1 != null) {
                    // 8种可能的组合
                    long[] candidates = {
                            childMax1 * node.value * childResult.max,
                            childMin1 * node.value * childResult.max,
                            childMax1 * node.value * childResult.min,
                            childMin1 * node.value * childResult.min,
                            childMax2 * node.value * childResult.max,
                            childMin2 * node.value * childResult.max,
                            childMax2 * node.value * childResult.min,
                            childMin2 * node.value * childResult.min
                    };

                    // 更新全局最大乘积
                    for (long candidate : candidates) {
                        maxProduct = Math.max(maxProduct, candidate);
                    }
                }

                // 更新合并变量
                if (childMax1 == null) {
                    childMax1 = childResult.max;
                    childMin1 = childResult.max;
                    childMax2 = childResult.min;
                    childMin2 = childResult.min;
                } else {
                    childMax1 = Math.max(childMax1, childResult.max);
                    childMin1 = Math.min(childMin1, childResult.max);
                    childMax2 = Math.max(childMax2, childResult.min);
                    childMin2 = Math.min(childMin2, childResult.min);
                }

                // 更新当前节点的DP值
                currentMax = Math.max(currentMax, (long)node.value * childResult.max);
                currentMax = Math.max(currentMax, (long)node.value * childResult.min);
                currentMin = Math.min(currentMin, (long)node.value * childResult.max);
                currentMin = Math.min(currentMin, (long)node.value * childResult.min);
            }

            // 如果是叶子节点，更新全局最大乘积
            if (node.children.isEmpty()) {
                maxProduct = Math.max(maxProduct, node.value);
            }

            return new DPResult(currentMax, currentMin);
        }

        /**
         * 获取根节点
         */
        public TreeNode getRoot() {
            return root;
        }

        /**
         * 获取节点数量
         */
        public int getNodeCount() {
            return nodeCount;
        }

        /**
         * 获取指定ID的节点
         */
        public TreeNode getNode(int id) {
            if (id < 1 || id > nodeCount) {
                throw new IllegalArgumentException("Invalid node ID: " + id);
            }
            return nodes[id];
        }
    }

    /**
     * 计算树中叶子到叶子的最大乘积路径
     */
    public static long calculateMaxLeafProductPath(int n, int[] nodeValues, int[][] edges) {
        // 创建树
        NodeTree tree = new NodeTree(n, nodeValues);

        // 构建树
        List<int[]> edgeList = new ArrayList<>();
        for (int[] edge : edges) {
            edgeList.add(edge);
        }
        tree.buildTreeFromEdges(edgeList);

        // 计算最大乘积路径
        return tree.calculateMaxProductPath();
    }

    /**
     * 主函数，处理标准输入输出
     */
    public static void main1(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // 读取节点数量
        int N = Integer.parseInt(br.readLine());

        // 读取节点值
        int[] nodeValues = new int[N];
        String[] valueStrs = br.readLine().split(" ");
        for (int i = 0; i < N; i++) {
            nodeValues[i] = Integer.parseInt(valueStrs[i]);
        }

        // 读取边
        int[][] edges = new int[N - 1][2];
        for (int i = 0; i < N - 1; i++) {
            String[] edgeStrs = br.readLine().split(" ");
            edges[i][0] = Integer.parseInt(edgeStrs[0]);
            edges[i][1] = Integer.parseInt(edgeStrs[1]);
        }

        // 计算并输出结果
        long result = calculateMaxLeafProductPath(N, nodeValues, edges);
        System.out.println(result);
    }

    /**
     * 测试函数
     */
    public static void testTreeMaxLeafProductPath() {
        System.out.println("=== SHL_021 树中叶子到叶子的最大乘积路径测试（面向对象版本）===");

        // 测试用例1：题目示例
        int n1 = 4;
        int[] values1 = {-1, 2, 3, 2};
        int[][] edges1 = {{1, 2}, {1, 3}, {3, 4}};
        long result1 = calculateMaxLeafProductPath(n1, values1, edges1);
        System.out.println("测试用例1 - 题目示例: " + (result1 == -12 ? "通过" : "失败"));

        // 测试用例2：所有节点值都为正
        int n2 = 5;
        int[] values2 = {2, 3, 4, 1, 5};
        int[][] edges2 = {{1, 2}, {1, 3}, {2, 4}, {2, 5}};
        long result2 = calculateMaxLeafProductPath(n2, values2, edges2);
        System.out.println("测试用例2 - 所有节点值都为正: " + (result2 == 120 ? "通过" : "失败"));

        // 测试用例3：只有两个节点
        int n3 = 2;
        int[] values3 = {-3, 4};
        int[][] edges3 = {{1, 2}};
        long result3 = calculateMaxLeafProductPath(n3, values3, edges3);
        System.out.println("测试用例3 - 只有两个节点: " + (result3 == -12 ? "通过" : "失败"));

        // 测试用例4：星形树
        int n4 = 5;
        int[] values4 = {1, -2, 3, -4, 5};
        int[][] edges4 = {{1, 2}, {1, 3}, {1, 4}, {1, 5}};
        long result4 = calculateMaxLeafProductPath(n4, values4, edges4);
        System.out.println("测试用例4 - 星形树: " + (result4 == 15 ? "通过" : "失败"));

        // 测试用例5：链式树
        int n5 = 4;
        int[] values5 = {-1, -2, -3, -4};
        int[][] edges5 = {{1, 2}, {2, 3}, {3, 4}};
        long result5 = calculateMaxLeafProductPath(n5, values5, edges5);
        System.out.println("测试用例5 - 链式树: " + (result5 == 24 ? "通过" : "失败"));

        // 测试用例6：包含零值
        int n6 = 5;
        int[] values6 = {2, 0, -3, 4, 1};
        int[][] edges6 = {{1, 2}, {1, 3}, {2, 4}, {2, 5}};
        long result6 = calculateMaxLeafProductPath(n6, values6, edges6);
        System.out.println("测试用例6 - 包含零值: 结果 = " + result6);

        // 测试用例7：复杂情况
        int n7 = 6;
        int[] values7 = {-2, 3, -4, 5, -1, 2};
        int[][] edges7 = {{1, 2}, {1, 3}, {2, 4}, {2, 5}, {3, 6}};
        long result7 = calculateMaxLeafProductPath(n7, values7, edges7);
        System.out.println("测试用例7 - 复杂情况: 结果 = " + result7);

        // 测试用例8：单个节点（特殊情况）
        int n8 = 1;
        int[] values8 = {5};
        int[][] edges8 = {};
        try {
            long result8 = calculateMaxLeafProductPath(n8, values8, edges8);
            System.out.println("测试用例8 - 单个节点: 结果 = " + result8);
        } catch (Exception e) {
            System.out.println("测试用例8 - 单个节点: 异常（预期内，因为至少需要两个节点）");
        }

        System.out.println("=== SHL_021 测试完成 ===");
    }

    /**
     * 程序入口
     */
    public static void main3(String[] args) {
        // 运行测试函数
        testTreeMaxLeafProductPath();

        // 如果要运行主函数，取消下面的注释
        // runMain();
    }



    //======================================================================================================================


    private static List<Integer>[] graph;
    private static int[] values;
    private static long answer = Long.MIN_VALUE;

    /**
     * 主函数
     */
    public static void main4(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int N = Integer.parseInt(br.readLine());

        // 读取节点值
        values = new int[N + 1];
        String[] valStr = br.readLine().split(" ");
        for (int i = 1; i <= N; i++) {
            values[i] = Integer.parseInt(valStr[i - 1]);
        }

        // 构建邻接表
        graph = new ArrayList[N + 1];
        for (int i = 1; i <= N; i++) {
            graph[i] = new ArrayList<>();
        }

        for (int i = 0; i < N - 1; i++) {
            String[] edge = br.readLine().split(" ");
            int a = Integer.parseInt(edge[0]);
            int b = Integer.parseInt(edge[1]);
            graph[a].add(b);
            graph[b].add(a);
        }

        // 特殊处理N=2的情况
        if (N == 2) {
            long product = (long)values[1] * values[2];
            System.out.println(product);
            return;
        }

        // 执行DFS计算
        dfs(1, 0);

        // 如果answer仍然是Long.MIN_VALUE，说明树只有一个叶子节点
        // 这种情况应该不会发生，因为至少有两个叶子节点
        if (answer == Long.MIN_VALUE) {
            // 如果没有找到有效的叶子到叶子路径，返回0
            System.out.println(0);
        } else {
            System.out.println(answer);
        }
    }

    /**
     * DP结果类
     */
    static class DPResult {
        long maxPath;  // 从当前节点到其子树中某个叶子的最大乘积路径
        long minPath;  // 从当前节点到其子树中某个叶子的最小乘积路径

        DPResult(long maxPath, long minPath) {
            this.maxPath = maxPath;
            this.minPath = minPath;
        }
    }

    /**
     * DFS计算每个节点的DP值
     * 返回从当前节点到其子树中某个叶子的最大乘积路径和最小乘积路径
     */
    private static DPResult dfs(int u, int parent) {
        // 如果是叶子节点（除了根节点外，只有一个邻居且这个邻居是父节点）
        boolean isLeaf = (parent != 0 && graph[u].size() == 1);

        if (isLeaf) {
            return new DPResult(values[u], values[u]);
        }

        // 存储子节点的DP结果
        List<DPResult> childResults = new ArrayList<>();
        List<Integer> children = new ArrayList<>();

        for (int v : graph[u]) {
            if (v != parent) {
                children.add(v);
                childResults.add(dfs(v, u));
            }
        }

        // 如果没有子节点（当前节点可能是孤立的叶子节点，但这种情况已经被上面的isLeaf处理）
        if (children.isEmpty()) {
            return new DPResult(values[u], values[u]);
        }

        // 更新全局答案：考虑通过当前节点的两条子路径组合
        int childCount = children.size();

        if (childCount >= 2) {
            // 我们需要考虑所有可能的子节点对
            for (int i = 0; i < childCount; i++) {
                for (int j = i + 1; j < childCount; j++) {
                    DPResult ci = childResults.get(i);
                    DPResult cj = childResults.get(j);

                    // 四种组合
                    long candidate1 = ci.maxPath * values[u] * cj.maxPath;
                    long candidate2 = ci.maxPath * values[u] * cj.minPath;
                    long candidate3 = ci.minPath * values[u] * cj.maxPath;
                    long candidate4 = ci.minPath * values[u] * cj.minPath;

                    answer = Math.max(answer, candidate1);
                    answer = Math.max(answer, candidate2);
                    answer = Math.max(answer, candidate3);
                    answer = Math.max(answer, candidate4);
                }
            }
        }

        // 计算当前节点的DP值
        long maxPath = values[u];
        long minPath = values[u];

        for (DPResult child : childResults) {
            // 考虑从当前节点到子节点子树中的叶子的路径
            long candidateMax1 = (long)values[u] * child.maxPath;
            long candidateMax2 = (long)values[u] * child.minPath;
            long candidateMin1 = (long)values[u] * child.maxPath;
            long candidateMin2 = (long)values[u] * child.minPath;

            maxPath = Math.max(maxPath, candidateMax1);
            maxPath = Math.max(maxPath, candidateMax2);
            minPath = Math.min(minPath, candidateMin1);
            minPath = Math.min(minPath, candidateMin2);
        }

        return new DPResult(maxPath, minPath);
    }

    /**
     * 测试函数
     */
    public static void test() {
        System.out.println("=== SHL_021 树中叶子到叶子的最大乘积路径测试开始 ===\n");

        // 保存原始静态变量
        List<Integer>[] origGraph = graph;
        int[] origValues = values;
        long origAnswer = answer;

        // 测试用例1：题目示例
        System.out.println("测试用例1 - 题目示例：");
        int n1 = 4;
        int[] values1 = {-1, 2, 3, 2};
        List<int[]> edges1 = Arrays.asList(
                new int[]{1, 2},
                new int[]{1, 3},
                new int[]{3, 4}
        );

        // 重置静态变量
        graph = new ArrayList[n1 + 1];
        for (int i = 1; i <= n1; i++) graph[i] = new ArrayList<>();
        values = values1;
        answer = Long.MIN_VALUE;

        // 构建图
        for (int[] edge : edges1) {
            int a = edge[0];
            int b = edge[1];
            graph[a].add(b);
            graph[b].add(a);
        }

        // 计算
        if (n1 == 2) {
            long result1 = (long)values[1] * values[2];
            System.out.println("  期望: -12, 实际: " + result1 + " -> " + (result1 == -12 ? "通过" : "失败"));
        } else {
            dfs(1, 0);
            if (answer == Long.MIN_VALUE) {
                answer = 0;
            }
            System.out.println("  期望: -12, 实际: " + answer + " -> " + (answer == -12 ? "通过" : "失败"));
        }
        System.out.println();

        // 测试用例2：所有节点值都为正
        System.out.println("测试用例2 - 所有节点值都为正：");
        int n2 = 5;
        int[] values2 = {2, 3, 4, 1, 5};
        List<int[]> edges2 = Arrays.asList(
                new int[]{1, 2},
                new int[]{1, 3},
                new int[]{2, 4},
                new int[]{2, 5}
        );

        // 重置静态变量
        graph = new ArrayList[n2 + 1];
        for (int i = 1; i <= n2; i++) graph[i] = new ArrayList<>();
        values = values2;
        answer = Long.MIN_VALUE;

        // 构建图
        for (int[] edge : edges2) {
            int a = edge[0];
            int b = edge[1];
            graph[a].add(b);
            graph[b].add(a);
        }

        // 计算
        if (n2 == 2) {
            long result2 = (long)values[1] * values[2];
            System.out.println("  期望: 120, 实际: " + result2 + " -> " + (result2 == 120 ? "通过" : "失败"));
        } else {
            dfs(1, 0);
            if (answer == Long.MIN_VALUE) {
                answer = 0;
            }
            // 路径: 5->2->1->3 = 5*3*2*4 = 120
            System.out.println("  期望: 120, 实际: " + answer + " -> " + (answer == 120 ? "通过" : "失败"));
        }
        System.out.println();

        // 测试用例3：只有两个节点
        System.out.println("测试用例3 - 只有两个节点：");
        int n3 = 2;
        int[] values3 = {-3, 4};
        List<int[]> edges3 = Arrays.asList(
                new int[]{1, 2}
        );

        // 重置静态变量
        graph = new ArrayList[n3 + 1];
        for (int i = 1; i <= n3; i++) graph[i] = new ArrayList<>();
        values = values3;
        answer = Long.MIN_VALUE;

        // 构建图
        for (int[] edge : edges3) {
            int a = edge[0];
            int b = edge[1];
            graph[a].add(b);
            graph[b].add(a);
        }

        // 计算
        if (n3 == 2) {
            long result3 = (long)values[1] * values[2];
            System.out.println("  期望: -12, 实际: " + result3 + " -> " + (result3 == -12 ? "通过" : "失败"));
        } else {
            dfs(1, 0);
            if (answer == Long.MIN_VALUE) {
                answer = 0;
            }
            System.out.println("  期望: -12, 实际: " + answer + " -> " + (answer == -12 ? "通过" : "失败"));
        }
        System.out.println();

        // 测试用例4：星形树
        System.out.println("测试用例4 - 星形树：");
        int n4 = 5;
        int[] values4 = {1, -2, 3, -4, 5};
        List<int[]> edges4 = Arrays.asList(
                new int[]{1, 2},
                new int[]{1, 3},
                new int[]{1, 4},
                new int[]{1, 5}
        );

        // 重置静态变量
        graph = new ArrayList[n4 + 1];
        for (int i = 1; i <= n4; i++) graph[i] = new ArrayList<>();
        values = values4;
        answer = Long.MIN_VALUE;

        // 构建图
        for (int[] edge : edges4) {
            int a = edge[0];
            int b = edge[1];
            graph[a].add(b);
            graph[b].add(a);
        }

        // 计算
        if (n4 == 2) {
            long result4 = (long)values[1] * values[2];
            System.out.println("  期望: 15, 实际: " + result4 + " -> " + (result4 == 15 ? "通过" : "失败"));
        } else {
            dfs(1, 0);
            if (answer == Long.MIN_VALUE) {
                answer = 0;
            }
            // 路径: 3->1->5 = 3*1*5 = 15
            System.out.println("  期望: 15, 实际: " + answer + " -> " + (answer == 15 ? "通过" : "失败"));
        }
        System.out.println();

        // 测试用例5：链式树
        System.out.println("测试用例5 - 链式树：");
        int n5 = 4;
        int[] values5 = {-1, -2, -3, -4};
        List<int[]> edges5 = Arrays.asList(
                new int[]{1, 2},
                new int[]{2, 3},
                new int[]{3, 4}
        );

        // 重置静态变量
        graph = new ArrayList[n5 + 1];
        for (int i = 1; i <= n5; i++) graph[i] = new ArrayList<>();
        values = values5;
        answer = Long.MIN_VALUE;

        // 构建图
        for (int[] edge : edges5) {
            int a = edge[0];
            int b = edge[1];
            graph[a].add(b);
            graph[b].add(a);
        }

        // 计算
        if (n5 == 2) {
            long result5 = (long)values[1] * values[2];
            System.out.println("  期望: 24, 实际: " + result5 + " -> " + (result5 == 24 ? "通过" : "失败"));
        } else {
            dfs(1, 0);
            if (answer == Long.MIN_VALUE) {
                answer = 0;
            }
            // 唯一路径: 1->2->3->4 = (-1)*(-2)*(-3)*(-4) = 24
            System.out.println("  期望: 24, 实际: " + answer + " -> " + (answer == 24 ? "通过" : "失败"));
        }
        System.out.println();

        // 测试用例6：包含零值
        System.out.println("测试用例6 - 包含零值：");
        int n6 = 5;
        int[] values6 = {2, 0, -3, 4, 1};
        List<int[]> edges6 = Arrays.asList(
                new int[]{1, 2},
                new int[]{1, 3},
                new int[]{2, 4},
                new int[]{2, 5}
        );

        // 重置静态变量
        graph = new ArrayList[n6 + 1];
        for (int i = 1; i <= n6; i++) graph[i] = new ArrayList<>();
        values = values6;
        answer = Long.MIN_VALUE;

        // 构建图
        for (int[] edge : edges6) {
            int a = edge[0];
            int b = edge[1];
            graph[a].add(b);
            graph[b].add(a);
        }

        // 计算
        if (n6 == 2) {
            long result6 = (long)values[1] * values[2];
            System.out.println("  期望: 0, 实际: " + result6 + " -> " + (result6 == 0 ? "通过" : "失败"));
        } else {
            dfs(1, 0);
            if (answer == Long.MIN_VALUE) {
                answer = 0;
            }
            // 路径中包含0，乘积为0，但可能有正值路径
            System.out.println("  实际结果: " + answer);
        }
        System.out.println();

        // 恢复静态变量
        graph = origGraph;
        values = origValues;
        answer = origAnswer;

        System.out.println("=== SHL_021 测试完成 ===");
    }

    /**
     * 程序入口
     */
    public static void main(String[] args) {
        // 运行测试
        test();

        // 如果要运行主函数，取消下面的注释
        // runMain();
    }

}
