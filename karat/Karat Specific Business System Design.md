# Karat Specific Business System Design — 20 Drill Questions (面试详版)

---

## Q1 — URL Shortener (TinyURL / bit.ly)（短链系统）

**EN:** Design a URL shortener handling 100M new URLs/day, 1B redirects/day. Support custom aliases, expiration, click analytics. Discuss encoding, storage, cache, and redirect flow.

**CN:** 设计短链系统，日新增1亿短链、日重定向100亿次。支持自定义别名、过期、点击统计。讨论编码、存储、缓存、跳转流程。

**中文解题说明：**
- **发号器方案**：
    - 分布式 ID 生成器（Snowflake/Leaf-segment）产生全局递增 ID → Base62 编码（0-9a-zA-Z，6位可表示 62^6≈568亿）
    - 预生成随机短码：先批量生成不重复的 7 位随机串存入 DB，分配时直接取（避免碰撞重试）
- **存储设计**：
    - `urls(short_code VARCHAR PK, long_url TEXT, user_id BIGINT, created_at, expire_at, click_count BIGINT)`
    - 按 short_code 分片（hash），读远多于写
- **缓存层**：
    - Redis 缓存热点短链（LRU），TTL 根据热度设几小时~几天
    - 布隆过滤器挡无效短码（防止缓存穿透）
- **跳转流程**：
    - DNS → CDN/Load Balancer → 应用层查缓存 → 命中直接 302 跳转；未命中查 DB 并回填缓存
    - 点击统计：异步写入消息队列 → 批量更新 click_count + 明细日志（用于分析）
- **自定义别名**：检查唯一性后插入，冲突返回错误
- **过期清理**：定时任务扫描 expire_at < now() 删除或标记（也可懒删除：访问时检查过期）

**Spoken Answer (EN):**
"Core flow: generate a unique short code (6-7 chars via Base62 encoding of a distributed ID or pre-generated random strings), store mapping in DB, cache hot links in Redis with LRU eviction. Redirect is a simple 302 lookup — check cache first, fallback to DB, async-increment click counter via message queue. Scale: hash-partition by short_code, use CDN at edge for geo latency. Custom aliases just need a uniqueness check before insert. Expiration via lazy-delete on access + daily sweep job. Bloom filter on cache miss prevents DB hammering from invalid codes."

---

## Q2 — Flash Sale / Seckill（秒杀系统）

**EN:** 10,000 iPhone units, 1M users rush at exact timestamp. Design to prevent oversell, DB meltdown, and bot abuse. Discuss inventory pre-warming, request filtering pipeline, async order creation.

**CN:** 1万部 iPhone，100万人准点抢购。设计防超卖、防 DB 崩溃、防机器人刷单。讨论库存预热、请求过滤链路、异步下单。

**中文解题说明：**
- **分层漏斗（逐层递减流量）**：
    - L1：Nginx 限流（per-IP 令牌桶），挡掉明显刷子
    - L2：风控服务（用户画像、设备指纹、行为特征）标记可疑请求
    - L3：Redis Lua 脚本原子扣减库存（单线程保证不超卖），库存≤0 直接返回"已抢完"
    - L4：扣减成功后才允许进入下单队列
- **库存预热**：
    - 活动开始前将库存加载到 Redis（如 `iphone_stock = 10000`）
    - Lua 脚本：`if redis.call('GET',key)>0 then redis.call('DECR',key) return 1 else return 0 end`
- **异步下单**：
    - 扣库存成功后，发送消息到 MQ（携带 userId+itemId）
    - 消费者从 MQ 拉取，写 DB 创建订单（最终一致）
    - 用户轮询订单状态（或 WebSocket 推送）
- **防重复购买**：Redis Set(userId) 记录已抢用户，`SADD` 返回 0 则拒绝
- **DB 兜底**：`UPDATE items SET stock=stock-1 WHERE id=? AND stock>0`（乐观锁，防止 Redis 崩溃后超卖）

**Spoken Answer (EN):**
"Multi-layer funnel: Nginx rate-limit per IP → risk engine filters bots → Redis Lua atomic DECR for inventory (single-threaded, zero oversell) → only successful decrements enter an MQ for async order creation in DB. This reduces DB writes from 1M to exactly 10,000. Per-user dedup via Redis Set prevents one person grabbing multiple. DB has a final optimistic lock: `UPDATE stock SET cnt=cnt-1 WHERE cnt>0`. If Redis fails, fall back to DB pessimistic lock briefly. Users poll order status — no synchronous wait."

---

## Q3 — Social Media Feed / Timeline（社交 Feed 流）

**EN:** Design Twitter-like home timeline for 500M users (avg 200 follows). Support posting tweets, viewing timeline, pagination. Compare push vs pull vs hybrid.

**CN:** 设计 Twitter 式首页时间线，5亿用户（人均关注200）。支持发推、看时间线、翻页。对比推模式、拉模式、混合模式。

