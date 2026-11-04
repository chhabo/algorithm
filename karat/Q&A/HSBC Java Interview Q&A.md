# HCCC Java 面试题 · 中英对照 / HCCC Java Interview Q&A (Bilingual)

> 来源：Naveen Gowda（Bangalore）HCCC 面试经验分享；26 道场景题，几乎全部为实战/情景题而非概念背诵。
> Source: Naveen Gowda's HCCC interview experience; 26 scenario-based questions rather than theory recitation.

---

## 一、分布式系统与微服务 / Distributed Systems & Microservices

### Q1 · 01:13 · 支付重复扣款 / Idempotent Payment
- **Question (EN)**: A payment API times out, but the payment may already have been completed. How would you prevent the payment from happening twice?
- **题目（中）**：支付 API 超时，但支付可能已经成功，如何避免重复扣款？
- **Answer (EN)**: I'd use an **idempotency key**. The client generates one unique key per payment and reuses it on every retry. The payment service checks that key first — if it is already processed, it simply returns the stored response instead of charging again. I'd also add a **unique constraint on the key at the database level**, because two concurrent retries could both pass the application-level check. In short: one payment, one key, same key on every retry, with the DB as the final guard.
- **回答（中）**：用**幂等键**：客户端为每笔支付生成唯一 key，重试复用同一个；服务端先查 key，已处理就直接返回上次结果，不再扣款。**DB 层对该 key 加唯一约束**兜底，防止两个请求几乎同时到达都判断「未处理」。一句话：一次支付一个 key、重试复用、靠 DB 做最终保护。

### Q2 · 02:16 · DB 成功但 Kafka 发送失败 / Transactional Outbox
- **Question (EN)**: Your application updates the database successfully, but publishing the Kafka event fails. How would you keep the database consistent?
- **题目（中）**：数据库更新成功、Kafka 事件发布失败，怎么保证一致性？
- **Answer (EN)**: The DB update and the Kafka publish are two separate operations, so I'd use the **Transactional Outbox pattern**. In a single local transaction I write both the business data and the event payload into an `outbox` table — either both commit or both roll back. A separate relay process reads pending rows, publishes them to Kafka, retries on failure and marks them completed afterwards. This gives at-least-once delivery, so consumers should also be idempotent.
- **回答（中）**：两者是独立操作，用 **Transactional Outbox**：业务数据 + 事件明细写进**同一个数据库事务**（要么都成功要么都失败）；后台进程读 outbox 里 pending 事件推 Kafka，失败可重试、成功后标记完成。本质是「一个本地事务 + 可重放异步投递」，属于至少一次投递，消费端也要幂等。

### Q3 · 03:32 · 慢服务拖垮全链路 / Slow Microservice
- **Question (EN)**: One microservice becomes slow and starts affecting all the other services. How would you control the problem?
- **题目（中）**：某个微服务变慢，开始拖垮其他服务，怎么控制？
- **Answer (EN)**: I'd apply four layers: a **reasonable timeout** so callers don't wait 30s or a minute per request; **bounded retries with backoff** (never infinite — retries make an overloaded service worse); a **circuit breaker** that temporarily stops sending traffic and returns a fallback or a clear error; and the **bulkhead pattern**, giving each important dependency its own thread pool so one slow dependency cannot consume all threads. Timeout first, controlled retry, circuit breaker, resource isolation.
- **回答（中）**：四层组合：**合理超时**（不让调用方长等）→ **受控重试**（不无限重试，过载时重试会雪上加霜）→ **熔断**（暂时断流，给 fallback 或明确报错）→ **Bulkhead 隔离**（重要依赖独立线程池，防一个慢依赖吃光全部线程）。顺序：先超时，再限重试，再熔断，再隔离。

