# Karat Production Debugging & Reliability — 20 Drill Questions (中英对照 + 中文解题说明 + 英文口语答案)

---

## Q1 — Memory Leak Detection (内存泄漏排查)

**EN:** A process shows steady CPU but linearly increasing memory over time, eventually hitting OOM. How do you diagnose and fix it?

**CN:** 进程 CPU 平稳但内存随时间线性上涨，最终触发 OOM。如何诊断和修复？

**中文解题说明：**
- 第一步：确认内存增长模式（是否每次请求后不释放，还是全局缓存无限扩张）
- 第二步：抓取 Heap Dump（Java 用 jmap，Go 用 pprof，Python 用 tracemalloc）
- 第三步：分析 Heap Dump 找到占用最大的对象类型和引用链（Dominator Tree）
- 第四步：检查代码中的静态集合、缓存未设 TTL、未关闭的资源（流/连接）、监听器未注销
- 第五步：修复后做压力测试验证
- 工具：jstat（看 GC 频率和堆变化）、jmap + MAT/Eclipse MAT、Arthas

**Spoken Answer (EN):**
"First, I'd confirm it's a real leak vs. just high allocation by checking if memory drops after a full GC — if not, it's a leak.
Next, take a heap dump using jmap or your language's equivalent (pprof for Go, tracemalloc for Python).
Analyze the dump with a profiler like Eclipse MAT or YourKit to find the dominator objects — usually it's a static HashMap, an unbounded cache, or unclosed resources like streams/connections.
Then trace the reference chain to the code that's holding onto them.
Fix: add proper eviction/TTL to caches, close resources in finally blocks, remove listeners on destroy.
Finally, re-run a soak test to confirm memory stabilizes."

---

## Q2 — Thread Pool Exhaustion (线程池耗尽)

**EN:** A multi-threaded application becomes unresponsive; threads are not enough. How do you debug and resolve?

**CN:** 多线程应用变得无响应，线程不够用。如何调试和解决？

**中文解题说明：**
- 现象：请求排队、超时、CPU 可能不高但吞吐量骤降
- 排查：抓取 Thread Dump（jstack 或 kill -3），查看线程状态
- 常见原因：
    1. 死锁（线程互相等待对方持有的锁）
    2. 活锁/自旋（CPU 高但无进展）
    3. 线程池配置过小 + 任务执行慢（阻塞在 IO/DB）
    4. 线程泄漏（创建后未回收）
- 解决：增大线程池（需配合压测）、改用异步非阻塞、修复死锁代码、加超时

**Spoken Answer (EN):**
"First, take a thread dump — jstack or kill -3 — and look for threads stuck in BLOCKED or WAITING state.
Check if there's a deadlock: look for circular lock chains in the dump.
If no deadlock, check what the RUNNABLE threads are doing — if they're all blocked on DB calls or external API, your thread pool is too small for the blocking nature of tasks.
Solutions: increase pool size (but watch memory), switch to async I/O (Netty, CompletableFuture), add timeouts to external calls, or use a work-stealing pool.
Also check if threads are leaking — are new threads created but never terminated?"

---

## Q3 — Load Test Validation (压测验证)

**EN:** A service currently handles 1 RPS in production. How do you verify it can handle 1000 RPS?

**CN:** 服务目前生产环境只处理 1 RPS。如何验证它能扛住 1000 RPS？

**中文解题说明：**
- 不能直接在生产环境打 1000 RPS（会搞挂服务）
- 步骤：
    1. 搭建与生产等配的 staging 环境（或影子库）
    2. 用压测工具（JMeter / wrk / Locust / Gatling）逐步加压：10 → 50 → 100 → 500 → 1000
    3. 监控指标：响应时间 P50/P99、错误率、CPU/内存/GC、DB 连接池、网络带宽
    4. 找到瓶颈点（通常 DB 或外部依赖先挂）
    5. 优化后重复压测
- 注意：压测数据要隔离（影子表/标记），不能污染生产数据

