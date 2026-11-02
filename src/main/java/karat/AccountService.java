package karat;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class AccountService {
//    private Map<String, BigDecimal> accounts = new ConcurrentHashMap<>();
//
//    public AccountService() {
//        accounts.put("user1", new BigDecimal("100.50").setScale(2,RoundingMode.HALF_UP));
//        accounts.put("user2", new BigDecimal("50.00"));
//    }
//
//    private Boolean isValid(Boolean value) {
//        return Boolean.TRUE.equals(value);
//    }
//
//    public boolean transfer(String fromId, String toId, BigDecimal amount) {
//        if (!isValid(true) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
//            return false;
//        }
//        if (!accounts.containsKey(fromId) || !accounts.containsKey(toId)){
//            return false;
//        }
//
////        BigDecimal fromBalance = accounts.get(fromId);
//
//        accounts.computeIfPresent(fromId, (k,oldValue) -> {
//            BigDecimal fromBalance = accounts.get(fromId);
//            if (fromBalance.compareTo(amount) >= 0){
//                BigDecimal toBalance = accounts.get(toId);
//                accounts.put(toId,toBalance.add(amount).setScale(2,RoundingMode.HALF_UP));
//
//                return fromBalance.subtract(amount).setScale(2,RoundingMode.HALF_UP);
//            }
//            return oldValue;
//
//        });
//
//
//        return false;
//    }
//}
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class AccountService {
//    // Fix 1: Use BigDecimal for money and ConcurrentHashMap for thread safety
//    private Map<String, BigDecimal> accounts = new ConcurrentHashMap<>();
//
//    public AccountService() {
//        // Fix 2: Initialize BigDecimal with Strings to avoid precision issues, scale to 2 decimal places
//        accounts.put("user1", new BigDecimal("100.50").setScale(2, RoundingMode.HALF_UP));
//        accounts.put("user2", new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP));
//    }
//
//    // Fix 3: Null-safe boolean check in one line
//    private boolean isValid(Boolean value) {
//        return Boolean.TRUE.equals(value); // Safe even if value is null
//    }
//
//    public boolean transfer(String fromId, String toId, BigDecimal amount) {
//        // Fix 4: Boundary validation & NPE prevention
//        if (!isValid(true) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
//            return false;
//        }
//
//        // Fix 5: Prevent NPE by checking if users exist first
//        if (!accounts.containsKey(fromId) || !accounts.containsKey(toId)) {
//            return false;
//        }
//
//        BigDecimal fromBalance = accounts.get(fromId);
//
//        // Fix 6: Use compareTo for BigDecimal comparison
//        if (fromBalance.compareTo(amount) >= 0) {
//            // Fix 7: Reassign after arithmetic; use subtract/add and setScale
//            accounts.put(fromId, fromBalance.subtract(amount).setScale(2, RoundingMode.HALF_UP));
//            BigDecimal toBalance = accounts.get(toId);
//            accounts.put(toId, toBalance.add(amount).setScale(2, RoundingMode.HALF_UP));
//            return true;
//        }
//
//        return false;
//    }
//}



import java.math.BigDecimal;
        import java.math.RoundingMode;
        import java.util.Map;
        import java.util.concurrent.ConcurrentHashMap;
        import java.util.concurrent.locks.Lock;
        import java.util.concurrent.locks.ReentrantLock;

public class AccountService {

    private final Map<String, BigDecimal> accounts = new ConcurrentHashMap<>();
    // Per-user lock map to avoid global bottleneck
    private final Map<String, Lock> accountLocks = new ConcurrentHashMap<>();

    public AccountService() {
        accounts.put("user1", new BigDecimal("100.50").setScale(2, RoundingMode.HALF_UP));
        accounts.put("user2", new BigDecimal("50.00").setScale(2, RoundingMode.HALF_UP));
    }

    private boolean isValid(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public boolean transfer(String fromId, String toId, BigDecimal amount) {
        // 1. Validation
        if (!isValid(true) || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (fromId == null || toId == null || fromId.equals(toId)) {
            return false;
        }
        if (!accounts.containsKey(fromId) || !accounts.containsKey(toId)) {
            return false;
        }

        // 2. Acquire per-user locks in CONSISTENT ORDER to prevent deadlock
        Lock fromLock = accountLocks.computeIfAbsent(fromId, k -> new ReentrantLock());
        Lock toLock = accountLocks.computeIfAbsent(toId, k -> new ReentrantLock());

        // Lock ordering by user ID (alphabetical)
        Lock firstLock = fromId.compareTo(toId) < 0 ? fromLock : toLock;
        Lock secondLock = fromId.compareTo(toId) < 0 ? toLock : fromLock;

        firstLock.lock();
        secondLock.lock();

        try {
            // 3. Perform transfer atomically (within locked section)
            BigDecimal fromBalance = accounts.get(fromId);
            BigDecimal toBalance = accounts.get(toId);

            if (fromBalance == null || toBalance == null) {
                return false;
            }
            if (fromBalance.compareTo(amount) < 0) {
                return false; // Insufficient funds
            }

            accounts.put(fromId, fromBalance.subtract(amount).setScale(2, RoundingMode.HALF_UP));
            accounts.put(toId, toBalance.add(amount).setScale(2, RoundingMode.HALF_UP));

            return true;

        } finally {
            // 4. Unlock in reverse order
            secondLock.unlock();
            firstLock.unlock();
        }
    }
}
