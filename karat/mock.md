# karaat Mock Question: DiscountService (E-commerce Discount System)

> **Interview Round**: 30-minute Coding Round  
> **Level**: Mid-level (GCB5)  
> **Focus**: Concurrency safety, monetary precision, API design, testing mindset, distributed awareness

---

## Background

You maintain a `DiscountService` for an e-commerce platform. It manages promo codes: validation, applying discounts to carts, and tracking usage.

**Requirements**:
- Load promo codes into cache
- Validate promo code (not expired, not exceeding max usage)
- Apply discount to a shopping cart
- Query promo codes expiring within N days

---

## Original Code (Buggy Version)

```java
import java.math.*;
import java.time.*;
import java.util.*;

class PromoCode {
    private String code;
    private double discountPercent;
    private LocalDate expiryDate;
    private int maxUsage;
    private int currentUsage;

    public PromoCode(String code, double discountPercent, LocalDate expiryDate, int maxUsage) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.expiryDate = expiryDate;
        this.maxUsage = maxUsage;
        this.currentUsage = 0;
    }

    public String getCode() { return code; }
    public double getDiscountPercent() { return discountPercent; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getMaxUsage() { return maxUsage; }
    public int getCurrentUsage() { return currentUsage; }
    public void incrementUsage() { currentUsage++; }
}

class CartItem {
    private String productId;
    private double price;
    private int quantity;

    public CartItem(String productId, double price, int quantity) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getTotal() { return price * quantity; }
}

public class DiscountService {
    private Map<String, PromoCode> promoCache;

    public DiscountService() {
        this.promoCache = new HashMap<>();
    }

    public void loadPromos(List<PromoCode> promos) {
        for (int i = 0; i < promos.size(); i++) {
            PromoCode p = promos.get(i);
            promoCache.put(p.getCode(), p);
        }
    }

    public double applyDiscount(List<CartItem> cart, String promoCode) {
        PromoCode promo = promoCache.get(promoCode);
        if (promo == null) return calculateTotal(cart);
        if (promo.getExpiryDate().isBefore(LocalDate.now())) return calculateTotal(cart);
        if (promo.getCurrentUsage() >= promo.getMaxUsage()) return calculateTotal(cart);

        double total = calculateTotal(cart);
        double discount = total * promo.getDiscountPercent() / 100.0;
        promo.incrementUsage();
        return total - discount;
    }

    private double calculateTotal(List<CartItem> cart) {
        double total = 0.0;
        for (CartItem item : cart) total += item.getTotal();
        return total;
    }

    public List<String> getExpiringPromos(int days) {
        List<String> result = new ArrayList<>();
        LocalDate cutoff = LocalDate.now().plusDays(days);
        for (PromoCode p : promoCache.values()) {
            if (p.getExpiryDate().isBefore(cutoff)) result.add(p.getCode());
        }
        return result;
    }
}
```

---

## Step 1: Bug Identification — Detailed English Answers

### Bug #1: `incrementUsage()` is not thread-safe (Concurrency)

**Detailed Verbal Answer:**

> "The most critical issue I see is in the `incrementUsage` method. It uses `currentUsage++`, which looks like a single operation but is actually three steps at the bytecode level: read the current value, add one, write it back. This is called a **read-modify-write** cycle.
>
> Under concurrency, if two threads call `applyDiscount` simultaneously with the same promo code, they might both read `currentUsage` as, say, 5 at the same time. Both increment to 6 and write back 6. So `currentUsage` becomes 6, but we actually had two usages — it should be 7. This is a **lost update** problem.
>
> To fix this, I'd change `currentUsage` from `int` to `AtomicInteger`. Then `incrementUsage` becomes `currentUsage.incrementAndGet()`. This uses a **CAS (Compare-And-Swap)** operation under the hood, which is lock-free and guarantees atomicity even under high contention.
>
> Why not `synchronized`? `synchronized` works but it's a pessimistic lock — it blocks all other threads even for reads. `AtomicInteger` is non-blocking and scales much better under high concurrency."

**Key vocabulary**: read-modify-write, lost update, Compare-And-Swap (CAS), lock-free, pessimistic lock, contention

---

### Bug #2: `HashMap` is not thread-safe (Concurrency)

**Detailed Verbal Answer:**

> "Another concurrency issue is that `promoCache` is a plain `HashMap`. `HashMap` is not thread-safe. If one thread calls `loadPromos` (which does `put` operations) while another thread calls `applyDiscount` (which does `get`), we have a **read-write race condition**.
>
> In JDK 7, this could actually cause an **infinite loop** during `resize`. In JDK 8+, it won't loop infinitely but you can get **data corruption** — entries might disappear or `get` might return `null` even though the key exists.
>
> The fix is simple: replace `HashMap` with `ConcurrentHashMap`. It uses **segment-level locking** (or in newer JDK versions, CAS + synchronized on bins), which allows concurrent reads without locking and safe concurrent writes. For our use case, `ConcurrentHashMap` is perfect because we do frequent reads (`applyDiscount` does a `get` on every call) and occasional writes (`loadPromos` is called rarely)."

**Key vocabulary**: read-write race condition, data corruption, segment-level locking, concurrent reads, thread-safe

---

### Bug #3: `double` for monetary values (Precision)

**Detailed Verbal Answer:**

> "The third major issue is using `double` for prices, discounts, and totals. `double` is a floating-point type that cannot represent all decimal values exactly in binary. For example, `0.1` in binary is a repeating fraction, so `0.1 + 0.2` in `double` gives `0.30000000000000004`, not `0.3`.
>
> In an e-commerce system, this is unacceptable. Imagine a customer buys 100 items at $0.10 each. The total should be $10.00, but with `double` arithmetic, it might compute as $9.999999999997. When we apply a 10% discount, the error compounds. At scale, these rounding errors cause **reconciliation failures** — the finance team will see mismatches between order totals and payment gateway records.
>
> The fix is to use `BigDecimal` everywhere: `price`, `discountPercent`, `total`, and return type of `applyDiscount`. `BigDecimal` stores decimal numbers exactly as a `BigInteger` plus a scale. We should also specify a `RoundingMode` (typically `RoundingMode.HALF_UP` for financial systems) whenever we do division.
>
> One thing to note: always construct `BigDecimal` from a `String`, not from a `double`, because `new BigDecimal(0.1)` already carries the floating-point imprecision. Use `new BigDecimal("0.1")` instead."

**Key vocabulary**: floating-point, reconciliation failure, compounding error, RoundingMode, scale, exact decimal representation

---

### Bug #4: `isBefore(cutoff)` excludes same-day expiry (Logic)

**Detailed Verbal Answer:**

> "In `getExpiringPromos`, the code checks `p.getExpiryDate().isBefore(cutoff)`. The problem is that `isBefore` is **strictly less than**. If a promo expires today and `cutoff` is today, `isBefore` returns `false` because they are equal — not before.
>
> So promos expiring exactly on the cutoff date are silently excluded from the result. Depending on business requirements, this might mean we don't send expiry reminder notifications to merchants for promos that expire today — a real business impact.
>
> The fix is to use `!isAfter(cutoff)`, which means 'is on or before the cutoff date'. Alternatively, we could use `isBefore(cutoff) || isEqual(cutoff)` for explicit clarity."

**Key vocabulary**: strictly less than, silently excluded, business impact, expiry reminder

---

### Bug #5: No null/empty validation (Boundary)

**Detailed Verbal Answer:**

> "There are no guard clauses in `applyDiscount`. If `cart` is `null`, the method will throw a `NullPointerException` when it tries to iterate. If `promoCode` is `null`, `promoCache.get(null)` returns `null`, and the method falls through to `calculateTotal` — which is actually safe but returns the wrong semantic result (no discount applied without any indication that the promo code was invalid).
>
> Best practice is to add explicit checks at the start: if `cart` is `null` or empty, return `BigDecimal.ZERO`. If `promoCode` is `null` or blank, also return `calculateTotal(cart)` or throw an `IllegalArgumentException` depending on the API contract."

**Key vocabulary**: guard clause, NullPointerException, semantic result, API contract, IllegalArgumentException

---

### Bug #6: `loadPromos` accumulates without clearing (Design)

**Detailed Verbal Answer:**

> "Every time `loadPromos` is called, it just iterates and `put`s into the map. If it's called a second time (maybe a scheduled job refreshes promos every hour), old promo codes that are no longer active will **remain in the cache**. This means customers might be able to use expired or deactivated promos.
>
> The fix is to call `promoCache.clear()` at the start of `loadPromos`, or use `putIfAbsent` if the intent is to only add new ones without removing existing ones. I'd go with `clear()` because typically a 'load' operation implies a full refresh."

**Key vocabulary**: accumulates, full refresh, deactivated, scheduled job

---

## Step 2: Fix + New Feature — Detailed English Answers

### New Requirement

> Add `applyDiscountWithCap(List<CartItem> cart, String promoCode, BigDecimal maxDiscountCap)`
> - Discount amount must never exceed `maxDiscountCap`
> - If `maxDiscountCap` is `null`, no cap applies (same behavior as original `applyDiscount`)

### Fully Fixed & Enhanced Code