**中文解题说明：**
- **推模式 (Fan-out on write)**：
    - 发推时写入粉丝的 inbox（每个粉丝一个 Redis Sorted Set，score=timestamp，value=tweetId）
    - 读时间线 = `ZREVRANGE user:inbox 0 19` → O(1) 读
    - 缺点：大V（百万粉丝）发一条要写百万次 → 写风暴
- **拉模式 (Fan-in on read)**：
    - 只存自己的推文表，读时查关注列表所有人最近推文 → 合并排序
    - 优点：写轻；缺点：读重（关注200人=200次查询+归并），延迟高
- **混合模式（业界主流）**：
    - 普通用户（粉丝<1万）：推模式写入粉丝 inbox
    - 大V（粉丝>1万）：只存自己时间线，读时单独拉取 merge 到结果中
    - 离线任务：大V推文异步合并到粉丝 inbox（降低读延迟）
- **存储**：
    - 推文表 `tweets(id, user_id, content, created_at)` 按 user_id 分片
    - Inbox `user_timeline(user_id, tweet_id, score)` Redis ZSET
    - 关注列表走 `follows` 表
- **翻页**：游标 `(max_score, max_id)` 避免 offset

**Spoken Answer (EN):**
"Hybrid fan-out: for normal users, push tweet to all followers' inboxes (Redis ZSET per user, score=timestamp) — read is O(1) ZREVRANGE. For celebrities with >10K followers, don't push; instead pull their tweets on-read and merge with the inbox results. This avoids write storms while keeping read latency low for 99% of cases. Pagination via cursor (last_seen_timestamp, tweet_id). Storage: tweets table sharded by user_id, inbox in Redis with TTL eviction for old entries, cold data archived to object storage."

---

## Q4 — Shopping Cart（购物车系统）

**EN:** Design cart for 100M users, support add/remove/update quantity, merge guest+logged-in cart, expiration, settlement. Discuss storage choice, data structure, merge logic.

**CN:** 设计购物车系统（1亿用户），支持增删改数量、未登录/登录合并、过期、结算。讨论存储选型、数据结构、合并逻辑。

**中文解题说明：**
- **未登录购物车**：
    - 前端 localStorage / cookie 存 JSON 数组 `[{skuId, qty, selected, addTime}]`
    - 容量有限（cookie ~4KB），超量提示登录
- **登录后购物车**：
    - Redis Hash：`cart:{user_id} → {field: skuId, value: JSON({qty, selected, price_snapshot, add_time})}`
    - TTL 30天滑动续期
    - 优点：O(1) 增删改、天然过期、高并发
- **合并逻辑**：
    - 登录时后端读取 cookie/localStorage 中的购物车
    - 逐 SKU 合并：若已存在则 qty 相加（不超过库存上限），不存在则新增
    - 合并后删除客户端 cookie，后续全走 Redis
- **结算**：
    - 勾选商品 → 从购物车取 SKU 列表 → 调用库存校验 → 创建订单 → 清空已购项
- **降级**：Redis 不可用时购物车退化为 DB `cart_items(user_id, sku_id, qty, selected)` 表

**Spoken Answer (EN):**
"Guest cart lives in localStorage/cookie as JSON. On login, server reads it and merges into Redis Hash `cart:{user_id}` — for each SKU, sum quantities (cap at max allowed), deduplicate. Post-merge, cookie is cleared and all future ops hit Redis. Redis Hash gives O(1) per-SKU updates and automatic TTL renewal on every action. Settlement reads selected items, validates inventory in real-time, creates order, then HDEL those SKUs from cart. Fallback to DB if Redis is down — eventual consistency is fine for cart."

---

## Q5 — Distributed Unique ID Generator（分布式唯一 ID 生成器）

**EN:** Design a service generating 100K IDs/sec, globally unique, roughly time-ordered, <20 bytes. Compare Snowflake, UUIDv7, Leaf-segment, and discuss clock skew handling.

**CN:** 设计服务生成 10万 ID/秒，全局唯一、大致时间有序、<20字节。对比雪花、UUIDv7、Leaf-segment，讨论时钟回拨处理。

**中文解题说明：**
- **Snowflake (64-bit)**：
    - 结构：1位符号 + 41位时间戳(ms) + 10位机器ID + 12位序列号
    - 每机器每毫秒最多 4096 个 ID
    - 优点：8字节紧凑、有序、无网络开销
    - 缺点：时钟回拨导致 ID 重复（解决方案：等待时钟追上 / 回拨时序列号从高位继续 / 用 ZooKeeper 分配 workerId 并检测漂移）
- **UUIDv7**：
    - 48位毫秒时间戳 + 74位随机/计数器
    - 优点：无需协调、时间有序、16字节
    - 缺点：稍大、随机部分仍可能导致页分裂
- **Leaf-segment (美团)**：
    - DB 分配号段：`UPDATE table SET max_id=max_id+step WHERE biz_tag='order'` → 应用缓存 [current, max_id]
    - 优点：ID 严格递增、DB 压力小（step=1000 则 1000次才访问一次 DB）
    - 缺点：依赖 DB（可双 DB 高可用）、号段用完瞬间有延迟
