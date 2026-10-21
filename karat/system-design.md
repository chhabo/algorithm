# Karat Capacity Estimation — 全量题库 (Q1–Q19) 中英对照 + 中文解题说明 + 英文口语答案

---

## Q1 — Video Upload & Storage Cost Estimation

**EN:** A short-video app: users upload ~1-minute clips. Estimate storage & bandwidth cost next year; how to reduce.

**CN:** 短视频 App：用户上传约 1 分钟短片。估算明年的存储与带宽成本，如何降本？

**中文解题说明：**
- 假设 DAU 100 万，日活中 5% 上传视频 → 每天 5 万次上传
- 1 分钟 1080p 压缩后约 50MB
- 视频过期平均 30 天（有的 7 天，有的永久）
- 日增存储：50,000 × 50MB = 2.5 TB/天
- 若平均存活 30 天 → 峰值存储 ≈ 2.5 × 30 = **75 TB**
- 若多分辨率转码（480p/720p/1080p ≈ ×3）→ 实际 ~225 TB
- S3 标准档 $0.023/GB/月 → 75TB×1024×0.023 ≈ **$1,770/月**（单副本单版本）
- 带宽远大于存储：假设每人每天看 10 条 ×40MB，100万用户 → 400 TB/天下行，走 CDN
- 需澄清：上传率、TTL 分布、码率、是否多版本、CDN 命中率、DAU 增长曲线
- 降本：过期转 Glacier（便宜 5–6×）、低频降副本、客户端预压+ABR 降码率、提高 CDN 命中

**Spoken Answer (EN):**
"First I'd make assumptions: DAU ~1M, 5% upload daily → 50k uploads/day, each ~50MB → 2.5 TB/day ingest.
With 30-day TTL, steady-state storage ~75 TB; if we store 3 resolutions it's ~225 TB.
Monthly S3 cost roughly $1,700–$5k depending on replicas. But bandwidth dwarfs storage —
if each user watches 10 videos/day at 40MB that's ~400 TB/day egress, so CDN hit ratio matters most.
To cut cost: transcode to adaptive bitrate (lower avg size), push to CDN with high cache hit,
expire to Glacier, dedupe identical uploads."

---

## Q2 — Log System Machine Estimation

**EN:** Service emits one ~2KB log line per request at 10k QPS, retained 90 days. Estimate machines (2TB disk / 16-core).

**CN:** 服务每条请求产生一条 ~2KB 日志，QPS=10,000，保留 90 天。估算机器数（2TB盘/16核）。

**中文解题说明：**
- 写入速率：10k × 2KB = 20 MB/s ≈ 1.73 TB/天
- 90 天原始量 ≈ 1.73 × 90 ≈ **155 TB**
- ES 场景：副本×2 + 索引开销 ~30% → 有效 ≈ **400 TB**
- 单节点可用 2TB → 至少 **200 节点**（存储瓶颈）
- 写吞吐：单节点约 5k docs/s 含索引 → 10k QPS 至少 2 写节点，写不是瓶颈
- 读搜索（假设 90k 读 QPS）需更多副本/shard → 实际 **250–300 节点**
- 降本：热暖分层（7天 SSD，其余 HDD）、旧日志聚合降采样、TTL 自动删、批量写

**Spoken Answer (EN):**
"Ingest = 10k × 2KB = 20 MB/s ≈ 1.7 TB/day → 90 days raw ≈ 155 TB.
With RF=2 and ~30% index overhead ≈ 400 TB. At 2TB usable/node → ~200 nodes for storage.
Writes aren't the bottleneck (one node handles ~5k docs/s). Search replicas push it to 250–300.
Cut cost via hot/warm tiering, downsampling old logs, TTL deletion, batch writes."

---

## Q3 — Pipeline Throughput Bottleneck

**EN:** Pipeline A→B→C→D→E→F throughputs 1000/800/1200/500/900/1100 req/s. Max throughput? How to improve?

**CN:** 流水线 A→B→C→D→E→F 吞吐 1000/800/1200/500/900/1100。最大吞吐？如何提升？

**中文解题说明：**
- 系统最大吞吐由最慢阶段决定 → **D=500 req/s**，整体上限 500
- 阶段间缓冲可吸收短时突发，但持续吞吐受 D 限制
- 提升手段：水平扩展 D（加副本/分片）；D 输出可缓存则加缓存跳过重复计算；
  上游预计算分担 D；若 D 访问外部 DB 则批量+连接池+只读副本；加背压防上游压垮 D