### Q4 · 04:41 · 生产内存持续上涨 / Memory Leak
- **Question (EN)**: Your Java application's memory usage keeps increasing in production. How would you find the cause?
- **题目（中）**：生产环境 Java 内存持续上涨，怎么定位原因？
- **Answer (EN)**: First I'd confirm whether the heap actually comes down after GC — a temporary rise is normal, a continuous rise is not. Then I'd review heap/GC metrics from monitoring, take a **heap dump**, and analyse it with Eclipse MAT, VisualVM, JFR or a profiler to find the biggest objects and, more importantly, **who still holds them** (GC roots). Usual suspects: growing static collections, unbounded caches, listeners never removed, ThreadLocal/thread-pool state not cleared, and unclosed streams or DB connections. After the fix I'd re-run a load test and verify the heap stabilises after GC.
- **回答（中）**：先确认 **GC 后堆是否回落**（临时上涨正常，持续不降才是问题）→ 看内存/GC 监控 → 抓 **heap dump** → 用 MAT / VisualVM / JFR 找占内存最大的对象**以及谁还持有它**。典型元凶：static 集合只增不减、无界缓存、listener 只注册不移除、ThreadLocal/线程池未清理、流与连接未关。修完**压测 + 监控堆**验证闭环。

### Q5 · 05:57 · 承载百万级数据的 API / High-Volume API
- **Question (EN)**: How would you design an API that can handle millions of records?
- **题目（中）**：如何设计一个能处理百万级记录/请求的 API？
- **Answer (EN)**: I'd make the application **stateless** and run multiple instances behind a load balancer with horizontal auto-scaling based on CPU, memory or traffic. I'd add caching for hot data, proper indexes plus read replicas or partitioning at the DB layer, offload non-synchronous work (notifications, reports, heavy processing) to Kafka or a queue, and apply rate limiting so one client cannot saturate the system. Finally I'd load-test to find the real bottleneck — and for genuinely large result sets I'd return data via cursor-based pagination or streaming rather than one huge response.
- **回答（中）**：**无状态化** → 多实例 + 负载均衡 + 按 CPU/内存/流量自动扩缩；热点数据**缓存**；DB 加**索引 / 读副本 / 分区**；通知、报表、重计算等丢给 **Kafka/队列异步化**；加**限流**；最后**压测找真实瓶颈**。补一句区分点：若真要返回大结果集，用**游标分页 / 流式响应**，别一次性吐全量。

### Q6 · 07:22 · 并发更新同一条记录 / Concurrent Updates
- **Question (EN)**: Two users try to update the same database record at the same time. How would you prevent incorrect data?
- **题目（中）**：两个用户同时更新同一条数据库记录，怎么防止数据错乱？
- **Answer (EN)**: I'd normally use **optimistic locking** with a JPA `@Version` column. Both users read version 5; user 1 commits and the version becomes 6; user 2 then updates with version 5, Hibernate sees the mismatch and the second update fails instead of silently overwriting the first one. The app reloads the latest data and asks the user to retry. This suits infrequent conflicts. For critical cases where no other transaction may touch the row at all, I'd use **pessimistic locking**, but not everywhere — it increases waiting time and can cause deadlocks.
- **回答（中）**：**乐观锁**：JPA `@Version` 版本列。两人同读 v5，A 先更新变 v6，B 拿 v5 更新会失败而非静默覆盖，应用重载最新数据让用户重试，适合冲突不频繁的场景。**悲观锁**只在「绝不允许别的事务动这条记录」等关键场景用，因为增加等待、可能死锁，不能到处用。

### Q7 · 08:39 · 重试导致重复建单 / Duplicate Order Creation
- **Question (EN)**: A retry mechanism sends the same order request twice. How would you prevent duplicate order creation?
- **题目（中）**：重试机制把同一订单请求发了两次，怎么防止重复建单？
- **Answer (EN)**: I'd use an **idempotency key / unique request ID**. Before creating the order, the service checks whether that request ID was already processed and returns the existing order if so. Crucially, I would **not rely on Java code alone**: two duplicate requests can hit two application instances at the same instant and both see "not exists". A **unique DB constraint on the request ID** is the final protection, so any number of retries still produces exactly one order.
- **回答（中）**：**幂等键 / 唯一 request ID**：建单前查是否已处理，已存在就返回已有订单。关键是**不能只靠 Java 层判断**——多实例并发下两个请求可能同时查到「不存在」，必须在 DB 对 request ID 加**唯一约束**做最终保护。与 Q1 同构，一个落在支付、一个落在订单。