- **选型建议**：
    - 高并发低延迟：Snowflake（需解决时钟问题）
    - 简单无依赖：UUIDv7
    - 业务需要严格递增：Leaf-segment

**Spoken Answer (EN):**
"Three main options. Snowflake: 64-bit (timestamp + worker_id + sequence), 8 bytes, ordered, ~4M IDs/sec per worker. Clock skew is the gotcha — solve by waiting for clock to catch up or using a ZooKeeper-backed workerId allocator. UUIDv7: 16 bytes, embeds timestamp prefix, zero coordination, good default if you don't want infra dependency. Leaf-segment: DB-managed ranges (fetch 1000 at a time), strictly increasing, great for business IDs needing monotonic order, but adds DB dependency. For 100K/sec I'd pick Snowflake variant with NTP + clock-monitoring and a fallback to UUIDv7 if clock drifts beyond threshold."

---

## Q6 — Real-time Leaderboard（实时排行榜）

**EN:** Gaming leaderboard: 50M players, score updates 10K/sec, need top 100, percentile rank, and per-player rank lookup. Design with Redis ZSET and discuss scaling beyond single instance.

**CN:** 游戏排行榜：5000万玩家，分数更新 1万/秒，需 Top100、百分位排名、查个人名次。用 Redis ZSET 设计并讨论超出单实例的扩展。

**中文解题说明：**
- **基础方案**：Redis ZSET
    - `ZADD leaderboard {score} {player_id}` 更新分数
    - `ZREVRANGE leaderboard 0 99 WITHSCORES` 取 Top100
    - `ZREVRANK leaderboard {player_id}` 查个人排名
    - `ZCOUNT leaderboard {min} +inf` 算百分位
- **单实例瓶颈**：
    - 内存：5000万 × (8字节score + 8字节id) ≈ 1.2GB，单实例可承受
    - 写入：1万/秒 ZADD，单 Redis 轻松处理（~10万/秒）
- **超出单实例**：
    - 按服务器/段位分 key：`leaderboard:server1`、`leaderboard:diamond`
    - 全局榜定时聚合（每小时从各分片拉 Top1000 合并）
    - 或用 Redis Cluster（注意 ZSET 操作必须同 slot → 用 hash tag `{global}` 强制同 slot，但丧失分布性）
- **周榜/季榜**：独立 key + TTL，到期自动销毁
- **防作弊**：分数更新需校验（服务端计算，不信任客户端）

**Spoken Answer (EN):**
"Redis ZSET is perfect: ZADD for updates (atomic, O(log N)), ZREVRANGE for top-N (O(N)), ZREVRANK for individual rank. At 50M members and 10K updates/sec, a single Redis instance handles it — memory ~1-2GB, throughput well within limits. If you outgrow one instance: shard by segment/tier (bronze/silver/gold leaderboards) so each fits on one node. Global cross-tier leaderboard computed periodically offline by merging each shard's top players. Use separate ZSET keys per time window (weekly/monthly) with TTL auto-expiry."

---

## Q7 — IM Messaging System（即时通讯系统）

**EN:** Design 1-on-1 and group chat for 100M DAU. Support offline messages, read receipts, multi-device sync, message ordering. Discuss storage model, push vs pull, message diffusion for groups.

**CN:** 设计支持单聊和群聊的 IM 系统（1亿 DAU）。支持离线消息、已读回执、多端同步、消息排序。讨论存储模型、推拉模式、群消息扩散。

**中文解题说明：**
- **整体架构**：客户端 → 接入层(长连接网关) → 逻辑层 → 存储/MQ
- **消息存储模型**：
    - `messages(conversation_id, sender_id, msg_type, content, seq_id, created_at)`
    - `conversations(id, type{single/group}, members_json, last_msg_id)`
    - seq_id 全局/会话内单调递增（保证顺序）
- **消息投递**：
    - 在线：网关推送(Push) 到客户端长连接
    - 离线：存离线消息表 `offline_msgs(user_id, msg_id, received)` → 上线后拉取
    - 多端同步：每个设备维护 sync_key（最后收到的 seq_id），同步时 `WHERE seq_id > sync_key`
- **群消息扩散**：
    - 小群（<200人）：写扩散——发一条消息写入每个成员的收件箱
    - 大群（>200人）：读扩散——只存群消息表，成员读时拉取（配合增量游标）
- **已读回执**：`read_receipts(conversation_id, user_id, read_seq_id)` 记录已读位置
- **顺序保证**：seq_id 用 Snowflake 或 分段发号器，同一会话内严格递增