**Spoken Answer (EN):**
"Max throughput = slowest stage D at 500 req/s — that bounds the whole pipeline.
Buffers smooth bursts but sustained rate stays at 500.
Improve by scaling D horizontally (replicas/sharding), caching D's idempotent output,
precomputing part of D's work upstream, batching its DB calls, and adding back-pressure."

---

## Q4 — Kids Drawing Animation Render Farm (detailed)

**EN:** Kids' drawing app; each render ~30s CPU-heavy on server farm. DAU ~500k, ~2 drawings/user/day. Estimate machines & cut cost.

**CN:** 儿童绘画动画 App，每次渲染约 30 秒（CPU 密集）。DAU≈50 万，每人每天约 2 幅。估算机器数与降本。

**中文解题说明：**
- 日任务：500k × 2 = **100 万次/天**
- 流量集中在放学后 4 小时（16:00–20:00），假设 70% 落在窗口：700k / 14,400s ≈ **49 QPS 峰值**
- 单次 30s → 并发 ≈ 49 × 30 ≈ **1,470 个并发渲染**
- 单 16 核机跑 4 并发 → 1470/4 ≈ **370 台**峰值，+30% 余量 ≈ **480 台**
- **核心洞察：渲染不必同步**——入队后通知完成，把 4h 峰值摊到 24h → 降到 ~120 台
- 降本：异步队列+弹性伸缩（夜间近 0）、Spot 实例（省 70–90%）、内容哈希去重（省 20–30%）、先出低清预览再按需高清、30 天后转冷存
- 综合降本 **3–5 倍**

**Spoken Answer (EN):**
"1M jobs/day. 70% in a 4h after-school window → 700k/14400s ≈ 49 peak QPS.
Job 30s → ~1,470 concurrent renders. One 16-core box runs ~4 → ~370 boxes, +30% ≈ 480 at peak.
Key: make it async via a job queue, flatten spike across 24h → ~120 boxes.
Then Spot instances (70–90% cheaper), content-hash dedupe (~20–30% saved),
low-res preview first then on-demand HD, cold-tier after 30 days → 3–5x cost cut."

---

## Q5 — Generic "Estimate Next-Year Machines" Framing

**EN:** Kids drawing app, each request needs farm rendering. How to estimate machine count next year & reduce cost?

**CN:** 儿童绘画动画 App，每次请求需渲染集群。如何估算明年机器数并降本？

**中文解题说明（五步法）：**
1. 澄清假设：DAU、人均任务数、单次耗时、高峰窗口
2. 算 QPS：日均任务/86400 得均值，峰值通常 3–5× 均值
3. 并发数 = 峰值 QPS × 任务秒数
4. 节点数 = ceil(并发 / 单机并发) + 冗余
5. 存储 = 输出大小 × 总任务 × 保留天数
- 最大杠杆：**异步化 + 弹性 Spot 伸缩**（省 60–80%），其次去重、预览优先、冷分层

**Spoken Answer (EN):**
"Five steps: clarify DAU/jobs-per-user/render-seconds/peak-window; compute avg then peak QPS (peak ~3–5× avg);
concurrency = peak_QPS × job_seconds; nodes = ceil(concurrency / jobs_per_node) + redundancy;
storage = output_size × total × retention.
Biggest lever is async queue + elastic Spot scaling (60–80% savings), plus dedupe, preview-first, cold tiering."

---

## Q6 — URL Shortener (TinyURL) Capacity

**EN:** Design a URL shortener like bit.ly. Estimate storage & QPS for 100M new URLs/month, 10:1 read:write ratio, 5-year lifetime.

**CN:** 设计短链服务（类似 bit.ly）。每月新增 1 亿条短链，读写比 10:1，数据保留 5 年。估算存储与 QPS。

**中文解题说明：**
- 写 QPS：100M / 30天 / 86400 ≈ 100M / 2.59M ≈ **~39 writes/s**（均值），峰值约 2–3× ≈ 100 w/s
- 读 QPS：×10 ≈ **~390 reads/s**（均值），峰值 ~1000 r/s
- 单条记录：原 URL(~500B) + short key(7B) + 元数据(创建时间/用户ID等 ~100B) ≈ **~700B**，取整 1KB
- 5 年总量：100M×12×5 = **60 亿条** × 1KB ≈ **6 TB**（纯数据，加索引约 2× → ~12 TB）
  → 单台大磁盘机器足够存，瓶颈不在存储而在读缓存
