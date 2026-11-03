package karat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.*;
public class Membership {
    private Set<String> registeredUsers = new HashSet<>();
        private Map<String, List<Integer>> workouts = new ConcurrentHashMap<>();
    private Map<String, BigDecimal> duePayments = new ConcurrentHashMap<>();

    public Membership() {
        registeredUsers.add("user1");
        registeredUsers.add("user2");
        duePayments.put("user1", new BigDecimal("30.00").setScale(2, RoundingMode.HALF_UP));
        duePayments.put("user2", new BigDecimal("0.00").setScale(2,RoundingMode.HALF_UP));
    }

    // Ignore the workout if the user does not exist
    public void add_workout(String userId, String workoutType, int duration) {

        if (!registeredUsers.contains(userId) || duration < 0){
            return;
        }
        workouts.compute(userId,(key, oldValue) ->{
            if (oldValue == null){
                return Collections.singletonList(duration);
            }

            List<Integer> next = new ArrayList<>();
            next.addAll(oldValue);
            next.add(duration);

            return Collections.unmodifiableList(next);
        });
    }

    // Return userId -> average workout duration
    public Map<String, Double> get_average_workout_durations() {
        Map<String, Double> result = new HashMap<>();
        for (String userId : workouts.keySet()) {
            List<Integer> list = workouts.get(userId);
            int sum = 0;
            for (int d : list) sum += d;
            result.put(userId, (double) (sum / list.size()));
        }
        return result;
    }

    // Return userId -> overdue amount
    public Map<String, BigDecimal> get_due_payments() {
        return duePayments;
    }
}



class Membership2 {

    // Fix 3: thread-safe set, and THIS is the source of truth for "user exists"
    private final Set<String> registeredUsers = ConcurrentHashMap.newKeySet();
    private final Map<String, List<Integer>> workouts = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> duePayments = new ConcurrentHashMap<>();

    public Membership2() {
        registeredUsers.add("user1");
        registeredUsers.add("user2");
        duePayments.put("user1", new BigDecimal("30.00").setScale(2, RoundingMode.HALF_UP));
        duePayments.put("user2", new BigDecimal("0.00").setScale(2, RoundingMode.HALF_UP));
    }

    // Ignore the workout if the user does not exist
    public void add_workout(String userId, String workoutType, int duration) {
        // Fix 1: check registeredUsers, not workouts; reject non-positive duration
        if (userId == null || !registeredUsers.contains(userId) || duration <= 0) {
            return;
        }
        // Fix 1b: compute handles the "first workout" case (oldValue == null)
        workouts.compute(userId, (k, oldValue) -> {
            List<Integer> next = (oldValue == null)
                    ? new ArrayList<>()
                    : new ArrayList<>(oldValue);
            next.add(duration);
            return Collections.unmodifiableList(next);   // immutable snapshot
        });
    }

    // Return userId -> average workout duration
    public Map<String, Double> get_average_workout_durations() {
        Map<String, Double> result = new HashMap<>();
        // Fix 5: single pass over entrySet, no second get()
        for (Map.Entry<String, List<Integer>> e : workouts.entrySet()) {
            List<Integer> list = e.getValue();
            if (list == null || list.isEmpty()) continue;

            long sum = 0L;                              // Fix 2b: avoid int overflow
            for (int d : list) sum += d;
            result.put(e.getKey(), sum / (double) list.size());   // Fix 2: cast FIRST
        }
        return result;
    }

    // Return userId -> overdue amount
    // I deliberately changed Integer -> BigDecimal because these are money amounts.
    public Map<String, BigDecimal> get_due_payments() {
        return new HashMap<>(duePayments);            // Fix 4: defensive copy
    }
}




class Membership3 {
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

    public Membership3() {
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