**Spoken Answer (EN):**
"Two storage patterns: write-heavy fan-out for small groups (write once per member into their inbox table), read-heavy pull for large groups (single group message stream, members fetch with cursor). Each conversation has a monotonically increasing seq_id (via Snowflake or per-conversation counter) for ordering. Online delivery via persistent WebSocket gateway push; offline messages queued in per-user offline table, pulled on login. Read receipts tracked per conversation per user (last_read_seq). Multi-device sync uses sync_key (last received seq) to fetch deltas. Scale: partition by conversation_id, cache recent messages in Redis."

---

## Q8 — API Gateway Rate Limiting & Circuit Breaker（网关限流与熔断）

**EN:** Design rate limiting for an API gateway serving 1M req/sec. Support per-user, per-IP, per-API limits. Also discuss circuit breaker pattern to prevent cascade failure.

**CN:** 为 API 网关设计限流（100万请求/秒），支持按用户/IP/接口限流。讨论熔断器模式防止级联故障。

**中文解题说明：**
- **限流算法**：
    - **令牌桶**：Guava RateLimiter 思路，支持突发（桶容量允许短时超过平均速率）
    - **漏桶**：恒定速率处理，严格平滑
    - **滑动窗口**：Redis `ZSET` 存请求时间戳，count 窗口内请求数
    - **固定窗口**：简单但有临界突刺问题
- **分布式限流**：
    - Redis + Lua 原子脚本：`INCRBY + EXPIRE` 或 `ZREMRANGEBYSCORE + ZCARD`
    - 按 key 维度：`rate_limit:{user_id}` / `rate_limit:{ip}:{api}`
    - 多级限流：本地令牌桶（单机维度）+ 分布式限流（全局维度）
- **熔断模式**：
    - 状态机：Closed（正常）→ Open（错误率超阈值，直接失败）→ Half-Open（试探放行少量请求）
    - 指标收集：滑动窗口统计成功率/延迟
    - 实现：Hystrix/Sentinel/Resilience4j
- **降级**：熔断后返回默认值/缓存/排队页面

**Spoken Answer (EN):**
"Rate limiting: two-tier. Local token bucket per gateway node (fast, approximate) + Redis Lua script for global accurate counting (keyed by user_id or IP). Use sliding window instead of fixed window to avoid boundary spikes. For circuit breaker: each downstream dependency has a state machine — Closed (requests pass, track error rate), Open (fail fast when error rate > threshold for N seconds), Half-Open (let one probe request through). If probe succeeds, close circuit; else stay open. Sentinel/Hystrix-style. Degradation: return cached response or 'try later' page when open."

---

## Q9 — Red Packet / Lucky Money（微信红包系统）

**EN:** Design a red packet system: sender sets total amount and count, receivers grab randomly. Guarantee exact total distribution, no double-grab, handle 100K grabs/sec during Chinese New Year.

**CN:** 设计红包系统：发红包设总金额和份数，抢红包随机分配。保证总额精确、不重复抢、春晚峰值 10万次/秒。

**中文解题说明：**
- **红包金额分配算法**：
    - 二倍均值法：每次从剩余金额中按 `[0.01, 剩余金额/剩余份数 × 2]` 随机，保证不会先抢者占优太多
    - 预分配：发红包时一次性算好所有金额存入 Redis List
- **抢红包流程**：
    - `LPOP red_packet:{id}` 原子取一个金额 → 成功则记录领取关系
    - 领取记录表 `packet_records(packet_id, user_id, amount, created_at)` 唯一键 `(packet_id, user_id)` 防重复
- **高并发**：
    - Redis Cluster 分片，红包 key 按 packet_id hash
    - 预分配金额列表到 Redis List，抢时 LPOP 无锁
    - 异步落库：抢成功后发 MQ → 消费者写 DB（最终一致）
- **过期退还**：定时任务扫描未抢完的红包 → 退回余额
- **金额精度**：用分（整数）避免浮点误差

**Spoken Answer (EN):**
"Two-phase: creation pre-generates all amounts via double-mean algorithm (random between 0.01 and 2×remaining_avg) and pushes them into a Redis List `packet:{id}`. Grab = atomic LPOP from that list. This gives O(1) no-lock grabs at 100K+/sec. Grab success writes to a dedup table (unique on packet_id+user_id) to prevent double-grabs, then async-persists to DB via MQ. Unclaimed amounts auto-refunded after expiry via sweep job. Amounts stored as integers (cents) to avoid float precision issues."

---

## Q10 — Delayed Task Queue / Order Auto-Cancel（延时任务/订单自动取消）

**EN:** Orders auto-cancel if unpaid for 30 minutes. Design a system handling 1M delayed tasks/sec with at-least-once delivery and no premature execution.

**CN:** 订单30分钟未支付自动取消。设计处理 100万延时任务/秒的系统，保证至少一次执行且不提前触发。

**中文解题说明：**
- **方案对比**：
    - 数据库轮询：定时扫 `WHERE status='pending' AND created_at < now()-30min` → 简单但有延迟、DB压力大
    - Redis ZSET：score=执行时间戳，`ZRANGEBYSCORE` 轮询取到期任务 → 轻量但有精度限制
    - 时间轮（HashedWheelTimer）：O(1) 插入/删除，适合短延时
    - RocketMQ/Kafka 延时消息：内置延时级别或任意时间