- 优化：短码用 base62（7位够 62^7≈3.5万亿），热点 URL 全放 Redis（99% 读命中），持久层用普通 DB/SSD

**Spoken Answer (EN):**
"Writes: 100M/month ÷ 2.6M seconds ≈ 39 writes/sec avg, peak ~100. Reads 10× → ~400 reads/sec.
Record ~1KB each (URL + 7-char key + metadata). Five years = 6 billion rows × 1KB ≈ 6 TB raw, ~12 TB with indexes — trivially fits on a couple of big disks.
The real design lever is caching: 99% of reads hit a hot-key cache (Redis), so DB only sees the misses.
Short key uses base62, 7 chars covers trillions of URLs. Cheap and horizontally shardable by key prefix."

---

## Q7 — Twitter Timeline (Feed) Capacity

**EN:** Twitter-like service, 300M DAU, each user follows 200 avg, posts 2 tweets/day, home timeline shows latest 1000 from followees. Estimate read QPS & fan-out storage/compute.

**CN:** 类 Twitter，3 亿 DAU，人均关注 200，每天发 2 条推文，首页展示关注者最新 1000 条。估算读 QPS 与 fan-out 开销。

**中文解题说明：**
- 发推：300M × 2 = **6 亿条/天** ≈ 7000 writes/s
- 读时间线：假设每人每天刷 30 次，300M × 30 = **90 亿次读/天** ≈ **10万 reads/s** 均值，峰值(早晚) 3–5× ≈ **30–50万 r/s**
- Fan-out（推模式）：每条推文要写入 200 个粉丝的时间线缓存 → 6亿 × 200 = **120 亿次写入/天** ≈ 1.4万次/s 的列表 push
- 时间线存 Redis list（每用户保留最新 1000 条 × ~推文ID 8B ≈ 8KB/用户）→ 3亿 × 8KB ≈ **2.4 TB** 常驻内存（很大，可只给活跃用户缓存）
- 折中：大 V 用拉模式（读时合并），普通用户用推模式（混合 fan-out）

**Spoken Answer (EN):**
"Posts: 300M users × 2 = 600M tweets/day ≈ 7k writes/s.
Home timeline reads: assume 30 refreshes/user/day → 9 billion reads/day ≈ 100k reads/s avg, 300k–500k at peak.
Pure push fan-out would write each tweet to 200 follower timelines → 600M×200 = 120B list pushes/day, heavy.
Timeline cache ~8KB/user × 300M ≈ 2.4 TB — keep only active users in Redis, others pull on demand.
Use hybrid: push for normal users, pull (merge on read) for celebrities with millions of followers to avoid hot-key explosion."

---

## Q8 — Image Sharing (Instagram-like) Storage

**EN:** Photo-sharing app, 50M DAU, each uploads 3 photos/day avg 3MB after compression, multiple thumbnails (5 sizes). Keep forever. Estimate storage growth & bandwidth.

**CN:** 图片分享 App，5000万 DAU，每人每天上传 3 张图，压缩后约 3MB，生成 5 种缩略图。永久保存。估算存储增长与带宽。

**中文解题说明：**
- 日新增原图：50M × 3 × 3MB = **450M × 3MB ≈ 1.35 TB/天原图**
- 5 个缩略图平均约原图 15%（合计 ~0.45MB）→ +0.45 ×150M ≈ **~68 GB/天**
- 日增总计 ≈ **~1.4 TB/天** → 年增 ≈ **~510 TB/年**，永久累积 → 第 N 年约 0.5×N² TB（线性增长逐年累加）
  5 年 ≈ 累计 **~7.5 PB**
- 带宽：浏览为主，假设每人看 50 张缩略图/天（平均 50KB）→ 50M×50×50KB ≈ **125 TB/天下行**，远超上传，靠 CDN 缓存命中（~90%）降回源
- 降本：原图转存一次 Glacier（极少回看），热数据只留缩略图；WebP/AVIF 再压 30%；CDN 边缘缓存热门图

**Spoken Answer (EN):**
"Daily originals: 50M × 3 × 3MB ≈ 1.35 TB/day; thumbnails (5 sizes, ~0.45MB total/user-photo) add ~68 GB → ~1.4 TB/day, ~510 TB/year.
Permanent retention → cumulative ~linear accumulation, ~7–8 PB over 5 years.
Bandwidth dominated by viewing: ~50 thumbnails/user/day × 50KB ≈ 125 TB/day egress — must lean on CDN (90%+ hit).
Cost cuts: store originals in cold/Glacier (rarely fetched), serve only hot thumbnails from CDN, convert to AVIF/WebP for ~30% smaller, dedupe identical images via perceptual hash."

