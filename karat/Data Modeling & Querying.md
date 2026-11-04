# karaat Data Modeling & Query Design — 20 Drill Questions (面试详版)

---

## Q1 — ER Modeling: User-Order-Product（用户订单商品建模）

**EN:** Design schema for an e-commerce system: users place orders, each order contains multiple products with quantities and prices. Support queries like "orders by user", "top selling products", "revenue by date". Show tables, keys, and indexing strategy.

**CN:** 设计电商系统 schema：用户下单，每单含多个商品（数量+价格）。支持"某用户所有订单"、"销量最高商品"、"按日期统计营收"。给出表结构、键和索引策略。

**中文解题说明：**
- 核心表设计：
    - `users(id BIGINT PK, email VARCHAR UNIQUE, created_at)`
    - `products(id BIGINT PK, name, price DECIMAL(10,2), category_id, created_at)`
    - `orders(id BIGINT PK, user_id BIGINT FK, status VARCHAR, created_at TIMESTAMP, total_amount DECIMAL)`
    - `order_items(id BIGINT PK, order_id BIGINT FK, product_id BIGINT FK, quantity INT, unit_price DECIMAL(10,2))`
- 关键设计决策：
    1. **价格快照**：`order_items.unit_price` 在创建订单时从 `products.price` 拷贝，防止后续改价影响历史订单
    2. **冗余 total_amount**：`orders.total_amount` 可冗余避免每次 SUM，也可不冗余靠 `order_items` 实时算——看读频次
    3. **索引**：`orders(user_id, created_at)` 复合索引加速"用户订单列表+时间排序"；`order_items(order_id)` 单索引；`order_items(product_id)` 用于销量统计
- 查询示例：
    - 用户订单：`SELECT * FROM orders WHERE user_id=? ORDER BY created_at DESC LIMIT 20`
    - 销量 Top10：`SELECT product_id, SUM(quantity) FROM order_items GROUP BY product_id ORDER BY SUM DESC LIMIT 10`
    - 日营收：`SELECT DATE(created_at), SUM(total_amount) FROM orders WHERE status='paid' GROUP BY DATE(created_at)`
- 扩展：分库分表时 `orders` 按 `user_id` hash 分片保证单用户订单同片；`order_items` 按 `order_id` 分片

**Spoken Answer (EN):**
"Core tables: users, products, orders, and order_items as the join table. Critical design choice: snapshot the product price into order_items at order creation time — never reference live product price, otherwise historical orders break when prices change. For indexing: compound index on orders(user_id, created_at) so user order history queries are covered with no filesort. For top-selling products: index on order_items(product_id) enables fast GROUP BY aggregation; for large scale you'd pre-aggregate into a daily sales summary table. If sharding: shard orders by user_id hash so one user's orders land on the same shard — single-shard query, no scatter-gather."

---

## Q2 — Many-to-Many: Tags on Posts（多对多标签建模）

**EN:** A blogging platform allows posts to have multiple tags. Need to query "all posts with tag X" and "all tags for post Y" efficiently. Design schema and discuss why NOT to store as comma-separated string.

**CN:** 博客平台文章支持多标签。需高效查询"含标签X的所有文章"和"文章Y的所有标签"。设计 schema 并解释为什么不用逗号分隔字符串。

**中文解题说明：**
- Schema：
    - `posts(id, title, content, author_id, created_at)`
    - `tags(id, name VARCHAR UNIQUE)`
    - `post_tags(post_id BIGINT FK, tag_id BIGINT FK, created_at)` — 复合主键 `(post_id, tag_id)`，反向索引 `(tag_id, post_id)`
- 为什么不用逗号分隔：
    1. 无法建索引 → "找含标签X的文章"必须全表扫描+字符串匹配
    2. 无法保证数据完整性（拼写错误、多余空格）
    3. 增删标签需读-改-写整行（锁竞争）
    4. 无法统计"标签X下有多少文章"而不全表扫描
    5. 违反第一范式
- 查询优化：
    - "标签X的文章"：`SELECT p.* FROM posts p JOIN post_tags pt ON p.id=pt.post_id WHERE pt.tag_id=? ORDER BY p.created_at DESC`
    - "文章Y的标签"：`SELECT t.* FROM tags t JOIN post_tags pt ON t.id=pt.tag_id WHERE pt.post_id=?`
    - 索引：`post_tags(tag_id, post_id)` 覆盖第一个查询；`post_tags(post_id, tag_id)` 覆盖第二个
- 大数据量扩展：如果标签量巨大（千万级），`post_tags` 可按 `tag_id` 分片或按 `post_id` 分片，取决于查询模式

**Spoken Answer (EN):**
"Junction table post_tags(post_id, tag_id) with composite primary key in both directions — (post_id, tag_id) for 'tags of a post', (tag_id, post_id) for 'posts with a tag'. Never store as comma-separated — you can't index it, can't enforce referential integrity, and adding/removing a tag requires read-modify-write of the whole string. With the junction table and proper indexes, both queries are single-range scans on a covering index. At massive scale (hundreds of millions of posts), shard post_tags by post_id if reads are mostly 'tags of post Y', or by tag_id if 'posts with tag X' dominates."

---

## Q3 — Adjacency List vs Closure Table vs Path Enumeration（树形/层级结构存储）

**EN:** Design a category tree (e-commerce categories with unlimited depth) and a comment reply thread (nested comments). Compare adjacency list, closure table, and path enumeration. Which for which?

**CN:** 设计分类树（无限层级）和评论回复线程（嵌套评论）。对比邻接表、闭包表、路径枚举。分别适用什么场景？