- **推荐架构**：
    - 创建订单时发延时消息（RocketMQ 延时级别18个，或自定义 Kafka topic + 消费者延迟处理）
    - 兜底：定时任务每5分钟扫一次（防止消息丢失）
- **去重/幂等**：消费者检查订单状态，已取消/已支付则跳过
- **精度**：秒级精度足够（30分钟±几秒可接受）

**Spoken Answer (EN):**
"Primary path: on order creation, publish a delayed message (RocketMQ delay level or Kafka with custom delay topic) with 30-min delivery. Consumer receives and cancels if still unpaid. For higher precision and scale: Redis ZSET where score=execute_at, a worker polls ZRANGEBYSCORE every second to fetch due tasks and pushes to execution queue. Critical: idempotent cancel handler (check status before update). Always have a DB sweep job as safety net — queries orders stuck in pending >30min — catches any message loss. At 1M/sec you'd shard the ZSET by time bucket or use a distributed time-wheel library."

---

## Q11 — Coupon / Voucher System（优惠券系统）

**EN:** Platform issues millions of coupons per campaign. Each coupon has limited stock, per-user limit. Support grab, validate at checkout, and prevent oversell/duplicate-grab.

**CN:** 平台每场活动发放百万级优惠券，每张限量、每人限领。支持领取、结算校验、防超发/重复领。

**中文解题说明：**
- **数据模型**：
    - `coupon_campaigns(id, total_stock, per_user_limit, start_time, end_time, status)`
    - `user_coupons(id, user_id, campaign_id, status{claimed/used/expired}, claimed_at, used_at)`
    - 唯一索引 `(user_id, campaign_id)` 防重复领
- **领取流程**：
    - 校验活动状态/时间 → Redis Lua 原子扣库存 `DECR campaign:{id}:stock` → 成功则写 `user_coupons` 表
    - 库存 key 预热到 Redis
- **结算核销**：
    - 校验 user_coupon 状态=claimed、未过期 → 更新为 used → 调用订单服务抵扣
    - 乐观锁防并发核销
- **库存回滚**：订单取消/超时未支付 → 恢复优惠券状态为 claimed + `INCR` 库存
- **防刷**：风控校验 + 图形验证码 + 限流

**Spoken Answer (EN):**
"Campaign stock pre-loaded to Redis. Grab flow: Lua script atomically checks per-user count (via Redis INCR on user_counter key) and decrements stock in one shot — if both pass, return success; else reject. Then async-insert into user_coupons table (unique index on user_id+campaign_id as final guard). At checkout: validate coupon belongs to user, is in 'claimed' state, not expired — then CAS-update to 'used'. On order cancel: revert status to 'claimed' and INCR stock. Safety net: daily reconciliation between Redis stock and DB counts."

---

## Q12 — Inventory Deduction & Oversell Prevention（库存扣减与超卖防护）

**EN:** E-commerce inventory: 100 SKUs, some hot (10K orders/sec). Design deduction flow ensuring no oversell, minimal lock contention, and fast recovery on failure.

**CN:** 电商库存：100个SKU，部分热点（1万单/秒）。设计扣减流程保证不超卖、最小锁竞争、故障快速恢复。

**中文解题说明：**
- **架构**：Redis 预扣 → MQ 异步落库 → DB 最终校验
- **Redis 扣减**：
    - 库存按 SKU 分 key：`stock:{sku_id}`
    - Lua 脚本原子校验+扣减：`if GET(key) >= qty then DECRBY key qty return 1 else return 0`
- **热点拆分（库存分桶）**：
    - 单 key 热点 → 拆为 `stock:{sku_id}:{bucket_0..15}`
    - 扣减时随机选桶，失败则尝试下一桶
    - 读取时 SUM 所有桶
- **DB 层兜底**：
    - `UPDATE inventory SET stock=stock-qty WHERE sku_id=? AND stock>=qty`
    - 影响行数=0 说明超卖（回滚 Redis）
- **归还库存**：订单取消 → Redis INCRBY + MQ 异步更新 DB
- **对账**：定时任务核对 Redis 与 DB 库存差异，修复不一致

**Spoken Answer (EN):**
"Redis pre-deduction via Lua atomic script — single-threaded, zero oversell. For hot SKUs (10K/sec), shard inventory into 16 buckets (stock:sku:bucket_0..15), randomly pick a bucket per order to spread lock contention; read = SUM all buckets. After Redis success, publish to MQ for async DB persistence. DB has final guard: `UPDATE inventory SET stock=stock-qty WHERE stock>=qty`. On failure, compensate by refunding Redis stock. Order cancellation reverses both layers. Daily reconciliation job compares Redis vs DB and fixes drift."

---

## Q13 — LBS / Nearby People（附近的人 / LBS 地理搜索）

**EN:** Find users within 5km of a given location, sorted by distance. 100M users, 10K location updates/sec. Design storage, indexing, and query flow.

