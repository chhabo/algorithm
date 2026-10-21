## 分布式 & 一致性 高频20原题（详版）

## Q1 — CAP Theorem 本质
EN: Explain CAP theorem — can you pick all three? Trade-offs in practice.
CN: 解释 CAP 定理，能否三者兼得？实践中如何取舍。
中文解题说明：
- CAP：Consistency（线性一致读）、Availability（每个非故障节点都返回响应）、Partition tolerance（网络分区下仍能工作）。
- 网络分区 P 在分布式系统中必然发生 → 实际只能在 C 和 A 之间二选一（CP vs AP）。
- CP：分区时牺牲可用性保证一致（ZooKeeper/etcd/HBase）——选主期间拒绝服务。
- AP：分区时保证可用但可能返回旧数据（Dynamo/Cassandra/DNS/Eureka）——最终一致。
- 注意：CAP 是“分区发生时”的瞬时取舍，不是全天放弃某一项；无分区时两者可同时满足。
- 常见误区：说“选 CA”是错的——CA 意味着无分区容忍，等于单机。
  Spoken Answer (EN):
  "CAP says a distributed system facing a network partition can only guarantee either Consistency or Availability, not both. Since partitions are unavoidable in real networks, you must pick CP or AP. ZooKeeper/etcd are CP — during a partition they stop serving until a quorum elects a leader (lose A, keep C). Dynamo/Cassandra are AP — keep serving reads/writes on both sides (keep A, give up immediate C, reconcile later). Important nuance: CAP only applies during an actual partition event; the rest of the time you can have both."

## Q2 — BASE vs ACID
EN: Contrast BASE (AP systems) vs ACID (traditional DB).
CN: 对比 AP 系统的 BASE 与传统 DB 的 ACID。
中文解题说明：
- ACID：Atomicity/Consistency/Isolation/Durability，强一致事务，适合金融核心账本。
- BASE：Basically Available（基本可用）、Soft state（软状态，允许中间态）、Eventually consistent（最终一致）。
- BASE 牺牲强一致换取高可用与分区容忍（CAP 里的 A）。
- 典型：电商库存扣减允许短暂不一致后对账补偿；订单最终落库。
- 现代趋势：混合——核心用 ACID（如支付用强一致），外围用 BASE（如评论、计数）。
  Spoken Answer (EN):
  "ACID guarantees strict atomicity and isolation — every transaction is all-or-nothing and reads never see intermediate state. Expensive but correct, used for money.
  BASE relaxes this: Basically Available (degrade gracefully), Soft state (state may change without input due to async replication), Eventual consistency (converges eventually). Trades immediate correctness for availability and partition tolerance. Modern systems mix: ACID for payments ledger, BASE for likes/counters/feeds."

## Q3 — Two-Phase Commit (2PC) 优缺点
EN: Explain 2PC protocol and its fatal flaw.
CN: 解释两阶段提交及其致命缺陷。
中文解题说明：
- 阶段1（Prepare/Vote）：协调者问所有参与者“能否提交”，参与者锁资源、写 undo/redo 日志，回 YES/NO。
- 阶段2（Commit/Abort）：全 YES → 发 Commit；任一 NO → 发 Abort。
- 致命缺陷：
    1. **同步阻塞**：参与者锁资源等到协调者指令 → 持有锁久，并发差。
    2. **单点故障**：协调者宕机 → 参与者永远阻塞在“不确定”状态（资源锁死）。
    3. **数据不一致风险**：协调者发 Commit 后宕机，部分收到、部分没收到 → 脑裂。
- 因此 2PC 不适合高可用长事务，被 TCC/Saga/3PC 替代。
  Spoken Answer (EN):
  "2PC has a prepare phase (coordinator asks all participants to vote, they lock resources and log) then a commit phase (global commit or abort). Its fatal flaws: it's blocking — participants hold locks until commit, and if the coordinator crashes after sending prepare, participants hang forever waiting. Also if coordinator dies mid-commit, some nodes commit while others don't → inconsistency. Hence 2PC is rarely used for long-lived distributed transactions; people prefer compensations (Saga/TCC)."