**中文解题说明：**
- **邻接表 (Adjacency List)**：
    - 结构：`categories(id, name, parent_id)`，`comments(id, content, parent_id, post_id)`
    - 优点：简单直观，插入/移动节点只需改一行
    - 缺点：查子树需递归 CTE（MySQL 8+）或 N+1 查询；查祖先路径也需递归
    - 适用：深度浅（<5层）、读少写多、树不频繁遍历
- **闭包表 (Closure Table)**：
    - 结构：额外表 `category_closure(ancestor_id, descendant_id, depth)`
    - 存所有 (祖先, 后代) 对，含自身 (depth=0)
    - 优点：查子树 `WHERE ancestor=?` 一行搞定 O(1)；查路径 `WHERE descendant=? ORDER BY depth`；移动子树只需删旧关系插新关系
    - 缺点：空间 O(n²) 最坏但实际远小于；写入需维护闭包表（事务内）
    - 适用：深度深、频繁查子树/路径、读远多于写
- **路径枚举 (Path Enumeration)**：
    - 结构：`categories(id, name, path VARCHAR)` 如 `/1/4/9/`
    - 优点：查子树用 `WHERE path LIKE '/1/4/%'` 或前缀索引；查祖先直接解析字符串
    - 缺点：路径长度有限制；移动节点需更新所有后代路径（大量写）
    - 适用：树深度有限、读多写少、不需要复杂中间操作
- **评论线程推荐**：闭包表（查某评论下所有回复高效）或 邻接表+递归CTE（如果深度<10且DB支持）
- **分类树推荐**：如果后台管理频繁移动分类 → 邻接表；如果前台频繁展示整棵树/面包屑 → 闭包表

**Spoken Answer (EN):**
"Three approaches, each with trade-offs. Adjacency list is simplest — just parent_id. Great for shallow trees and frequent writes/moves. But reading a subtree requires recursive CTEs, which don't scale for deep trees. Closure table adds a relation table storing every (ancestor, descendant, depth) triple. Subtree queries become a single indexed join — O(log n). Perfect for deep hierarchies and read-heavy workloads like category navigation. Downside: writes must maintain the closure table in a transaction. Path enumeration stores '/1/4/9/' strings. Subtree via prefix LIKE, ancestors by parsing. Simple but moving a subtree means updating every descendant's path — expensive. For comment threads: closure table if you need efficient 'all replies under comment X' at any depth. For category trees: if you frequently move categories, adjacency list; if you mostly render full trees/breadcrumbs, closure table."

---

## Q4 — Social Graph Follow Relation（关注关系建模与查询）

**EN:** Twitter-style follow system: 500M users, avg 200 follows each. Design schema, support "who I follow", "my followers", "mutual follows", and discuss sharding.

**CN:** Twitter 式关注系统：5亿用户，人均关注200。设计 schema，支持"我关注谁"、"我的粉丝"、"互关"，并讨论分片。

**中文解题说明：**
- Schema：`follows(follower_id BIGINT, followee_id BIGINT, created_at TIMESTAMP, PRIMARY KEY(follower_id, followee_id), INDEX(followee_id, follower_id))`
- 查询：
    - 我关注谁：`SELECT followee_id FROM follows WHERE follower_id=? ORDER BY created_at DESC LIMIT 100`（走 PK）
    - 我的粉丝：`SELECT follower_id FROM follows WHERE followee_id=? ORDER BY created_at DESC LIMIT 100`（走反向索引）
    - 互关：`SELECT f1.followee_id FROM follows f1 JOIN follows f2 ON f1.followee_id=f2.follower_id WHERE f1.follower_id=? AND f2.followee_id=?`
- 分片挑战：
    - 按 `follower_id` 分片 → "我关注谁" 单分片搞定，但 "我的粉丝" 需 scatter-gather 所有分片
    - 按 `followee_id` 分片 → 反了
    - 解决方案：双写两份（一份按 follower 分，一份按 followee 分）—— 空间换时间；或按 `follower_id` 分片 + 粉丝列表走搜索集群（Elasticsearch）异步构建
- 规模：500M × 200 = 1000亿行 → 必须分片；单表不可能
- 优化：粉丝列表/关注列表通常只需前几页 → 可缓存（Redis sorted set，score=timestamp）

**Spoken Answer (EN):**
"Single table follows(follower_id, followee_id) with composite PK on (follower_id, followee_id) and a reverse index on (followee_id, follower_id). At 500M users × 200 follows = 100 billion rows — must shard. Sharding dilemma: shard by follower_id makes 'who I follow' local but 'my followers' requires scatter-gather. Two options: dual-write two copies (one sharded by follower, one by followee) — doubles storage but each query hits one shard. Alternative: serve 'followers' from a read-optimized store like Elasticsearch or a Redis sorted set per user, asynchronously updated via the write stream. For mutual follows: it's a simple self-join, but at scale you'd cache the intersection or compute it in a graph DB."

---

## Q5 — Time-Series Metrics Modeling（时序指标建模与查询）

**EN:** Monitor 1M servers, each emitting 500 metrics every 10s. Design storage for 2 years of data, support "CPU usage of server X last 24h" and "top 10 servers by memory at time T". Discuss columnar vs row storage.

**CN:** 监控 100万台服务器，每台每 10 秒上报 500 个指标。设计存储支持 2 年数据，查询"服务器X过去24小时CPU"和"T时刻内存 Top10"。讨论列存 vs 行存。