**CN:** 查给定位置 5km 内的用户，按距离排序。1亿用户，1万次位置更新/秒。设计存储、索引、查询流程。

**中文解题说明：**
- **GeoHash 原理**：
    - 经纬度 → 二进制交错 → Base32 编码（如 `wx4g0b`）
    - 前缀相同 ≈ 地理位置接近
    - 精度：5位 GeoHash 约 ±2.4km，6位约 ±0.6km
- **存储方案**：
    - Redis GEO：`GEOADD` 存位置，`GEORADIUS` 查询 → 底层用 ZSET + GeoHash
    - 或 Elasticsearch `geo_point` 字段 + `geo_distance` 查询
- **查询流程**：
    - 计算目标点 GeoHash → 取周围 8 个相邻网格
    - 对每个网格内用户计算精确距离（Haversine 公式）
    - 按距离排序返回 TopN
- **更新频率**：位置更新频繁 → Redis 内存存储合适（自动过期旧位置）
- **分片**：按 GeoHash 前缀分片，同城用户同分片

**Spoken Answer (EN):**
"Use GeoHash to convert (lat, lng) into a string prefix — nearby points share prefixes. Store in Redis GEO (backed by sorted set with geohash as score): GEOADD to update location, GEORADIUS to query within 5km, sorted by distance. Under the hood Redis does bounding-box filter then precise distance calc. At 100M users and 10K updates/sec, Redis Cluster sharded by geohash prefix keeps load balanced. For more complex geo-queries (polygon, route-aware), use Elasticsearch geo_point with geo_distance filter. Cache popular area results (dense urban) with short TTL."

---

## Q14 — Comment & Like System（评论与点赞系统）

**EN:** Social post with nested comments (3 levels deep) and like counts. 10M posts, avg 500 comments/post, 1K likes/post. Design schema, pagination, like deduplication, hot comment ranking.

**CN:** 社交帖子支持嵌套评论（3层深）和点赞计数。1000万帖子，平均500评论/帖，1000赞/帖。设计 schema、分页、点赞去重、热评排序。

**中文解题说明：**
- **评论表**：
    - `comments(id, post_id, parent_id, root_id, user_id, content, like_count, created_at)`
    - `root_id` 标记顶级评论（方便查某帖所有一级评论）
    - `parent_id` 指向直接父评论（支持嵌套）
    - 索引：`(post_id, root_id, created_at)` 查某帖评论树
- **分页**：游标 `(created_at, id)` 避免 offset
- **嵌套展示**：先取一级评论列表 → 每条展开前 N 条子评论 → "查看更多回复" 异步加载
- **点赞**：
    - 关系表 `likes(user_id, target_type, target_id, created_at)` 唯一键防重复
    - 计数：`post_like_count(post_id, count)` Redis INCR 异步刷 DB
    - 热门评论排序：综合赞数 + 时间衰减（如 `score = likes / (age_in_hours+2)^1.5`）

**Spoken Answer (EN):**
"Comments table with (post_id, parent_id, root_id) — root_id for top-level grouping, parent_id for nesting. Query top-level comments by (post_id, root_id=0, created_at DESC) with cursor pagination. For each top comment, pre-fetch first 3 child replies. Likes: separate relation table with unique constraint (user_id, target_type, target_id) prevents double-likes; counter cached in Redis ZINCRBY per post, flushed async to DB. Hot comment ranking: score = like_count / pow(age_hours+2, 1.5) — surfaces recent high-quality comments. At scale, shard comments by post_id so one post's tree stays on one shard."

---

## Q15 — Configuration Center / Dynamic Config（配置中心）

**EN:** Design a config center like Apollo/Nacos serving 10K apps, 100K instances. Support config versioning, gray release, real-time push, and local fallback.

**CN:** 设计配置中心（类似 Apollo/Nacos），服务 1万应用、10万实例。支持版本管理、灰度发布、实时推送、本地兜底。

**中文解题说明：**
- **数据模型**：
    - `configs(app_id, namespace, key, value, version, env)`
    - `releases(id, app_id, version, configs_json, status, gray_rule)`
- **客户端拉取**：
    - 启动时拉全量配置 + 本地文件缓存（防中心不可用）
    - 长轮询（Long Polling）：客户端持连接等 30s，服务端有变更立即响应
- **推送变更**：
    - 配置发布后写 `release_message` 表（带 version）
    - 客户端轮询 `release_message` 或服务端主动 HTTP 通知
    - 灰度：按 IP/标签匹配规则，只推给部分实例
- **版本管理**：每次发布生成新版本号，支持回滚
- **高可用**：
    - 配置中心集群 + DB 主从
    - 客户端本地缓存（文件/内存），中心宕机仍可启动