**Spoken Answer (EN):**
"You never test 1000 RPS directly on production — that'd take down your live service.
Set up a staging environment with identical specs, or use shadow traffic / canary.
Use a load generator like wrk, JMeter, or Locust. Start at 10 RPS, ramp up gradually: 50, 100, 500, 1000.
Monitor P50/P99 latency, error rate, CPU, GC pauses, DB connection pool usage, and network I/O.
Find the knee point where latency spikes or errors appear — that's your bottleneck.
Usually it's the database connection pool or an unoptimized query.
Fix the bottleneck, then re-run the test until you sustain 1000 RPS with acceptable latency."

---

## Q4 — Missing Request IDs in Distributed Logs (分布式日志中缺失ID排查)

**EN:** Across 500 machines, some requests fail but leave no log entry. How do you find these "missing" failed requests and debug them?

**CN:** 在 500 台机器的日志中，有些请求失败了但没有留下日志。如何找到这些"缺失"的失败请求并排查？

**中文解题说明：**
- 问题本质：请求在进入日志系统之前就失败了（如负载均衡层、网络丢包、进程崩溃）
- 排查思路：
    1. 在入口层（LB / API Gateway）记录所有请求 ID 和响应状态
    2. 对比入口日志和下游服务日志，找出有入口记录但无下游记录的 ID
    3. 检查 LB 健康检查、连接超时配置
    4. 检查是否有进程 OOM 被 kill 导致请求中断
    5. 用分布式追踪（Jaeger / Zipkin）看请求是否到达目标服务
- 预防：全链路 Request-ID 透传 + 入口层必须记 access log

**Spoken Answer (EN):**
"The problem is likely that requests fail before they reach your application logging code — maybe at the load balancer, or the process crashes mid-request.
First, check your LB/API gateway access logs — they should record every request with a request ID and response status.
Cross-reference gateway logs with application logs: find request IDs that exist in the gateway but never appear in app logs.
Those are your 'missing' requests. Then check:
- LB health check status (was the backend marked unhealthy?)
- TCP connection timeouts
- Whether the app process was OOM-killed or restarted during those requests
- Network partition between LB and backend
  Add distributed tracing (OpenTelemetry/Jaeger) so every hop records a span — you'll immediately see where the trace breaks."

---

## Q5 — Third-Party API Outage (第三方 API 频繁宕机)

**EN:** Your sports data service depends on a third-party API that frequently goes down. How do you design for resilience?

**CN:** 你的体育数据服务依赖一个频繁宕机的第三方 API。如何设计系统保证韧性？

**中文解题说明：**
- 核心策略：熔断 + 降级 + 缓存 + 超时 + 重试（有限）
- 具体方案：
    1. 熔断（Circuit Breaker）：连续失败 N 次后打开熔断，直接拒绝请求，避免资源耗尽
    2. 降级：返回缓存的旧数据、默认值或简化结果
    3. 缓存：将第三方数据缓存一段时间（TTL），API 挂了也能返回旧数据
    4. 超时：设短超时（如 2s），快速失败
    5. 异步刷新：后台定时拉取第三方数据更新缓存，前端请求只走缓存
    6. 多数据源：准备备用数据源，主源挂了自动切换

**Spoken Answer (EN):**
"Design for failure — the third-party WILL go down, so assume it's already down.
First, add a circuit breaker (Resilience4j, Hystrix). After N consecutive failures, open the circuit and fail fast instead of blocking threads.
Second, cache third-party responses aggressively with a TTL — serve stale data when the API is down.
Third, set strict timeouts (2–3 seconds max) on calls; don't let slow calls tie up your thread pool.
Fourth, implement a fallback: return cached data, a default response, or a simplified version of the result.
Fifth, consider async refresh — a background job polls the third-party API and updates a local cache; user requests only hit the cache.
Finally, have a backup data source if possible — when primary fails, switch to secondary."

---

## Q6 — Sudden Latency Spike (网站突然变慢)

**EN:** Your website suddenly becomes very slow. Walk me through your debugging process.

**CN:** 网站突然变得很慢。请描述你的排查流程。

**中文解题说明：**
- 分层排查（从外到内）：
    1. 确认影响范围：是所有用户还是部分地区？是全部接口还是特定接口？
    2. 检查监控大盘：CPU、内存、网络、磁盘 IO、负载
    3. 检查最近变更：是否有最近部署、配置变更、流量突增
    4. 数据库：慢查询？连接池满？锁等待？
    5. 外部依赖：第三方 API 变慢？缓存命中率下降？
    6. 网络：DNS、CDN、丢包
    7. 应用层：GC 频繁？线程阻塞？Full GC Stop-The-World？