## Q4 — Three-Phase Commit (3PC)
EN: How does 3PC improve over 2PC? Is it fully safe?
CN: 3PC 如何改进 2PC？是否完全安全？
中文解题说明：
- 三阶段：CanCommit（询问能否）→ PreCommit（预提交，先进入就绪状态）→ DoCommit。
- 引入超时机制：协调者/参与者都有超时，超时默认提交（解决 2PC 阻塞）。
- 但仍无法彻底避免网络分区下的不一致——PreCommit 后分区，一侧提交一侧中止可能冲突。
- 结论：3PC 降低阻塞概率但牺牲性能、仍未严格保证一致性，实际生产很少用（共识算法 Raft/Paxos 更优）。
  Spoken Answer (EN):
  "3PC adds a CanCommit phase and timeouts so participants don't block forever — if coordinator times out, participants proceed to commit by default. But it still can't guarantee safety under arbitrary network partitions (a partitioned node may commit while others abort). It reduces blocking at the cost of extra round-trips and weaker guarantees, so production systems prefer Paxos/Raft consensus instead."

## Q5 — Paxos 核心思想
EN: Explain Paxos (Basic Paxos) roles and how it reaches consensus despite failures.
CN: 解释 Basic Paxos 角色及如何在故障下达成共识。
中文解题说明：
- 三角色（可重叠）：Proposer（提议案）、Acceptor（投票者）、Learner（学习者）。
- 两阶段：
    1. Prepare(n)：Proposer 发编号 n，Acceptor 承诺“不再接受 <n 的提案”，返回曾接受的最高编号提案值。
    2. Accept(n, value)：若多数派承诺，Proposer 用返回的已有值（若有）或自选值发 Accept；Acceptor 接受则达成决议。
- 安全性：一旦某值被选定，后续更高编号提案只能带该值 → 保证一致。
- 活锁：两个 Proposer 互飙编号 → 需选单一 Leader（Multi-Paxos）。
  Spoken Answer (EN):
  "Paxos has proposers, acceptors, learners. A proposer sends Prepare(n); acceptors promise to ignore lower numbers and reply with any previously accepted value. Then Accept(n,value) — if a majority promises, value is chosen (if acceptors had a prior value, proposer must reuse it to preserve safety). Once a majority accepts, consensus is reached and learners learn it. Livelock between competing proposers is solved by electing a stable leader → Multi-Paxos."

## Q6 — Multi-Paxos / Raft 动机
EN: Why Multi-Paxos/Raft instead of basic Paxos for replicated log?
CN: 为何复制日志用 Multi-Paxos/Raft 而非 Basic Paxos？
中文解题说明：
- Basic Paxos 每决定一个值要 2 轮 RPC，慢且只定单值。
- 日志复制 = 连续定序一系列值 → 选一个稳定 Leader 负责提所有序号提案，省去每轮的 Prepare（Leader 只需一次 Prepare 获多数授权，之后连续 Accept）→ 1 轮 RPC/entry。
- Raft 把“选主 + 日志复制 + 安全约束”拆得更清晰易懂，成为 etcd/Consul/TiKV 基础。
  Spoken Answer (EN):
  "Replicated state machines need to agree on a total order of many log entries. Running Basic Paxos per entry is 2 RTTs each and allows any proposer to race. Multi-Paxos elects one stable leader who only does the prepare phase once, then appends entries with a single Accept RPC each — 1 RTT/entry, in-order. Raft makes this concrete with explicit leader election (term + randomized timeout) and log matching properties, becoming the de-facto standard (etcd, TiKV, Consul)."