```java
import java.math.*;
import java.time.*;
import java.util.*;
import java.math.RoundingMode;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a promotional code with thread-safe usage tracking.
 */
class PromoCode {
    private String code;
    private BigDecimal discountPercent;          // FIX: was double
    private LocalDate expiryDate;
    private int maxUsage;
    private AtomicInteger currentUsage;           // FIX: was int, now thread-safe

    public PromoCode(String code, BigDecimal discountPercent, LocalDate expiryDate, int maxUsage) {
        this.code = code;
        this.discountPercent = discountPercent;
        this.expiryDate = expiryDate;
        this.maxUsage = maxUsage;
        this.currentUsage = new AtomicInteger(0);
    }

    public String getCode() { return code; }
    public BigDecimal getDiscountPercent() { return discountPercent; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getMaxUsage() { return maxUsage; }
    public int getCurrentUsage() { return currentUsage.get(); }
    public void incrementUsage() { currentUsage.incrementAndGet(); }  // FIX: atomic
}

/**
 * Represents a single item in the shopping cart.
 */
class CartItem {
    private String productId;
    private BigDecimal price;                    // FIX: was double
    private int quantity;

    public CartItem(String productId, BigDecimal price, int quantity) {
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductId() { return productId; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public BigDecimal getTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));  // FIX: BigDecimal arithmetic
    }
}

/**
 * Service for managing and applying promotional discounts.
 */
public class DiscountService {

    private final Map<String, PromoCode> promoCache;  // FIX: now ConcurrentHashMap

    public DiscountService() {
        this.promoCache = new ConcurrentHashMap<>();   // FIX: thread-safe map
    }

    /**
     * Loads all promo codes into cache. Clears existing entries first.
     */
    public void loadPromos(List<PromoCode> promos) {
        promoCache.clear();  // FIX: prevent stale data accumulation
        for (PromoCode p : promos) {
            promoCache.put(p.getCode(), p);
        }
    }

    /**
     * Original method — delegates to new method with null cap.
     */
    public BigDecimal applyDiscount(List<CartItem> cart, String promoCode) {
        return applyDiscountWithCap(cart, promoCode, null);
    }

    /**
     * Applies discount with an optional maximum cap.
     * FIX: added cap logic, null safety, BigDecimal throughout.
     */
    public BigDecimal applyDiscountWithCap(List<CartItem> cart, String promoCode, BigDecimal maxDiscountCap) {
        // FIX: guard clause for null/empty cart
        if (cart == null || cart.isEmpty()) {
            return BigDecimal.ZERO;
        }

        PromoCode promo = promoCache.get(promoCode);
        if (promo == null) {
            return calculateTotal(cart);
        }

        if (promo.getExpiryDate().isBefore(LocalDate.now())) {
            return calculateTotal(cart);
        }

        if (promo.getCurrentUsage() >= promo.getMaxUsage()) {
            return calculateTotal(cart);
        }

        BigDecimal total = calculateTotal(cart);
        BigDecimal discountRate = promo.getDiscountPercent()
            .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);  // FIX: explicit scale + rounding
        BigDecimal discount = total.multiply(discountRate);

        // NEW FEATURE: apply cap if provided
        if (maxDiscountCap != null && discount.compareTo(maxDiscountCap) > 0) {
            discount = maxDiscountCap;
        }

        promo.incrementUsage();
        return total.subtract(discount);
    }

    private BigDecimal calculateTotal(List<CartItem> cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cart) {
            total = total.add(item.getTotal());
        }
        return total;
    }

    /**
     * Returns promo codes expiring within the given number of days.
     * FIX: now includes promos expiring exactly on the cutoff date.
     */
    public List<String> getExpiringPromos(int days) {
        List<String> result = new ArrayList<>();
        LocalDate cutoff = LocalDate.now().plusDays(days);
        for (PromoCode p : promoCache.values()) {
            if (!p.getExpiryDate().isAfter(cutoff)) {  // FIX: was isBefore(cutoff)
                result.add(p.getCode());
            }
        }
        return result;
    }
}
```

### Detailed Verbal Walkthrough (What to say while coding):

> "I'm starting with the concurrency fixes. First, I'll change `currentUsage` from `int` to `AtomicInteger`. The constructor initializes it to zero, `incrementUsage` now uses `incrementAndGet`, and `getCurrentUsage` returns `get()`.
>
> Next, I'm switching `promoCache` from `HashMap` to `ConcurrentHashMap`. Just one line change in the constructor.
>
> Now for the money fields — I'm replacing every `double` with `BigDecimal`. In `CartItem`, `price` becomes `BigDecimal`, and `getTotal()` uses `multiply`. In `PromoCode`, `discountPercent` becomes `BigDecimal`. In `calculateTotal`, I'm using `BigDecimal.ZERO` and the `add` method.
>
> For the discount calculation, I need to divide by 100 to get the rate. `BigDecimal.divide` requires a scale and rounding mode, so I'll use `divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)`.
>
> For the new feature `applyDiscountWithCap`, I'll add a `maxDiscountCap` parameter. After calculating the discount, I check if `maxDiscountCap` is not null and if `discount.compareTo(maxDiscountCap) > 0`. If so, I cap it. I'll also make the original `applyDiscount` delegate to this new method with `null` as the cap.
>
> Finally, I'm adding a guard clause for `cart` being null or empty, fixing `loadPromos` to call `clear()` first, and fixing `getExpiringPromos` to use `!isAfter(cutoff)`."

---

## Step 3: Testing + Analysis — Detailed English Answers

### Test Cases (Verbal with numbers)

> "Let me walk through three test cases.
>
> **Test Case 1 — Normal flow, no cap.** Cart has two items: a shirt at $60 and pants at $40. Total = $100. Promo code 'SAVE15' gives 15% off. No cap. Calculated discount = $100 × 0.15 = $15. Final amount = $85. `currentUsage` increments from 0 to 1.
>
> **Test Case 2 — Cap applied.** Cart has three items: a laptop at $200, a mouse at $150, and a keyboard at $50. Total = $400. Promo is 25% off. `maxDiscountCap` = $60. Calculated discount = $400 × 0.25 = $100. Since $100 > $60, the discount is capped at $60. Customer pays $340. This tests the new feature.
>
> **Test Case 3 — Edge case, expired promo.** Cart has one item at $80. Promo 'OLD20' gives 20% off but expired yesterday. `maxDiscountCap` = $10. The method checks `expiryDate.isBefore(LocalDate.now())` → true → returns full total $80 without applying discount. `currentUsage` stays unchanged. This verifies that the expiry check happens before any cap logic.
>
> **Test Case 4 — Edge case, null cart.** `cart` is `null`. Guard clause returns `BigDecimal.ZERO` immediately. No promo lookup, no usage increment. This prevents `NullPointerException`."

### Complexity Analysis (Detailed)

> "Let me analyze the complexity of each method.
>
> **`applyDiscountWithCap`**: Time complexity is **O(n)** where n is the number of items in the cart. This is because `calculateTotal` iterates through all items once. The `ConcurrentHashMap.get` is O(1) average case. Space complexity is **O(1)** — we only allocate a few local `BigDecimal` variables regardless of input size.
>
> **`loadPromos`**: Time complexity is **O(m)** where m is the number of promo codes being loaded, plus O(k) for `clear()` where k is existing entries. Each `put` is O(1) average. Space complexity is **O(m)** because the cache stores m entries.
>
> **`getExpiringPromos`**: Time complexity is **O(m)** where m is total promo codes in cache — we iterate all values. Space complexity is **O(k)** where k is the number of expiring promos added to the result list. In the worst case where all promos are expiring, this is O(m)."

### Distributed Scenario (Detailed)

> "Now, if this service runs on 3 servers behind a load balancer, does `AtomicInteger` still work?
>
> **No, it does not.** `AtomicInteger` guarantees atomicity only within a single JVM. Each server has its own JVM, and each JVM has its own instance of `DiscountService` with its own `PromoCode` objects in memory. So if Server A and Server B both process a request with the same promo code, each will increment its own `AtomicInteger` from 0 to 1. The actual usage is 2, but each server thinks it's only 1. This means we could exceed the `maxUsage` limit — a serious bug in production.
>
> To fix this in a distributed environment, I'd move the counter to a **shared data store**. The most common solution is **Redis `INCR`**. Redis is single-threaded, so `INCR` is atomic across all servers. We'd use a key like `promo:usage:{code}`. Every time `applyDiscount` is called, we call `INCR` on that key and check if the returned value exceeds `maxUsage`.
>
> Another option is **database optimistic locking**: `UPDATE promo_codes SET current_usage = current_usage + 1 WHERE code = ? AND current_usage < max_usage`. This gives strong consistency but adds database load.
>
> A third option is a **distributed lock** using Redis `SETNX` or Redisson `RLock`, but that's heavier than needed — `INCR` is simpler and faster.
>
> So my recommendation: use Redis `INCR` for the counter, and `ConcurrentHashMap` is still fine for the local cache (since promo metadata like `discountPercent` and `expiryDate` doesn't change often)."

---

## Key English Expressions Cheat Sheet

| Situation | Detailed Phrases |
|---|---|
| Spotting concurrency issue | "This is a **read-modify-write** cycle, which is not atomic under concurrency. Multiple threads can interleave, causing **lost updates**." |
| Proposing `AtomicInteger` | "I'd use `AtomicInteger` with `incrementAndGet()`. It uses **CAS (Compare-And-Swap)** under the hood, which is **lock-free** and scales well under contention." |
| Rejecting `synchronized` | "`synchronized` is a **pessimistic lock** — it blocks all other threads. `AtomicInteger` is **non-blocking** and performs better." |
| `HashMap` → `ConcurrentHashMap` | "`HashMap` is not thread-safe and can cause **data corruption** or **infinite loops** on concurrent access. `ConcurrentHashMap` allows **concurrent reads without locking**." |
| Money precision | "`double` uses **floating-point arithmetic**, which causes **rounding errors**. In financial systems, `BigDecimal` ensures **exact decimal representation**." |
| `BigDecimal` constructor tip | "Always construct `BigDecimal` from a `String`, not a `double`, because `new BigDecimal(0.1)` already carries **floating-point imprecision**." |
| Distributed limitation | "`AtomicInteger` is **JVM-local**. For multi-server deployment, I'd use **Redis `INCR`** — it's single-threaded and atomic across all nodes." |
| Complexity | "Time is **O(n)** where n is cart size — we iterate once in `calculateTotal`. Space is **O(1)** — only local variables, no extra data structures." |
| Trade-off | "The trade-off is between **consistency and performance**. Redis `INCR` gives us **eventual consistency** with low latency, while database locking gives **strong consistency** at the cost of throughput." |
| Edge case handling | "I added a **guard clause** at the start to handle `null` or empty input, preventing `NullPointerException` and giving clear behavior." |

---

## Bonus: Interviewer Follow-up Questions & Model Answers

**Q: "What if `applyDiscount` is called twice for the same order due to a retry?"**

> "Good question. Right now, if a client retries, we'd increment `currentUsage` twice and apply the discount twice. To prevent this, we need **idempotency**. We could generate a unique `orderId` or `requestId` for each checkout attempt, store it in a Redis set with `SETNX`, and check it before applying the discount. If the `requestId` already exists, we return the previously computed discount without incrementing usage."

**Q: "How would you add metrics to this service?"**

> "I'd add counters for key events: `promo_not_found`, `promo_expired`, `promo_usage_exceeded`, `promo_capped` (when cap was applied), and `promo_applied_successfully`. Using a metrics library like Micrometer or Dropwizard, we can track these and set up alerts — for example, if `promo_not_found` spikes, maybe there's a bug in `loadPromos`."

**Q: "What if promo codes change frequently? How do you invalidate the cache?"**

> "Two options. First, **TTL-based**: give each cache entry a time-to-live (e.g., 5 minutes) and reload from DB. Second, **pub-sub**: when an admin updates a promo in the database, publish an event to a message queue (e.g., Kafka or Redis pub/sub), and all service instances listen and evict or reload that specific promo from cache. The pub-sub approach gives near real-time consistency."

---







# karaat Mock Question 2: InventoryLockService (Flash-Sale Inventory Locking System)

> **Interview Round**: 30-minute Coding Round  
> **Level**: Mid-level (GCB5)  
> **Focus**: Concurrency safety, overselling prevention, transaction semantics, lock design, distributed inventory

---

## Background

You maintain an `InventoryLockService` for a flash-sale e-commerce platform. During high-traffic events, users complain that the same item gets oversold — inventory goes negative. Your task is to identify the bugs, fix them, and add a new feature.

**Requirements**:
- Load inventory items into cache
- Reserve items when user clicks "Buy Now"
- Confirm purchase after payment succeeds
- Release lock after payment failure or timeout
- Query items with stock below a threshold

---

## Original Code (Buggy Version)

```java
import java.util.*;
import java.util.concurrent.*;

class InventoryItem {
    private String itemId;
    private int totalStock;
    private int lockedStock;  // items reserved but not yet paid

    public InventoryItem(String itemId, int totalStock) {
        this.itemId = itemId;
        this.totalStock = totalStock;
        this.lockedStock = 0;
    }

    public String getItemId() { return itemId; }
    public int getTotalStock() { return totalStock; }
    public int getLockedStock() { return lockedStock; }
    public int getAvailableStock() { return totalStock - lockedStock; }

    public boolean reserve(int quantity) {
        if (getAvailableStock() >= quantity) {
            lockedStock += quantity;
            return true;
        }
        return false;
    }

    public void confirmPurchase(int quantity) {
        lockedStock -= quantity;
        totalStock -= quantity;
    }

    public void releaseLock(int quantity) {
        lockedStock -= quantity;
    }
}

public class InventoryLockService {
    private Map<String, InventoryItem> inventory;

    public InventoryLockService() {
        this.inventory = new HashMap<>();
    }

    public void loadInventory(List<InventoryItem> items) {
        for (InventoryItem item : items) {
            inventory.put(item.getItemId(), item);
        }
    }

    public boolean tryReserve(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item == null) return false;
        return item.reserve(quantity);
    }

    public void confirm(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item != null) {
            item.confirmPurchase(quantity);
        }
    }

    public void release(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item != null) {
            item.releaseLock(quantity);
        }
    }

    public List<String> getLowStockItems(int threshold) {
        List<String> result = new ArrayList<>();
        for (InventoryItem item : inventory.values()) {
            if (item.getAvailableStock() < threshold) {
                result.add(item.getItemId());
            }
        }
        return result;
    }
}
```

---

## Step 1: Bug Identification — Detailed English Answers

### Bug #1 & #2: `reserve()` is not atomic — Overselling (Most Critical)

**Detailed Verbal Answer:**

> "The most critical bug is in `reserve()`. It performs a **check-then-act** operation: first it calls `getAvailableStock() >= quantity`, then if true, it does `lockedStock += quantity`. These are two separate steps with no synchronization between them.
>
> Under high concurrency — which is exactly the scenario for a flash-sale system — here's what happens: Thread A checks `getAvailableStock()` and sees 1 item left. Before Thread A executes `lockedStock += quantity`, Thread B also checks `getAvailableStock()` and also sees 1 item left (because A hasn't updated `lockedStock` yet). Both threads pass the check and both execute `lockedStock += 1`. Now `lockedStock` increased by 2, but we only had 1 item in stock. This is the classic **race condition** that causes **overselling**.
>
> To fix this, I need to make the entire `reserve` method atomic. Since it involves checking one field and updating another, a simple `AtomicInteger` isn't enough — I need a `ReentrantLock` or `synchronized` block to protect the whole check-and-update sequence.
>
> Alternatively, if we move to a database, we can use `UPDATE inventory SET locked_stock = locked_stock + ? WHERE item_id = ? AND total_stock - locked_stock >= ?` — a single atomic SQL statement."