- 工具：top/vmstat/iostat、APM（New Relic/Datadog）、DB 慢查询日志

**Spoken Answer (EN):**
"Start with the basics: check monitoring dashboards for CPU, memory, disk I/O, network, and error rates. Look for any anomaly in the last 30–60 minutes.
Check recent deployments — was there a code push or config change? If yes, rollback first, investigate later.
If no recent change, check the database: are there slow queries? Lock waits? Connection pool exhaustion?
Check external dependencies: are third-party APIs responding slowly? A 5-second timeout on a downstream call can cascade.
Check GC logs — frequent Full GC causes Stop-The-World pauses that manifest as latency spikes.
Check if traffic spiked — maybe a retry storm from a buggy client.
Use distributed tracing to pinpoint which service/hop is adding latency.
Quick mitigation: scale out, restart stuck instances, increase timeouts, enable circuit breakers."

---

## Q7 — Database CPU 100% (数据库 CPU 打满)

**EN:** Your database CPU is pegged at 100%. How do you find the culprit and fix it?

**CN:** 数据库 CPU 持续 100%。如何找到原因并修复？

**中文解题说明：**
- 第一步：确认是读还是写导致的
- 第二步：查慢查询日志（Slow Query Log），按执行时间排序
- 第三步：EXPLAIN 分析 Top 慢查询的执行计划
- 常见原因：
    1. 缺失索引或索引失效（全表扫描）
    2. 复杂 JOIN / 子查询
    3. 大量数据聚合（GROUP BY / ORDER BY 无索引）
    4. 锁等待导致线程堆积
    5. 突发大量请求（缓存击穿）
- 解决：加索引、优化 SQL、加缓存、读写分离、限流

**Spoken Answer (EN):**
"First, check if it's reads or writes causing the spike — look at the database's process list / active queries.
Enable slow query log if not already on, and grab the top offenders.
Run EXPLAIN on those queries — usually it's a missing index causing full table scans, or a bad join order.
Common fixes: add composite indexes matching the WHERE/ORDER BY clauses, rewrite the query to avoid SELECT *, add pagination.
If it's a traffic spike (cache miss storm), warm the cache or add a read replica to distribute load.
If it's lock contention, check for long-running transactions and deadlocks.
Short-term mitigation: kill the worst queries, add rate limiting at the app layer, scale up the DB temporarily."

---

## Q8 — Cache Stampede / Thundering Herd (缓存击穿/雪崩)

**EN:** A popular cache key expires and suddenly 10,000 requests hit the database simultaneously. How do you prevent this?

**CN:** 一个热门缓存 key 过期，瞬间 1 万个请求打到数据库。如何防止？

**中文解题说明：**
- 问题：缓存击穿（单个热点 key）+ 缓存雪崩（大量 key 同时过期）
- 解决方案：
    1. 互斥锁（Mutex Lock）：第一个请求去加载数据，其他请求等待或返回旧值
    2. 逻辑过期：key 在内存中不过期，后台异步刷新，过期只是标记
    3. 随机 TTL：给 key 加随机偏移（±5分钟），避免同时过期
    4. 双层缓存：本地缓存 + Redis，本地缓存挡第一层
    5. 预热：系统启动时预加载热点数据
    6. 熔断：DB 压力过大时直接返回错误

**Spoken Answer (EN):**
"Classic cache stampede. Solutions:
First, use a mutex or distributed lock — when cache misses, only one thread fetches from DB and repopulates; others wait or return stale data.
Second, logical expiration: don't actually expire the key; instead, store an expiry timestamp inside the value. When it's stale, return the old value and trigger an async refresh in the background.
Third, add jitter to TTL — instead of all keys expiring at midnight, spread expiry times with random offset (±5 min).
Fourth, use a two-tier cache: local in-process cache (Caffeine) + Redis. Local cache absorbs most reads, Redis handles cross-instance sharing.
Fifth, pre-warm hot keys before expected traffic spikes."