### Q8 · 09:44 · 分布式事务部分成功 / Saga Pattern
- **Question (EN)**: How would you handle a situation where only some steps of a distributed transaction are completed?
- **题目（中）**：分布式事务只完成了一部分步骤，怎么处理？
- **Answer (EN)**: There is no single global rollback, because each service owns its own database and transaction. I'd use the **Saga pattern**: every service commits its own local transaction, and if a later step fails I run **compensating actions** for the steps that already succeeded — e.g. if inventory reservation fails after payment, refund the payment and cancel the order. Compensations must be **idempotent** because they can be retried, and I'd track explicit states (pending / completed / failed / compensating). Saga undoes completed steps instead of rolling back one big transaction.
- **回答（中）**：各服务各有自己的库与事务，**没有跨库大事务可回滚**，所以用 **Saga**：每步提交本地事务；后续步骤失败就对已成功步骤做**补偿**（库存失败 → 退款 → 取消订单）。补偿操作本身要**幂等**（会被重试），并维护 pending/completed/failed/compensating 状态。本质：不回滚大事，而是「本地提交 + 反向撤销」。

### Q9 · 10:58 · 单体迁微服务 / Strangler Pattern
- **Question (EN)**: How would you move a large monolith to microservices without breaking the existing system?
- **题目（中）**：大型单体迁到微服务，如何不搞垮现有系统？
- **Answer (EN)**: I would never rewrite the whole monolith in one go — that is too risky. Using the **Strangler pattern**, I'd first identify a relatively independent module with few dependencies (notifications is a good candidate), extract only that functionality into a separate service, and route new requests for it to the new service while everything else keeps running in the monolith. Once it is stable, I pick the next module and repeat. The old system stays online throughout, and parts are replaced incrementally rather than all at once.
- **回答（中）**：不一次性重写。用 **Strangler（绞杀者）模式**：先挑依赖少、相对独立的模块（例：通知服务）拆成独立微服务，其余继续跑在单体里；该功能的新请求路由到新服务，稳定后再挑下一个模块、重复此过程。老系统全程在线，逐步替换而非整体重写。

---

## 二、Java 基础与并发 / Java Core & Concurrency

### Q10 · 11:58 · 可变对象作 HashMap 的 key / Mutable HashMap Key
- **Question (EN)**: What happens when a mutable object is used as a key in a HashMap and its value is changed?
- **题目（中）**：可变对象当 HashMap 的 key，之后改了它的字段会怎样？
- **Answer (EN)**: HashMap computes the hash at insertion and stores the entry in a bucket derived from it. If I later mutate a field used in `hashCode()`, the same object now returns a different hash, so `get()` looks in a different bucket while the original entry still sits in the old one — the value becomes unreachable and the entry may linger in memory. That is why keys should be **immutable** (`String`, `Integer`, `Long`) or never mutated after insertion.
- **回答（中）**：插入时按当时 hash 落进某个 bucket；改了参与 `hashCode` 的字段后 hash 变了，`get` 会去另一个 bucket 找，而原条目还留在旧 bucket → **取不到值**，还可能一直滞留占内存。结论：key 用**不可变对象**（String / Integer / Long），或插入后绝不修改。