**Key vocabulary**: check-then-act, race condition, overselling, flash-sale, atomic operation, ReentrantLock, synchronized block, mutual exclusion

---

### Bug #3: `confirmPurchase` — Two writes are not atomic

**Detailed Verbal Answer:**

> "In `confirmPurchase`, we do `lockedStock -= quantity` then `totalStock -= quantity`. These are two separate writes. If a thread gets interrupted (or another thread calls `releaseLock`) between these two lines, `lockedStock` and `totalStock` become inconsistent. For example, `lockedStock` goes down but `totalStock` hasn't been updated yet — another thread calling `getAvailableStock()` would see a wrong, inflated available count.
>
> The fix is to wrap both operations in a `ReentrantLock` to make them atomic together."

**Key vocabulary**: inconsistent state, interrupted between operations, wrap in lock, atomic sequence

---

### Bug #4: `releaseLock` has no lower-bound check

**Detailed Verbal Answer:**

> "`releaseLock` simply does `lockedStock -= quantity` with no validation. If there's a bug in the calling code — say it calls `release` twice for the same order, or passes a `quantity` larger than what was locked — `lockedStock` becomes negative. Then `getAvailableStock()` returns `totalStock - (negative number)` = a value **larger** than `totalStock`, which is logically impossible and causes **overselling** again.
>
> Fix: add a validation check: if `quantity > lockedStock`, reject the operation instead of blindly subtracting. This prevents `lockedStock` from going negative."

**Key vocabulary**: lower-bound check, blindly subtracting, logically impossible, validation guard

---

### Bug #5: `HashMap` is not thread-safe

**Detailed Verbal Answer:**

> "`inventory` is a plain `HashMap`. `loadInventory` does `put` operations while `tryReserve` does `get`. Under concurrent access, `HashMap` can cause **data corruption** or **infinite loops** (JDK 7). Replace with `ConcurrentHashMap` for thread-safe lookups and updates."

**Key vocabulary**: data corruption, thread-safe, ConcurrentHashMap, concurrent reads

---

### Bug #6: No timeout/cleanup mechanism (Design Flaw)

**Detailed Verbal Answer:**

> "There's no timeout mechanism. If a user reserves an item but their payment fails, or the service crashes before `release` is called, that inventory stays locked forever. In a flash-sale, this means items become artificially unavailable even though no one actually bought them — **permanent dead stock**.
>
> I'd add a **timestamp** to each reservation and a background thread that periodically scans for reservations older than, say, 15 minutes and releases them automatically. Alternatively, on each `tryReserve` call, lazily clean up expired reservations for that specific item."

**Key vocabulary**: permanent dead stock, timeout mechanism, background cleanup thread, lazy cleanup, TTL (time-to-live)

---

### Bug #7: `loadInventory` accumulates without clearing

**Detailed Verbal Answer:**

> "Every time `loadInventory` is called, it just iterates and `put`s into the map without clearing existing entries. If it's called a second time (maybe a refresh job), old inventory items remain in the cache. This means stale stock data persists. Fix: call `inventory.clear()` at the start."

**Key vocabulary**: accumulates, stale data, full refresh

---

### Bug #8: `getLowStockItems` reads without lock (Stale Data)

**Detailed Verbal Answer:**

> "`getLowStockItems` reads `getAvailableStock()` without any lock. Under concurrent modifications, it may return stale or inconsistent data. For a monitoring endpoint, eventual consistency is usually acceptable, but I'd mention this trade-off — if the business requires real-time accuracy, we need a lock or a different approach."

**Key vocabulary**: stale data, eventual consistency, trade-off, real-time accuracy

---

## Step 2: Fix + New Feature — Detailed English Answers

### New Requirement

> Add `batchReserve(Map<String, Integer> itemQuantities)` — batch reserve multiple items:
> - Either ALL reservations succeed, or ALL are rolled back (**all-or-nothing**)
> - If ANY item has insufficient stock, already-locked items must be released
> - Return `true` if all succeeded, `false` if all rolled back

### Fully Fixed & Enhanced Code

```java
import java.util.*;
import java.util.concurrent.*;

/**
 * Thread-safe inventory item with ReentrantLock protection.
 */
class InventoryItem {
    private final String itemId;
    private int totalStock;
    private int lockedStock;
    private final ReentrantLock lock = new ReentrantLock();  // FIX: lock for atomicity

    public InventoryItem(String itemId, int totalStock) {
        this.itemId = itemId;
        this.totalStock = totalStock;
        this.lockedStock = 0;
    }

    public String getItemId() { return itemId; }
    public int getTotalStock() { return totalStock; }
    public int getLockedStock() { return lockedStock; }
    public int getAvailableStock() { return totalStock - lockedStock; }

    /**
     * Attempt to reserve quantity. Thread-safe.
     * FIX: entire method is atomic under lock.
     */
    public boolean reserve(int quantity) {
        lock.lock();
        try {
            if (quantity <= 0) return false;
            if (getAvailableStock() >= quantity) {
                lockedStock += quantity;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Confirm purchase: reduce both locked and total stock.
     * FIX: atomic under lock.
     */
    public boolean confirmPurchase(int quantity) {
        lock.lock();
        try {
            if (quantity <= 0 || quantity > lockedStock) return false;
            lockedStock -= quantity;
            totalStock -= quantity;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Release locked inventory.
     * FIX: added validation to prevent negative lockedStock.
     */
    public boolean releaseLock(int quantity) {
        lock.lock();
        try {
            if (quantity <= 0 || quantity > lockedStock) return false;
            lockedStock -= quantity;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Peek available stock without modifying (for read-only queries).
     */
    public int peekAvailableStock() {
        lock.lock();
        try {
            return getAvailableStock();
        } finally {
            lock.unlock();
        }
    }
}

/**
 * Service for managing inventory locks during flash sales.
 */
public class InventoryLockService {

    private final Map<String, InventoryItem> inventory;  // FIX: ConcurrentHashMap

    public InventoryLockService() {
        this.inventory = new ConcurrentHashMap<>();
    }

    /**
     * Load inventory. FIX: clears old data first.
     */
    public void loadInventory(List<InventoryItem> items) {
        inventory.clear();  // FIX: prevent stale data accumulation
        for (InventoryItem item : items) {
            inventory.put(item.getItemId(), item);
        }
    }

    /**
     * Reserve items for a single product.
     */
    public boolean tryReserve(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item == null) return false;
        return item.reserve(quantity);
    }

    /**
     * Confirm purchase after payment.
     */
    public boolean confirm(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item == null) return false;
        return item.confirmPurchase(quantity);
    }

    /**
     * Release lock after payment failure or timeout.
     */
    public boolean release(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item == null) return false;
        return item.releaseLock(quantity);
    }

    /**
     * Get items with available stock below threshold.
     * Note: this is a snapshot, may be slightly stale under concurrency.
     */
    public List<String> getLowStockItems(int threshold) {
        List<String> result = new ArrayList<>();
        for (InventoryItem item : inventory.values()) {
            if (item.peekAvailableStock() < threshold) {
                result.add(item.getItemId());
            }
        }
        return result;
    }

    // ============ NEW FEATURE ============

    /**
     * Batch reserve multiple items. All-or-nothing semantics.
     * If ANY item fails, rollback ALL previously locked items.
     *
     * @param itemQuantities Map of itemId -> quantity to reserve
     * @return true if ALL reserved successfully, false if ALL rolled back
     */
    public boolean batchReserve(Map<String, Integer> itemQuantities) {
        if (itemQuantities == null || itemQuantities.isEmpty()) return false;

        // Track what we've successfully locked so far (for rollback)
        List<Map.Entry<String, Integer>> reservedItems = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            InventoryItem item = inventory.get(itemId);
            if (item == null) {
                // Item doesn't exist -> rollback everything
                rollback(reservedItems);
                return false;
            }

            boolean success = item.reserve(quantity);
            if (!success) {
                // Stock insufficient -> rollback everything
                rollback(reservedItems);
                return false;
            }

            // Successfully reserved, record for potential rollback
            reservedItems.add(entry);
        }

        // All items reserved successfully
        return true;
    }

    /**
     * Helper: rollback all reservations in the list.
     */
    private void rollback(List<Map.Entry<String, Integer>> reservedItems) {
        for (Map.Entry<String, Integer> entry : reservedItems) {
            InventoryItem item = inventory.get(entry.getKey());
            if (item != null) {
                item.releaseLock(entry.getValue());
            }
        }
    }
}
```