## Q7 — Raft 选主机制
EN: Detail Raft leader election: terms, timeouts, vote rules.
CN: 详述 Raft 选主：任期、超时、投票规则。
中文解题说明：
- Term（任期号）单调递增，每个 term 至多一个 leader。
- Follower 在 electionTimeout（如150-300ms随机）内没收到心跳 → 转 Candidate，自增 term，投自己一票，向全员 RequestVote。
- 投票约束：每 term 每节点只投一票（先到先得）；候选者日志必须 up-to-date（最后日志 term/index >= 自己）才投。
- 获多数票 → 成 Leader，发心跳压制他人。
- 分裂投票（同票）→ 超时重选（随机退避避免反复冲突）。
  Spoken Answer (EN):
  "Raft splits time into terms. A follower that misses heartbeats for a randomized election timeout becomes candidate, increments term, votes itself, and requests votes. A node grants its single vote per term only to candidates whose log is at least as up-to-date (last log term/index >= own). Majority wins → leader; it sends periodic heartbeats. Ties (split vote) cause another randomized timeout retry — randomness avoids livelock."

## Q8 — Raft 日志复制与安全性
EN: How Raft replicates logs and guarantees only committed entries get applied (Log Matching + CommitIndex advance).
CN: Raft 如何复制日志并保证只提交已确认条目（日志匹配+提交索引推进）。
中文解题说明：
- Leader 收到客户端命令 append 到本地 log，广播 AppendEntries 给 follower。
- 多数派持久化成功 → leader 推进 commitIndex = 该 entry 的 index（仅当当前 term 的 entry 被复制才允许推进旧 term 条目提交——避免旧 leader 已复制到多数但未提交的坑）。
- Log Matching：相同 (term,index) 的条目内容必相同（AppendEntries 携带 prevLogTerm/prevLogIndex 做一致性检查，不匹配则 follower 拒收，leader 递减 nextIndex 回溯）。
- 安全性：Candidate 竞选必须包含所有已提交条目（up-to-date 检查）→ 不会选丢数据的节点。
  Spoken Answer (EN):
  "Leader appends to its log, sends AppendEntries with prevLogTerm/prevIndex; followers reject mismatches forcing leader to backtrack nextIndex until log matches (Log Matching Invariant). Entry is committed once replicated to majority AND a current-term entry is also committed (Raft's extra safety rule prevents committing stale-term entries prematurely). Only committed entries are applied to state machine. Election restricts votes to up-to-date logs so a new leader never loses committed data."

## Q9 — ZAB 协议 (ZooKeeper Atomic Broadcast)
EN: How ZAB differs from Raft — crash recovery & broadcast phases.
CN: ZAB 与 Raft 区别——崩溃恢复与广播两阶段。
中文解题说明：
- ZAB 是专为 ZK 设计的原子广播协议，类似 Raft 但分两模式：
    1. **广播(Broadcast)**：类似 2PC——Leader 发 PROPOSAL 给所有 Follower，过半 ACK 后发 COMMIT。
    2. **恢复(Recovery)**：选新 Leader 后需同步（ZXID 最大者当选），Leader 用 DIFF/TRUNC/SNAP 让 follower 对齐。
- ZXID = (epoch, counter)，epoch 即 leader 朝代。
- 与 Raft 区别：Raft 用日志连续 index+term，ZAB 用事务 id 单调；ZAB 保证全局有序广播、写顺序严格 FIFO。
  Spoken Answer (EN):
  "ZAB has two modes: broadcast (leader proposes a transaction with ZXID=(epoch,counter), waits for majority ACK, then commits) and recovery (on leader change, pick follower with highest ZXID as leader, sync others via diff/truncate/snapshot). Like Raft it ensures total order and leader-based replication, but models transactions with epoch-counter ZXIDs and uses a 2-phase commit flavor internally. ZooKeeper relies on it for its linearizable znode writes."

