# karaat 12 道高频编程题（Java 版）

## 1. Subdomain Visit Count（LeetCode 811 · Medium）
### 题目
一个计数域名格式如 `"900 mail.google.com"`，表示该域名被访问900次。访问子域名时，其所有上级域名也会被统计访问量。
给定一组计数域名，返回所有子域名及其总访问次数。

### 示例
```
输入: ["900 discuss.leetcode.com"]
输出: ["900 discuss.leetcode.com", "900 leetcode.com", "900 com"]
```

### 题解
```java
import java.util.*;

class Solution {
    public List<String> subdomainVisits(String[] cpdomains) {
        Map<String, Integer> countMap = new HashMap<>();
        for (String cp : cpdomains) {
            int spaceIdx = cp.indexOf(' ');
            int count = Integer.parseInt(cp.substring(0, spaceIdx));
            String domain = cp.substring(spaceIdx + 1);
            
            countMap.put(domain, countMap.getOrDefault(domain, 0) + count);
            for (int i = 0; i < domain.length(); i++) {
                if (domain.charAt(i) == '.') {
                    String sub = domain.substring(i + 1);
                    countMap.put(sub, countMap.getOrDefault(sub, 0) + count);
                }
            }
        }
        List<String> res = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            res.add(entry.getValue() + " " + entry.getKey());
        }
        return res;
    }
}
```
- 时间复杂度：O(N)，N 为所有域名总字符数
- 空间复杂度：O(N)

---

## 2. Find Common Elements（Easy-Medium）
### 题目
给定两个整数数组，返回两个数组中都出现过的**不重复**公共元素，优化时间复杂度。

### 示例
```
输入: a = [1, 2, 2, 3], b = [2, 2, 4]
输出: [2]
```

### 题解
```java
import java.util.*;

public class CommonElements {
    public List<Integer> findCommon(int[] a, int[] b) {
        Set<Integer> setA = new HashSet<>();
        Set<Integer> common = new HashSet<>();
        for (int num : a) setA.add(num);
        for (int num : b) {
            if (setA.contains(num)) common.add(num);
        }
        return new ArrayList<>(common);
    }
}
```
- 时间复杂度：O(N + M)
- 空间复杂度：O(N)

---

## 3. Check Valid Matrix（Easy）
### 题目
给定一个 n×n 矩阵，判断矩阵的每一行、每一列是否都恰好包含 1~n 的所有整数。

### 示例
```
输入:
[[1, 2, 3],
 [2, 3, 1],
 [3, 1, 2]]
输出: true
```

### 题解
```java
public class MatrixValid {
    public boolean checkValid(int[][] matrix) {
        int n = matrix.length;
        for (int i = 0; i < n; i++) {
            boolean[] rowSeen = new boolean[n + 1];
            boolean[] colSeen = new boolean[n + 1];
            for (int j = 0; j < n; j++) {
                int rowVal = matrix[i][j];
                int colVal = matrix[j][i];
                if (rowVal < 1 || rowVal > n || rowSeen[rowVal]) return false;
                rowSeen[rowVal] = true;
                if (colVal < 1 || colVal > n || colSeen[colVal]) return false;
                colSeen[colVal] = true;
            }
        }
        return true;
    }
}
```
- 时间复杂度：O(n²)
- 空间复杂度：O(n)

---

## 4. Subarray Sum Equals K（LeetCode 560 · Medium）
### 题目
给定整数数组 nums 和整数 k，返回和为 k 的连续子数组的个数。数组中可能包含负数。

### 示例
```
输入: nums = [1, 1, 1], k = 2
输出: 2
```

### 题解
```java
import java.util.*;

class Solution {
    public int subarraySum(int[] nums, int k) {
        Map<Integer, Integer> prefixMap = new HashMap<>();
        prefixMap.put(0, 1);
        int sum = 0, count = 0;
        for (int num : nums) {
            sum += num;
            if (prefixMap.containsKey(sum - k)) {
                count += prefixMap.get(sum - k);
            }
            prefixMap.put(sum, prefixMap.getOrDefault(sum, 0) + 1);
        }
        return count;
    }
}
```
- 时间复杂度：O(N)
- 空间复杂度：O(N)

---

## 5. Rotate Array（LeetCode 189 · Medium）
### 题目
将数组向右旋转 k 步，要求**原地修改**，空间复杂度 O(1)。

### 示例
```
输入: nums = [1,2,3,4,5,6,7], k = 3
输出: [5,6,7,1,2,3,4]
```

