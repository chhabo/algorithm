================================================================================
HCCC karaat 模拟面试完整复盘
================================================================================

================================================================================
一、原始题目
================================================================================

面试官要求：审查以下银行转账服务类，找出所有 Bug 并修复，同时用英语边写边解释。

--- 原始代码 START ---

import java.util.*;

public class AccountService {
private Map<String, Double> accounts = new HashMap<>();

    public AccountService() {
        accounts.put("user1", 100.50);
        accounts.put("user2", 50.00);
    }

    private boolean isValid(Boolean value) {
        if (value.equals(true)) {
            return true;
        }
        return false;
    }

    public boolean transfer(String fromId, String toId, double amount) {
        if (!isValid(true) || amount <= 0) {
            return false;
        }
        
        double fromBalance = accounts.get(fromId);
        double toBalance = accounts.get(toId);
        
        if (fromBalance >= amount) {
            accounts.put(fromId, fromBalance - amount);
            accounts.put(toId, toBalance + amount);
            return true;
        }
        
        return false;
    }
}

--- 原始代码 END ---

================================================================================
二、问题 1：找出 5 个致命 Bug
================================================================================

面试官指出的 5 个问题：

1. 金额类型错误：double 有精度丢失，必须用 BigDecimal + 字符串构造
2. NPE 风险：isValid 中 value.equals(true) 在 value=null 时抛 NPE
3. 线程不安全：HashMap 并发下崩溃，应换 ConcurrentHashMap
4. 缺少边界校验：accounts.get(fromId) 用户不存在返回 null，拆箱抛 NPE
5. BigDecimal 不可变：加减后必须重新赋值，比较必须用 compareTo

================================================================================
三、问题 2：第一轮代码修复
================================================================================

面试官提问：请修复上述 Bug，写出改进后的代码。