### Detailed Verbal Walkthrough (What to say while coding):

> "I'm starting with the most critical fix — making `InventoryItem` thread-safe. I'm adding a `ReentrantLock` to each `InventoryItem` instance. Every method that reads or modifies `lockedStock` or `totalStock` now acquires the lock at the start and releases it in a `finally` block. This ensures **mutual exclusion** — only one thread can execute `reserve`, `confirmPurchase`, or `releaseLock` for a given item at a time.
>
> In `reserve`, the check `getAvailableStock() >= quantity` and the update `lockedStock += quantity` are now atomic together because they're inside the same lock.
>
> In `releaseLock`, I added a validation: if `quantity > lockedStock`, I reject it instead of blindly subtracting. This prevents `lockedStock` from going negative.
>
> In `confirmPurchase`, both `lockedStock -= quantity` and `totalStock -= quantity` happen inside the lock, so they're atomic together.
>
> I switched `inventory` to `ConcurrentHashMap` for thread-safe lookups.
>
> For `loadInventory`, I added `clear()` first to prevent stale data.
>
> Now for the new feature `batchReserve`: I iterate through the requested items, try to reserve each one. If any reservation fails — either because the item doesn't exist or stock is insufficient — I call a `rollback` helper that releases everything I've already locked. This gives us **all-or-nothing** semantics. I'm using a `List` to track what was successfully reserved so far, and on failure I iterate that list and call `releaseLock` on each.
>
> One edge case: if `rollback` itself fails (e.g., another thread already released that lock), `releaseLock` will return `false` but won't throw — so the rollback is best-effort. In production, I'd add logging for rollback failures.
>
> Also worth noting: `batchReserve` has a potential **deadlock** risk if two threads call it with overlapping items in different order. To prevent this, I should **sort item IDs alphabetically** before acquiring locks. This is a standard deadlock prevention technique."

---

## Step 3: Testing + Analysis — Detailed English Answers

### Test Cases (Verbal with numbers)

> "Let me walk through four test cases.
>
> **Test Case 1 — Normal reservation.** Item A has `totalStock` = 10, `lockedStock` = 0. Call `reserve(3)`. Available goes from 10 to 7. Returns `true`. `lockedStock` = 3. This verifies basic happy-path functionality.
>
> **Test Case 2 — Overselling prevention.** Item B has `totalStock` = 1. Two threads simultaneously call `reserve(1)`. With the lock in place, Thread 1 acquires the lock, checks `getAvailableStock()` = 1 >= 1, sets `lockedStock` = 1, releases lock. Thread 2 then acquires the lock, checks `getAvailableStock()` = 0, returns `false`. No overselling. This verifies the concurrency fix.
>
> **Test Case 3 — Batch reservation all succeed.** Request `batchReserve` with {A: 2, B: 3}. Both items have sufficient stock. Both are locked. Returns `true`. This verifies the new feature happy path.
>
> **Test Case 4 — Batch reservation rollback.** Request `batchReserve` with {A: 2, B: 5}. A succeeds (locked 2), but B has insufficient stock. The method detects failure, calls `rollback`, releases A's 2 units. Returns `false`. A's `lockedStock` is back to original. This verifies all-or-nothing semantics.
>
> **Test Case 5 — Release guard.** Item C has `lockedStock` = 2. Call `releaseLock(5)`. Validation `quantity > lockedStock` triggers, returns `false`. `lockedStock` stays at 2. Prevents negative inventory.
>
> **Test Case 6 — Confirm purchase.** Item D has `totalStock` = 10, `lockedStock` = 3. Call `confirmPurchase(3)`. `lockedStock` becomes 0, `totalStock` becomes 7. Returns `true`. This verifies atomic confirmation."

### Complexity Analysis (Detailed)

> "Let me analyze the complexity of each method.
>
> **`reserve`**: Time complexity is **O(1)** — a simple subtraction and comparison inside a lock. Space complexity is **O(1)** — no extra allocations.
>
> **`batchReserve`**: Time complexity is **O(k)** where k is the number of distinct items in the request. Each `reserve` call is O(1), and in the worst case (failure), the rollback iterates over already-reserved items, also O(k). Space complexity is **O(k)** for the `reservedItems` tracking list.
>
> **`rollback`**: Time complexity is **O(r)** where r is the number of items to roll back. Each `releaseLock` is O(1).
>
> **`getLowStockItems`**: Time complexity is **O(n)** where n is the total number of items in cache — we iterate all values. Space complexity is **O(m)** where m is the number of items below threshold (added to result list).
>
> **`loadInventory`**: Time complexity is **O(n)** where n is the number of items loaded. Space complexity is **O(n)** for storing entries in the map."

### Distributed Scenario (Detailed)

> "Now, if this service runs on 3 servers behind a load balancer, does `ReentrantLock` still work?
>
> **No, it does not.** `ReentrantLock` guarantees mutual exclusion only within a single JVM. Each server has its own JVM, and each JVM has its own `InventoryItem` objects in memory. So if Server A and Server B both process a reservation for the same item, each acquires its own local lock, both pass the availability check, and we oversell — exactly the same problem as before but at a different level.
>
> To fix this in a distributed environment, I'd use one of these approaches:
>
> 1. **Redis with Lua scripts** — Lua scripts execute atomically in Redis. We can write a script that checks available stock and decrements in one atomic operation. Redis handles 100k+ ops/sec, perfect for flash sales.
>
> 2. **Database optimistic locking** — `UPDATE inventory SET locked_stock = locked_stock + ? WHERE item_id = ? AND total_stock - locked_stock >= ?`. This gives strong consistency and survives server crashes.
>
> 3. **Distributed lock** — Redisson `RLock` or ZooKeeper. More heavyweight but works for complex multi-step operations.
>
> 4. **Request queuing** — Put all reservation requests into a Kafka topic partitioned by `itemId`. Only one consumer per partition processes reservations for that item, eliminating contention entirely.
>
> My recommendation: use **Redis Lua scripts** for the hot path (reservation) because it's extremely fast and atomic. Use the **database as the source of truth** for reconciliation. `ConcurrentHashMap` is still fine for local caching of item metadata."

---

## Key English Expressions Cheat Sheet

| Situation | Detailed Phrases |
|---|---|
| Spotting overselling bug | "This is a classic **check-then-act race condition**. Two threads both pass the availability check before either updates, causing **overselling**." |
| Proposing ReentrantLock | "I'm adding a `ReentrantLock` because we need to protect **multiple fields** across **multiple operations**. `AtomicInteger` alone isn't enough — we need **mutual exclusion**." |
| Explaining all-or-nothing | "I implemented **all-or-nothing semantics** — if any reservation fails, I **roll back** all previously locked items using a tracking list." |
| Deadlock prevention | "To prevent **deadlock**, I should **sort item IDs alphabetically** before acquiring locks, ensuring all threads follow the same acquisition order." |
| Distributed limitation | "`ReentrantLock` is **JVM-local**. For multi-server deployment, I'd use **Redis Lua scripts** or **database optimistic locking** for atomic cross-server operations." |
| Timeout/cleanup | "Without a **timeout mechanism**, crashed reservations cause **permanent dead stock**. I'd add a **TTL** or a **background cleanup thread**." |
| Lower-bound guard | "I added a **validation check** to prevent `lockedStock` from going negative, which would cause **logical overselling**." |
| Trade-off discussion | "The trade-off is between **consistency and throughput**. Redis gives us **atomicity with low latency**, while database locking gives **strong consistency** at higher cost." |

---

## Bonus: Interviewer Follow-up Questions & Model Answers

**Q: "What if `batchReserve` succeeds but then the server crashes before the caller confirms?"**

> "That's why we need a **timeout mechanism**. Each reservation should record a timestamp. A background thread (or a lazy check on next access) releases reservations older than, say, 15 minutes. This is called **eventual release** — it's not instant but it prevents permanent dead stock. In production, I'd also persist reservations to a database so they survive crashes."

**Q: "How would you handle a flash sale with 10,000 requests per second?"**

> "At that scale, in-memory locks become a bottleneck even on a single server. I'd move to **Redis with Lua scripts** for atomic reservation — Redis handles 100k+ ops/sec single-threaded. I'd also add a **request queue** (Kafka) per item to serialize reservations, and use **read replicas** for `getLowStockItems`. The database would be updated asynchronously for durability."

**Q: "What about deadlock in `batchReserve`?"**

> "Great question. If two threads call `batchReserve` with overlapping items in different order — Thread 1 locks A then B, Thread 2 locks B then A — we get a **deadlock**. To prevent this, I should **sort item IDs alphabetically** before acquiring any locks, ensuring all threads acquire locks in the same order. This is a standard **deadlock prevention** technique. Alternatively, I could use `tryLock` with a timeout and retry on failure."

**Q: "What if `rollback` fails partway through?"**

> "In my current implementation, `rollback` is best-effort — if `releaseLock` returns `false` (e.g., already released by another thread), I just skip it. In production, I'd add **logging and alerting** for rollback failures. For stronger guarantees, I'd persist the rollback intent to a database or message queue so it can be retried by a background job."

---

*Generated for HCCC GCB5 karaat Prep | 2026-10-18*# karaat Mock Question 2: InventoryLockService (Flash-Sale Inventory Locking System)

> **Interview Round**: 30-minute Coding Round  
> **Level**: Mid-level (GCB5)  
> **Focus**: Concurrency safety, overselling prevention, transaction semantics, lock design, distributed inventory

---

## Background

You maintain an `InventoryLockService` for a flash-sale e-commerce platform. During high-traffic events, users complain that the same item gets oversold — inventory goes negative. Your task is to identify the bugs, fix them, and add a new feature.

**Requirements**:
- Load inventory items into cache
- Reserve items when user clicks "Buy Now"
- Confirm purchase after payment succeeds
- Release lock after payment failure or timeout
- Query items with stock below a threshold

---

## Original Code (Buggy Version)