**中文解题说明：**
- 数据量：1M × 500 × 6/min × 60 × 24 = **4.32 万亿点/天** → 不可能全存原始
- 降采样策略：原始 10s → 1min → 5min → 1hour → 1day（逐级聚合）
- Schema（行存/宽表）：`metrics(host_id, metric_name, ts, value)` — 窄表，每行一个点
- 列存（ClickHouse/Druid）：`(ts, host_id, metric_name, value)` 按 `(metric_name, ts)` 排序 → 同指标时间连续存储，列压缩比极高（Gorilla/Delta-of-Delta）
- 查询优化：
    - "X 的 CPU 24h"：`WHERE host_id=X AND metric_name='cpu' AND ts > now()-24h` → 列存只需读 cpu 列 + 时间范围，极快
    - "T 时刻内存 Top10"：`WHERE metric_name='mem' AND ts=T ORDER BY value DESC LIMIT 10` → 列存秒出
- 索引：时间分区（按天/小时）+ 主键排序
- 为什么不用 MySQL/PostgreSQL：行存 + B-tree 索引无法承受写入速率（4万亿/天 ≈ 5000万/s），且压缩比差
- 推荐：TimescaleDB（PostgreSQL 扩展，自动分区+列存压缩）或 ClickHouse 原生列存

**Spoken Answer (EN):**
"Raw ingest: 1M hosts × 500 metrics × 6/sec = ~3 billion points/sec — impossible for row stores. Use a columnar TSDB (ClickHouse, Druid, TimescaleDB). Schema: narrow rows (host_id, metric_name, ts, value) ordered by (metric_name, ts) — columnar compression (Delta-of-Delta + XOR) gives 10–20× ratio. Partition by time (daily), set TTL to downsample: keep 10s for 7 days, 1min for 30 days, 1hr for 2 years. Query 'CPU last 24h': single column scan on cpu metric, time-range filtered — sub-second. 'Top 10 memory at T': filter metric_name='mem' and ts=T, sort by value — columnar store handles this trivially. Never use a relational row store for this volume; you'd die on ingest and storage cost."

---

## Q6 — Auto-Increment PK vs UUID vs Snowflake（主键选型）

**EN:** Compare BIGINT auto-increment, UUIDv4, UUIDv7, and Snowflake ID as primary keys. Which for a sharded multi-region system?

**CN:** 对比 BIGINT 自增、UUIDv4、UUIDv7、雪花ID作主键。分片多区域系统选哪个？

**中文解题说明：**
- **自增 BIGINT**：
    - 优点：8字节紧凑、插入顺序→聚簇索引友好（无页分裂）、可读
    - 缺点：跨分片合并冲突、暴露业务量（竞品可爬取推断）、多主写入需额外协调（auto-inc 锁竞争）
- **UUIDv4**：
    - 优点：全局唯一、无协调、16字节
    - 缺点：完全随机→聚簇索引页分裂严重、写入分散导致缓冲池污染、索引膨胀、不可排序
- **UUIDv7**（时间有序 UUID）：
    - 优点：48位时间戳前缀+随机后缀→插入顺序友好、全局唯一、无协调、可排序
    - 缺点：16字节仍比 BIGINT 大，但解决了页分裂问题
- **Snowflake**（64位：1位符号+41位时间+10位机器+12位序列）：
    - 优点：8字节紧凑、时间有序、可排序、多机无冲突（机器ID分配）、可嵌入机器/分片信息
    - 缺点：依赖时钟同步（时钟回拨问题）、机器ID需分配管理
- **分片多区域推荐**：Snowflake（紧凑+有序+无协调）或 UUIDv7（简单无时钟依赖）。自增不行（冲突），UUIDv4 太伤写入性能。

**Spoken Answer (EN):**
"Auto-increment is great for single-instance — compact, sequential, clustered-index friendly. But it breaks in sharded or multi-region: you'd need a central allocator or per-shard offset, both painful. UUIDv4 is globally unique with zero coordination, but random insertion causes massive page splits and buffer pool pollution in B-tree clustered indexes — write throughput tanks. UUIDv7 fixes this: embeds a 48-bit timestamp prefix so inserts are mostly sequential, while keeping global uniqueness. No coordination needed. Snowflake (64-bit: timestamp + machine_id + sequence) is even better: 8 bytes like BIGINT, time-ordered, embeds shard info in the ID itself — great for sharding (you can extract shard_id from the ID). For multi-region sharded: Snowflake if you can manage machine IDs and clock sync; UUIDv7 if you want zero infra overhead. Avoid UUIDv4 and auto-increment."

---

## Q7 — Normalization vs Denormalization Trade-off（范式化 vs 反范式化）

**EN:** When does normalization hurt performance, and when does denormalization create problems? Give concrete examples and the middle ground.

**CN:** 范式化何时损害性能？反范式化何时制造问题？给具体例子和折中方案。

**中文解题说明：**
- **范式化（3NF）优点**：数据无冗余、更新异常消除、一致性高
- **范式化伤害性能的场景**：
    - 高频 JOIN：订单列表需 JOIN users, products, categories → N 表 JOIN 拖慢查询
    - 聚合计算：每次算"用户总消费"需 SUM 全量订单
    - 读远多于写时，每次 JOIN 代价不值得
- **反范式化的问题**：
    - 更新异常：用户改了 username，所有订单里的 `order.username` 都要更新（或不同步导致数据不一致）
    - 冗余字段占用空间
    - 写放大：一次更新要改多处
- **折中方案**：
    - 读多写少的热路径反范式（订单冗余 username、商品快照）
    - 异步同步：写主表，CDC/触发器更新冗余字段
    - 物化视图/汇总表：每日统计表定时刷新
    - 宽表（OLAP）：ClickHouse 中直接存扁平宽表
- **具体例子**：
    - 社交 Feed：完全范式化（posts, users, likes 分开）读 Feed 要 JOIN → 反范式化到宽表或 Redis 缓存
    - 电商订单：反范式化商品名/价格快照（必须），但用户昵称可不冗余（实时查或缓存）