**Spoken Answer (EN):**
"Server stores configs in DB with versioning; on publish, writes a release message with new version. Clients use long-polling (hold connection up to 30s) to detect changes — server responds immediately when a new version is available. Client fetches diff, updates local cache (memory + disk file for cold start fallback). Gray release via IP/tag-based rules — only matching instances receive new config. Rollback = republish previous version. Scale: 100K instances handled by clustering config servers behind load balancer; DB read replicas for fetch traffic. Client-side disk cache ensures app can still boot if config center is fully down."

---

## Q16 — Message Queue Selection & Guarantees（消息队列选型与保障）

**EN:** Choose between Kafka, RabbitMQ, RocketMQ for different use cases. Discuss ordering, exactly-once, deduplication, handling consumer lag.

**CN:** 对比 Kafka、RabbitMQ、RocketMQ 选型。讨论顺序性、精确一次、去重、消费积压处理。

**中文解题说明：**
- **Kafka**：
    - 优点：超高吞吐（百万/秒）、持久化、多消费者组、分区有序
    - 缺点：运维复杂、延迟较高（ms级）
    - 顺序：同 partition 内严格有序 → 按业务 key hash 到同 partition
    - 精确一次：幂等 producer + 事务（0.11+）
- **RabbitMQ**：
    - 优点：低延迟（μs~ms）、灵活路由（exchange+binding）、易运维
    - 缺点：吞吐较低（万/秒级）、无分区概念
    - 顺序：单队列有序，但单队列是性能瓶颈
- **RocketMQ**：
    - 优点：高吞吐、事务消息、延时消息、Tag 过滤
    - 缺点：社区较小
- **消费积压处理**：
    - 增加 consumer 实例（受限于分区数）
    - 临时扩容 consumer 数量 > 分区数无效
    - 紧急处理：新增临时 topic + 更多 partition + 更多 consumer 并行消费后写回
- **去重**：consumer 端幂等（Redis SETNX messageId / DB 唯一约束）

**Spoken Answer (EN):**
"Kafka for high-throughput event streaming (logs, analytics) — partition-level ordering via key hashing, at-least-once by default, exactly-once with idempotent producer + transactions. RabbitMQ for low-latency task queues (order processing) — flexible routing, great for point-to-point, but ordering requires single queue (bottleneck). RocketMQ for transactional outbox pattern (distributed transactions with message queue). For consumer lag: scale consumers up to partition count; if still lagging, increase partitions (careful, breaks ordering) or add a parallel processing layer. Deduplication always at consumer: store processed message IDs in Redis with TTL."

---

## Q17 — Check-in / Continuous Sign-in（签到与连续签到）

**EN:** Daily check-in system for 200M users. Track consecutive days, monthly calendar view, bonus for streaks. 500K check-ins/sec at 8am peak.

**CN:** 2亿用户每日签到系统。记录连续天数、月历视图、连续奖励。早8点高峰 50万签到/秒。

**中文解题说明：**
- **Bitmap 方案**：
    - 每月一个 key：`checkin:{user_id}:{yyyyMM}` → BITFIELD/BITSET
    - 每天对应一个 bit（1=签到，0=未签）
    - 签到：`SETBIT key offset 1`
    - 查连续天数：从当天 bit 往前数连续为1的个数（位运算）
    - 月历：取整月 bitmap → 前端解析
- **优势**：极省空间（每月 31 bit ≈ 4 字节/用户），操作 O(1)
- **连续天数计算**：
    - Redis BITFIELD 取范围 → 位运算数末尾连续1
    - 或 Lua 脚本逐位检查
- **奖励发放**：连续7天/30天触发奖励（异步事件）
- **峰值处理**：Redis Cluster 按 user_id 分片，50万/秒轻松应对

**Spoken Answer (EN):**
"Bitmap per user per month: key=`checkin:{uid}:202401`, SETBIT day_offset 1 on check-in. Storage is ~4 bytes per user per month — trivial for 200M users. Consecutive day count: BITFIELD to fetch last N days as integer, then bit-twiddle to count trailing ones from today backwards. Monthly calendar: GET whole bitmap, client decodes bits. Peak 500K/sec handled by Redis Cluster sharded by user_id. Streak rewards triggered by checking count after each check-in — if divisible by 7/30, publish award event to MQ. Cold data archived to compressed bitmap in DB after 6 months."

---

## Q18 — Mutual Follow / Common Friends（互关与共同好友）

**EN:** Given two users, find their common friends/follows. 500M users, avg 200 follows. Design query for "mutual follows" and "people you may know".

**CN:** 给定两个用户，找共同关注/好友。5亿用户，人均关注200。设计"互关"和"可能认识的人"查询。

**中文解题说明：**
- **共同关注**：
    - 方案1：Redis Set `follows:{user_id}` 存关注集合 → `SINTER follows:A follows:B` → O(min(N,M))
    - 方案2：DB 自连接 `SELECT f1.followee_id FROM follows f1 JOIN follows f2 ON f1.followee_id=f2.followee_id WHERE f1.follower_id=A AND f2.follower_id=B`