```java
import java.util.*;
import java.util.concurrent.*;

class InventoryItem {
    private String itemId;
    private int totalStock;
    private int lockedStock;  // items reserved but not yet paid

    public InventoryItem(String itemId, int totalStock) {
        this.itemId = itemId;
        this.totalStock = totalStock;
        this.lockedStock = 0;
    }

    public String getItemId() { return itemId; }
    public int getTotalStock() { return totalStock; }
    public int getLockedStock() { return lockedStock; }
    public int getAvailableStock() { return totalStock - lockedStock; }

    public boolean reserve(int quantity) {
        if (getAvailableStock() >= quantity) {
            lockedStock += quantity;
            return true;
        }
        return false;
    }

    public void confirmPurchase(int quantity) {
        lockedStock -= quantity;
        totalStock -= quantity;
    }

    public void releaseLock(int quantity) {
        lockedStock -= quantity;
    }
}

public class InventoryLockService {
    private Map<String, InventoryItem> inventory;

    public InventoryLockService() {
        this.inventory = new HashMap<>();
    }

    public void loadInventory(List<InventoryItem> items) {
        for (InventoryItem item : items) {
            inventory.put(item.getItemId(), item);
        }
    }

    public boolean tryReserve(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item == null) return false;
        return item.reserve(quantity);
    }

    public void confirm(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item != null) {
            item.confirmPurchase(quantity);
        }
    }

    public void release(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item != null) {
            item.releaseLock(quantity);
        }
    }

    public List<String> getLowStockItems(int threshold) {
        List<String> result = new ArrayList<>();
        for (InventoryItem item : inventory.values()) {
            if (item.getAvailableStock() < threshold) {
                result.add(item.getItemId());
            }
        }
        return result;
    }
}
```

---

## Step 1: Bug Identification — Detailed English Answers

### Bug #1 & #2: `reserve()` is not atomic — Overselling (Most Critical)

**Detailed Verbal Answer:**

> "The most critical bug is in `reserve()`. It performs a **check-then-act** operation: first it calls `getAvailableStock() >= quantity`, then if true, it does `lockedStock += quantity`. These are two separate steps with no synchronization between them.
>
> Under high concurrency — which is exactly the scenario for a flash-sale system — here's what happens: Thread A checks `getAvailableStock()` and sees 1 item left. Before Thread A executes `lockedStock += quantity`, Thread B also checks `getAvailableStock()` and also sees 1 item left (because A hasn't updated `lockedStock` yet). Both threads pass the check and both execute `lockedStock += 1`. Now `lockedStock` increased by 2, but we only had 1 item in stock. This is the classic **race condition** that causes **overselling**.
>
> To fix this, I need to make the entire `reserve` method atomic. Since it involves checking one field and updating another, a simple `AtomicInteger` isn't enough — I need a `ReentrantLock` or `synchronized` block to protect the whole check-and-update sequence.
>
> Alternatively, if we move to a database, we can use `UPDATE inventory SET locked_stock = locked_stock + ? WHERE item_id = ? AND total_stock - locked_stock >= ?` — a single atomic SQL statement."

**Key vocabulary**: check-then-act, race condition, overselling, flash-sale, atomic operation, ReentrantLock, synchronized block, mutual exclusion

---

### Bug #3: `confirmPurchase` — Two writes are not atomic

**Detailed Verbal Answer:**

> "In `confirmPurchase`, we do `lockedStock -= quantity` then `totalStock -= quantity`. These are two separate writes. If a thread gets interrupted (or another thread calls `releaseLock`) between these two lines, `lockedStock` and `totalStock` become inconsistent. For example, `lockedStock` goes down but `totalStock` hasn't been updated yet — another thread calling `getAvailableStock()` would see a wrong, inflated available count.
>
> The fix is to wrap both operations in a `ReentrantLock` to make them atomic together."

**Key vocabulary**: inconsistent state, interrupted between operations, wrap in lock, atomic sequence

---

### Bug #4: `releaseLock` has no lower-bound check

**Detailed Verbal Answer:**

> "`releaseLock` simply does `lockedStock -= quantity` with no validation. If there's a bug in the calling code — say it calls `release` twice for the same order, or passes a `quantity` larger than what was locked — `lockedStock` becomes negative. Then `getAvailableStock()` returns `totalStock - (negative number)` = a value **larger** than `totalStock`, which is logically impossible and causes **overselling** again.
>
> Fix: add a validation check: if `quantity > lockedStock`, reject the operation instead of blindly subtracting. This prevents `lockedStock` from going negative."

**Key vocabulary**: lower-bound check, blindly subtracting, logically impossible, validation guard

---

### Bug #5: `HashMap` is not thread-safe

**Detailed Verbal Answer:**

> "`inventory` is a plain `HashMap`. `loadInventory` does `put` operations while `tryReserve` does `get`. Under concurrent access, `HashMap` can cause **data corruption** or **infinite loops** (JDK 7). Replace with `ConcurrentHashMap` for thread-safe lookups and updates."

**Key vocabulary**: data corruption, thread-safe, ConcurrentHashMap, concurrent reads

---

### Bug #6: No timeout/cleanup mechanism (Design Flaw)

**Detailed Verbal Answer:**

> "There's no timeout mechanism. If a user reserves an item but their payment fails, or the service crashes before `release` is called, that inventory stays locked forever. In a flash-sale, this means items become artificially unavailable even though no one actually bought them — **permanent dead stock**.
>
> I'd add a **timestamp** to each reservation and a background thread that periodically scans for reservations older than, say, 15 minutes and releases them automatically. Alternatively, on each `tryReserve` call, lazily clean up expired reservations for that specific item."

**Key vocabulary**: permanent dead stock, timeout mechanism, background cleanup thread, lazy cleanup, TTL (time-to-live)

---

### Bug #7: `loadInventory` accumulates without clearing

**Detailed Verbal Answer:**

> "Every time `loadInventory` is called, it just iterates and `put`s into the map without clearing existing entries. If it's called a second time (maybe a refresh job), old inventory items remain in the cache. This means stale stock data persists. Fix: call `inventory.clear()` at the start."

**Key vocabulary**: accumulates, stale data, full refresh

---

### Bug #8: `getLowStockItems` reads without lock (Stale Data)

**Detailed Verbal Answer:**

> "`getLowStockItems` reads `getAvailableStock()` without any lock. Under concurrent modifications, it may return stale or inconsistent data. For a monitoring endpoint, eventual consistency is usually acceptable, but I'd mention this trade-off — if the business requires real-time accuracy, we need a lock or a different approach."

**Key vocabulary**: stale data, eventual consistency, trade-off, real-time accuracy

---

## Step 2: Fix + New Feature — Detailed English Answers

### New Requirement

> Add `batchReserve(Map<String, Integer> itemQuantities)` — batch reserve multiple items:
> - Either ALL reservations succeed, or ALL are rolled back (**all-or-nothing**)
> - If ANY item has insufficient stock, already-locked items must be released
> - Return `true` if all succeeded, `false` if all rolled back

### Fully Fixed & Enhanced Code

```java
import java.util.*;
import java.util.concurrent.*;

/**
 * Thread-safe inventory item with ReentrantLock protection.
 */
class InventoryItem {
    private final String itemId;
    private int totalStock;
    private int lockedStock;
    private final ReentrantLock lock = new ReentrantLock();  // FIX: lock for atomicity

    public InventoryItem(String itemId, int totalStock) {
        this.itemId = itemId;
        this.totalStock = totalStock;
        this.lockedStock = 0;
    }

    public String getItemId() { return itemId; }
    public int getTotalStock() { return totalStock; }
    public int getLockedStock() { return lockedStock; }
    public int getAvailableStock() { return totalStock - lockedStock; }

    /**
     * Attempt to reserve quantity. Thread-safe.
     * FIX: entire method is atomic under lock.
     */
    public boolean reserve(int quantity) {
        lock.lock();
        try {
            if (quantity <= 0) return false;
            if (getAvailableStock() >= quantity) {
                lockedStock += quantity;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Confirm purchase: reduce both locked and total stock.
     * FIX: atomic under lock.
     */
    public boolean confirmPurchase(int quantity) {
        lock.lock();
        try {
            if (quantity <= 0 || quantity > lockedStock) return false;
            lockedStock -= quantity;
            totalStock -= quantity;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Release locked inventory.
     * FIX: added validation to prevent negative lockedStock.
     */
    public boolean releaseLock(int quantity) {
        lock.lock();
        try {
            if (quantity <= 0 || quantity > lockedStock) return false;
            lockedStock -= quantity;
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Peek available stock without modifying (for read-only queries).
     */
    public int peekAvailableStock() {
        lock.lock();
        try {
            return getAvailableStock();
        } finally {
            lock.unlock();
        }
    }
}

/**
 * Service for managing inventory locks during flash sales.
 */
public class InventoryLockService {

    private final Map<String, InventoryItem> inventory;  // FIX: ConcurrentHashMap

    public InventoryLockService() {
        this.inventory = new ConcurrentHashMap<>();
    }

    /**
     * Load inventory. FIX: clears old data first.
     */
    public void loadInventory(List<InventoryItem> items) {
        inventory.clear();  // FIX: prevent stale data accumulation
        for (InventoryItem item : items) {
            inventory.put(item.getItemId(), item);
        }
    }

    /**
     * Reserve items for a single product.
     */
    public boolean tryReserve(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item == null) return false;
        return item.reserve(quantity);
    }

    /**
     * Confirm purchase after payment.
     */
    public boolean confirm(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item == null) return false;
        return item.confirmPurchase(quantity);
    }

    /**
     * Release lock after payment failure or timeout.
     */
    public boolean release(String itemId, int quantity) {
        InventoryItem item = inventory.get(itemId);
        if (item == null) return false;
        return item.releaseLock(quantity);
    }

    /**
     * Get items with available stock below threshold.
     * Note: this is a snapshot, may be slightly stale under concurrency.
     */
    public List<String> getLowStockItems(int threshold) {
        List<String> result = new ArrayList<>();
        for (InventoryItem item : inventory.values()) {
            if (item.peekAvailableStock() < threshold) {
                result.add(item.getItemId());
            }
        }
        return result;
    }

    // ============ NEW FEATURE ============

    /**
     * Batch reserve multiple items. All-or-nothing semantics.
     * If ANY item fails, rollback ALL previously locked items.
     *
     * @param itemQuantities Map of itemId -> quantity to reserve
     * @return true if ALL reserved successfully, false if ALL rolled back
     */
    public boolean batchReserve(Map<String, Integer> itemQuantities) {
        if (itemQuantities == null || itemQuantities.isEmpty()) return false;

        // Track what we've successfully locked so far (for rollback)
        List<Map.Entry<String, Integer>> reservedItems = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : itemQuantities.entrySet()) {
            String itemId = entry.getKey();
            int quantity = entry.getValue();

            InventoryItem item = inventory.get(itemId);
            if (item == null) {
                // Item doesn't exist -> rollback everything
                rollback(reservedItems);
                return false;
            }

            boolean success = item.reserve(quantity);
            if (!success) {
                // Stock insufficient -> rollback everything
                rollback(reservedItems);
                return false;
            }

            // Successfully reserved, record for potential rollback
            reservedItems.add(entry);
        }

        // All items reserved successfully
        return true;
    }

    /**
     * Helper: rollback all reservations in the list.
     */
    private void rollback(List<Map.Entry<String, Integer>> reservedItems) {
        for (Map.Entry<String, Integer> entry : reservedItems) {
            InventoryItem item = inventory.get(entry.getKey());
            if (item != null) {
                item.releaseLock(entry.getValue());
            }
        }
    }
}
```