**Spoken Answer (EN):**
"Normalization prevents anomalies but kills read performance when you're joining 5 tables for every feed render. Denormalization speeds reads but creates update anomalies — change a username and you've got stale data in 50 million order rows. Middle ground: denormalize immutable snapshots (product price at order time, post content at share time) — these never change so no anomaly risk. For mutable data like usernames, keep normalized in users table and either join (if infrequent) or cache in Redis with invalidation. Use async materialized views for heavy aggregations: pre-compute 'user total spend' nightly instead of SUM on every request. Rule of thumb: normalize writes, denormalize reads — and use CDC or triggers to keep denormalized fields in sync."

---

## Q8 — Deep Pagination Optimization（深翻页优化）

**EN:** A feed API `GET /posts?page=10000&size=20` is slow. Explain why OFFSET is bad and design keyset pagination.

**CN:** Feed API `GET /posts?page=10000&size=20` 很慢。解释 OFFSET 为什么差，设计 keyset 翻页。

**中文解题说明：**
- **OFFSET 问题**：`LIMIT 10000,20` 实际扫描 10020 行再丢弃前 10000 行 → O(n) 随页码线性变慢；且结果不稳定（翻页间有新数据插入会导致重复/遗漏）
- **Keyset Pagination（游标翻页）**：
    - 用上一页最后一条的排序值作为下一页起点
    - `WHERE created_at < '2024-01-15T10:30:00' ORDER BY created_at DESC LIMIT 20`
    - 需有 `(created_at, id)` 复合索引（id 用于同时间排序稳定）
    - 复杂度 O(log n) 恒定，不随深度变慢
- **缺点**：不支持"跳到第 N 页"（只能上一页/下一页）→ 前端改为无限滚动
- **混合方案**：浅页用 OFFSET（前 100 页可接受），深页强制用 keyset 或禁止直接跳页
- **Cursor 编码**：将 `(created_at, id)` 编码为 base64 cursor 字符串传给客户端，避免暴露内部字段

**Spoken Answer (EN):**
"OFFSET 10000 means the DB scans and discards 10,000 rows before returning 20 — O(n) cost that grows linearly with page number. Plus, if new rows insert during pagination, you get duplicates or skips. Keyset pagination: instead of page numbers, use a cursor — the sort value of the last item on the previous page. Query becomes: `WHERE (created_at, id) < ('2024-01-15T10:30:00', 98765) ORDER BY created_at DESC, id DESC LIMIT 20`. Requires a composite index on (created_at, id). Cost is O(log n) — constant time regardless of 'page depth'. Trade-off: you lose 'go to page 500' functionality — fine for infinite scroll, not fine for 'jump to middle'. Mitigate by mixing: allow OFFSET only for first 100 pages, keyset beyond that."

---

## Q9 — Composite Index Design & Leftmost Prefix（联合索引设计与最左匹配）

**EN:** Table `orders(user_id, status, created_at, total)`. You have indexes on `(user_id)` and `(status, created_at)`. A query `WHERE status='paid' AND created_at > '2024-01-01' AND total > 100` is slow. Design the optimal index.

**CN:** 表 `orders(user_id, status, created_at, total)`，已有索引 `(user_id)` 和 `(status, created_at)`。查询 `WHERE status='paid' AND created_at > '2024-01-01' AND total > 100` 很慢。设计最优索引。

**中文解题说明：**
- 现有索引分析：
    - `(status, created_at)` 能用于 `status='paid' AND created_at > ...` 的范围过滤
    - 但 `total > 100` 无法用索引，需在过滤后逐行判断
- 最优索引设计：
    - 将等值条件列放前面，范围条件列放后面：`(status, total, created_at)` 或 `(status, created_at, total)`
    - 因为 `created_at` 是范围查询，放它后面的列 `total` 无法用索引 → 所以 `(status, total)` 让 total 也能用（如果 total 是等值/范围但选择性高）
    - 实际上 `total > 100` 选择性通常低（很多订单 >100），所以放最后也行
    - 推荐：`(status, created_at, total)` — status 等值，created_at 范围，total 用于过滤但无法走索引（在范围后）
    - 更好的：覆盖索引 `(status, created_at)` INCLUDE (total, user_id) — 如果 DB 支持（SQL Server INCLUDE, MySQL 8.0 可用二级索引包含）
- **最左匹配原则**：联合索引 (a,b,c)，查询条件必须含 a 才能用索引；含 a,b 则 c 也能用；范围查询（>、<、BETWEEN、LIKE 'abc%'）会截断后续列
- **索引选择性**：高选择性列放前面（但等值优先于范围）

**Spoken Answer (EN):**
"Current index (status, created_at) helps with the first two filters but `total > 100` is a residual filter — scanned but not seeked. Optimal: create index on (status, created_at, total). Status is equality → first; created_at is range → second (range stops further column usage, so total can't be seeked but it's at least in the index leaf for covering). If total had high selectivity, you could put it before created_at: (status, total, created_at) — then total range is seeked, but created_at range after it also can't use index. So either way one condition is residual. Best practice: check EXPLAIN, look at filtered/rows_examined. If total > 100 filters 90% of rows, put it before created_at. Also consider a covering index: INCLUDE (total, user_id) so the query reads only the index, no table lookup."

---

## Q10 — Covering Index & Index-Only Scan（覆盖索引与索引覆盖扫描）

**EN:** Query `SELECT email FROM users WHERE age BETWEEN 20 AND 30 AND city='NYC'` runs slow despite index on `(city, age)`. Fix it with covering index.

