# HSBC Karat Mock Interview — Question 2 (Membership Rewards System)

## Part 1 — Interview Questions (all rounds)

**Round 1 — Debugging**
"Here is a `Membership` class for a banking rewards system. It tracks member points and total membership count. The code has several critical bugs and thread-safety issues. Please review it, identify the issues, fix them, and explain your reasoning out loud."

```java
import java.util.*;

public class Membership {
    private Map<String, Integer> points = new HashMap<>();
    private int totalMembers = 0;

    public void addMember(String memberId) {
        if (memberId != null) {
            points.put(memberId, 0);
            totalMembers++;
        }
    }

    public void addPoints(String memberId, int pointsToAdd) {
        if (points.containsKey(memberId)) {
            int current = points.get(memberId);
            points.put(memberId, current + pointsToAdd);
        }
    }

    public int getPoints(String memberId) {
        return points.get(memberId);
    }

    public int getTotalMembers() {
        return totalMembers;
    }
}
```

**Round 2 — Extension: transferPoints**
"Add `transferPoints(String fromId, String toId, int points)`. Members can transfer points to each other with the same thread-safety guarantees. Reject the transfer if the sender's balance would drop below zero."

**Round 3 — Candidate's catch (merge semantics)**
"`ConcurrentHashMap.merge()` silently inserts a new entry when the key does not exist. So calling `addPoints` with a non-existent member would create that member — which violates the business rule. How do you fix it, and how do you say this in English?"

**Round 4 — Extension: tiered membership**
"HSBC wants a tiered system. Members start at **Silver**. At **1,000** points they are promoted to **Gold**, at **5,000** to **Platinum**.
1. Add `getTier(String memberId)` returning `"Silver" | "Gold" | "Platinum"`.
2. Promotion must happen automatically inside `addPoints` — no separate method.
3. Thread-safety must be maintained.
4. Add `getMembersByTier(String tier)` returning `List<String>` of member IDs."

**Round 5 — Legacy constraint follow-up**
"What if we MUST keep points and levels in **two separate maps** due to legacy system constraints? How would you ensure the tier update is consistent with the points update **without creating deadlocks**?"

**Round 6 — Final re-stated requirement: `transferPoints`**
Implement `transferPoints(String fromId, String toId, int points)` under all constraints:
1. Deduct from `fromId`, credit to `toId`.
2. Return `false` on insufficient funds, non-existent users, `fromId.equals(toId)`, or `points <= 0`.
3. Recalculate tiers for **both** users after a successful transfer.
4. Thread-safe, no lost updates, no negative balances, **deadlock-free**.
5. Points and tiers stay in **two separate maps**; use per-user `ReentrantLock` with consistent lock ordering.

---