### Detailed Verbal Walkthrough (What to say while coding):

> "I'm starting with the most critical fix — making `InventoryItem` thread-safe. I'm adding a `ReentrantLock` to each `InventoryItem` instance. Every method that reads or modifies `lockedStock` or `totalStock` now acquires the lock at the start and releases it in a `finally` block. This ensures **mutual exclusion** — only one thread can execute `reserve`, `confirmPurchase`, or `releaseLock` for a given item at a time.
>
> In `reserve`, the check `getAvailableStock() >= quantity` and the update `lockedStock += quantity` are now atomic together because they're inside the same lock.
>
> In `releaseLock`, I added a validation: if `quantity > lockedStock`, I reject it instead of blindly subtracting. This prevents `lockedStock` from going negative.
>
> In `confirmPurchase`, both `lockedStock -= quantity` and `totalStock -= quantity` happen inside the lock, so they're atomic together.
>
> I switched `inventory` to `ConcurrentHashMap` for thread-safe lookups.
>
> For `loadInventory`, I added `clear()` first to prevent stale data.
>
> Now for the new feature `batchReserve`: I iterate through the requested items, try to reserve each one. If any reservation fails — either because the item doesn't exist or stock is insufficient — I call a `rollback` helper that releases everything I've already locked. This gives us **all-or-nothing** semantics. I'm using a `List` to track what was successfully reserved so far, and on failure I iterate that list and call `releaseLock` on each.
>
> One edge case: if `rollback` itself fails (e.g., another thread already released that lock), `releaseLock` will return `false` but won't throw — so the rollback is best-effort. In production, I'd add logging for rollback failures.
>
> Also worth noting: `batchReserve` has a potential **deadlock** risk if two threads call it with overlapping items in different order. To prevent this, I should **sort item IDs alphabetically** before acquiring locks. This is a standard deadlock prevention technique."

---

## Step 3: Testing + Analysis — Detailed English Answers

### Test Cases (Verbal with numbers)

> "Let me walk through four test cases.
>
> **Test Case 1 — Normal reservation.** Item A has `totalStock` = 10, `lockedStock` = 0. Call `reserve(3)`. Available goes from 10 to 7. Returns `true`. `lockedStock` = 3. This verifies basic happy-path functionality.
>
> **Test Case 2 — Overselling prevention.** Item B has `totalStock` = 1. Two threads simultaneously call `reserve(1)`. With the lock in place, Thread 1 acquires the lock, checks `getAvailableStock()` = 1 >= 1, sets `lockedStock` = 1, releases lock. Thread 2 then acquires the lock, checks `getAvailableStock()` = 0, returns `false`. No overselling. This verifies the concurrency fix.
>
> **Test Case 3 — Batch reservation all succeed.** Request `batchReserve` with {A: 2, B: 3}. Both items have sufficient stock. Both are locked. Returns `true`. This verifies the new feature happy path.
>
> **Test Case 4 — Batch reservation rollback.** Request `batchReserve` with {A: 2, B: 5}. A succeeds (locked 2), but B has insufficient stock. The method detects failure, calls `rollback`, releases A's 2 units. Returns `false`. A's `lockedStock` is back to original. This verifies all-or-nothing semantics.
>
> **Test Case 5 — Release guard.** Item C has `lockedStock` = 2. Call `releaseLock(5)`. Validation `quantity > lockedStock` triggers, returns `false`. `lockedStock` stays at 2. Prevents negative inventory.
>
> **Test Case 6 — Confirm purchase.** Item D has `totalStock` = 10, `lockedStock` = 3. Call `confirmPurchase(3)`. `lockedStock` becomes 0, `totalStock` becomes 7. Returns `true`. This verifies atomic confirmation."

### Complexity Analysis (Detailed)

> "Let me analyze the complexity of each method.
>
> **`reserve`**: Time complexity is **O(1)** — a simple subtraction and comparison inside a lock. Space complexity is **O(1)** — no extra allocations.
>
> **`batchReserve`**: Time complexity is **O(k)** where k is the number of distinct items in the request. Each `reserve` call is O(1), and in the worst case (failure), the rollback iterates over already-reserved items, also O(k). Space complexity is **O(k)** for the `reservedItems` tracking list.
>
> **`rollback`**: Time complexity is **O(r)** where r is the number of items to roll back. Each `releaseLock` is O(1).
>
> **`getLowStockItems`**: Time complexity is **O(n)** where n is the total number of items in cache — we iterate all values. Space complexity is **O(m)** where m is the number of items below threshold (added to result list).
>
> **`loadInventory`**: Time complexity is **O(n)** where n is the number of items loaded. Space complexity is **O(n)** for storing entries in the map."

### Distributed Scenario (Detailed)

> "Now, if this service runs on 3 servers behind a load balancer, does `ReentrantLock` still work?
>
> **No, it does not.** `ReentrantLock` guarantees mutual exclusion only within a single JVM. Each server has its own JVM, and each JVM has its own `InventoryItem` objects in memory. So if Server A and Server B both process a reservation for the same item, each acquires its own local lock, both pass the availability check, and we oversell — exactly the same problem as before but at a different level.
>
> To fix this in a distributed environment, I'd use one of these approaches:
>
> 1. **Redis with Lua scripts** — Lua scripts execute atomically in Redis. We can write a script that checks available stock and decrements in one atomic operation. Redis handles 100k+ ops/sec, perfect for flash sales.
>
> 2. **Database optimistic locking** — `UPDATE inventory SET locked_stock = locked_stock + ? WHERE item_id = ? AND total_stock - locked_stock >= ?`. This gives strong consistency and survives server crashes.
>
> 3. **Distributed lock** — Redisson `RLock` or ZooKeeper. More heavyweight but works for complex multi-step operations.
>
> 4. **Request queuing** — Put all reservation requests into a Kafka topic partitioned by `itemId`. Only one consumer per partition processes reservations for that item, eliminating contention entirely.
>
> My recommendation: use **Redis Lua scripts** for the hot path (reservation) because it's extremely fast and atomic. Use the **database as the source of truth** for reconciliation. `ConcurrentHashMap` is still fine for local caching of item metadata."

---

## Key English Expressions Cheat Sheet

| Situation | Detailed Phrases |
|---|---|
| Spotting overselling bug | "This is a classic **check-then-act race condition**. Two threads both pass the availability check before either updates, causing **overselling**." |
| Proposing ReentrantLock | "I'm adding a `ReentrantLock` because we need to protect **multiple fields** across **multiple operations**. `AtomicInteger` alone isn't enough — we need **mutual exclusion**." |
| Explaining all-or-nothing | "I implemented **all-or-nothing semantics** — if any reservation fails, I **roll back** all previously locked items using a tracking list." |
| Deadlock prevention | "To prevent **deadlock**, I should **sort item IDs alphabetically** before acquiring locks, ensuring all threads follow the same acquisition order." |
| Distributed limitation | "`ReentrantLock` is **JVM-local**. For multi-server deployment, I'd use **Redis Lua scripts** or **database optimistic locking** for atomic cross-server operations." |
| Timeout/cleanup | "Without a **timeout mechanism**, crashed reservations cause **permanent dead stock**. I'd add a **TTL** or a **background cleanup thread**." |
| Lower-bound guard | "I added a **validation check** to prevent `lockedStock` from going negative, which would cause **logical overselling**." |
| Trade-off discussion | "The trade-off is between **consistency and throughput**. Redis gives us **atomicity with low latency**, while database locking gives **strong consistency** at higher cost." |

---

## Bonus: Interviewer Follow-up Questions & Model Answers

**Q: "What if `batchReserve` succeeds but then the server crashes before the caller confirms?"**

> "That's why we need a **timeout mechanism**. Each reservation should record a timestamp. A background thread (or a lazy check on next access) releases reservations older than, say, 15 minutes. This is called **eventual release** — it's not instant but it prevents permanent dead stock. In production, I'd also persist reservations to a database so they survive crashes."

**Q: "How would you handle a flash sale with 10,000 requests per second?"**

> "At that scale, in-memory locks become a bottleneck even on a single server. I'd move to **Redis with Lua scripts** for atomic reservation — Redis handles 100k+ ops/sec single-threaded. I'd also add a **request queue** (Kafka) per item to serialize reservations, and use **read replicas** for `getLowStockItems`. The database would be updated asynchronously for durability."

**Q: "What about deadlock in `batchReserve`?"**

> "Great question. If two threads call `batchReserve` with overlapping items in different order — Thread 1 locks A then B, Thread 2 locks B then A — we get a **deadlock**. To prevent this, I should **sort item IDs alphabetically** before acquiring any locks, ensuring all threads acquire locks in the same order. This is a standard **deadlock prevention** technique. Alternatively, I could use `tryLock` with a timeout and retry on failure."

**Q: "What if `rollback` fails partway through?"**

> "In my current implementation, `rollback` is best-effort — if `releaseLock` returns `false` (e.g., already released by another thread), I just skip it. In production, I'd add **logging and alerting** for rollback failures. For stronger guarantees, I'd persist the rollback intent to a database or message queue so it can be retried by a background job."

---


# karaat Mock Question 3: NotificationDispatcher (Multi-Channel Notification System)

> **Interview Round**: 30-minute Coding Round  
> **Level**: Mid-level (GCB5)  
> **Focus**: Concurrency, idempotency, async processing, retry logic, duplicate prevention, throughput

---

## Background

You maintain a `NotificationDispatcher` for an e-commerce platform. It sends order updates to users via Email, SMS, and Push notifications. The system must handle high throughput and retry transient failures. Currently, users complain about **duplicate notifications** and **slow response times** from the API. Your task is to identify the bugs, fix them, and add a new feature.

**Requirements**:
- Send notifications via Email / SMS / Push
- Prevent duplicate sends for the same `notificationId`
- Retry failed sends (max 3 attempts)
- Support high-throughput dispatch
- Return quickly to the caller (non-blocking)

---

## Original Code (Buggy Version)