---

## Q9 — Instant Messaging (Chat) Message Volume

**EN:** WeChat-like IM, 1B registered / 500M DAU, avg 40 messages/user/day, messages retained 1 year, p99 delivery < 1s. Estimate msg throughput & storage.

**CN:** 类微信 IM，10 亿注册/5亿 DAU，人均每天 40 条消息，消息保留 1 年，p99 投递 <1s。估算消息吞吐与存储。

**中文解题说明：**
- 消息量：500M × 40 = **200 亿条/天** ≈ **~23k msgs/s** 均值，聊天有早晚双峰 → 峰值 3× ≈ **~70k msgs/s**
- 单条约 500B（文本+元），多媒体走对象存储另计
- 日增：20B × 500B ≈ **10 TB/天**纯消息 → 年 ~3.6 PB，保留 1 年 → 稳态 **~3.6 PB**
- 投递要求 p99<1s → 需消息队列 + 长连接网关（每用户一条 TCP 长连，5亿并发连接需数十万台接入机分片）
- 降本：文本消息冷热分层（>30天转 HDD/冷存），群消息只存一份引用（扇出在读取时展开或用引用计数），媒体走独立对象存储+CDN

**Spoken Answer (EN):**
"Messages: 500M × 40 = 20 billion/day ≈ 23k msgs/s avg, ~70k at dual peaks (morning/evening).
~500 bytes each → ~10 TB/day of message data; 1-year retention steady-state ≈ 3.6 PB.
p99<1s delivery means persistent pub/sub queues + long-connection gateways (5e8 concurrent sockets → tens of thousands of gateway nodes, sharded by user hash).
Cost cuts: text messages tier to HDD/cold after 30 days; group messages stored once with fan-out references rather than N copies; media offloaded to object storage + CDN."

---

## Q10 — Ride-Hailing Location Tracking (Uber/Lyft)

**EN:** Ride-hailing app with 10M daily rides, each ride lasts 15 min, driver location updates every 3 seconds. Estimate write QPS, storage, and bandwidth for location data.

**CN:** 网约车 App，每天 1000 万单，每单 15 分钟，司机每 3 秒上报一次位置。估算位置数据的写入 QPS、存储和带宽。

**中文解题说明：**
- 每单位置更新数：15 min × 60 / 3s = **300 次/单**
- 日总更新：1000万 × 300 = **300 亿次/天** ≈ **347k writes/s** 均值
- 峰值（早晚高峰 2 小时占 50%）：500万单/7200s ≈ 694 QPS 订单，但位置更新是 694×300 ≈ **~208k/s**，加上非高峰摊匀后峰值约 **500k writes/s**
- 单条位置记录：driver_id(8B) + ride_id(8B) + lat/lng(16B) + timestamp(8B) + 状态(1B) ≈ **~50B**
- 日增存储：300亿 × 50B = **1.5 TB/天** → 保留 7 天用于纠纷处理 → 稳态 **~10.5 TB**
- 带宽：347k/s × 50B ≈ **17.4 MB/s 写入** ≈ 139 Mbps
- 优化：客户端聚合批量上报（每 10 条打一个包），减少连接开销；位置数据 TTL 短，用时序数据库（TSDB）自动过期；只给活跃订单保留实时位置

**Spoken Answer (EN):**
"Per ride: 15 min / 3s = 300 location pings. 10M rides/day × 300 = 3 billion pings/day ≈ 347k writes/sec avg, peaking around 500k during rush hours.
Each ping ~50 bytes → 1.5 TB/day. Keep 7 days for dispute resolution → ~10.5 TB steady state.
Bandwidth ~17–25 MB/s, not huge. Main challenge is write amplification, not volume.
Optimizations: batch client-side (send every 10 pings as one request), use a time-series DB with auto-TTL, shard by city/region, and only keep real-time tracking for active rides."

---

## Q11 — Flash Sale / E-commerce Spike (秒杀系统)

**EN:** Flash sale: 1M users competing for 100 limited items. Sale starts at exactly 8:00 PM. Estimate peak QPS, how to protect inventory, and capacity needed.

**CN:** 秒杀系统：100 万人抢 100 件商品，准点 8:00 PM 开抢。估算峰值 QPS、如何保护库存、需要多少容量。