## Q10 — Quorum / NWR 模型 (Dynamo)
EN: Explain N, W, R and how tuning them trades consistency vs availability.
CN: 解释 N/W/R 及调参如何权衡一致性与可用性。
中文解题说明：
- N=副本总数，W=写需成功副本数，R=读需成功副本数。
- 若 W+R>N → 读写必交集 → 强一致（读能拿到最新写）。
- 若 W+R≤N → 可能读旧（最终一致，高可用）。
- 典型：(N=3,W=2,R=2) → 强一致且容忍1故障；(N=3,W=1,R=1) → 最终一致高性能；(N=3,W=3,R=1)纯强但写不可靠。
- 配合版本向量/时间戳解决多写冲突（last-write-wins 或返回多版本）。
  Spoken Answer (EN):
  "N replicas, write to W, read from R. If W+R>N, the read/write quorums overlap so reads always see latest write → strong consistency. If W+R≤N they may not intersect → eventual consistency, higher availability/lower latency. Dynamo lets you tune per use-case: (N=3,W=2,R=2)=quorum strong; (W=1,R=1)=fast AP. Conflicts resolved via vector clocks/version stamps."

## Q11 — Consistency levels taxonomy（一致性等级谱系）
EN: Define linearizability, sequential, causal, eventual consistency precisely.
CN: 精确区分线性一致、顺序一致、因果一致、最终一致。
中文解题说明：
- **Linearizability（线性一致/强一致）**：每个读看到最近一次写，且实时有序（real-time）；等价于单副本+原子操作。最严。
- **Sequential（顺序一致）**：所有进程看到相同操作顺序，但不要求符合真实时间（可重排但全局统一顺序）。
- **Causal（因果一致）**：有因果关系的操作保序（A→B 则全局看 A 先于 B），并发无序。
- **Eventual（最终一致）**：无新写后最终所有副本收敛到相同值，期间可能读到旧值。
- 强度：Linear > Sequential > Causal > Eventual。
  Spoken Answer (EN):
  "Linearizability = single-copy semantics with real-time ordering (reads return the most recent write as of wall-clock). Sequential consistency = all processes agree on one total order, but order need not match real time. Causal consistency = only causally related ops ordered (happens-before preserved), concurrent ops free. Eventual consistency = replicas converge to same state once writes stop, interim reads may be stale. Hierarchy strictly decreases in strength."

## Q12 — Split-Brain & how to prevent（脑裂及预防）
EN: What is split-brain, and mechanisms to prevent (fencing, quorum, STONITH).
CN: 脑裂是什么，预防手段（ fencing、quorum、STONITH）。
中文解题说明：
- 脑裂：网络分区导致两个节点都以为自己是主，同时对外服务 → 双写数据冲突。
- 预防：
    1. **Quorum/多数派**：只有获得多数投票才能成为主，少数派分区自动降级（失去法定人数不能服务）。
    2. **Fencing Token（ fencing ）**：存储侧拒绝旧主令牌的写。
    3. **STONITH**（Shoot The Other Node In The Head）：硬杀掉旧主电源/进程。
    4. **租约 Lease**：主权力带 TTL，过期自动失效。
       Spoken Answer (EN):
       "Split-brain = network partition lets two nodes believe each is the primary, both accept writes → divergent data. Prevent via quorum (only a node with majority votes may lead; minority side stops), leases (leadership expires automatically), fencing tokens (storage rejects stale leader's writes), and STONITH (forcibly power-cycle the rogue node). Quorum + fencing is the combination that truly kills split-brain."