```java
import java.util.*;
import java.util.concurrent.*;

class Notification {
    private String notificationId;
    private String userId;
    private String channel; // "EMAIL", "SMS", "PUSH"
    private String message;
    private int retryCount;

    public Notification(String notificationId, String userId, String channel, String message) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.channel = channel;
        this.message = message;
        this.retryCount = 0;
    }

    public String getNotificationId() { return notificationId; }
    public String getUserId() { return userId; }
    public String getChannel() { return channel; }
    public String getMessage() { return message; }
    public int getRetryCount() { return retryCount; }
    public void incrementRetry() { retryCount++; }
}

class EmailSender {
    public boolean send(String userId, String message) {
        System.out.println("Sending email to " + userId + ": " + message);
        return true;
    }
}

class SmsSender {
    public boolean send(String userId, String message) {
        System.out.println("Sending SMS to " + userId + ": " + message);
        return true;
    }
}

class PushSender {
    public boolean send(String userId, String message) {
        System.out.println("Sending push to " + userId + ": " + message);
        return true;
    }
}

public class NotificationDispatcher {
    private Map<String, Notification> sentCache;
    private EmailSender emailSender;
    private SmsSender smsSender;
    private PushSender pushSender;

    public NotificationDispatcher() {
        this.sentCache = new HashMap<>();
        this.emailSender = new EmailSender();
        this.smsSender = new SmsSender();
        this.pushSender = new PushSender();
    }

    public boolean dispatch(Notification notification) {
        // Check if already sent
        if (sentCache.containsKey(notification.getNotificationId())) {
            return false;
        }

        boolean success = false;
        String channel = notification.getChannel();

        if (channel.equals("EMAIL")) {
            success = emailSender.send(notification.getUserId(), notification.getMessage());
        } else if (channel.equals("SMS")) {
            success = smsSender.send(notification.getUserId(), notification.getMessage());
        } else if (channel.equals("PUSH")) {
            success = pushSender.send(notification.getUserId(), notification.getMessage());
        }

        if (success) {
            sentCache.put(notification.getNotificationId(), notification);
            return true;
        } else {
            notification.incrementRetry();
            if (notification.getRetryCount() < 3) {
                dispatch(notification); // retry
            }
            return false;
        }
    }

    public List<String> getSentNotificationIds() {
        List<String> result = new ArrayList<>();
        for (String id : sentCache.keySet()) {
            result.add(id);
        }
        return result;
    }
}
```

---

## Step 1: Bug Identification — Detailed English Answers

### Bug #1: `sentCache` check-then-act is not atomic → Duplicate notifications (Most Critical)

**Detailed Verbal Answer:**

> "The most critical bug causing duplicate notifications is in the `dispatch` method. It does `sentCache.containsKey(notificationId)` followed by `sentCache.put(notificationId, notification)` on success. These are two separate operations with no synchronization.
>
> Under concurrency, two threads can simultaneously check `containsKey` for the same `notificationId` and both get `false`. Both threads then proceed to send the notification — one via Email, another might also send Email — causing **duplicate notifications**. This is a classic **TOCTOU (Time-Of-Check-To-Time-Of-Use)** race condition.
>
> The fix is to use `ConcurrentHashMap.putIfAbsent()`, which is atomic. If the key already exists, it returns the existing value instead of `null`, and we can short-circuit without sending. This guarantees **idempotency** at the cache level."

**Key vocabulary**: TOCTOU race condition, idempotency, check-then-act, putIfAbsent, atomic operation, duplicate prevention

---

### Bug #2: `HashMap` is not thread-safe

**Detailed Verbal Answer:**

> "`sentCache` is a plain `HashMap`. Multiple threads calling `dispatch` concurrently will do `containsKey`, `put`, and `getSentNotificationIds` does `keySet()` iteration — all unsafe. `HashMap` can corrupt its internal structure under concurrent modification, causing infinite loops (JDK 7) or `ConcurrentModificationException`.
>
> Fix: replace with `ConcurrentHashMap`. It supports concurrent reads and safe `putIfAbsent`."

**Key vocabulary**: ConcurrentModificationException, internal structure corruption, ConcurrentHashMap, thread-safe iteration

---

### Bug #3: Recursive retry without returning result

**Detailed Verbal Answer:**

> "On failure, the code calls `dispatch(notification)` recursively but **doesn't return its result**. So even if the retry succeeds inside the recursive call, the original caller still receives `false`. This is a **logic error** — the retry work is wasted from the API consumer's perspective.
>
> Also, recursion without a base case limit could cause `StackOverflowError` if retries keep failing and `retryCount` is somehow not incremented properly. The current code does increment, but the recursion depth is bounded by `retryCount < 3`, so max depth is 3 — acceptable but poor design."

**Key vocabulary**: logic error, wasted retry, StackOverflowError, recursion depth, base case

---

### Bug #4: No delay / backoff between retries

**Detailed Verbal Answer:**

> "When `dispatch` fails, it immediately calls itself recursively. This is a **tight retry loop** — if the downstream Email/SMS service is down or throttling, hammering it instantly makes things worse. It also blocks the calling thread, contributing to **slow response times**.
>
> Proper retry logic should use **exponential backoff with jitter** — wait a bit before retrying (e.g., 1s, 2s, 4s). In an async model, retries should be scheduled on a separate thread or a message queue."

**Key vocabulary**: tight retry loop, exponential backoff, jitter, throttling, downstream service

---

### Bug #5: Blocking I/O in dispatch method → Slow response times

**Detailed Verbal Answer:**

> "The `dispatch` method calls `emailSender.send()`, `smsSender.send()`, or `pushSender.send()` **synchronously**. These are network I/O calls that can take hundreds of milliseconds or even seconds. The calling thread is blocked until the send completes (or times out). Under high throughput, this exhausts the thread pool quickly, causing **slow response times** and **thread starvation**.
>
> Fix: make `dispatch` **asynchronous**. Use a `ThreadPoolExecutor` or `CompletableFuture` to offload the actual sending to a background thread. Return immediately to the caller with an `CompletableFuture<Boolean>` or a correlation ID."

**Key vocabulary**: blocking I/O, synchronous call, thread starvation, offload, CompletableFuture, non-blocking

---

### Bug #6: `sentCache` grows indefinitely → Memory leak

**Detailed Verbal Answer:**

> "`sentCache` never evicts entries. Over time, as millions of notifications are sent, the map grows unbounded, causing **memory leaks** and eventually `OutOfMemoryError`.
>
> Fix: use a `ConcurrentHashMap` with a **size limit** (e.g., via `LinkedHashMap` with LRU, or a proper caching library like Caffeine). Alternatively, add a `TTL` and periodically clean up old entries. For production, this cache should be in Redis or a database, not in-memory."

**Key vocabulary**: memory leak, OutOfMemoryError, unbounded growth, LRU eviction, TTL, Caffeine cache

---

### Bug #7: Unknown channel handling → Wasted retries

**Detailed Verbal Answer:**

> "If `channel` is not EMAIL/SMS/PUSH (e.g., typo like 'email' lowercase, or 'WEBHOOK'), `success` stays `false`. The code then retries up to 3 times — but it will **never succeed** because the channel is invalid. This wastes retry attempts and delays the failure response.
>
> Fix: validate the channel upfront. If unknown, fail fast without retrying."

**Key vocabulary**: fail fast, invalid channel, wasted retry attempts, upfront validation

---

### Bug #8: No `equals`/`hashCode` on `Notification`

**Detailed Verbal Answer:**

> "`Notification` is used as a value in `sentCache`, but it doesn't override `equals` and `hashCode`. If someone retrieves from cache and compares, reference equality is used. While not directly causing a bug here (we key by `notificationId`), it's a code smell and can cause subtle bugs if the cache is ever changed to use `Notification` as key."

**Key vocabulary**: reference equality, code smell, subtle bugs

---

## Step 2: Fix + New Feature — Detailed English Answers

### New Requirement

> Add `dispatchAsync(Notification notification)` — asynchronous dispatch with:
> - Returns `CompletableFuture<Boolean>` immediately
> - Uses a thread pool for actual sending
> - Retries with **exponential backoff** (max 3 attempts, delays: 1s, 2s, 4s)
> - Deduplication via `ConcurrentHashMap.putIfAbsent`
> - Proper shutdown hook via `shutdown()` method

### Fully Fixed & Enhanced Code