**中文解题说明：**
- 峰值 QPS：100 万人集中在 1 秒内涌入（极端情况）→ **100 万 QPS** 理论峰值
- 实际中 CDN/前端限流/随机延迟会削峰，真实到达后端的约 **10–20 万 QPS**
- 库存扣减：100 件商品 → 核心瓶颈是库存 DB 的单行并发写
- 策略：Redis 原子计数器预扣（单机 10 万+ ops/s 够用），扣成功后再异步落盘；前端加验证码/排队/随机等待；读请求全部 CDN 缓存（商品详情页静态化）
- 容量：接入层需要能扛 100 万并发连接 → L4/L7 负载均衡集群；核心 Redis 集群 3–5 个分片；订单 DB 只需处理成功订单（最多 100 条/秒）→ 极小
- 关键：99.99% 的请求在 Redis 层就被拒绝，不会穿透到 DB

**Spoken Answer (EN):**
"At 8:00 PM exactly, 1M users hammer the endpoint → theoretical peak 1M QPS, but real backend sees maybe 100k–200k after CDN caching, rate-limiting, and client-side jitter.
The real bottleneck isn't capacity — it's inventory. Only 100 items exist.
So the design: use Redis atomic decrement as a fast gate — single-threaded, 100k+ ops/s, exactly what we need.
99.99% of requests get rejected at Redis ('sold out') within milliseconds.
Only the 100 successful decrements proceed to async order creation in DB.
Frontend adds CAPTCHA, queue tokens, and randomized delay to smooth the spike.
Capacity-wise: a small Redis cluster + a few dozen load balancers handles the entire event."

---

## Q12 — Collaborative Document Editing (Google Docs-like)

**EN:** Collaborative doc editor, 50M DAU, avg 30 min session, 1 edit (keystroke) per 2 seconds per user. Estimate ops/sec, storage for version history (1 year), and real-time sync bandwidth.

**CN:** 在线协作文档编辑器，5000万 DAU，平均会话 30 分钟，每人每 2 秒一次编辑（按键）。估算操作数/秒、版本历史存储（1年）、实时同步带宽。

**中文解题说明：**
- 编辑操作 QPS：50M × (30×60/2) = 50M × 900 = **450 亿次操作/天** ≈ **~52k ops/s** 均值
- 但协作场景下操作不是都写持久层——先走 OT/CRDT 在内存合并，批量落盘
- 单条操作：doc_id + user_id + 操作类型(insert/delete) + 位置 + 内容(avg 5B) + timestamp ≈ **~50B**
- 日增操作日志：450亿 × 50B ≈ **225 GB/天** → 1 年 ≈ **~82 TB**（可压缩 3–5× → ~20 TB）
- 实时同步带宽：每个文档假设平均 3 人同时编辑，每人 0.5 ops/s 广播给其他 2 人 → 每文档 1 op/s × 50B × 2 接收者 = 100B/s → 乘以活跃文档数（假设 500万并发编辑）→ **~500 MB/s** 广播带宽 ≈ 4 Gbps
- 优化：操作压缩（合并连续按键为单词级操作）、版本快照（每 100 次操作存一个全量快照，中间用 diff）、冷热分离（>30 天历史转冷存）

**Spoken Answer (EN):**
"Edits: 50M DAU × 900 edits/session ≈ 45 billion ops/day ≈ 52k ops/sec avg.
But ops don't all hit storage — they go through OT/CRDT in memory, batched to persistent log.
Each op ~50 bytes → 225 GB/day raw, ~82 TB/year (compressible 3–5×).
Real-time sync: assume 5M concurrent active docs, avg 3 collaborators, 0.5 ops/s each → broadcast ~500 MB/s total ≈ 4 Gbps.
Storage strategy: store periodic full snapshots (every 100 ops) plus incremental diffs; compress old version history to cold storage after 30 days.
Main scaling challenge is fan-out broadcast, not storage — solved by WebSocket pub/sub sharded by doc ID."

---

## Q13 — Stock/Crypto Price Feed (Real-time Ticker)

**EN:** Crypto exchange streaming price updates for 5,000 coins, each updates 10 times/sec, 1M concurrent subscribers. Estimate bandwidth and push rate.

**CN:** 加密货币交易所实时推送 5000 种币的价格，每种每秒更新 10 次，100 万并发订阅者。估算带宽和推送速率。

