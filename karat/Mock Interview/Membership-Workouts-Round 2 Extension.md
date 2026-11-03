# HSBC Karat Mock Interview — Question 3 (Membership / Workouts), Round 2 Extension

## Part 1 — Interview Question

**Round 1 (already completed):** Fix the buggy `Membership` class (`add_workout`, `get_average_workout_durations`, `get_due_payments`). Key fixes: check `registeredUsers` instead of `workouts`, cast before integer division, thread-safe set, defensive copy, `BigDecimal` for money.

**Round 2 — Extension requirement (verbatim from interviewer):**

> "HSBC wants two new features on top of your fixed class:
>
> **1.** `get_average_workout_durations_by_type(String workoutType)` → `Map<String, Double>`
> Returns, per user, the average duration **only for workouts of that type**.
>
> ⚠️ Catch: in the original `add_workout`, the `workoutType` parameter was **accepted but never stored**. So you'll need to change your internal data structure first — you can't answer this query with `List<Integer>` anymore. Think about what to store instead, and keep it thread-safe.
>
> **2.** `pay_due(String userId, BigDecimal amount)` → `boolean`
> Reduce the user's overdue balance by `amount`. Rules:
> - Return `false` for unknown user, `null`/non-positive amount, or amount greater than the balance (no partial mutation)
> - Must be idempotent-friendly and thread-safe (two concurrent payments must not both pass the balance check)
> - Return `true` on success
>
> Constraints unchanged: `BigDecimal` for money, thread-safe collections, per-user locking if cross-map atomicity is needed, and explain your reasoning out loud in English."

---

## Part 2 — Final Code Implementation

```java
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Membership {

    // ---- Round 1 fixes retained ----
    private final Set<String> registeredUsers = ConcurrentHashMap.newKeySet();

    // CHANGED: List<Integer> -> List<Workout>.
    // The old structure discarded workoutType, so a by-type query was impossible.
    private final Map<String, List<Workout>> workouts = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal>   duePayments = new ConcurrentHashMap<>();

    // One lock per user: makes the balance check + deduct atomic.
    // Also leaves room to append an audit entry in the same critical section later.
    private final Map<String, Lock> userLocks = new ConcurrentHashMap<>();

    /** Immutable value object — safe to publish via ConcurrentHashMap. */
    private static final class Workout {
        final String type;
        final int duration;
        Workout(String type, int duration) {
            this.type = type;
            this.duration = duration;
        }
    }

    public Membership() {
        registeredUsers.add("user1");
        registeredUsers.add("user2");
        duePayments.put("user1", new BigDecimal("30.00").setScale(2, RoundingMode.HALF_UP));
        duePayments.put("user2", new BigDecimal("0.00").setScale(2, RoundingMode.HALF_UP));
    }

    // ---------- Round 1 (corrected) ----------

    /** Ignore the workout if the user does not exist. */
    public void add_workout(String userId, String workoutType, int duration) {
        if (userId == null || !registeredUsers.contains(userId) || duration <= 0) {
            return;
        }
        // Copy-on-write + immutable snapshot: atomic, no in-place mutation of a published list.
        workouts.compute(userId, (k, oldValue) -> {
            List<Workout> next = (oldValue == null)
                    ? new ArrayList<>()
                    : new ArrayList<>(oldValue);
            next.add(new Workout(workoutType, duration));
            return Collections.unmodifiableList(next);
        });
    }

    /** userId -> average duration across ALL workout types. */
    public Map<String, Double> get_average_workout_durations() {
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, List<Workout>> e : workouts.entrySet()) {
            List<Workout> list = e.getValue();
            if (list == null || list.isEmpty()) continue;
            long sum = 0L;
            for (Workout w : list) sum += w.duration;
            result.put(e.getKey(), sum / (double) list.size());   // cast FIRST, not after
        }
        return result;
    }

    public Map<String, BigDecimal> get_due_payments() {
        return new HashMap<>(duePayments);   // defensive copy, never leak internals
    }

    // ---------- Round 2: extension ----------

    /** userId -> average duration for ONE workout type only. */
    public Map<String, Double> get_average_workout_durations_by_type(String workoutType) {
        Map<String, Double> result = new HashMap<>();
        if (workoutType == null || workoutType.isEmpty()) return result;

        for (Map.Entry<String, List<Workout>> e : workouts.entrySet()) {
            long sum = 0L;
            int count = 0;
            for (Workout w : e.getValue()) {
                if (workoutType.equals(w.type)) {   // equals on the constant, NPE-safe
                    sum += w.duration;
                    count++;
                }
            }
            // Users with zero matching workouts are simply omitted from the result.
            if (count > 0) {
                result.put(e.getKey(), sum / (double) count);
            }
        }
        return result;
    }

    /** Reduce overdue balance. Returns false on any invalid case, with no partial mutation. */
    public boolean pay_due(String userId, BigDecimal amount) {
        if (userId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (!registeredUsers.contains(userId)) {
            return false;
        }

        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        Lock lock = userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
        lock.lock();
        try {
            BigDecimal balance = duePayments.get(userId);
            if (balance == null) return false;

            // compareTo, NOT equals — equals also compares scale (30.0 != 30.00)
            if (normalized.compareTo(balance) > 0) return false;

            BigDecimal remaining = balance.subtract(normalized)
                                          .setScale(2, RoundingMode.HALF_UP);
            duePayments.put(userId, remaining);

            // Idempotent-friendly: a repeated identical call on a zero balance
            // simply returns false instead of double-charging. In production I'd
            // also gate on a paymentId / idempotency key here.
            return true;
        } finally {
            lock.unlock();
        }
    }

    private Lock lockFor(String userId) {
        return userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
    }
}
```

