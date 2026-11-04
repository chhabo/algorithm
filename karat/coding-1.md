### Bug Version

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

### Fix version
```java
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Membership class for HCCC Rewards System
 * Thread-safe, null-safe, with overflow protection
 */
public class Membership {
    
    // Fix 1: HashMap is NOT thread-safe. Replace with ConcurrentHashMap
    // Fix 2: Use Integer (not int) to avoid auto-unboxing NPE on null returns
    private final Map<String, Integer> points = new ConcurrentHashMap<>();
    
    // Fix 3: int totalMembers++ is NOT atomic (read-modify-write race condition)
    // Use AtomicInteger for lock-free thread-safe increments
    private final AtomicInteger totalMembers = new AtomicInteger(0);

    /**
     * Add a new member to the system
     * @param memberId unique identifier for the member
     */
    public void addMember(String memberId) {
        // Fix 4: Null check — reject null memberId
        if (memberId == null || memberId.isEmpty()) {
            return;
        }
        
        // Fix 5: Only add if NOT already present (putIfAbsent is atomic)
        // Prevents overwriting existing member's points
        Integer previous = points.putIfAbsent(memberId, 0);
        
        if (previous == null) {
            // Fix 6: Only increment counter if this is a genuinely new member
            totalMembers.incrementAndGet();
        }
        // If previous != null, member already exists — do nothing (idempotent)
    }

    /**
     * Add points to an existing member
     * @param memberId the member to award points to
     * @param pointsToAdd number of points to add (must be positive)
     */
    public void addPoints(String memberId, int pointsToAdd) {
        // Fix 7: Validate inputs — reject null memberId and negative points
        if (memberId == null || pointsToAdd <= 0) {
            return;
        }
        
        // Fix 8: Check if member exists first (defensive, though compute handles null)
        if (!points.containsKey(memberId)) {
            return; // or throw IllegalArgumentException
        }
        
        // Fix 9: get + put is NOT atomic (race condition: two threads could both
        // read 100, both add 50, one overwrites the other → only +50 instead of +100)
        // Use merge() — it's atomic and lock-free
        points.merge(memberId, pointsToAdd, (oldValue, newValue) -> {
            // Fix 10: Check for integer overflow before adding
            long result = (long) oldValue + newValue;
            if (result > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE; // Cap at max (or throw exception)
            }
            return (int) result;
        });
    }

    /**
     * Retrieve points for a member
     * @param memberId the member to query
     * @return points balance, or -1 if member not found (never returns null)
     */
    public int getPoints(String memberId) {
        // Fix 11: Original code returns points.get(memberId) which returns Integer
        // Auto-unboxing null → NullPointerException!
        // Return -1 for non-existent members (or throw, depending on API design)
        if (memberId == null) {
            return -1;
        }
        
        Integer balance = points.get(memberId);
        return (balance != null) ? balance : -1;
    }

    /**
     * Get total number of members
     * @return total member count (thread-safe)
     */
    public int getTotalMembers() {
        // Fix 12: AtomicInteger.get() is thread-safe, no race condition
        return totalMembers.get();
    }
}
```