**CN:** 查询 `SELECT email FROM users WHERE age BETWEEN 20 AND 30 AND city='NYC'` 很慢，尽管有索引 `(city, age)`。用覆盖索引修复。

**中文解题说明：**
- 现有索引 `(city, age)` 可用于过滤 city='NYC' AND age BETWEEN 20 AND 30，但 `email` 不在索引中 → 需要回表（bookmark lookup / clustered index lookup）逐行取 email
- 回表代价：随机 IO，如果匹配行数多（如几万行），回表成本巨大
- 覆盖索引：将查询所需的所有列都包含在索引中 → 引擎只需扫描索引即可返回结果，无需回表
- 方案：`CREATE INDEX idx_city_age_email ON users(city, age, email)` — 顺序：等值 city 在前，范围 age 在中，email 放最后（因 age 是范围，email 不能用于过滤但可作为覆盖列）
- 结果：Index-Only Scan → 顺序读索引页，极快
- 注意：覆盖索引会增加索引大小和维护成本（写放大），需权衡

**Spoken Answer (EN):**
"Index (city, age) filters rows but then the engine must do a bookmark lookup for each matching row to fetch `email` from the clustered table — that's random I/O, very expensive if many rows match. Fix: create a covering index (city, age, email). City is equality, age is range, email is just included for covering. The optimizer can do an index-only scan — read sequentially from the index leaf pages, never touch the table. Trade-off: index is larger on disk and slightly slower to update on writes, but for a read-heavy query like this, it's worth it."

---

## Q11 — Hot Row Counter Update（热点行计数器更新）

**EN:** A viral post's `like_count` row is updated 50,000 times/sec. Database row locks cause 2s latency. Design a scalable counter.

**CN:** 爆款帖子 `like_count` 行每秒更新 5 万次，数据库行锁导致 2 秒延迟。设计可扩展计数器。

**中文解题说明：**
- 问题本质：单行热点 → 所有更新串行化（行锁），即使 InnoDB 也扛不住 50k/s 的锁竞争
- 解决方案 1：**分片计数器 (Counter Sharding)**
    - 建表 `post_like_counts(post_id, slot, count)` 主键 `(post_id, slot)`
    - 更新时随机选 slot (0~15) → `UPDATE ... SET count=count+1 WHERE post_id=? AND slot=?`
    - 读取时 `SELECT SUM(count) FROM post_like_counts WHERE post_id=?`
    - 分散锁到 16 行 → 吞吐线性提升
- 解决方案 2：**Redis INCR + 异步落库**
    - 每次点赞 Redis `INCR post:123:likes`
    - 后台 worker 每秒/每 1000 次批量写 DB
    - 读走 Redis（最终一致）
- 解决方案 3：**本地内存聚合 + 批量写**
    - 应用层攒批（每 100ms 或 1000 次），单条 UPDATE `SET count=count+1000`
- 推荐组合：Redis 实时计数 + 分片 DB 兜底持久化

**Spoken Answer (EN):**
"Single-row counter at 50k updates/sec is a non-starter — row locks serialize everything. Solution 1: shard the counter into N rows (slot 0..15). On each like, pick a random slot and UPDATE that specific row. Reads SUM all slots. Locks are now spread across 16 rows — 16× throughput. Solution 2: use Redis INCR for real-time counting (single-threaded, 100k+ ops/sec easily), then asynchronously flush to DB every second or every N increments. Solution 3: application-side batching — accumulate 1000 likes in memory, then issue one UPDATE ... SET count=count+1000. Best practice: Redis for hot-path counting + sharded DB rows for durability, with periodic reconciliation."

---

## Q12 — EAV Anti-Pattern & Alternatives（EAV 反模式与替代方案）

**EN:** A product catalog has 500+ variable attributes (color, size, RAM, calories, ISBN...). Team used EAV `(entity_id, attribute_name, value)`. Queries like "find laptops with RAM=16GB AND price<1000" time out. Fix the schema.

**CN:** 商品目录有 500+ 可变属性，团队用 EAV `(entity_id, attribute_name, value)`。查询"找 RAM=16GB 且价格<1000 的笔记本"超时。修复 schema。

**中文解题说明：**
- EAV 问题：
    - 每个属性一行 → 查多条件需 N 次 self-join 或 pivot → 性能灾难
    - value 都是字符串 → 无法做数值比较（price<1000 字符串比较错误）
    - 无类型约束、无外键、索引困难
- 替代方案：
    1. **JSON 列**（MySQL 5.7+/PostgreSQL）：`products` 表加 `attributes JSON`，建生成列+索引 `ALTER TABLE products ADD COLUMN ram INT GENERATED ALWAYS AS (attributes->>'$.ram') STORED, ADD INDEX(ram)`
    2. **宽表 + 稀疏列**：按品类建子表 `product_laptop(sku, ram, cpu, screen_size)` 继承 `products`
    3. **搜索引擎**：Elasticsearch 存全部属性，支持任意组合过滤+聚合，DB 只存核心字段
    4. **混合**：热属性（价格、品牌）提为列，冷属性放 JSON
- 推荐：核心筛选属性提为独立列 + 剩余放 JSON + 搜索引擎做复杂查询

**Spoken Answer (EN):**
"EAV turns every multi-attribute query into N self-joins — 'RAM=16GB AND price<1000' means join twice, scan millions of rows. Plus value is a string so numeric comparison is wrong. Fix: promote hot/filterable attributes into real typed columns (price DECIMAL, ram INT). For the long tail of attributes, use a JSON column — modern databases support indexing JSON fields via generated columns or functional indexes. For complex faceted search across hundreds of attributes, sync to Elasticsearch — it's designed for exactly this. Schema: products table with core columns (price, category_id, brand) + attributes JSON for the rest. Index the top 20 filterable attributes as generated columns. Query hits indexes for core filters, falls back to JSON for niche ones."