## Q13 — Leader Election 常见算法
EN: Survey leader election approaches (Bully, Ring, Raft-style).
CN: 综述选主算法：Bully、环、Raft式。
中文解题说明：
- **Bully**：节点 ID 最大者当选；发起选举时向更大 ID 发选举消息，若无更大响应则自宣为主通知全员。简单但消息 O(n²)、易震荡。
- **Ring（环）**：令牌沿环传递，选举消息收集所有 ID 找最大再绕环广播。
- **Raft/Paxos式**：随机超时+任期投票，工程主流（etcd/Consul）。
- 关键属性：必须保证唯一主（安全性）、最终选出（活性）、分区下不双主（靠 quorum）。
  Spoken Answer (EN):
  "Bully algorithm: highest-ID node wins, sends election to higher IDs, declares self if none answer. Simple O(n^2). Ring: circulate an election message collecting max ID then broadcast winner. Both replaced in practice by Raft-style randomized-timeout + quorum voting which avoids ties elegantly and integrates with log replication. All must ensure safety (≤1 leader/term) and liveness (eventually elects)."

## Q14 — Distributed Lock correctness pitfalls
EN: Why naive Redis SETNX locks are unsafe; Redlock debate.
CN: 为何简单 Redis SETNX 锁不安全；Redlock 争议。
中文解题说明：
- 朴素 SETNX + 过期：若持锁进程 GC 停顿超过 TTL，锁自动释放被他人获取 → 双持锁；需 fencing token 兜底。
- Redlock（多个独立 Redis 实例多数派获取）：Martin Kleppmann 批评——依赖近似同步时钟/GC 停顿仍可能双主；正确做法需 fencing token 或强一致存储(ZooKeeper/etcd)。
- 正确姿势：用支持 fencing/租约的协调服务（etcd lease / Chubby / ZooKeeper ephemeral node）。
  Spoken Answer (EN):
  "Naive SETNX with TTL fails under GC pauses: process A pauses past TTL, lock auto-expires, B acquires, A wakes and both think they hold it. Redlock (majority of N independent Redis) still attacked by Kleppmann for relying on wall-clock assumptions during pauses. Correct solution: use consensus-backed locks with fencing tokens (etcd/zk lease + monotonic token) so storage rejects the stale holder's writes."

## Q15 — Fencing Token 原理
EN: How fencing token guarantees at-most-one correct writer despite stale locks.
CN: fencing token 如何保证即便旧锁失效也至多一个合法写入者。
中文解题说明：
- 锁服务每次授权递增 token（如每次 grant 返回全局单调 seq）。
- 客户端每次写存储带 token；存储记录 seen_max_token，仅接受 token≥max 的写，并拒绝更小 token（旧主苏醒带旧 token 被拒）。
- 把“锁正确性”从时间维度转移到单调序维度，免疫 GC/时钟漂移。
  Spoken Answer (EN):
  "Lock service mints a monotonically increasing token per grant. Client passes token on every write; storage persists the highest token seen and rejects any write with a lower token. So even if old leader revives after losing lock and issues writes with stale token, storage drops them — guarantees at most one progress-making writer regardless of timing."

## Q16 — Lease 机制优势
EN: Lease = TTL authority; why better than pure heartbeats for failure detection.
CN: Lease（带 TTL 授权）为何优于纯心跳故障检测。
中文解题说明：
- 纯心跳：超时阈值需远大于网络抖动 → 故障发现慢（几十秒）；阈值小又易误判。
- Lease：授予权直到明确过期时刻 T；无需持续心跳维持，到期无续约即失效，最坏不可用 = lease 时长（可控）。
- GFS chunk lease、Chubby、etcd lease 均基于此。续约只需在到期前刷新。
  Spoken Answer (EN):
  "Lease grants authority valid until time T without needing per-message heartbeats. Holder renews before T to extend. Worst-case unavailability is bounded by lease duration (tunable, e.g. a few seconds) — far better than heartbeat timeouts that must be set conservatively to avoid false positives. Chubby/GFS/etcd all use leases for primary authority & session expiry."