### Q11 · 12:56 · HashMap 哈希冲突 / Collision Handling
- **Question (EN)**: How does HashMap handle collisions internally?
- **题目（中）**：HashMap 内部如何处理哈希冲突？
- **Answer (EN)**: Colliding keys are not overwritten; multiple entries are kept in the same bucket. HashMap first uses the hash to locate the bucket, then `equals()` to identify the exact key inside it. Before Java 8 the bucket was a linked list; since Java 8, when a bucket reaches about 8 entries and the table capacity is at least 64, the list is **treeified into a balanced tree**, improving lookup from O(n) to roughly O(log n) (otherwise it just resizes).
- **回答（中）**：同 bucket 可存多个 entry，不直接覆盖：先用 hash 定位 bucket，再用 `equals` 找确切 key。**Java 8 之前**主要是链表；**Java 8 起**当 bucket 约 8 个条目且 table 容量 ≥ 64 时转**平衡树**（容量不足则先扩容），查找从 O(n) 降到约 O(log n)。

### Q12 · 13:54 · parallel stream 何时更慢 / Parallel Streams
- **Question (EN)**: When can parallel streams make an application slower?
- **题目（中）**：parallel stream 什么时候反而让应用更慢？
- **Answer (EN)**: Parallel streams split data into tasks across multiple threads, which only pays off for **large datasets, CPU-intensive work and independent per-element operations**. With just 50–100 records, the overhead of splitting, thread scheduling and merging results dominates. Blocking work such as DB or external API calls rarely benefits, and modifying shared mutable state inside the stream introduces correctness risks. I never enable parallelism just to "go faster" — I check suitability and benchmark the actual performance first.
- **回答（中）**：它只在**数据量大 + CPU 密集 + 元素间独立计算**时划算。几十上百条数据时，切分/调度/合并的开销盖过收益；含 DB/外部 API 等**阻塞 IO** 收益也不佳；流内改共享数据还有并发风险。结论：先判断是否适合并行，再**实测**，别默认「并行 = 更快」。

### Q13 · 15:01 · 定位并修复竞态 / Race Condition
- **Question (EN)**: How would you identify and fix a race condition in an application?
- **题目（中）**：怎么定位并修复竞态条件？
- **Answer (EN)**: A race condition occurs when multiple threads read and update the same shared mutable data and the final result depends on thread interleaving — e.g. two threads both read balance 1000 and one update overwrites the other. I'd start with logs and thread dumps to find which shared data is being modified concurrently, then locate the critical section in code. Depending on where it lives, I'd make the read-modify-write atomic using `synchronized`, `ReentrantLock`, atomic classes or concurrent collections; if the data is in the DB, I'd use DB locking or optimistic locking. The goal is always: identify shared mutable state and make its update atomic or properly synchronised.
- **回答（中）**：竞态 = 多线程同时读写同一份共享可变数据，结果取决于谁跑得快（都基于余额 1000 更新，后一次覆盖前一次）。排查：查日志 / 抓 thread dump，找出被并发修改的共享数据 → 定位临界区 → 按场景用 `synchronized`、`ReentrantLock`、Atomic 类、并发容器；数据在 DB 里就用 DB 锁或乐观锁。核心：**让读-改-写原子化，或正确同步**。

### Q14 · 16:06 · ReentrantLock vs synchronized / Lock Choice
- **Question (EN)**: When would you use ReentrantLock instead of the synchronized keyword?
- **题目（中）**：何时用 ReentrantLock 而不是 synchronized？
- **Answer (EN)**: For simple locking I prefer `synchronized` — it is concise and the JVM releases the lock automatically when the block exits. I reach for `ReentrantLock` when I need finer control: `tryLock()` to avoid waiting forever, timed waits, interruptible waiting, multiple `Condition` objects, or optional fairness. The trade-off is that **the lock must be released manually in a `finally` block**, otherwise an exception leaves it held forever. Simple case → `synchronized`; advanced control → `ReentrantLock`.
- **回答（中）**：简单加锁用 `synchronized`，JVM 自动释放、省心。需要**高级控制**才上 `ReentrantLock`：`tryLock` 不无限等待、限时等待、可中断、多个 `Condition`、可选公平锁。代价：**必须手动 `unlock()` 且放在 `finally`**，保证异常时也释放。