--- 用户第一轮修改代码 START ---

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AccountService {
private Map<String, BigDecimal> accounts = new ConcurrentHashMap<>();

    public AccountService() {
        accounts.put("user1", new BigDecimal("100.50"));
        accounts.put("user2", new BigDecimal("50.0"));
    }

    private Boolean isValid(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public boolean transfer(String fromId, String toId, BigDecimal amount) {
        if (!isValid(true) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (!accounts.containsKey(fromId) && !accounts.containsKey(toId)){
            return false;
        }

        BigDecimal fromBalance = accounts.get(fromId);
        BigDecimal toBalance = accounts.get(toId);

        if (fromBalance.compareTo(amount) >=0) {
            accounts.put(fromId, fromBalance.subtract(amount));
            accounts.put(toId, toBalance.add(amount).setScale(2, RoundingMode.HALF_UP));
            return true;
        }

        return false;
    }
}

--- 用户第一轮修改代码 END ---

面试官反馈：
- 正确：BigDecimal + String 构造、ConcurrentHashMap、null-safe Boolean 检查方向正确
- Bug：逻辑运算符应为 || 而非 &&（任一用户不存在都应拒绝）
- Issue：fromBalance.subtract(amount) 未调用 setScale(2)
- Issue：get -> check -> put 非原子操作，并发下丢钱

================================================================================
四、问题 3：修复运算符 + 尝试 computeIfPresent
================================================================================

面试官提问：修复逻辑运算符，并尝试用 computeIfPresent 实现原子性。

--- 用户第二轮修改代码 START ---

public class AccountService {
private Map<String, BigDecimal> accounts = new ConcurrentHashMap<>();

    public AccountService() {
        accounts.put("user1", new BigDecimal("100.50").setScale(2,RoundingMode.HALF_UP));
        accounts.put("user2", new BigDecimal("50.0"));
    }

    private Boolean isValid(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public boolean transfer(String fromId, String toId, BigDecimal amount) {
        if (!isValid(true) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (!accounts.containsKey(fromId) || !accounts.containsKey(toId)){
            return false;
        }

        accounts.computeIfPresent(fromId, (k,oldValue) -> {
            BigDecimal fromBalance = accounts.get(fromId);
            if (fromBalance.compareTo(amount) >= 0){
                BigDecimal toBalance = accounts.get(toId);
                toBalance.add(amount).setScale(2,RoundingMode.HALF_UP);
                return fromBalance.subtract(amount).setScale(2,RoundingMode.HALF_UP);
            }
            return oldValue;
        });

        return false;
    }
}

--- 用户第二轮修改代码 END ---

面试官反馈：
- 正确：|| 修复正确
- 误用 put() 返回值调用 setScale
- 构造函数中 "50.0" scale=1 未统一
- toId 未更新：add 结果未赋值也未 put 回 Map
- 方法总是返回 false
- computeIfPresent 只能保证单 key 原子性，无法跨两个 key 原子更新

================================================================================
五、问题 4：解释全局锁的优缺点
================================================================================

面试官提问：为什么使用全局 ReentrantLock 是正确的？性能问题是什么？如何改进？

--- 用户原始回答 ---

"Yes, it's correct because the, it is a um single global lock for for the trans- trans- transfer method, and that mean um That mean we have a multiple thread.But um it will process one by one, so uh there is no concurrency mess issue. And if the, so that the performance is bad.Um, so I think if we want to have a um lucky scale, um, we can lock by user level, um, such as."

--- 语法修正版 ---

"Yes, it's correct because it uses a single global lock for the transfer method. This means that with multiple threads, transfers are processed one by one, so there is no concurrency issue. However, the performance is poor. If we want to achieve better scalability, we can lock at the user level instead."

--- 面试推荐口语版 ---

"Yes, this approach is correct because it applies a single global lock to the entire transfer method. With multiple threads, all transfers are serialized—processed one at a time—so there's no race condition or data inconsistency. The trade-off is poor throughput. To scale better, we'd use per-user locking instead."

面试官评价：正确识别全局锁序列化问题，提出 per-user locking 方向。

================================================================================
六、问题 5：死锁场景描述
================================================================================

面试官提问：如果 A 转给 B 同时 B 转给 A，没有 lock ordering 会发生什么？

--- 用户原始回答 ---

"If A transfer to B and then B also transfer to A and if there is no lock ordering that may be um the thread A will lock A user A first and the thread B thread 2 is lockThe thread A and then waiting to lock uh lock the thread the user B, and they are all waiting and user A and user B's lock, so they are in will be in the deadlock."

--- 语法修正版 ---

"If Thread A transfers to user B while Thread B simultaneously transfers to user A, and there is no lock ordering, Thread A will lock user A first while Thread B locks user B. Then Thread A waits to acquire user B's lock, and Thread B waits to acquire user A's lock. Since both threads are waiting for each other's locks, they will enter a deadlock."

--- 面试推荐口语版 ---

"If Thread A is transferring from user A to user B, and simultaneously Thread B is transferring from user B back to user A—without lock ordering—Thread A locks user A first, and Thread B locks user B. Then Thread A tries to lock user B while Thread B tries to lock user A. Both threads end up waiting for each other indefinitely, resulting in a circular-wait deadlock."

面试官评价：正确描述死锁场景，建议用 "simultaneously" 替代 "and then"。

================================================================================
七、问题 6：Per-User Lock 弱点
================================================================================

面试官提问：Per-user lock 方案在 HCCC 规模下的弱点是什么？

--- 用户原始回答 ---

"The account lock map will keep growing if we don't clean it up properly, which can potentially cause a memory Leaks. For the second question, if thread A acquire the lock for user A and then waits for user B's lock.Other operation that want to transfer to or from user A can still happen. This is because thread A only holds user A's lock. Those other transfer are also waiting on lock, so there are no block entries need."

--- 语法修正版（保留原意） ---

"The account lock map will keep growing if we don't clean it up properly, which can potentially cause a memory leak. For the second question, if Thread A acquires the lock for user A and then waits for user B's lock, other operations that want to transfer to or from user A can still happen. This is because Thread A only holds user A's lock. Those other transfers are also waiting on the lock, so no blocking is needed."

--- 面试推荐口语版（你的原意，但逻辑有误） ---

"The account lock map keeps growing indefinitely, which could cause a memory leak if we don't clean it up. For the second part, if Thread A acquires user A's lock and then waits for user B's lock, other transfers involving user A can still proceed since Thread A only holds one lock. Those other transfers would just wait on the lock, so there's no blocking issue."

--- 面试官纠正后的正确回答 ---

"The first weakness is that the lock map grows forever, causing a memory leak. More importantly, if Thread A acquires user A's lock but then waits for user B's lock, any other thread trying to transfer to or from user A will be blocked—even though Thread A hasn't modified user A's balance yet. This causes unnecessary lock contention and reduces concurrency, especially for hot accounts."

--- 面试推荐口语版（正确版） ---

"The first weakness is that the lock map grows forever, causing a memory leak. The second is unnecessary contention: when Thread A holds user A's lock while waiting for user B's lock, any other thread wanting to transfer to or from user A gets blocked immediately—even though Thread A hasn't actually updated anything yet. At HCCC scale, this would severely limit throughput for frequently used accounts."

面试官纠正：第二部分理解有误——Thread A 持有 user A 锁期间，其他涉及 user A 的转账会被阻塞，造成不必要的锁竞争。

================================================================================
八、最终版代码（Per-User Lock + Lock Ordering）
================================================================================

--- 最终代码 START ---

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AccountService {
private final Map<String, BigDecimal> accounts = new ConcurrentHashMap<>();
private final Map<String, Lock> accountLocks = new ConcurrentHashMap<>();

    public AccountService() {
        accounts.put("user1", new BigDecimal("100.50").setScale(2, RoundingMode.HALF_UP));
        accounts.put("user2", new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP));
    }
    
    private boolean isValid(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
    
    public boolean transfer(String fromId, String toId, BigDecimal amount) {
        if (!isValid(true) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (fromId == null || toId == null || fromId.equals(toId)) {
            return false;
        }
        if (!accounts.containsKey(fromId) || !accounts.containsKey(toId)) {
            return false;
        }
        
        Lock fromLock = accountLocks.computeIfAbsent(fromId, k -> new ReentrantLock());
        Lock toLock = accountLocks.computeIfAbsent(toId, k -> new ReentrantLock());
        
        Lock firstLock = fromId.compareTo(toId) < 0 ? fromLock : toLock;
        Lock secondLock = fromId.compareTo(toId) < 0 ? toLock : fromLock;
        
        firstLock.lock();
        secondLock.lock();
        
        try {
            BigDecimal fromBalance = accounts.get(fromId);
            BigDecimal toBalance = accounts.get(toId);
            
            if (fromBalance == null || toBalance == null) {
                return false;
            }
            if (fromBalance.compareTo(amount) < 0) {
                return false;
            }
            
            accounts.put(fromId, fromBalance.subtract(amount).setScale(2, RoundingMode.HALF_UP));
            accounts.put(toId, toBalance.add(amount).setScale(2, RoundingMode.HALF_UP));
            
            return true;
            
        } finally {
            secondLock.unlock();
            firstLock.unlock();
        }
    }
}

--- 最终代码 END ---

================================================================================
九、生产级解决方案
================================================================================

| 方案 | 说明 |
|------|------|
| 数据库行级锁 | SELECT FOR UPDATE 在事务内锁定行 |
| 乐观锁 | 版本号字段 + CAS，冲突重试 |
| 事件溯源 | 只追加交易记录，余额从日志计算 |
| 按账户分片 | 每个分片顺序处理该账户交易 |

karaat 最佳回答：使用数据库行级锁 + 事务。

================================================================================
十、最终评分
================================================================================

| 维度 | 得分 | 说明 |
|------|------|------|
| Bug 识别 | 通过 | 正确识别 BigDecimal、NPE、线程安全 |
| 死锁理解 | 通过 | 能描述死锁场景和 lock ordering |
| 并发深度 | 部分 | 对 holding lock while waiting 影响理解有误 |
| 英文口语 | 需提升 | 能表达核心意思但不够流畅 |
| 代码完成度 | 通过 | 最终版本结构完整 |

总分：7/10

================================================================================
十一、关键 Takeaway
================================================================================

1. 金额处理铁律：BigDecimal + 字符串构造 + setScale(2) + compareTo
2. 并发三板斧：ConcurrentHashMap 只保证单操作原子，check-then-act 必须加锁
3. 死锁预防：多锁场景必须定义一致的获取顺序
4. 边写边讲：karaat 极度看重沟通，每写一行说出意图
5. 生产意识：应用层锁是教学演示，生产用 DB 事务/乐观锁