---

## Q13 — Multi-Tenant Data Isolation（多租户数据隔离策略）

**EN:** SaaS app with 10,000 tenants (from 10-user startups to 10,000-user enterprises). Design data isolation strategy balancing cost, security, and per-tenant customization.

**CN:** SaaS 应用 1万租户（从 10 人小团队到 1万人企业）。设计数据隔离策略，平衡成本、安全和租户定制。

**中文解题说明：**
- 三种模式：
    1. **共享表 + tenant_id**（行级隔离）：所有租户共享表，每行有 `tenant_id`
        - 优点：运维简单、成本低、schema 变更一次完成
        - 缺点：需每查询带 `tenant_id`（易漏→越权）、大租户影响小租户（noisy neighbor）、难物理隔离/独立备份
    2. **每租户独立 Schema**（同 DB 不同 schema）：`tenant_123.users`, `tenant_456.users`
        - 优点：逻辑隔离、可定制 schema、单租户备份/恢复
        - 缺点：连接池复杂、DB 连接数压力大、跨租户查询需 union
    3. **每租户独立数据库**：物理隔离
        - 优点：完全隔离、安全合规（金融/医疗）、可定制、独立扩缩容
        - 缺点：运维成本高（1万 DB 实例）、连接池爆炸
- **混合策略（推荐）**：
    - 小租户（<100用户）共享集群，按 tenant_id 分片（每片含多个小租户）
    - 大租户（>5000用户）独立数据库/独立 schema
    - 用连接池中间件（如 PgBouncer）管理连接
    - 应用层自动路由（tenant_id → 对应 DB 连接）
- **安全**：共享模式下必须强制 `tenant_id` 过滤（DB 行级安全策略 RLS / 中间件自动注入）

**Spoken Answer (EN):**
"Three tiers: Small tenants (<100 users): shared database, shared tables with tenant_id column. Use row-level security (RLS) or middleware to auto-inject tenant_id filter on every query — prevents cross-tenant data leaks. Mid-size tenants: dedicated schema within a shared database — logical isolation, allows custom fields/indexes per tenant, easier per-tenant backup. Large enterprises: dedicated database instance — full physical isolation, compliance-ready, independent scaling. Routing layer in the app maps tenant_id to the correct database/schema connection pool. For shared tables, always enforce tenant_id in a base DAO class — never trust application code to remember it. Use DB RLS as defense-in-depth."

---

## Q14 — Soft Delete & Unique Constraint Conflict（软删除与唯一约束冲突）

**EN:** `users(email, is_deleted)` — unique constraint on email breaks when same email registers again after soft-delete. Design a solution that allows re-registration while keeping historical data.

**CN:** `users(email, is_deleted)` — email 唯一约束在软删除后重新注册时冲突。设计允许重新注册同时保留历史数据的方案。

**中文解题说明：**
- 问题：软删除行 `is_deleted=true` 仍占唯一索引位置 → 同一 email 无法再插入
- 方案 1：**部分唯一索引**（PostgreSQL）
    - `CREATE UNIQUE INDEX idx_email_active ON users(email) WHERE is_deleted=false`
    - 只约束未删除行，已删除行不约束
- 方案 2：**复合唯一键 + 删除标记**
    - `(email, is_deleted)` 联合唯一 → 但只能删一次（第二次删会冲突，因为 (email, true) 已存在）
    - 改进：`(email, deleted_at)` 唯一 → 软删时填 `deleted_at` 时间戳（每次不同）→ 允许多次删+重注
- 方案 3：**随机后缀**
    - 软删时 `email = 'user@example.com_deleted_' + random()` → 释放原 email
    - 缺点：丑陋、关联数据外键断裂
- 方案 4：**独立历史表**
    - 软删时搬移到 `users_deleted` 表，原表物理删除 → 原表可重注
    - 缺点：外键需 CASCADE 或设为 NULL
- **推荐**：PostgreSQL 用部分唯一索引；MySQL 不支持部分索引 → 用 `(email, deleted_at)` 复合唯一（deleted_at 默认 NULL，软删时填时间戳）

**Spoken Answer (EN):**
"Partial unique index is the cleanest: `CREATE UNIQUE INDEX ON users(email) WHERE is_deleted=false` — only active rows enforce uniqueness, deleted rows don't block re-registration. MySQL doesn't support partial indexes, so workaround: composite unique key on (email, deleted_at). Set deleted_at=NULL for active rows (NULL doesn't collide in unique index... actually MySQL treats NULL as distinct, so multiple NULLs allowed — wait, that means multiple active rows could exist! Fix: use a sentinel value like 0 for active, timestamp for deleted). Better MySQL solution: (email, deleted_token) where active rows have deleted_token='', deleted rows get a UUID — unique constraint allows re-insertion. Alternative: move soft-deleted rows to a separate archive table, physically DELETE from main table — then re-registration is just a normal insert."

---

## Q15 — Large Column Separation & Blob Storage（大字段分离与 Blob 存储）

**EN:** `users` table has `avatar_blob` (5MB) and `bio_text` (10KB). Table has 50M rows. Queries for `SELECT name, email FROM users WHERE id=?` are slow. Fix.

**CN:** `users` 表含 `avatar_blob`(5MB) 和 `bio_text`(10KB)，5000万行。查询 `SELECT name, email FROM users WHERE id=?` 很慢。修复。