---

## Q9 — Message Queue Backlog (消息队列堆积)

**EN:** Your Kafka/RabbitMQ consumer lag is growing — millions of messages backlogged. How do you handle it?

**CN:** Kafka/RabbitMQ 消费延迟不断增长，堆积了数百万消息。如何处理？

**中文解题说明：**
- 先判断：消费者处理能力不足 vs 消费者挂了
- 排查：
    1. 消费者是否存活？线程是否阻塞？
    2. 单条消息处理耗时是否突然变长？（DB 慢、外部 API 超时）
    3. 分区数是否太少，消费者实例不够？
- 紧急处理：
    1. 紧急扩容消费者实例（Kafka 可加到分区数）
    2. 临时跳过非关键消息（dead letter queue）
    3. 批量消费（一次拉 100 条批量处理）
    4. 如果消息可重放，临时建新 topic 分流
- 长期：优化消费逻辑、增加分区、监控 lag 告警

**Spoken Answer (EN):**
"First, check if consumers are alive and healthy — are they crashing? OOM? Blocked on something?
Check processing time per message — if it suddenly increased (slow DB, external API timeout), that's your bottleneck.
Immediate mitigation: scale out consumers — add more instances, increase partition count if using Kafka.
If messages are non-critical, route them to a dead-letter queue temporarily.
Switch to batch consumption if not already — pull 100 messages at once and process in bulk.
If it's a one-time backlog and messages are replayable, create a new topic, drain the old one offline.
Long-term: optimize consumer logic (async processing, batch DB writes), add lag monitoring with alerts, and implement backpressure."

---

## Q10 — Disk Full / High IO (磁盘满/IO 高)

**EN:** Production server disk is 100% full, or disk IO is saturated. How do you diagnose and resolve?

**CN:** 生产服务器磁盘 100% 满，或磁盘 IO 饱和。如何诊断和解决？