### 题解
```java
class Solution {
    public void rotate(int[] nums, int k) {
        k %= nums.length;
        reverse(nums, 0, nums.length - 1);
        reverse(nums, 0, k - 1);
        reverse(nums, k, nums.length - 1);
    }

    private void reverse(int[] arr, int left, int right) {
        while (left < right) {
            int temp = arr[left];
            arr[left] = arr[right];
            arr[right] = temp;
            left++;
            right--;
        }
    }
}
```
- 时间复杂度：O(N)
- 空间复杂度：O(1)

---

## 6. Text Justification（LeetCode 68 · Hard）
### 题目
给定单词数组和最大行宽 maxWidth，对文本进行两端对齐排版。
- 普通行：空格均匀分布在单词之间，无法均分则左侧空格更多
- 最后一行：左对齐，单词间单空格，末尾补空格

### 示例
```
输入: words = ["This", "is", "an", "example", "of", "text", "justification."], maxWidth = 16
输出:
[
   "This    is    an",
   "example  of text",
   "justification.  "
]
```

### 题解
```java
import java.util.*;

class Solution {
    public List<String> fullJustify(String[] words, int maxWidth) {
        List<String> res = new ArrayList<>();
        int i = 0, n = words.length;
        while (i < n) {
            int j = i, lineLen = 0;
            while (j < n && lineLen + words[j].length() + (j - i) <= maxWidth) {
                lineLen += words[j].length();
                j++;
            }
            int gaps = j - i - 1;
            int totalSpaces = maxWidth - lineLen;
            StringBuilder line = new StringBuilder();
            
            if (j == n || gaps == 0) {
                for (int p = i; p < j; p++) {
                    line.append(words[p]);
                    if (p != j - 1) line.append(" ");
                }
                while (line.length() < maxWidth) line.append(" ");
            } else {
                int spacePerGap = totalSpaces / gaps;
                int extra = totalSpaces % gaps;
                for (int p = i; p < j; p++) {
                    line.append(words[p]);
                    if (p != j - 1) {
                        int add = spacePerGap + (extra-- > 0 ? 1 : 0);
                        line.append(" ".repeat(add));
                    }
                }
            }
            res.add(line.toString());
            i = j;
        }
        return res;
    }
}
```
- 时间复杂度：O(N)（总字符数）
- 空间复杂度：O(N)

---

## 7. Password Validation（Medium · Debug 高频题）
### 题目
给定密码字符串，返回所有不满足的校验规则列表。
规则：
1. 长度至少 8 位
2. 至少包含一个大写字母
3. 至少包含一个小写字母
4. 至少包含一个数字
5. 不能包含空格

### 示例
```
输入: "pass1"
输出: ["too short", "no uppercase"]
```

### 题解
```java
import java.util.*;

public class PasswordValidator {
    public List<String> validate(String password) {
        List<String> violations = new ArrayList<>();
        if (password.length() < 8) violations.add("too short");
        
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpace = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (c == ' ') hasSpace = true;
        }
        
        if (!hasUpper) violations.add("no uppercase");
        if (!hasLower) violations.add("no lowercase");
        if (!hasDigit) violations.add("no digit");
        if (hasSpace) violations.add("contains space");
        return violations;
    }
}
```
- 时间复杂度：O(N)
- 空间复杂度：O(1)（不计输出列表）

---

## 8. Longest Substring Without Repeating Characters（LeetCode 3 · Medium）
### 题目
给定字符串 s，找出最长无重复字符的子串长度。

### 示例
```
输入: "abcabcbb"
输出: 3
```

### 题解
```java
import java.util.*;

class Solution {
    public int lengthOfLongestSubstring(String s) {
        Set<Character> window = new HashSet<>();
        int left = 0, maxLen = 0;
        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            while (window.contains(c)) {
                window.remove(s.charAt(left));
                left++;
            }
            window.add(c);
            maxLen = Math.max(maxLen, right - left + 1);
        }
        return maxLen;
    }
}
```
- 时间复杂度：O(N)
- 空间复杂度：O(min(N, 字符集大小))

---

## 9. Sort Characters By Frequency（LeetCode 451 · Medium）
### 题目
按照字符出现频率降序排列字符串，频率相同的字符顺序不限。

### 示例
```
输入: "tree"
输出: "eetr" 或 "eert"
```

### 题解
```java
import java.util.*;

class Solution {
    public String frequencySort(String s) {
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : s.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }
        List<Map.Entry<Character, Integer>> entries = new ArrayList<>(freqMap.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Character, Integer> entry : entries) {
            sb.append(String.valueOf(entry.getKey()).repeat(entry.getValue()));
        }
        return sb.toString();
    }
}
```
- 时间复杂度：O(N log N)
- 空间复杂度：O(N)