## Part 2 — Final Code Implementation

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MembershipLegacy {

    // Legacy constraint: points and levels MUST stay in two separate maps
    private final Map<String, Integer> points    = new ConcurrentHashMap<>();
    private final Map<String, String> levels    = new ConcurrentHashMap<>();
    private final Map<String, Lock>  userLocks = new ConcurrentHashMap<>();
    private final AtomicInteger totalMembers   = new AtomicInteger(0);

    // ---------- Round 1 fixes ----------

    public void addMember(String memberId) {
        if (memberId == null || memberId.isEmpty()) return;

        Lock lock = lockFor(memberId);
        lock.lock();
        try {
            // putIfAbsent is atomic: avoids the containsKey -> put race
            Integer prev = points.putIfAbsent(memberId, 0);
            if (prev == null) {
                levels.put(memberId, "Silver");
                totalMembers.incrementAndGet();   // count ONLY genuinely new members
            }
        } finally {
            lock.unlock();
        }
    }

    public void addPoints(String memberId, int pointsToAdd) {
        if (memberId == null || pointsToAdd <= 0) return;

        Lock lock = lockFor(memberId);
        lock.lock();
        try {
            // computeIfPresent: does NOTHING if the member is absent.
            // (merge() would silently INSERT the key -> wrong business semantics)
            // Both maps are updated inside the SAME locked section -> consistent.
            points.computeIfPresent(memberId, (id, oldVal) -> {
                long sum = (long) oldVal + pointsToAdd;          // overflow guard
                int newPoints = (int) Math.min(sum, Integer.MAX_VALUE);
                levels.put(id, computeTier(newPoints));          // auto-promotion
                return newPoints;
            });
        } finally {
            lock.unlock();
        }
    }

    public int getPoints(String memberId) {
        if (memberId == null) return 0;
        Integer balance = points.get(memberId);   // local var avoids double-get / unboxing NPE
        return balance != null ? balance : 0;
    }

    public int getTotalMembers() {
        return totalMembers.get();
    }

    // ---------- Round 4: tier queries ----------

    public String getTier(String memberId) {
        if (memberId == null) return "Silver";
        return levels.getOrDefault(memberId, "Silver");
    }

    public List<String> getMembersByTier(String tier) {
        if (tier == null || tier.isEmpty()) return new ArrayList<>();
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String> e : levels.entrySet()) {
            if (tier.equals(e.getValue())) result.add(e.getKey());
        }
        return result;
    }

    // ---------- Round 6: transferPoints ----------

    public boolean transferPoints(String fromId, String toId, int pointsToTransfer) {
        // 1. Input validation
        if (fromId == null || toId == null || fromId.equals(toId) || pointsToTransfer <= 0) {
            return false;
        }

        // 2. Consistent lock ordering by user ID -> prevents circular-wait deadlock
        String firstId  = fromId.compareTo(toId) < 0 ? fromId : toId;
        String secondId = fromId.compareTo(toId) < 0 ? toId   : fromId;
        Lock firstLock  = lockFor(firstId);
        Lock secondLock = lockFor(secondId);

        firstLock.lock();
        secondLock.lock();
        try {
            // 3. Existence + balance checks (now atomic w.r.t. other writers)
            if (!points.containsKey(fromId) || !points.containsKey(toId)) return false;

            int fromBalance = points.get(fromId);
            if (fromBalance < pointsToTransfer) return false;   // insufficient funds

            // 4. Deduct / credit with overflow protection
            int newFromBalance = fromBalance - pointsToTransfer;
            int toBalance      = points.get(toId);
            long credited      = (long) toBalance + pointsToTransfer;
            int newToBalance   = (int) Math.min(credited, Integer.MAX_VALUE);

            points.put(fromId, newFromBalance);
            points.put(toId,   newToBalance);

            // 5. BOTH tiers recalculated inside the same locked section
            levels.put(fromId, computeTier(newFromBalance));
            levels.put(toId,   computeTier(newToBalance));

            return true;
        } finally {
            secondLock.unlock();   // unlock in reverse order
            firstLock.unlock();
        }
    }

    // ---------- helpers ----------

    private Lock lockFor(String memberId) {
        return userLocks.computeIfAbsent(memberId, k -> new ReentrantLock());
    }

    private String computeTier(int pts) {
        if (pts >= 5000) return "Platinum";
        if (pts >= 1000) return "Gold";
        return "Silver";
    }
}
```

**Bug / decision checklist covered by the code**

| # | Original issue | Fix |
|---|---|---|
| 1 | `HashMap` not thread-safe | `ConcurrentHashMap` + per-user `ReentrantLock` |
| 2 | `totalMembers++` not atomic | `AtomicInteger`, only on `putIfAbsent` success |
| 3 | `get + put` race in `addPoints` | `computeIfPresent` inside lock |
| 4 | `getPoints` unboxing `null` -> NPE | local `Integer`, return `0` if absent |
| 5 | `merge` inserts unknown members | switched to `computeIfPresent` |
| 6 | missing `pointsToAdd <= 0` / empty-id checks | explicit guards |
| 7 | integer overflow | `(long)` + `Math.min(..., MAX_VALUE)` |
| 8 | points/level updated separately | same locked section => consistent |
| 9 | two-user transfer deadlock | lock ordering by `compareTo`, reverse unlock |

---

## Part 3 — Oral English Answer (Interview-Ready)

**Main walkthrough**

"First, on the original class: I'd replace `HashMap` with `ConcurrentHashMap`, because the rewards service handles concurrent requests and a plain `HashMap` can corrupt its internal structure under simultaneous writes. The `totalMembers++` is a classic read-modify-write race, so I use an `AtomicInteger` and only increment it when `putIfAbsent` actually creates a new member — that also makes `addMember` idempotent. `getPoints` used to return `points.get(memberId)` and auto-unbox a `null` into an `int`, which throws a `NullPointerException` for an unknown member; I store the result in a local `Integer` and return zero instead.

One important detail I want to call out: my first instinct was to use `merge()` in `addPoints`, but `merge` **silently inserts** a new entry when the key is absent. That would mean calling `addPoints` with a non-existent member ID creates that member with free points, which violates our business rule. So I switched to `computeIfPresent` — it only runs the lambda when the member already exists and does nothing otherwise.

For the tier extension, the cleanest design is to wrap points and tier into one immutable object and update them with a single atomic `compute`. However, with the legacy constraint that points and levels must stay in **two separate maps**, I can't get that atomicity from one `ConcurrentHashMap` call. My solution is a **per-user lock**: a `ConcurrentHashMap` of `ReentrantLock` objects keyed by member ID. Inside `addPoints` I acquire exactly one lock, update the points map and then the levels map in the same critical section. From any reader's perspective the two maps move together. Deadlock is impossible here because a single-user operation only ever acquires **one** lock — there is no circular wait.

For `transferPoints`, we now touch two users, so I must prevent deadlock. I sort the two IDs and always acquire the lock for the 'smaller' ID first, then the larger one, and release them in reverse order. That gives a consistent global lock order and eliminates circular wait even if A transfers to B while B simultaneously transfers to A. Inside the locked block I validate both users exist, check the sender has enough points, deduct, credit with an overflow guard, and recalculate **both** users' tiers before returning `true`. Any failed check returns `false` with no partial mutation.

The trade-off is that the `userLocks` map grows forever unless we evict idle locks, and a hot account serializes all its updates — but for a Karat-level answer this is correct, deadlock-free, and keeps the legacy schema intact. In real HSBC production I'd push this down to a database transaction with `SELECT ... FOR UPDATE` and let the DB handle row-level locking and deadlock detection, instead of managing an application-level lock map."

**Short follow-up answers**

- *"Why not `synchronized` on the whole method?"*
  "It would be correct but it serializes every member globally, killing throughput. Per-user locking lets different members be updated concurrently while still protecting each member's two-map consistency."

- *"Why `replace`/CAS loop instead of `computeIfPresent` for deduction earlier?"*
  "`computeIfPresent` has no clean way to signal 'operation rejected, roll back'. A CAS loop with `replace` lets me retry on contention and simply return `false` when the balance is insufficient, without having mutated anything."

- *"What about readers seeing points = 1000 but tier = Silver?"*
  "With two maps I can't give a lock-free atomic snapshot. If strict read consistency is required, I'd route `getPoints` and `getTier` through the same per-user lock, or — better — migrate to the single immutable `MemberData` wrapper so one atomic reference swap publishes both fields together."