### Q15 · 17:12 · 死锁与线上排查 / Deadlock
- **Question (EN)**: What is a deadlock and how would you find it in a running Java application?
- **题目（中）**：什么是死锁，生产上怎么查出来？
- **Answer (EN)**: A deadlock is a circular wait: thread 1 holds lock A and waits for B, while thread 2 holds B and waits for A, so neither can ever proceed. If I suspect one in production, a **thread dump** is the first thing I take — via `jstack`, VisualVM, JFR or an APM tool — and I look for BLOCKED threads, which locks they hold and which they are waiting for. Prevention: always acquire locks in a **consistent global order**, avoid unnecessary nested locking, keep critical sections small, and use `tryLock()` with a timeout where appropriate.
- **回答（中）**：两/多线程互持对方要的锁（T1 持 A 等 B，T2 持 B 等 A），永远互等。排查第一步是**抓 thread dump**（jstack / VisualVM / JFR / APM），看哪些线程 BLOCKED、持有哪些锁、在等哪些锁。预防：**统一固定的加锁顺序**、避免嵌套锁、缩小锁粒度、必要时用带超时的 `tryLock`。

### Q16 · 18:14 · 阻塞 vs 非阻塞 / Blocking vs Non-blocking
- **Question (EN)**: What is the difference between blocking and non-blocking operations?
- **题目（中）**：阻塞与非阻塞操作的区别是什么？
- **Answer (EN)**: In a blocking call the thread stays occupied until the operation completes — a 5-second external API call holds that thread for 5 seconds, unable to serve other requests. With non-blocking IO the thread starts the operation, is free to do other work, and resumes via a callback or reactive mechanism when the result is ready. Blocking code is simpler and perfectly fine for most normal applications; but under a large number of slow IO calls, blocked threads become expensive, and non-blocking stacks such as WebFlux can handle more concurrent requests with fewer threads. I choose based on the requirement, not because non-blocking sounds more advanced.
- **回答（中）**：阻塞：线程发起调用后一直被占着等结果（外部 API 5 秒，线程空占 5 秒），期间处理不了别的请求。非阻塞：发起后线程即可做别的，结果就绪再通过 callback / reactive 续上。阻塞写法简单、普通场景够用；**大量慢 IO** 时阻塞线程成本很高，非阻塞能用更少线程撑更多并发（如 WebFlux）。按需求选，别只因「听起来更先进」。

---

## 三、Spring / Spring Boot / Spring Framework

### Q17 · 19:23 · 同类型多个 Bean / Ambiguous Injection
- **Question (EN)**: What happens when Spring finds multiple beans of the same type?
- **题目（中）**：Spring 发现同类型多个 bean 会怎样？
- **Answer (EN)**: When injecting by type, Spring sees more than one candidate and cannot decide, so it throws `NoUniqueBeanDefinitionException`. Two common fixes: **`@Qualifier`** names the exact bean I want (e.g. `stripePaymentService` vs `paypalPaymentService`), and **`@Primary`** marks one bean as the default whenever multiple candidates exist. Use `@Qualifier` to pick a specific implementation; use `@Primary` to define a sensible default.
- **回答（中）**：按类型注入时候选不唯一 → 抛 `NoUniqueBeanDefinitionException`。两个解法：**`@Qualifier`** 指定具体 bean 名（要选特定实现时，如 Stripe / PayPal）；**`@Primary`** 标一个默认 bean（要兜底默认时）。