**Design decision checklist**

| # | Decision | Why |
|---|---|---|
| 1 | `List<Integer>` → `List<Workout>` | Original code accepted `workoutType` but threw it away; a by-type query is impossible without storing it |
| 2 | Immutable `Workout` + `unmodifiableList` copy-on-write | `ConcurrentHashMap.compute` swaps one atomic reference; readers never see a half-built list |
| 3 | `sum / (double) count` | `int / int` truncates first — `61/2` would give `30.0` instead of `30.5` |
| 4 | `long sum` | Avoids `int` overflow when a user has many workouts |
| 5 | `entrySet()` not `keySet()+get()` | One read instead of two weakly-consistent reads |
| 6 | `computeIfAbsent` + `ReentrantLock` in `pay_due` | Check-then-act (`compareTo` → `subtract` → `put`) must be atomic; two concurrent payments must not both pass |
| 7 | `compareTo` not `equals` on `BigDecimal` | `equals` compares scale as well as value |
| 8 | `setScale(2, HALF_UP)` after subtract | Keeps every stored amount at consistent 2-decimal precision |
| 9 | `new HashMap<>(duePayments)` | Defensive copy, callers cannot mutate internal state |
| 10 | Omit users with `count == 0` | Average of an empty set is undefined — don't emit `NaN` or `0.0` |

---

## Part 3 — Oral English Answer (Interview-Ready)

**Main walkthrough**

"The first thing I want to call out is that the original `add_workout` accepted a `workoutType` parameter but never stored it — the map was `Map<String, List<Integer>>`, so the type was silently dropped. That means I literally cannot answer a by-type query with the existing structure. So before writing the new method, I refactor the internal model to `Map<String, List<Workout>>`, where `Workout` is a small immutable value object holding the type and the duration.

I keep the same concurrency strategy as Round 1: inside `compute` I copy the old list, append the new `Workout`, and return an unmodifiable list. That's a copy-on-write atomic reference swap, so a reader iterating the list never sees a partially updated collection. I also keep checking `registeredUsers` — not the workouts map — because that's the actual business rule, and the workouts map starts empty.

For `get_average_workout_durations_by_type`, I iterate over `entrySet()` once, filter by type with `workoutType.equals(w.type)` — putting the constant on the left makes it null-safe — accumulate into a `long`, and divide by `count` cast to `double` first. That last detail matters: `sum / count` would be integer division and would truncate before the cast, so 61 over 2 would come back as `30.0` instead of `30.5`. If a user has no matching workouts, I simply omit them from the result rather than returning `NaN` or a misleading zero. I also keep the original `get_average_workout_durations()` unchanged so I don't break the existing contract.

For `pay_due`, the dangerous part is check-then-act: two threads could both read a balance of 30, both pass the comparison, and both deduct — that's a lost update. Even though this method only touches one map, I use a per-user `ReentrantLock` obtained via `computeIfAbsent`, and do the existence check, the `compareTo` check, the subtraction and the `put` all inside the same critical section. Any failed check returns `false` before a single mutation happens. I normalise the incoming amount with `setScale(2, HALF_UP)`, I compare with `compareTo` rather than `equals` because `equals` also compares scale — `30.0` and `30.00` are not equal under `equals` — and I re-apply `setScale` after subtraction.

On idempotency: a repeated identical call against a zero balance just returns `false`, so it can't double-charge. In a real HSBC system I'd go further and require an idempotency key or payment ID with a unique constraint, plus an audit row written inside the same transaction — that's why I chose an explicit lock over a `computeIfPresent` lambda, because it extends cleanly when a second map or an audit log has to move atomically with the balance.

The trade-off is the same as before: the `userLocks` map grows forever unless we evict idle locks, and a hot user serialises their own payments. For production I'd push this down to a database transaction with `SELECT ... FOR UPDATE` and let the DB handle row-level locking, deadlock detection and the audit trail."

**Short follow-up answers**

- *"Why not just use `computeIfPresent` on `duePayments` since it's a single map?"*
  "That would be atomic too, and it's a valid lighter-weight answer. I chose an explicit lock because I need an unambiguous `true`/`false` return without putting a side-effecting flag inside the remapping lambda, and because the moment we add an audit-log map the two-map consistency requirement comes back. I'd mention both options to the interviewer and let them pick."

- *"Why an immutable `Workout` object instead of a mutable one?"*
  "Because `ConcurrentHashMap` gives me atomicity on the reference swap, not on field writes. If I mutated a published object in place, a concurrent reader could see the new duration with the old type. Immutable value objects make the whole record publish atomically."

- *"What if `workoutType` itself is null when adding a workout?"*
  "Right now I'd store it and the by-type query would skip it. Stricter behaviour would be to reject null/empty types in `add_workout` alongside `duration <= 0`. I'd confirm that with the interviewer — it's a contract decision, not a correctness bug."

- *"Complexity?"*
  "`add_workout` is O(n) in the user's existing workout count because of the copy-on-write, and the two average queries are O(total workouts) with O(users) output. If workout history gets large, I'd switch to storing a per-user, per-type running `sum` and `count` so both the write and the read become O(1) — that's the natural next optimisation."