**中文解题说明：**
- 磁盘满排查：
    1. df -h 看哪个挂载点满了
    2. du -sh /* 逐层找大文件/目录
    3. 常见元凶：日志文件未轮转、core dump 文件、临时文件、Docker 镜像堆积
- 磁盘 IO 高排查：
    1. iostat -x 1 看 %util、await
    2. iotop 看哪个进程在大量读写
    3. 常见原因：频繁 fsync、大量小文件写入、日志同步刷盘、swap 颠簸
- 解决：清理日志/加 logrotate、迁移数据到更大磁盘、用 SSD、调大 OS 页缓存

**Spoken Answer (EN):**
"For disk full: run df -h to find which mount is full, then du -sh on subdirectories to drill down.
Usually it's logs — check /var/log, application log directories. If logs aren't rotated (no logrotate), truncate or delete old files.
Other culprits: core dumps, Docker images/containers, temp files in /tmp.
Quick fix: delete old files, compress logs, add logrotate config.
For high IO: run iostat -x 1 to see %util and await — if %util is near 100%, IO is saturated.
Use iotop to find the process doing heavy IO. Common causes: synchronous logging (fsync every write), database without enough memory for buffer pool, or swap thrashing.
Mitigations: add more RAM (reduce swap/improve cache hit), move IO-heavy workloads to SSD, batch writes, tune DB to use async commit."

---

## Q11 — OOM Killed (进程被 OOM Killer 杀掉)

**EN:** Your Java/Go/Python service keeps getting killed by the OOM killer, but you didn't set a memory limit. What's happening and how to fix?

**CN:** Java/Go/Python 服务不断被 OOM Killer 杀掉，但你没设内存限制。发生了什么？怎么修？

**中文解题说明：**
- Linux OOM Killer 会在系统整体内存不足时杀进程（即使单个容器/进程没超 limit）
- 排查：
    1. dmesg | grep -i oom 查看 OOM kill 记录
    2. 看被 kill 的进程占了多少内存
    3. 检查是否有内存泄漏（参考 Q1）
    4. 检查系统总内存：是否有其他进程占用过多
    5. 容器环境：容器内存 limit 触发 cgroup OOM
- 解决：设置合理的 JVM/运行时内存参数、修复内存泄漏、增加机器内存、限制其他进程内存

**Spoken Answer (EN):**
"Check dmesg | grep -i oom to confirm OOM killer activity — it'll show which process was killed and how much memory it was using.
If you're in a container, check cgroup memory limits — even if you didn't set one explicitly, the node may have a default limit.
The OOM killer triggers when system-wide memory is exhausted, not just per-process.
Common causes: memory leak (see heap dump analysis), JVM heap set too large leaving no room for off-heap/metaspace, or a noisy neighbor on the same host consuming all RAM.
Fix: set explicit memory limits (Xmx for JVM, GOMEMLIMIT for Go), fix leaks, add swap as a safety net (though not ideal for latency), or move to a larger instance.
Monitor memory usage with Prometheus/node_exporter to catch trends before OOM."

---

## Q12 — Interface Timeout but CPU/Memory Normal (接口超时但CPU内存正常)

**EN:** An API endpoint times out (30s+) but CPU and memory usage are normal. What's likely wrong?

**CN:** 一个 API 接口超时（30秒以上）但 CPU 和内存都正常。可能是什么问题？

**中文解题说明：**
- CPU/内存正常说明不是计算密集或内存泄漏
- 常见原因：
    1. 外部依赖阻塞：调用下游服务/DB/第三方 API 卡住（无超时设置）
    2. 死锁/线程等待：线程在等锁、等连接池、等 IO
    3. 网络问题：DNS 解析慢、TCP 握手超时、连接被防火墙丢弃（重试慢）
    4. 数据库锁等待：行锁/表锁被长事务持有
    5. 文件 IO：读 NFS/网络存储卡住
- 排查：Thread dump 看线程在等什么、tcpdump 看网络、strace 看系统调用

**Spoken Answer (EN):**
"If CPU and memory are fine, the process is waiting on something — not computing.
Most likely: a blocking call to an external dependency with no timeout set. A downstream DB or API call hangs, and your thread sits there waiting forever.
Take a thread dump — look for threads in WAITING or BLOCKED state, check what lock or resource they're waiting on.
Could also be DNS resolution hanging (try timing a dig/curl), or a network storage mount (NFS) that's unresponsive.
Check database connection pool — if all connections are held by slow queries, new requests wait indefinitely.
Fix: always set timeouts on external calls (connect + read), use circuit breakers, add connection pool limits with timeout, and check network path with tcpdump/strace."

---

## Q13 — Data Inconsistency / Replication Lag (数据不一致/主从延迟)

**EN:** After writing data, subsequent reads sometimes return stale data. You use a primary DB with read replicas. How do you debug and fix?

**CN:** 写入数据后，后续读取有时返回旧数据。你用的是主库+只读副本架构。如何排查和修复？

**中文解题说明：**
- 根因：主从复制延迟（异步复制），写后立刻读可能落到从库拿到旧数据
- 排查：
    1. 检查复制延迟（Seconds_Behind_Master / pg_stat_replication）
    2. 确认读请求是否走了从库（读写分离中间件配置）
    3. 确认是否是特定接口有此问题
- 解决方案：
    1. 读写一致性：写后读走主库（关键路径）
    2. 延迟感知路由：从库延迟超过阈值时自动切主
    3. 同步复制（强一致但性能差）
    4. 应用层缓存写后短暂标记（sticky session）
    5. 业务层接受最终一致（非关键数据）

**Spoken Answer (EN):**
"This is classic replication lag — you write to primary, but read from a replica that hasn't caught up yet.
Check replication delay metrics: Seconds_Behind_Master in MySQL, or pg_stat_replication in Postgres.
If lag spikes correlate with write bursts or long-running queries on the replica, that's your cause.
Solutions: for read-after-write consistency, route reads for that user/session to the primary for a short window (30 seconds) after a write.
Or implement a 'read-your-own-writes' pattern: mark recently written keys and force those reads to primary.
Another option: use synchronous replication (wait for at least one replica to ack), but that adds latency to writes.
Long-term: monitor replica lag closely, tune replica hardware, and avoid long-running read queries on replicas that block replication replay."

---

## Q14 — DDoS vs Legitimate Traffic Spike (DDoS vs 正常流量高峰)

**EN:** Your site suddenly gets 100x normal traffic. How do you distinguish a DDoS attack from a legitimate viral spike, and how do you respond?

**CN:** 网站突然收到 100 倍正常流量。如何区分 DDoS 攻击和正常的病毒式流量高峰？如何应对？

**中文解题说明：**
- 区分方法：
    1. 流量来源分布：DDoS 通常来自少数 AS/国家/User-Agent 集中
    2. 请求模式：DDoS 请求路径单一（如只打一个接口）、无 cookie、无 JS 执行
    3. 用户行为：正常用户会浏览多个页面、停留时间、有转化行为
    4. 流量增长率：DDoS 是瞬间暴涨，正常 viral 是逐渐上升
- 应对：
    - DDoS：启用 WAF、IP 限流、Geo-blocking、Anycast 清洗、CDN 吸收
    - 正常高峰：弹性扩容、CDN 缓存、降级非核心功能

**Spoken Answer (EN):**
"Distinguishing them: look at traffic patterns. DDoS usually has a narrow request set — hammering one endpoint, no variety in URLs, no cookies, identical User-Agent strings, or coming from a concentrated set of IP ranges/ASNs.
Legitimate viral traffic shows diverse pages, real browser fingerprints, geographic spread matching your user base, and actual engagement (clicks, scrolls, conversions).
Growth curve also differs: DDoS spikes instantly; viral traffic ramps over minutes to hours.
Response: if DDoS, enable WAF rules, IP rate-limiting, geo-blocking, and lean on your CDN/DDoS protection provider (Cloudflare, AWS Shield).
If legitimate, auto-scale your fleet, push more to CDN edge, enable aggressive caching, and degrade non-critical features (disable recommendations, comments) to free resources for core functionality."

---

## Q15 — Retry Storm (重试风暴)

**EN:** A downstream service slows down slightly (from 50ms to 500ms). Suddenly your entire system becomes unresponsive due to retry storms. Explain and fix.

**CN:** 下游服务变慢了一点（从 50ms 到 500ms）。突然你的整个系统因为重试风暴变得无响应。解释原因并修复。

**中文解题说明：**
- 原因：下游变慢 → 上游请求超时 → 客户端/中间件自动重试 → 重试流量是正常 N 倍 → 下游更慢 → 恶性循环
- 雪崩效应：重试放大系数 = 重试次数 × 超时并发
- 修复：
    1. 客户端退避重试（Exponential Backoff + Jitter）
    2. 限制重试次数（最多 1 次重试）
    3. 熔断：失败率超阈值时停止重试
    4. 超时设置合理：不要无限等待
    5. 服务端限流：拒绝超额请求
    6. 舱壁模式（Bulkhead）：隔离不同依赖的资源池

**Spoken Answer (EN):**
"Here's what happened: downstream latency increased 10x (50→500ms). Your upstream clients start timing out and retrying — each original request becomes 2, 3, even 5 actual requests.
This retry amplification multiplies load on the already-slow downstream service, making it even slower, causing more timeouts, more retries — a death spiral.
Fixes: implement exponential backoff with jitter — don't retry immediately; wait and add randomness to spread retries.
Limit retries to at most 1 retry per request.
Add circuit breakers: when failure rate exceeds a threshold, stop sending requests entirely for a cooldown period.
Set aggressive timeouts — fail fast instead of holding threads.
On the server side, implement rate limiting and load shedding — reject excess requests with 429 so clients back off.
Use bulkhead pattern: isolate thread pools per dependency so one slow service doesn't exhaust all resources."

---

## Q16 — Connection Pool Exhaustion (连接池耗尽)

**EN:** Your app throws 'connection pool exhausted' errors during peak hours. How do you debug and fix?

**CN:** 应用在高峰期抛出"连接池耗尽"错误。如何排查和修复？

**中文解题说明：**
- 原因：
    1. 连接池配置太小（maxPoolSize 不够）
    2. 连接泄漏（借了没还——代码没 close）
    3. 慢查询占着连接不释放
    4. 突发流量超过池容量
- 排查：
    1. 监控连接池活跃连接数
    2. 检查是否有连接泄漏（借出时间分布）
    3. 查慢查询日志
- 解决：
    1. 增大 maxPoolSize（注意 DB 最大连接数限制）
    2. 修复连接泄漏（确保 try-with-resources / finally close）
    3. 加查询超时（statement timeout）
    4. 加连接等待超时（maxWaitMillis）快速失败
    5. 读写分离分散连接压力

**Spoken Answer (EN):**
"Connection pool exhaustion means all connections are in use and new requests are queuing or failing.
First, check pool metrics: active connections vs. max pool size. If active is always at max, you either need a bigger pool or connections aren't being returned.
Check for connection leaks — are connections borrowed but never closed due to missing try-with-resources or exceptions skipping close()?
Check average connection hold time — if it's high, slow queries are holding connections too long. Add statement timeouts.
Fixes: increase maxPoolSize (but mind the database's max_connections limit), fix leaks by ensuring proper close in finally blocks, add query timeouts so slow queries don't hog connections, and add a maxWait timeout so threads fail fast instead of blocking indefinitely.
Long-term: read/write split to distribute connections, or add a proxy layer like PgBouncer/ProxySQL."

---

## Q17 — Distributed Lock Failure (分布式锁失效)

**EN:** You use Redis-based distributed locks for critical sections, but under high concurrency the lock sometimes fails and two processes enter the critical section simultaneously. Why and how to fix?

**CN:** 你用基于 Redis 的分布式锁保护关键代码段，但在高并发下锁有时会失效，两个进程同时进入临界区。为什么？如何修复？

**中文解题说明：**
- 常见失效原因：
    1. 锁过期时间太短，业务没执行完锁就自动释放了（GC 停顿导致）
    2. 主从切换：在主节点加了锁，还没同步到从节点就故障切换了，新主没有锁记录
    3. 客户端 A 锁过期后，客户端 B 拿到锁，但 A 执行完误删了 B 的锁（没有 owner 校验）
    4. 时钟漂移
- 解决方案：
    1. 使用 Redlock 算法（多节点）
    2. 锁加唯一标识（UUID），释放时校验 owner（Lua 脚本原子操作）
    3. 锁续期（Watchdog 后台续约）
    4. 使用 Zookeeper/etcd 的 fencing token（更安全）

**Spoken Answer (EN):**
"Common Redis lock failures:
One, lock expires before the critical section finishes — the process pauses (GC, network), lock TTL fires, another process acquires the lock, then the first process resumes and both are in the critical section.
Two, Redis master fails over to a replica that didn't get the lock write — new master has no record of the lock.
Three, no ownership check — process A's lock expires, process B acquires, then A finishes and deletes B's lock.
Fixes: always set a unique owner ID (UUID) and use a Lua script to atomically check-and-delete on release.
Implement lock renewal (watchdog thread) to extend TTL while work is in progress.
For stronger guarantees, use Redlock (majority of Redis nodes must agree) or use Zookeeper/etcd with fencing tokens — they provide sequential consistency that Redis can't guarantee."

---

## Q18 — SSL/TLS Certificate Expiration (证书过期)

**EN:** Your production API suddenly starts failing with TLS/SSL errors for all clients. You discover the certificate expired. How do you prevent this from happening again?

**CN:** 生产 API 突然对所有客户端报 TLS/SSL 错误。发现是证书过期了。如何防止再次发生？

**中文解题说明：**
- 紧急修复：立即续签证书并部署（Let's Encrypt / 商业 CA）
- 根因：缺少证书过期监控和自动续期
- 预防措施：
    1. 监控告警：在证书过期前 30/14/7/1 天发送告警
    2. 自动续期：Let's Encrypt + Certbot 自动续签（cron job）
    3. 证书管理工具：HashiCorp Vault PKI、cert-manager（K8s）
    4. 多证书冗余：关键服务用多证书/多 CA
    5. 定期审计：脚本检查所有域名证书有效期
- 工具：crontab + openssl s_client、Prometheus blackbox exporter

**Spoken Answer (EN):**
"Immediate fix: renew the certificate and redeploy — for Let's Encrypt, run certbot; for internal CAs, generate a new cert and update your load balancer / ingress.
To prevent recurrence: first, add monitoring. Use Prometheus blackbox exporter or a simple cron script that checks certificate expiry via openssl s_client and alerts 30, 14, 7, and 1 day before expiry.
Second, automate renewal. For Let's Encrypt, certbot has auto-renew built in. For internal PKI, use HashiCorp Vault or cert-manager in Kubernetes.
Third, document the rotation process and test it in staging.
Fourth, consider running dual certificates during transitions so there's no downtime.
Fifth, add a calendar reminder and assign ownership — someone must be on-call for cert expiry alerts."

---

## Q19 — Full Table Scan in Production (生产环境全表扫描)

**EN:** A new feature deploys and suddenly DB load spikes. You find a missing index causing full table scans on a 100M row table. How do you respond?

**CN:** 新功能上线后 DB 负载飙升。发现是缺失索引导致对 1 亿行表的全表扫描。如何应对？

**中文解题说明：**
- 紧急处理：
    1. 立即回滚或 disable 该功能（如果可以）
    2. 如果不能回滚，紧急加索引（但 1 亿行加索引可能锁表）
    3. 用 pt-online-schema-change / gh-ost 在线加索引（不锁表）
    4. 临时限流：限制该接口的 QPS
- 事后复盘：
    1. 为什么没在 code review 发现？
    2. 为什么没在 staging 压测发现？（数据量不够）
    3. 加 SQL 审核工具（如 Yearning / Archery）
    4. 加慢查询告警

**Spoken Answer (EN):**
"Immediate response: if the feature can be toggled off via feature flag, disable it immediately to stop the scans.
If not, you need to add the index — but on a 100M row table, a normal ALTER TABLE will lock it.
Use an online schema change tool like gh-ost or pt-online-schema-change to add the index without locking.
In parallel, add rate limiting on that endpoint to reduce DB pressure.
Once the index is live, re-enable the feature.
Post-mortem: why wasn't this caught? Likely because staging data was too small to surface the scan cost.
Add SQL review to your CI pipeline — tools like Yearning or Archery catch missing index warnings.
Add slow query alerts on production so any full table scan triggers an immediate page.
And always EXPLAIN your queries before deploying."

---

## Q20 — Cascading Failure (级联故障)

**EN:** Service A depends on B, B depends on C. C slows down, then B fails, then A fails, then the whole system goes down. Walk through the failure propagation and how to prevent it.

**CN:** 服务 A 依赖 B，B 依赖 C。C 变慢，然后 B 挂了，然后 A 挂了，整个系统宕机。描述故障传播路径和预防措施。

**中文解题说明：**
- 传播路径：C 变慢 → B 的线程池被 C 的慢响应占满 → B 开始超时 → A 调用 B 也超时 → A 的线程池也满 → 全挂
- 这是典型的级联故障（Cascading Failure）
- 预防措施：
    1. 超时（Timeout）：每层设合理超时，快速失败
    2. 熔断（Circuit Breaker）：B 调用 C 失败率超阈值就熔断
    3. 舱壁隔离（Bulkhead）：不同依赖用独立线程池，互不影响
    4. 限流（Rate Limiting）：B 限制对 C 的并发调用数
    5. 降级（Fallback）：B 在 C 不可用时返回缓存/默认值
    6. 过载保护：A 在自身压力大时拒绝新请求（Load Shedding）
    7. 容量规划：C 的扩容预案

**Spoken Answer (EN):**
"Cascading failure propagation: C slows down → B's threads block waiting on C → B's thread pool exhausts → B starts timing out → A calls B, also blocks and times out → A's resources exhaust → entire system down.
It's a domino effect.
Prevention at every layer:
First, timeouts — every cross-service call must have a timeout. Don't wait forever.
Second, circuit breakers — when B detects C is failing, stop calling C and fail fast.
Third, bulkhead isolation — B should have separate thread pools for different dependencies; C's slowness shouldn't consume all threads.
Fourth, rate limiting — B should limit concurrent calls to C to protect it.
Fifth, fallback/degradation — B returns cached or default data when C is unavailable.
Sixth, load shedding — A should reject requests when overloaded (return 503) rather than accepting and failing.
Seventh, capacity planning — ensure C has headroom and auto-scaling configured.
Netflix's Chaos Engineering philosophy: test these scenarios in staging by injecting failures (kill C, add latency) to verify your defenses work."