**中文解题说明：**
- 问题：`avatar_blob` 5MB 使每行巨大 → 每页（16KB）只能存极少行 → 全表扫描/索引扫描需读大量页 → 缓冲池命中率低
- 即使只查 `name, email`（小字段），如果走聚簇索引（InnoDB PK 即数据），也会把 blob 加载到内存
- 解决方案：
    1. **垂直拆分**：`users` 只留核心字段 `(id, name, email, bio_text)`，大字段移到 `user_profiles(user_id, avatar_blob, avatar_url, bio_text)`
    2. **对象存储**：avatar 存 S3/MinIO，DB 只存 URL（`avatar_url VARCHAR`）
    3. **InnoDB 行溢出**：InnoDB 对 BLOB 会存页外（overflow page），但仍有指针和首 768 字节在主页 → 仍有影响
- 推荐：对象存储 URL + 垂直拆分 bio_text 到扩展表（如果 bio 不常查）
- 效果：主表行从 ~5MB 降到 ~200B → 缓冲池可缓存更多行 → 查询速度数量级提升

**Spoken Answer (EN):**
"5MB blob per row means InnoDB can only fit ~3 rows per 16KB page — buffer pool eviction goes crazy, and even selecting just name/email pulls the entire blob into memory (clustered index stores the whole row). Fix: vertical partitioning. Move avatar_blob to object storage (S3), store only the URL in users table. If bio_text is rarely needed, move it to a user_profiles extension table (one-to-one). After splitting, users row is ~200 bytes → ~80 rows per page → buffer pool hit ratio skyrockets. Queries for name/email become pure index-covered or tiny row lookups. Rule: never put large blobs in your primary table — offload to object storage or a separate 1:1 side table."

---

## Q16 — Read-Replica Lag & Read-After-Write Consistency（主从延迟与读后一致）

**EN:** User updates profile, then refreshes — sees old data because read hits a replica with 2s lag. Design a read-after-write consistency pattern.

**CN:** 用户更新资料后刷新，看到旧数据因为读请求落到了有 2 秒延迟的从库。设计读后写一致性方案。

**中文解题说明：**
- 问题：异步复制 → 写后立刻读从库可能读到旧数据
- 方案 1：**Sticky Read（会话绑定）**
    - 用户写操作后，该用户后续 N 秒内的读请求强制走主库
    - 实现：写后设置 `session.last_write_ts = now()`，读路由检查 `now() - last_write_ts < 5s` → 路由主库
- 方案 2：**版本号/ETag**
    - 写后返回新版本号，客户端后续读带 `?v=etag` → 路由层识别该版本号强制走主库
- 方案 3：**Fencing Token**
    - 写操作返回单调递增 token，读请求带 token → 从库检查本地已应用的最高 token，若未追上则返回错误/重试
- 方案 4：**异步复制监控**
    - 从库暴露 `last_applied_lsn`，读前检查从库是否追上写操作的 LSN → 未追上则转主库
- **推荐**：Sticky Read（简单有效，覆盖 99% 场景）

**Spoken Answer (EN):**
"Classic replication lag issue. Simplest fix: sticky reads. After a user writes, tag their session with a timestamp. For the next 5–10 seconds, route that user's reads to the primary. After that window, replica lag is guaranteed to have caught up, so reads can go to replicas safely. Alternative: version-based routing — write returns a version/ETag, client includes it in next read, load balancer routes to primary if version is recent. For stronger guarantee: check replica's applied LSN against the write's LSN — if replica hasn't caught up, redirect to primary. In practice, sticky reads with a 5-second window solve 99% of read-after-write complaints with near-zero complexity."

---

## Q17 — Leading Wildcard LIKE & Full-Text Search（前导通配符与全文检索）

**EN:** `SELECT * FROM products WHERE name LIKE '%bluetooth%'` is a full table scan on 50M rows. How to make it fast?

**CN:** `SELECT * FROM products WHERE name LIKE '%bluetooth%'` 在 5000万行上全表扫描。如何加速？

**中文解题说明：**
- `%bluetooth%` 前导 `%` → 无法用 B-tree 索引（需扫描所有行匹配子串）
- 方案 1：**反向索引 + 反转字符串**
    - 存 `name_reverse` 列（'etoothtoob%'），查询 `LIKE 'toob%'` 可用索引
    - 缺点：只支持后缀匹配（原字符串的后缀），不支持中间子串
- 方案 2：**Trigram 索引**（PostgreSQL `pg_trgm`）
    - `CREATE INDEX idx_name_trgm ON products USING GIN (name gin_trgm_ops)`
    - 支持 `%bluetooth%` 任意位置匹配，利用 GIN 索引
- 方案 3：**全文搜索引擎**（Elasticsearch）
    - 分词 + 倒排索引，支持任意子串/模糊/高亮
- 方案 4：**MySQL FULLTEXT 索引**
    - `ALTER TABLE products ADD FULLTEXT(name)` → `MATCH(name) AGAINST('bluetooth')`
    - 缺点：只支持单词级别匹配，不支持任意子串（如 'bluet' 匹配不到）
- **推荐**：生产环境用 Elasticsearch；PostgreSQL 用 trigram GIN；MySQL 用 FULLTEXT（仅单词）或考虑迁移到 ES

**Spoken Answer (EN):**
"Leading wildcard '%bluetooth%' can't use a B-tree index — it's a full scan. Options: 1) Reverse the string and index reversed column for suffix searches (but that only covers trailing wildcards, not leading). 2) Trigram GIN index (PostgreSQL pg_trgm) — breaks string into 3-character grams, indexes them, supports LIKE '%any%' efficiently. 3) Full-text index (MySQL FULLTEXT, PostgreSQL tsvector) — but these match whole words, not arbitrary substrings like 'bluet'. 4) Elasticsearch with ngram tokenizer — indexes every substring, supports prefix/suffix/contains, plus ranking/highlighting. For 50M rows, Elasticsearch is the right answer: sync product names to ES, query there, fetch IDs from DB."