- **可能认识的人（推荐）**：
    - 二度关系：朋友的关注（排除已关注的）
    - 离线计算：图计算框架（Spark GraphX）定期算二度/三度关系
    - 实时简化：随机取 N 个好友 → 取他们的关注 → 按出现频次排序 → 推荐 TopK
- **存储**：`follows` 表双索引 `(follower_id)` + `(followee_id)`
- **优化**：共同好友结果缓存（TTL 1小时），因为关系变化不频繁

**Spoken Answer (EN):**
"For mutual follows: Redis Set per user storing followee_ids — SINTER set_A set_B returns common in O(min set size). At 200 follows avg, that's trivial. For 'people you may know': offline graph computation (Spark/GraphX) pre-computes 2nd-degree connections daily, stores top recommendations per user in Redis. Real-time fallback: sample 20 friends of the user, union their followee sets, remove already-followed, count frequency, return top-K. Cache mutual results with 1-hour TTL since social graphs change slowly."

---

## Q19 — Payment System / State Machine（支付系统状态机）

**EN:** Design payment processing flow: create order → redirect to gateway → handle async callback → finalize. Discuss state machine, idempotency, reconciliation, handling double-callbacks.

**CN:** 设计支付处理流程：创建订单→跳转网关→处理异步回调→完成。讨论状态机、幂等、对账、重复回调处理。

**中文解题说明：**
- **状态机**：
    - INIT → PAYING → SUCCESS / FAILED / CLOSED
    - 终态不可逆（SUCCESS 不能转回 PAYING）
- **支付单表**：
    - `payments(id, order_id, amount, channel, status, channel_tx_id, created_at, updated_at, version)`
    - `channel_tx_id` 唯一索引（防重复回调）
- **回调处理**：
    - 验签 → 查支付单 → CAS 更新状态（`UPDATE ... SET status='SUCCESS' WHERE id=? AND status='PAYING'`）
    - 更新成功才处理后续（发货/通知）→ 失败说明已处理过（幂等返回 success）
- **主动查询补偿**：定时任务查 `status='PAYING' AND updated_at < now()-5min` → 调渠道查询接口确认状态
- **对账**：T+1 拉取渠道对账文件 → 与本地 payment 表逐笔核对 → 差异告警/人工处理
- **幂等**：out_trade_no（商户订单号）全局唯一，重复支付请求返回同一结果

**Spoken Answer (EN):**
"Payment state machine: INIT → PAYING → SUCCESS/FAILED/CLOSED. Terminal states never transition back. On gateway callback: verify signature → lookup payment by channel_tx_id → CAS update status (WHERE status='PAYING') → if affected rows=1, proceed to fulfill order; if 0, already processed (idempotent success response). Proactive query job: for PAYING orders >5min old, poll gateway to detect missed callbacks. T+1 reconciliation: download gateway settlement file, diff against local payments table, alert on mismatch. All payment APIs are idempotent via out_trade_no uniqueness."

---

## Q20 — Search Suggest / Autocomplete（搜索联想/前缀补全）

**EN:** E-commerce search suggest: as user types "iph", show "iphone 15", "iphone case", etc. sorted by popularity. 10M products, 50K queries/sec.

**CN:** 电商搜索联想：用户输入"iph"，提示"iphone 15"、"iphone case"等，按热度排序。1000万商品，5万查询/秒。

**中文解题说明：**
- **方案1：Redis ZSET 字典序范围**
    - 将所有商品名前缀插入 ZSET（score=热度）
    - 查询：`ZRANGEBYLEX suggest "iph" "[iph\xff"` → 返回字典序在范围内的词
    - 缺点：只能前缀匹配，不支持中间子串
- **方案2：Trie 树 + 热度**
    - 内存 Trie，每个节点存 TopK 推荐词及热度
    - 输入前缀时遍历到对应节点，返回该节点下最热词
    - 缺点：内存大、更新复杂
- **方案3：Elasticsearch Completion Suggester**
    - 专用 `completion` 类型字段，FST（有限状态机）结构，前缀搜索毫秒级
    - 支持权重排序、模糊匹配
- **方案4：离线构建 + 在线服务**
    - 离线任务统计搜索词频 → 构建前缀→候选词映射表 → 加载到 Redis
    - 定时刷新（小时级）
- **推荐**：中小规模用 Redis ZRANGEBYLEX；大规模/复杂需求用 ES Completion Suggester

**Spoken Answer (EN):**
"Two production paths. Simple: Redis ZSET with lexicographical range queries — insert all product names (and common search terms) with popularity as score; query via ZRANGEBYLEX prefix prefix\xff to get matching terms sorted by score. Works great up to millions of terms. For richer needs (fuzzy matching, typo tolerance, personalized ranking): Elasticsearch Completion Suggester — uses FST (finite state transducer) for millisecond prefix lookup, supports weights and fuzzy. Offline pipeline: aggregate search logs hourly, build prefix→candidates mapping, load into Redis as fallback. At 50K qps, Redis Cluster or ES both handle easily with proper caching of popular prefixes."