**中文解题说明：**
- 价格更新生产速率：5000 币 × 10/s = **50,000 updates/s**（从交易所/撮合引擎产出）
- 单条推送：symbol(5B) + price(8B) + timestamp(8B) + volume(8B) ≈ **~30B**
- 生产带宽：50k/s × 30B = **1.5 MB/s** ≈ 12 Mbps（很小）
- 但推送扇出给 100 万订阅者：每人关注平均 10 种币 → 每人接收 10×10=100 updates/s → 总推送 = 100万 × 100/s = **1 亿 pushes/s**
- 推送带宽：1亿/s × 30B = **3 GB/s** ≈ **24 Gbps**
- 优化：WebSocket 长连 + 二进制帧压缩；按 symbol 分 channel（用户只订阅自己关注的）；CDN/边缘推送（WebSocket 边缘终止）；差量推送（只推变化字段）

**Spoken Answer (EN):**
"Price production: 5,000 coins × 10 updates/sec = 50k updates/sec from matching engine — tiny, ~1.5 MB/s.
The real challenge is fan-out: 1M concurrent subscribers, each watching ~10 coins → 100 updates/sec per user → total push rate = 1M × 100 = 100 million pushes/sec.
At ~30 bytes each, that's 3 GB/s ≈ 24 Gbps outbound.
Architecture: WebSocket long-connections, shard subscribers by coin symbol into pub/sub channels, compress binary frames, and terminate at edge POPs to reduce origin load.
Most users receive <1 Mbps each, so total egress is manageable with proper CDN/edge infrastructure."

---

## Q14 — Cloud File Sync / Drive (Dropbox-like)

**EN:** Cloud drive service, 200M users, 20% daily active, avg 5 file syncs/day (mixed photos/docs), avg file size 2MB. Estimate daily sync volume, storage growth, and metadata DB load.

**CN:** 云盘服务，2 亿用户，20% 日活，人均每天同步 5 个文件（照片/文档混合），平均文件 2MB。估算每日同步量、存储增长和元数据 DB 负载。

**中文解题说明：**
- 日活：2亿 × 20% = **4000 万 DAU**
- 日同步文件：4000万 × 5 = **2 亿文件/天**
- 日传输量：2亿 × 2MB = **400 TB/天** 上传/下载（双向同步，实际可能更多）
- 日增存储（去重后）：假设 30% 是重复文件（照片重复率高）→ 有效新增 ~1.4亿 × 2MB ≈ **280 TB/天**
- 年存储增长 ≈ **~100 PB/年**
- 元数据 QPS：每次文件操作（上传/下载/列表/共享）约 5 次 DB 查询 → 2亿 × 5 = **10 亿次元数据操作/天** ≈ **~12k QPS** 均值，峰值 ~40k
- 元数据大小：每文件 ~500B（路径/inode/权限/哈希/版本）→ 2亿新文件/天 × 500B = **100 GB/天元数据**
- 优化：文件级去重（content hash 判重，相同文件只存一份）；客户端增量同步（rsync 算法只传 diff）；冷热分层（>90天未访问文件转冷存）；元数据用分布式 KV（如 Cassandra）

**Spoken Answer (EN):**
"DAU: 200M × 20% = 40M. 5 syncs/day → 200M file ops/day, avg 2MB each → 400 TB/day transfer.
Deduplication is critical: photos have high duplication (same meme, forwarded files), so effective new storage ~280 TB/day → ~100 PB/year.
Metadata is the hidden bottleneck: each file op triggers ~5 DB queries → 1B metadata ops/day ≈ 12k QPS avg, 40k peak.
Metadata per file ~500 bytes → 100 GB/day of metadata indexes.
Design: content-addressable storage (hash dedup), rsync-style delta sync for large files, hot/cold tiering (move inactive files to Glacier), and a distributed KV store (Cassandra/Dynamo) for metadata to avoid single-DB bottleneck."

---

## Q15 — IoT Telemetry Ingest（物联网设备上报）

**EN:** 1M smart meters report every 30 seconds, payload ~200 bytes (reading + timestamp + device ID). Keep 2 years for analytics. Estimate ingest QPS, storage, and time-series DB sizing.

**CN:** 100万台智能电表每 30 秒上报一次，载荷约 200 字节（读数+时间戳+设备ID）。保留 2 年供分析。估算写入 QPS、存储与时序库规模。