### Q18 · 20:27 · @Transactional 自调用失效 / Self-invocation
- **Question (EN)**: Why might @Transactional not work when one method calls another method inside the same class?
- **题目（中）**：同一个类里 A 调 B（B 标了 @Transactional）为什么不生效？
- **Answer (EN)**: Spring implements `@Transactional` through an **AOP proxy** around the bean; the transaction is only started when the call passes through that proxy. If method A calls method B directly inside the same class (**self-invocation**), the call goes straight to `this`, bypassing the proxy, so the advice on B is never applied. The cleanest fix is to move the transactional method into another Spring bean and call it through the injected reference; alternatives are injecting the self proxy or using AspectJ weaving.
- **回答（中）**：Spring 靠**代理**实现事务，只有调用经过代理才有机会开启事务；类内 **self-invocation** 直接走当前对象、绕过代理，B 的事务行为就不生效。解法：把带事务的方法挪到**另一个 Spring bean**，通过注入的 bean 调用，让调用重新经过代理（也可自注入代理或用 AspectJ 织入）。

### Q19 · 21:24 · @Transactional 调 @Async / Async & Transactions
- **Question (EN)**: What happens when a @Transactional method calls an @Async method?
- **题目（中）**：@Transactional 方法调用 @Async 方法会怎样？
- **Answer (EN)**: `@Async` runs the method on a **different thread**, while a Spring transaction is bound to the **current thread** (ThreadLocal), so the transaction does not propagate. The async method may complete successfully and then the caller's transaction may roll back — the async work will **not** roll back with it, and it may even read data that was never committed. If the async method needs transactionality it must manage its own transaction; for operations requiring reliable delivery I'd prefer **Kafka or the transactional outbox** over a bare `@Async` call.
- **回答（中）**：`@Async` 通常在**另一个线程**执行，而 Spring 事务绑定在**当前线程**，事务不会自动传到异步线程，**不能假设两者同一事务**：异步方法可能已成功，随后原事务回滚，异步那部分不会跟着回滚（还可能读到未提交数据）。需要事务就让异步方法自己管；要求可靠投递就用 **Kafka / Transactional Outbox**。

### Q20 · 22:25 · 多环境配置 / Spring Profiles
- **Question (EN)**: How do you manage different configurations for development, testing and production?
- **题目（中）**：dev / test / prod 的配置怎么管理？
- **Answer (EN)**: I'd use **Spring Profiles** — `application-dev.yml`, `application-test.yml`, `application-prod.yml` — each containing only environment-specific values such as DB URLs, Kafka brokers, log levels and internal API endpoints. The same build artifact runs everywhere; only the active profile changes, set locally via `spring.profiles.active` and in production via an environment variable or deployment config. Secrets must never be committed into those files; they belong in environment variables, Vault or Kubernetes Secrets.
- **回答（中）**：**Spring Profiles**：`application-dev/test/prod` 各只放环境相关项（DB URL、Kafka、日志级别、内部 API 地址），**同一份代码/制品到处跑，只切配置**。本地用 `spring.profiles.active`，生产用环境变量或部署配置激活；敏感值不写进文件提交，交给环境变量 / Vault / K8s Secrets。

### Q21 · 23:28 · 密钥安全存储 / Secrets Management
- **Question (EN)**: How would you store database passwords and API keys securely in a Spring Boot application?
- **题目（中）**：Spring Boot 里 DB 密码、API key 怎么安全存储？
- **Answer (EN)**: Never hardcode credentials in Java code, and never put production secrets into `application.properties` committed to Git. For local development, environment variables or a git-ignored local config file are fine. In production I'd use a secrets manager — HashiCorp Vault, AWS Secrets Manager, Azure Key Vault or Kubernetes Secrets — where the application authenticates and reads only what it is allowed to. On top of that: **least privilege** (a service should not see other services' secrets), regular **rotation**, and **never logging** passwords or keys. Rule of thumb: secrets stay outside source code, are granted narrowly, and are loaded securely at runtime.
- **回答（中）**：绝不硬编码，也绝不把生产密钥写进 `application.properties` 提交 Git。本地用环境变量或不入库的本地配置；生产用 **Vault / AWS Secrets Manager / Azure Key Vault / K8s Secret**，应用鉴权后按需读取。再加三条：**最小权限**（服务拿不到别的服务密钥）、**定期轮换**、**绝不把密钥打进日志**。核心：密钥在代码之外、按需授权、运行时安全加载。