```java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a notification with thread-safe retry tracking.
 */
class Notification {
    private final String notificationId;
    private final String userId;
    private final String channel;
    private final String message;
    private final AtomicInteger retryCount;  // FIX: thread-safe retry counter

    public Notification(String notificationId, String userId, String channel, String message) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.channel = channel;
        this.message = message;
        this.retryCount = new AtomicInteger(0);
    }

    public String getNotificationId() { return notificationId; }
    public String getUserId() { return userId; }
    public String getChannel() { return channel; }
    public String getMessage() { return message; }
    public int getRetryCount() { return retryCount.get(); }
    public int incrementAndGetRetry() { return retryCount.incrementAndGet(); }  // FIX: atomic
    public void resetRetry() { retryCount.set(0); }
}

/**
 * Sender interfaces (simulated).
 */
class EmailSender {
    public boolean send(String userId, String message) {
        System.out.println("Sending email to " + userId + ": " + message);
        return true; // simulate success
    }
}

class SmsSender {
    public boolean send(String userId, String message) {
        System.out.println("Sending SMS to " + userId + ": " + message);
        return true;
    }
}

class PushSender {
    public boolean send(String userId, String message) {
        System.out.println("Sending push to " + userId + ": " + message);
        return true;
    }
}

/**
 * Thread-safe, async notification dispatcher with retry and deduplication.
 */
public class NotificationDispatcher {

    private final Map<String, Notification> sentCache;  // FIX: ConcurrentHashMap
    private final EmailSender emailSender;
    private final SmsSender smsSender;
    private final PushSender pushSender;
    private final ExecutorService executor;  // FIX: thread pool for async dispatch
    private static final int MAX_RETRIES = 3;
    private static final List<Long> BACKOFF_DELAYS_MS = Arrays.asList(1000L, 2000L, 4000L); // 1s, 2s, 4s

    public NotificationDispatcher() {
        this.sentCache = new ConcurrentHashMap<>();  // FIX: thread-safe
        this.emailSender = new EmailSender();
        this.smsSender = new SmsSender();
        this.pushSender = new PushSender();
        this.executor = Executors.newFixedThreadPool(10);  // FIX: dedicated pool
    }

    /**
     * Synchronous dispatch (legacy, kept for backward compatibility).
     * FIX: now uses putIfAbsent for deduplication, validates channel, no recursion.
     */
    public boolean dispatch(Notification notification) {
        // FIX: atomic deduplication check
        Notification existing = sentCache.putIfAbsent(notification.getNotificationId(), notification);
        if (existing != null) {
            return false; // already sent
        }

        String channel = notification.getChannel();
        if (!isValidChannel(channel)) {
            sentCache.remove(notification.getNotificationId()); // clean up
            return false; // invalid channel, fail fast
        }

        boolean success = sendViaChannel(notification, channel);
        if (success) {
            return true;
        } else {
            sentCache.remove(notification.getNotificationId()); // allow retry
            notification.incrementAndGetRetry();
            return false;
        }
    }

    /**
     * NEW FEATURE: Asynchronous dispatch with exponential backoff retry.
     */
    public CompletableFuture<Boolean> dispatchAsync(Notification notification) {
        // FIX: atomic deduplication
        Notification existing = sentCache.putIfAbsent(notification.getNotificationId(), notification);
        if (existing != null) {
            return CompletableFuture.completedFuture(false);
        }

        String channel = notification.getChannel();
        if (!isValidChannel(channel)) {
            sentCache.remove(notification.getNotificationId());
            return CompletableFuture.completedFuture(false);
        }

        // Submit to thread pool
        return CompletableFuture.supplyAsync(() -> {
            return sendWithRetry(notification, channel);
        }, executor);
    }

    /**
     * Send with exponential backoff retry.
     */
    private boolean sendWithRetry(Notification notification, String channel) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            boolean success = sendViaChannel(notification, channel);
            if (success) {
                return true;
            }
            attempt++;
            notification.incrementAndGetRetry();
            if (attempt < MAX_RETRIES) {
                try {
                    // Exponential backoff
                    Thread.sleep(BACKOFF_DELAYS_MS.get(attempt - 1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        // All retries exhausted
        sentCache.remove(notification.getNotificationId()); // allow future retry
        return false;
    }

    private boolean sendViaChannel(Notification notification, String channel) {
        switch (channel) {
            case "EMAIL":
                return emailSender.send(notification.getUserId(), notification.getMessage());
            case "SMS":
                return smsSender.send(notification.getUserId(), notification.getMessage());
            case "PUSH":
                return pushSender.send(notification.getUserId(), notification.getMessage());
            default:
                return false;
        }
    }

    private boolean isValidChannel(String channel) {
        return "EMAIL".equals(channel) || "SMS".equals(channel) || "PUSH".equals(channel);
    }

    public List<String> getSentNotificationIds() {
        return new ArrayList<>(sentCache.keySet()); // FIX: safe copy
    }

    /**
     * NEW: Graceful shutdown.
     */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

### Detailed Verbal Walkthrough (What to say while coding):

> "I'm starting with the most visible issue — duplicate notifications. The original code uses `containsKey` then `put`, which is not atomic. I'm replacing it with `ConcurrentHashMap.putIfAbsent()`. This is a single atomic operation: if the key already exists, it returns the existing `Notification` object; if not, it puts the new one and returns `null`. So I check `if (existing != null)` → return `false` immediately. This guarantees **idempotency**.
>
> Next, I'm changing `sentCache` from `HashMap` to `ConcurrentHashMap` for thread-safe operations.
>
> For the retry logic, the original uses recursion which is problematic. I'm replacing it with a `while` loop in `sendWithRetry` that does up to `MAX_RETRIES` attempts. Between failures, I add `Thread.sleep` with **exponential backoff** delays (1s, 2s, 4s). This prevents hammering the downstream service.
>
> I'm adding a `dispatchAsync` method that returns `CompletableFuture<Boolean>`. It does the deduplication check, validates the channel, then submits the actual send work to a `ThreadPoolExecutor` via `CompletableFuture.supplyAsync`. This makes the API **non-blocking** — the caller gets a future immediately and can check the result later or attach a callback.
>
> I changed `retryCount` from `int` to `AtomicInteger` for thread-safe increments, though in the async model each notification is processed by one thread at a time, so it's less critical — but still good practice.
>
> I added `isValidChannel` to fail fast on unknown channels, preventing wasted retries.
>
> I added a `shutdown` method to gracefully terminate the thread pool — important for production services.
>
> Finally, `getSentNotificationIds` now returns a new `ArrayList` copy of the key set to avoid `ConcurrentModificationException` if the caller iterates while the cache is modified."

---

## Step 3: Testing + Analysis — Detailed English Answers

### Test Cases (Verbal with numbers)

> "Let me walk through five test cases.
>
> **Test Case 1 — Normal dispatch (sync).** Create notification ID 'N001', channel 'EMAIL'. Call `dispatch`. `putIfAbsent` returns `null`, send succeeds, returns `true`. `sentCache` contains 'N001'. This verifies happy path.
>
> **Test Case 2 — Duplicate prevention.** Call `dispatch` with 'N001' again. `putIfAbsent` returns the existing notification (not null), method returns `false` immediately without calling `emailSender.send`. This verifies **idempotency**.
>
> **Test Case 3 — Async dispatch with success.** Call `dispatchAsync` with ID 'N002', channel 'SMS'. Returns a `CompletableFuture`. In background, SMS send succeeds, future completes with `true`. `sentCache` has 'N002'. This verifies async non-blocking behavior.
>
> **Test Case 4 — Retry with backoff.** Mock `PushSender.send` to fail twice then succeed on third attempt. Call `dispatchAsync` with ID 'N003', channel 'PUSH'. The `sendWithRetry` loop fails first attempt, sleeps 1s, fails second, sleeps 2s, succeeds third. Future completes with `true`. Total time ≈ 3 seconds. This verifies exponential backoff.
>
> **Test Case 5 — Invalid channel fail-fast.** Call `dispatch` with channel 'WEBHOOK'. `isValidChannel` returns `false`, `sentCache` entry removed, returns `false` immediately. No retry attempted. This verifies fail-fast.
>
> **Test Case 6 — All retries exhausted.** Mock `EmailSender.send` to always fail. Call `dispatchAsync` with ID 'N004'. After 3 attempts (0, 1, 2) with backoff sleeps, future completes with `false`. `sentCache` removes 'N004'. This verifies retry exhaustion."

### Complexity Analysis (Detailed)

> "Let me analyze the complexity.
>
> **`dispatch` (sync)**: Time complexity is **O(1)** average — `putIfAbsent` is O(1) for `ConcurrentHashMap`, and `sendViaChannel` is O(1) (network call excluded from algorithmic complexity). Space complexity is **O(n)** where n is total unique notifications stored in `sentCache`.
>
> **`dispatchAsync`**: Time complexity is **O(1)** for the initial check and submission. The actual send happens asynchronously; the retry loop in `sendWithRetry` is **O(R)** where R = `MAX_RETRIES` (constant, so O(1)). Space complexity is **O(1)** for the future object, plus **O(n)** for cache.
>
> **`sendWithRetry`**: Time complexity is **O(R)** where R is max retries (constant 3). With backoff sleeps, wall-clock time is O(2^R) but computational complexity is O(1). Space complexity is **O(1)**.
>
> **`getSentNotificationIds`**: Time complexity is **O(n)** to copy keys into a new list. Space complexity is **O(n)** for the returned list.
>
> **Overall cache**: Space grows with number of notifications. In production, should add TTL/LRU to bound it."

### Distributed Scenario (Detailed)

> "Now, if this service runs on 3 servers behind a load balancer, does `ConcurrentHashMap.putIfAbsent` still prevent duplicates?
>
> **No, it does not.** `ConcurrentHashMap` is JVM-local. If Server A and Server B both receive a dispatch request for the same `notificationId` at the same time, each has its own `sentCache`. Both do `putIfAbsent`, both get `null`, both send — duplicates again.
>
> To fix this in a distributed environment, I'd use one of these approaches:
>
> 1. **Redis `SETNX` with TTL** — `SET notification:sent:{id} 1 NX EX 86400`. Atomic, distributed, auto-expires. This is the most common solution for idempotency keys.
>
> 2. **Database unique constraint** — Insert into a `sent_notifications` table with `notification_id` as primary key. If duplicate, insert fails with constraint violation. Strongest consistency but slower.
>
> 3. **Distributed lock** — Acquire a lock on `notificationId` via Redis `SETNX` or Redisson `RLock` before checking/sending. Heavier but works for complex multi-step flows.
>
> 4. **Idempotency header** — Require clients to send an `Idempotency-Key` header. Store it in Redis. If the same key arrives, return the cached response without reprocessing.
>
> My recommendation: use **Redis `SETNX`** for deduplication. It's atomic, fast, and supports TTL so the cache doesn't grow forever. For the async processing, use a **message queue** (Kafka/RabbitMQ) so that even if one server dies, the notification is still processed by another consumer."

---

## Key English Expressions Cheat Sheet

| Situation | Detailed Phrases |
|---|---|
| Spotting duplicate bug | "This is a **TOCTOU race condition** — `containsKey` and `put` are separate operations. Two threads can both pass the check and both send, causing **duplicate notifications**." |
| Proposing putIfAbsent | "I'll use `ConcurrentHashMap.putIfAbsent()` — it's an **atomic check-and-put** operation. If the key exists, it returns the existing value; otherwise it inserts and returns `null`. This guarantees **idempotency**." |
| Blocking I/O problem | "Synchronous network calls **block the calling thread**, causing **thread starvation** under high throughput. We need to **offload** to a thread pool." |
| Async solution | "I'm adding `dispatchAsync` returning `CompletableFuture<Boolean>`. It submits work to an `ExecutorService` and returns immediately — **non-blocking**." |
| Retry improvement | "Instead of **tight recursive retry**, I'm implementing **exponential backoff with jitter** — wait 1s, 2s, 4s between attempts to avoid **hammering** the downstream service." |
| Fail-fast | "I added **upfront validation** for the channel. If it's invalid, we **fail fast** without wasting retry attempts." |
| Memory leak | "`sentCache` grows **unbounded**. I'd add a **TTL** or use a **caching library** like Caffeine with **LRU eviction**." |
| Distributed fix | "`ConcurrentHashMap` is **JVM-local**. For multi-server, I'd use **Redis `SETNX`** with TTL for distributed **idempotency**." |
| Graceful shutdown | "I added a `shutdown` method that calls `executor.shutdown()` and waits for **graceful termination** with a timeout." |

---

## Bonus: Interviewer Follow-up Questions & Model Answers

**Q: "What if the Email service succeeds but the thread crashes before `putIfAbsent`?"**

> "That's a great point. If the send succeeds but we crash before marking it in cache, on retry we'd send again — a **duplicate**. To make this truly exactly-once, we need **transactional outbox** or **idempotency on the receiver side**. Most email/SMS providers support idempotency keys — we can pass `notificationId` as their idempotency key. Then even if we send twice, they deduplicate. Alternatively, we persist the sent status to a database **before** sending, but that adds latency. The pragmatic approach: rely on receiver-side idempotency for at-least-once delivery."

**Q: "How would you handle 1 million notifications per minute?"**

> "At that scale, in-memory `ConcurrentHashMap` won't scale — memory and CPU contention. I'd move to a **distributed message queue** (Kafka) with multiple consumer groups. The API just publishes to Kafka (O(1), very fast). Consumers process sends asynchronously with their own thread pools. Deduplication is done via Redis `SETNX` with TTL. For retries, use a **dead-letter queue** (DLQ) — after max retries, move to DLQ for manual inspection. This decouples ingestion from processing and scales horizontally."

**Q: "What about notification ordering? If a user gets 'Order Shipped' before 'Order Confirmed'?"**

> "Good question. If ordering matters, we need to **partition by userId** so all notifications for a user go to the same Kafka partition and are processed sequentially. This guarantees **per-user ordering**. Alternatively, add a `sequenceNumber` and have the client reorder, but that's complex. Partitioning by `userId` hash is the standard solution."

**Q: "How do you monitor this system?"**

> "I'd add metrics: `notification_sent_total` (counter, tagged by channel), `notification_failed_total`, `notification_retried_total`, `dispatch_latency_ms` (histogram), and `duplicate_prevented_total`. Use a dashboard (Grafana) with alerts on failure rate spikes. Also log every send attempt with correlation ID for tracing."

---