**中文解题说明：**
- 上报频率：每台 1次/30s = **0.033 msg/s**
- 总写入 QPS：1M × 1/30 ≈ **33,333 writes/s**（稳定均匀，无尖峰）
- 写入带宽：33k × 200B ≈ **6.7 MB/s**（~57 Mbps），很小
- 日增数据：33,333 × 200 × 86400 ≈ **576 GB/天**
- 2 年累计：576 × 365 × 2 ≈ **~421 TB**（未压缩）
- 时序压缩（Gorilla/RLE）通常省 10× → **~40 TB** 实际落盘
- 索引/分区：按 device_id + time 分区，每设备一条连续 chunk
- 优化：批量聚合（每设备攒 1 分钟再写）、边缘网关预聚合、冷数据降采样（>1月的数据从秒级降到分钟级，再省 30×）

**Spoken Answer (EN):**
"1M devices × 1 report / 30s = ~33k writes/sec, steady and even — no diurnal spikes.
Payload 200B → 6.7 MB/s ingest, trivial bandwidth.
Raw storage: 33k×200×86400 ≈ 576 GB/day → 420 TB over 2 years, but time-series compression (Gorilla) gives ~10×, so ~40 TB real.
Add rollup/downsampling: after 1 month, aggregate second-level to minute-level (30× smaller), pushing effective storage well under 10 TB.
Key design: partition by (device_id, day), batch inserts per device to amortize index overhead, and downsample cold partitions."

---

## Q16 — URL Shortener — Collision & Scale (短链系统·碰撞概率)

**EN:** TinyURL-like, 100M new URLs/day, 100:1 read:write ratio, 5-year link lifetime, 7-char base62 code. Estimate QPS, storage, and collision probability.

**CN:** 短链系统，每天新增 1 亿条，读写比 100:1，链接寿命 5 年，7位 base62 编码。估算 QPS、存储与碰撞概率。

**中文解题说明：**
- 写 QPS：1亿/86400 ≈ **1,157 writes/s**
- 读 QPS：×100 ≈ **~116k reads/s**
- 5 年总量：1亿 × 365 × 5 ≈ **1825 亿 ≈ 1.8×10¹¹** 条记录
- 每条记录：short_code(7B) + long_url(avg 200B) + ts + user ≈ **~250B**
- 存储：1.8×10¹¹ × 250B ≈ **~45 TB**（可忽略）
- 7位 base62 = 62⁷ ≈ **3.5×10¹²** 空间 → 用掉 1.8×10¹¹，占比 5%，生日碰撞概率极低（≈ n²/2m ≈ 0.005）
- 优化：写时雪花 ID 直接映射（无需查重），读走 Redis/LRU 缓存热点（Top 1% 占 90% 流量），KV 存储（如 RocksDB/DynamoDB）

**Spoken Answer (EN):**
"Writes: 100M/day ÷ 86400 ≈ 1.2k/sec. Reads 100× → ~116k/sec.
5-year record count ≈ 1.8×10¹¹ rows; at ~250 bytes each ≈ 45 TB total — small.
7-char base62 gives 62^7≈3.5 trillion keys; using ~0.18 trillion is ~5% occupancy → birthday-collision risk negligible, so we can assign IDs via a monotonic counter/snowflake without random collision checks.
Hot links dominate (power-law): cache top 1% in Redis (LRU), back the rest with a KV store. Read path is just key lookup + 302 redirect."

---

## Q17 — Recommendation Feature Store（推荐特征库）

**EN:** Feed recommender serves 50M DAU, each feed refresh fetches features for ~200 candidate items × user feature vector. Item embeddings dim=256 float32, user embedding dim=128. Estimate memory footprint & inference QPS.

**CN:** 信息流推荐服务 5000万 DAU，每次刷新取约 200 个候选 item 的特征 + 用户特征向量。item 向量 256维 float32，用户向量 128维。估算内存占用与推理 QPS。

**中文解题说明：**
- item 向量：256×4B=1KB/个，假设 1 亿候选 item → **100M×1KB=100 GB** 全放内存（可单节点或分片）
- 用户向量 128×4=512B，5000万用户 ≈ **25 MB**（极小）
- 每次请求拉 200×1KB = 200KB 特征 → 推理时带宽
- DAU 50M，人均刷新 ~30次/天（刷 feed）→ 15亿次请求/天 ≈ **~17k req/s** 均值，晚高峰 ×5 ≈ **~90k req/s**
- 每次打分 200 candidates × 轻量模型 → batch matmul，GPU/CPU 均可
- 内存预算：item embedding 表 100GB，加索引/缓存冗余 → 单机放不下全量则按 item hash 分片到若干内存节点（如 8 分片 ×16GB）
- 降本：item 向量量化到 int8（再省 4×→25GB），冷门 item 离线缓存，用户特征本地化