### Q22 · 24:38 · 限流 / Rate Limiting
- **Question (EN)**: How would you implement rate limiting in a Spring Boot application?
- **题目（中）**：Spring Boot 里怎么实现限流？
- **Answer (EN)**: Rate limiting caps how many requests one client may send in a window (e.g. 100/min), rejecting excess with **HTTP 429**. If there is an API gateway, I'd enforce it there so extra traffic is blocked before reaching the microservices. Inside Spring Boot, **Bucket4j** is a common choice. An in-memory counter only works for a single instance: with five instances each counting locally, a client effectively gets five times the limit. In a distributed deployment I'd keep the counters in **Redis** so all instances share one count. It protects against abuse, traffic spikes and a single client consuming the whole system.
- **回答（中）**：限制单用户/客户端单位时间请求数（如 100/min），超限返回 **429**。有 API Gateway 就**优先放网关**，在流量进微服务前挡掉；应用内可用 **Bucket4j**。单实例内存计数能凑合，但多实例各自计数会让实际额度翻 N 倍（5 实例 = 5 倍额度），所以分布式场景用 **Redis 存共享计数**。价值：防滥用、防突发流量、防单客户端吃满资源。

---

## 四、JPA / Hibernate / 数据访问

### Q23 · 25:50 · N+1 查询 / N+1 Query Problem
- **Question (EN)**: What is the N+1 query problem in Hibernate and how do you solve it?
- **题目（中）**：Hibernate 的 N+1 查询问题是什么，怎么解决？
- **Answer (EN)**: Hibernate runs one query for the main entities and then one additional query per related lazy collection — fetching 100 departments and touching their employees produces **101 queries**, which slows the app and loads the DB. I'd detect it by enabling Hibernate SQL logging or checking an APM tool for the same statement repeated many times. Fixes: **`JOIN FETCH`**, **`@EntityGraph`**, batch fetching (`@BatchSize`), or **DTO projections**, chosen according to what data is actually needed. The goal is fewer, more efficient queries — **not** blindly switching every relationship to EAGER.
- **回答（中）**：先 1 条查主数据，再对每条关联数据各发 1 条（100 个 department 懒加载各自 employees → **101 条**）。排查：开 Hibernate SQL 日志或看 APM，发现同类 SQL 反复出现。解法：`JOIN FETCH`、`@EntityGraph`、批量抓取、DTO 投影，按实际需要选。重点：**不是把所有关系改成 EAGER**，而是用更少更高效的查询取到需要的数据。

### Q24 · 26:59 · 乐观锁 vs 悲观锁 / Optimistic vs Pessimistic Locking
- **Question (EN)**: What is the difference between optimistic locking and pessimistic locking?
- **题目（中）**：乐观锁与悲观锁的区别？
- **Answer (EN)**: With **optimistic locking** the row is not locked while reading; a version column is checked at write time. Two users read version 5, the first update makes it 6, and the second update with version 5 fails, so the app reloads and retries — ideal when conflicts are rare. **Pessimistic locking** locks the row in the database for the duration of the transaction, forcing others to wait; it gives stronger protection but reduces throughput and increases deadlock risk. I default to optimistic locking and reserve pessimistic locking for very high-contention or business-critical operations.
- **回答（中）**：乐观锁**不锁记录**、靠 version 列，冲突时让后到者失败并重载重试，适合**冲突少**；悲观锁**直接在 DB 锁行**、其他事务等待，保护更强但降性能、易死锁，只在**冲突极频繁或业务强要求**时用。默认偏好乐观锁（与 Q6 同源，这题考概念对比）。