## Q17 — Gossip / Epidemic 协议
EN: Gossip mechanics & use cases (membership, anti-entropy).
CN: Gossip 机制及用途（成员管理、反熵）。
中文解题说明：
- 每周期随机选 k 个对等节点交换全量/增量状态（push/pull/push-pull）。
- 传播像传染病：T 轮后覆盖率 1-(1-k/n)^T → O(logN) 轮收敛。
- 容错强、无中心、带宽随规模对数增长。
- 用途：Cassandra 副本修复(anti-entropy)、Consul/Serf(SWIM改进版)成员探测、Redis Cluster gossip。
  Spoken Answer (EN):
  "Each node periodically gossips its state to a few random peers (push/pull). Information spreads epidemically in O(log N) rounds to all nodes — robust to churn, no single coordinator, bandwidth logarithmic. Powers Cassandra anti-entropy repair, Serf/Consul SWIM membership, Redis Cluster gossip. Variant SWIM adds suspicion to speed failure detection."

## Q18 — Vector Clock / Version Vector 冲突检测
EN: How vector clocks detect concurrent writes (Dynamo model).
CN: 向量时钟如何判定并发写（Dynamo 模型）。
中文解题说明：
- 每副本维护向量 V[node]=counter，写时自增本节点分量并附到数据。
- 比较 V_a, V_b：
    - V_a < V_b（所有分量 ≤ 且至少一个<）→ a 被 b 因果超越，丢弃 a。
    - 不可比较（有的分量大有的小）→ 并发冲突 → 保留两者(siblings)交应用层合并或 LWW。
- 解决“谁更新”无法单靠时间戳（时钟不可靠）。
  Spoken Answer (EN):
  "Each replica keeps a vector [node→count]. On write it bumps its own component. Comparing two versions: if one dominates component-wise it's the newer (discard other); if neither dominates → concurrent conflict → keep both siblings for app-level merge (or LWW). Captures causality without synchronized clocks, the backbone of Dynamo-style conflict resolution."

## Q19 — Kafka 不丢数据保证 (ISR + acks + unclean)
EN: Exactly-once-ish durability guarantees in Kafka replication.
CN: Kafka 复制如何保证已提交不丢（ISR、acks=all、禁脏选主）。
中文解题说明：
- Producer `acks=all` + `min.insync.replicas=2`：消息须被 ISR 中≥2副本确认才算提交。
- ISR（in-sync replicas）：与 leader 差距在阈值内的副本集合，落后太多被踢出。
- `unclean.leader.election.enable=false`：禁止选非 ISR 副本当主 → 避免丢已提交数据（宁可不可用）。
- At-least-once + 幂等 producer(enable.idempotence) + 事务可实现 effectively-once。
  Spoken Answer (EN):
  "acks=all waits for all in-sync replicas to acknowledge, min.insync.replicas=2 ensures a write survives losing one replica. ISR shrinks/grows based on lag. Crucially disable unclean leader election so a non-ISR node can never become leader and lose committed data (prefer unavailability over data loss). Idempotent producer + transactions give effectively-exactly-once per partition."

## Q20 — Idempotency & exactly-once in distributed writes
EN: Achieving idempotency under retries; dedupe strategies.
CN: 重试场景实现幂等；去重策略。
中文解题说明：
- 网络重试必然导致重复（at-least-once）。幂等=多次同操作结果一致。
- 方法：客户端带幂等键(Idempotency-Key)，服务端建去重表(唯一键或 Redis SETNX key TTL)记录已处理 requestId，重复直接返回首次结果。
- DB 层：唯一约束防重复插入；UPDATE 用乐观版本/CAS；消费侧记录已处理 offset/messageId。
- Kafka exactly-once：producer 幂等(seq+PID)+事务写多分区原子+consumer 读已提交隔离。
  Spoken Answer (EN):
  "Network retries make at-least-once delivery the norm, so writes must be idempotent: attach a client-supplied idempotency key, store processed keys in a dedup table/Redis with TTL, and on replay return the cached original response instead of re-executing side effects. At storage layer use unique constraints, optimistic CAS versions, and commit consumer offsets transactionally. Kafka achieves effectively-once via idempotent producer (PID+sequence) + transactional writes + read-committed isolation."