---

## 10. Obstacle Course Run Statistics（Medium · karaat 特色 OOP 题）
### 题目
障碍赛道的跑步记录中，每次 Run 包含每个障碍的耗时；只有所有障碍都有正整数耗时，才算完成全程。
实现两个方法：
1. `personalBest()`：返回所有完成全程的记录中，最快的总耗时
2. `bestOfBests()`：对每个障碍，取所有记录（含未完成）中的最快耗时，求和返回

### 题解
```java
import java.util.*;

class Run {
    private List<Integer> obstacleTimes;
    public Run(List<Integer> times) {
        this.obstacleTimes = times;
    }

    public boolean isComplete() {
        for (Integer time : obstacleTimes) {
            if (time == null || time <= 0) return false;
        }
        return true;
    }

    public int getTotalTime() {
        int sum = 0;
        for (int t : obstacleTimes) sum += t;
        return sum;
    }

    public int getObstacleTime(int index) {
        return obstacleTimes.get(index);
    }
}

public class CourseStatistics {
    private List<Run> runs;
    private int obstacleCount;

    public CourseStatistics(List<Run> runs, int obstacleCount) {
        this.runs = runs;
        this.obstacleCount = obstacleCount;
    }

    public int personalBest() {
        int best = Integer.MAX_VALUE;
        for (Run run : runs) {
            if (run.isComplete()) {
                best = Math.min(best, run.getTotalTime());
            }
        }
        return best;
    }

    public int bestOfBests() {
        int sum = 0;
        for (int i = 0; i < obstacleCount; i++) {
            int minTime = Integer.MAX_VALUE;
            for (Run run : runs) {
                int time = run.getObstacleTime(i);
                if (time > 0) minTime = Math.min(minTime, time);
            }
            sum += minTime;
        }
        return sum;
    }
}
```
- 时间复杂度：O(R × O)，R 为记录数，O 为障碍数
- 空间复杂度：O(R × O)

---

## 11. User-Name Table Join（Medium · 模拟 SQL 题）
### 题目
给定两个二维字符串数组模拟数据表：
- `customers`：列 = [userId, customerName]
- `orders`：列 = [orderId, userId, quantity]
  返回所有有订单的用户的 `userId -> customerName` 映射。

### 示例
```
输入:
customers = [["u1", "Alice"], ["u2", "Bob"]]
orders = [["o1", "u1", "5"], ["o2", "u2", "3"]]
输出: {u1=Alice, u2=Bob}
```

### 题解
```java
import java.util.*;

public class UserJoin {
    public Map<String, String> joinUserIdToName(String[][] customers, String[][] orders) {
        Map<String, String> userIdToName = new HashMap<>();
        for (String[] row : customers) {
            userIdToName.put(row[0], row[1]);
        }
        Map<String, String> res = new HashMap<>();
        for (String[] order : orders) {
            String uid = order[1];
            if (userIdToName.containsKey(uid)) {
                res.put(uid, userIdToName.get(uid));
            }
        }
        return res;
    }
}
```
- 时间复杂度：O(C + O)
- 空间复杂度：O(C)

---

## 12. Merge Intervals（LeetCode 56 · Medium）
### 题目
给定区间数组 `intervals[i] = [start_i, end_i]`，合并所有重叠区间，返回不重叠的区间数组。

### 示例
```
输入: [[1,3],[2,6],[8,10],[15,18]]
输出: [[1,6],[8,10],[15,18]]
```

### 题解
```java
import java.util.*;

class Solution {
    public int[][] merge(int[][] intervals) {
        if (intervals.length == 0) return new int[0][];
        Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
        List<int[]> merged = new ArrayList<>();
        int[] curr = intervals[0];
        
        for (int i = 1; i < intervals.length; i++) {
            int[] next = intervals[i];
            if (next[0] <= curr[1]) {
                curr[1] = Math.max(curr[1], next[1]);
            } else {
                merged.add(curr);
                curr = next;
            }
        }
        merged.add(curr);
        return merged.toArray(new int[merged.size()][]);
    }
}
```
- 时间复杂度：O(N log N)（排序为主）
- 空间复杂度：O(N)

---

## 面试注意要点
1. 编码前先讲思路，写完主动分析时间、空间复杂度
2. Debug 题先理解代码意图，再用测试用例定位问题，修复后验证
3. 提前做完会有 Bonus 追问：加规则、优化性能、处理边界
4. 全程用英文口述逻辑，表达清晰比完美写代码权重更高