### Q25 · 28:11 · LazyInitializationException / Lazy Loading Outside Session
- **Question (EN)**: Why does LazyInitializationException happen in Hibernate?
- **题目（中）**：Hibernate 为什么会出现 LazyInitializationException？
- **Answer (EN)**: Lazy loading defers fetching related data until it is actually accessed. If I load an Order inside a transaction but never touch its items, the transaction ends and the Hibernate **session is closed**; calling `order.getItems()` later in the controller means Hibernate wants to query the DB but no longer has an open session, so it throws `LazyInitializationException`. The proper fix is to fetch what is needed **while the transaction is still active** — `JOIN FETCH`, an EntityGraph, a DTO projection, or mapping the entity to a DTO in the service layer. Making every relationship EAGER is not the answer; it just creates unnecessary queries and new performance problems.
- **回答（中）**：懒加载不立即取关联数据：事务内只 load 了 Order、没碰 items，事务结束 **session 已关闭**，之后在 controller 调 `getItems()`，Hibernate 想查却查不了 → 抛异常。正解：**在事务仍活跃时把需要的数据取出来**（`JOIN FETCH` / EntityGraph / DTO 投影，或在 service 层把实体转 DTO）；不要简单全改 EAGER，会引入多余查询和性能问题。

### Q26 · 29:16 · JPA 大批量写入 / Batch Processing
- **Question (EN)**: How would you insert or update a large number of records efficiently using JPA?
- **题目（中）**：用 JPA 高效批量插入/更新大量记录怎么做？
- **Answer (EN)**: I would not keep saving endlessly while the persistence context holds every managed entity — memory would explode. I'd process in **batches of 500–1000**, calling `flush()` (push pending SQL to the DB) and `clear()` (detach entities from the persistence context) at each boundary, and enable JDBC batching via `hibernate.jdbc.batch_size` plus `order_inserts` / `order_updates=true` so similar statements are sent together. For extremely large volumes I'd skip JPA entirely and use `JdbcTemplate`, native bulk SQL or the database's bulk-load tool. The principle: batch the work and never keep millions of managed objects in memory.
- **回答（中）**：别让持久化上下文一直攒着托管实体（内存会飙）。**分批处理**：每 500 / 1000 条 `flush()` + `clear()`（前者把待执行 SQL 推到 DB，后者把实体移出持久上下文）；开启 **JDBC batching**（`hibernate.jdbc.batch_size`、`order_inserts` / `order_updates=true`）合并相似 SQL。**极大批量干脆不用 JPA**，改 `JdbcTemplate`、原生 bulk SQL 或数据库专用导入工具。核心：分批 + 别在内存里留百万个托管对象。

---

## 速记主线 / Key Answer Patterns

- **重复 / 重试 → 幂等键 + DB 唯一约束**：Q1、Q7、Q8 的补偿操作同源；务必补「只靠应用代码不够，并发下要 DB 兜底」。
- **两个操作跨边界 → 本地事务 + 异步可重放**：Q2 Outbox、Q8 Saga、Q19 用 Kafka/Outbox 替掉不可靠的 `@Async`。
- **一个服务变慢 → 超时 → 受控重试 → 熔断 → 资源隔离**：Q3；补一句「无限重试会加重过载」。
- **生产出问题 → 监控 → dump（heap / thread）→ 定位持有者或临界区 → 修复 → 压测验证**：Q4、Q13、Q15 共用这条闭环。
- **Spring 的坑集中在「代理」和「线程」**：Q18 没走代理、Q19 事务绑线程、Q17 注入歧义；先点破机制再给解法。
- **Hibernate 三类问题同一个解**：N+1、LazyInit、批量写，答案都不是「改 EAGER / 一把 save」，而是精确抓取 + 分批控制持久化上下文。

>  Interview takeaway: this was almost entirely **scenario-based** — the interviewer wants to hear "what you do first, where the safety net is, and how you verify the fix", not just the name of a pattern.
>  面试要点：几乎全是**实战场景题**，考的是「先做什么、兜底在哪、怎么验证修好了」，而不只是能不能报出模式名。