---

## Q18 — Sharding Key Choice & Hotspot Avoidance（分片键选择与热点避免）

**EN:** Shard a 100M-user table. Pick shard key? user_id hash vs region? Discuss hotspot, cross-shard query, and rebalancing.

**CN:** 1亿用户表分片，选分片键？user_id hash vs 按地区。讨论热点、跨分片查询、再平衡。

**中文解题说明：**
- **user_id hash**：
    - 优点：均匀分布、单用户数据同片（利于事务/查询）、无热点
    - 缺点：跨分片查询（按 email/phone 查需广播）、再平衡需 rehash（可用一致性 hash 缓解）
- **region**：
    - 优点：地理局部性好、可按地域部署
    - 缺点：数据倾斜（大城市用户多）、热点、单 region 故障影响大
- **推荐**：`user_id` 一致性 hash 分片
- **跨分片查询解决**：
    - 按 email 查 → 建全局二级索引（另一分片集群按 email hash）
    - 或异构冗余（同时按 email 分片一份）
- **再平衡**：一致性 hash 环 + 虚拟节点，扩缩容只影响相邻分片
- **避免热点**：监控每个分片 QPS/容量，发现倾斜可手动迁移热点 user 到独立分片（如大V用户单独分片）

**Spoken Answer (EN):**
"Hash on user_id gives even distribution and colocates one user's rows on one shard — single-shard transactions and queries work great. Avoid region as shard key unless geo-locality is critical — it creates hotspots (NYC users crush one shard). Cross-shard lookups (by email) need a secondary index table sharded by email hash, or a global lookup service. Rebalancing: use consistent hashing with virtual nodes so adding a shard only moves ~1/N of data. For extreme hotspots (celebrity accounts), consider a separate 'whale' shard or application-level routing to isolate them."

---

## Q19 — Full-Text Search vs LIKE（全文检索 vs 模糊匹配）

**EN:** Search product names across millions rows — LIKE '%word%' too slow. Compare FULLTEXT index vs Elasticsearch vs ngram.

**CN:** 百万商品名模糊搜索 LIKE '%word%'太慢，对比 FULLTEXT 索引、Elasticsearch、ngram。

**中文解题说明：**
- **LIKE '%word%'**：全表扫描，O(n)，不可接受
- **MySQL FULLTEXT**：
    - 倒排索引，按单词分词
    - 优点：内置、无需额外组件
    - 缺点：只支持单词匹配（不支持子串如 'bluet'）、中文需 ngram parser、相关性排序弱、不支持高亮
- **Elasticsearch**：
    - 倒排 + ngram/edge-ngram 分词
    - 优点：支持任意子串、模糊匹配、相关性评分、高亮、聚合
    - 缺点：运维成本、数据同步延迟
- **ngram 索引**（PostgreSQL `pg_trgm` / MySQL `ngram parser`）：
    - 将字符串拆成 N 个字符的片段建索引
    - 支持 `LIKE '%word%'` 走索引
    - 缺点：索引大（每个字符串生成很多片段）
- **推荐**：中小规模用 FULLTEXT + ngram parser；大规模/复杂搜索用 Elasticsearch

**Spoken Answer (EN):**
"LIKE '%word%' is a full scan — unusable at scale. MySQL FULLTEXT with ngram parser can index substrings but it's still limited in relevance ranking and Chinese tokenization. PostgreSQL trigram indexes (pg_trgm) let you keep data in DB and use LIKE with index scans, but index size balloons. Elasticsearch is purpose-built: inverted index + ngram/edge-ngram tokenization supports prefix, substring, fuzzy matching, relevance scoring, highlighting, faceting — all out of the box. For a serious search product, sync to ES; for simple substring search on modest data, trigram GIN index is a good middle ground."

---

## Q20 — Range Partition & TTL for Time-Series Data（按时间分区与 TTL）

**EN:** Audit log table grows to 2TB. Queries only need last 90 days. Design partitioning + retention + archival.

**CN:** 审计日志表涨到 2TB，查询只需最近 90 天。设计分区、保留策略、归档。

**中文解题说明：**
- **范围分区**：`PARTITION BY RANGE (created_at)` 按月/周分区
    - 查询自动裁剪（partition pruning）只扫相关分区
- **TTL / 数据淘汰**：
    - 方案 1：`DROP PARTITION` 删除旧分区（瞬时，无 DELETE 开销）
    - 方案 2：`DELETE FROM ... WHERE created_at < date_sub(NOW(), INTERVAL 90 DAY)` — 慢、产生碎片
- **归档**：旧分区可 `ALTER TABLE ... EXCHANGE PARTITION` 导出到归档表/冷存储（S3/对象存储）
- **索引**：分区内建本地索引，避免全局索引维护成本
- **注意**：分区键必须包含在主键/唯一键中（MySQL 限制）
- **扩展**：可结合列式存储（如 ClickHouse）做冷数据分析

**Spoken Answer (EN):**
"Range-partition by month on created_at. Queries with a time filter automatically prune to the relevant partition — no full scan. Retention: drop partitions older than 90 days — DROP PARTITION is a metadata operation, instant and doesn't bloat the table. For compliance, archive the dropped partition to cold storage (S3) via EXCHANGE PARTITION before dropping. This keeps active partitions small, queries fast, and storage costs bounded. For analytics on old data, export to a columnar warehouse. Key: partition key must be part of the PK in MySQL, so design PK as (id, created_at)."