**Spoken Answer (EN):**
"Item embeddings: 100M items × (256×4B)=1KB → 100 GB embedding table.
User vectors tiny (128×4=512B ×50M ≈ 25MB).
Each feed request scores ~200 candidates → pulls 200KB of features; with 50M DAU × ~30 refreshes/day ≈ 1.5B requests/day ≈ 17k req/s avg, ~90k peak.
Serve item table from a sharded in-memory KV (8 shards × ~16GB each); quantize float32→int8 cuts table to ~25GB, easily fits fewer nodes.
Inference is batched matrix multiply over 200 candidates — cheap on CPU; GPU only if model deep."

---

## Q18 — Live Stream Chat / Danmaku（直播弹幕广播）

**EN:** Live stream with 1M concurrent viewers, each sends ~1 danmaku/30s avg, all viewers in the same room see all messages. Estimate broadcast fan-out & bandwidth.

**CN:** 单一直播间 100万并发观众，人均每 30 秒发 1 条弹幕，同房间所有人可见。估算广播扇出与带宽。

**中文解题说明：**
- 发弹幕速率：1M × 1/30 ≈ **33k msgs/s** 进房间
- 每条弹幕要扇出给 100万观众 → 扇出量 = 33k × 1M = **330 亿条投递/秒**（极端）
- 带宽：每条 ~80B（含昵称+内容）→ 330亿×80 ≈ **26 TB/s = 210 Gbps** —— 不可行！
- **必须优化**：合并批量推送（每 100ms 攒一批下发给客户端）→ 每个客户端每秒收 ~33k×0.1=3300条/秒，但只需推 10次/秒的 batch
  实际扇出从“每消息×1M”变为“每批×1M”：33k/10≈3.3k batches/s × 1M ≈ **33亿投递/s**，仍大但可控
- 更优：弹幕允许丢/抽样（显示抽样 10%，后端仍统计）；长连接分片到多边缘节点复制（pub/sub tree，根→区域→观众）
- 最终单观众下行 ~3300×80B ≈ 264 KB/s ≈ 2 Mbps，合理

**Spoken Answer (EN):**
"Naively 33k msgs/s × 1M viewers = 33 billion fan-outs/s — impossible.
Mitigate with batching: buffer ~100ms then push one merged frame per client → batches ≈ 3.3k/s × 1M = 3.3B deliveries/s, feasible via a pub/sub tree (origin → regional brokers → edge → clients).
Further: cap display rate (sample top-N or throttle to e.g. 50 visible/s per viewer), keep full count server-side.
Per-viewer downlink ≈ sampled 50 msgs × 80B × 10 batches ≈ a few hundred KB/s — comfortable.
Architecture: WebSocket fanout tree, not point-to-point."

---

## Q19 — Search Engine Inverted Index（搜索引擎倒排索引）

**EN:** Web-scale index of 10 billion documents, avg 500 words/doc, avg 6 bytes/term, postings list compression. Estimate index size & query-time RAM need.

**CN:** 百亿网页索引，平均每篇 500 词，平均 term 6 字节，倒排表压缩。估算索引大小与查询内存需求。

**中文解题说明：**
- 总词项出现次数：10¹⁰ × 500 = **5×10¹²** postings
- 每个 posting ≈ docID(4B)+freq(1B)+位置可选，压缩后 avg ~3–4B → 仅 posting **~20 TB**
- 词典：唯一 term 假设 1千万，每项 (term 6B + 指针) ≈ 20B → **~200 MB**（常驻内存）
- 实际 Google 级索引 PB 级（含位置/多语言），但核心词典小、posting 大且分片落 SSD
- 查询：AND/OR 多个 term，拉多个 posting list 归并，单查询扫百万级 posting → 靠分片并行+跳表加速
- 降本：差量编码 (delta+varint)、位置索引可选择性加载、热门 term 缓存

**Spoken Answer (EN):**
"Total postings: 10B docs × 500 terms = 5 trillion occurrences. With varint/delta compression ~3–4 bytes each → ~20 TB posting store (lives on SSD, sharded).
Dictionary: ~10M unique terms × ~20B ≈ 200 MB — fits in RAM, maps term→posting-location.
A query merges several posting lists; use skip-lists + intersection early-termination.
Cache hot-term postings in RAM; everything else streamed from SSD-backed segments. Scales horizontally by document-sharding."