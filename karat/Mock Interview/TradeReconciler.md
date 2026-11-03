# HSBC Karat Mock Interview — Question 4 (TradeReconciler)

**Source note:** Not a verbatim Karat item. Reconstructed from a 2026 Citi Karat public debrief (full Spring Boot source + failing test, root cause `equals/hashCode` keyed only on `symbol`). Same root cause, different symptom: here one trade silently disappears instead of returning an empty list. Method names `TradeReconciler` / `reconcile` are from the debrief; test data and the extension are added.

---

## Part 1 — Interview Questions (all rounds)

**Warm-up (oral, asked before the editor opens)**
1. "If two objects are equal per `equals()`, what must be true about their `hashCode()`? What happens if you override one but not the other?"
2. "What's the difference between a checked and an unchecked exception in Java? Give one example of each."

**Round 1 — Debugging**
"Here is a trade reconciliation service. `reconcile()` should return every trade in `booked` with NO identical counterpart in `settled` — a match requires `symbol`, `side`, `quantity` and `price` to be identical. The test is failing: expected 2 unreconciled trades, actual 1 — the `HSBC/SELL` trade silently disappears. Review the class, find every bug, fix it, and explain out loud in English. Run through the failing test in your head and tell me exactly why that one trade vanishes."

```java
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectators;

enum Side { BUY, SELL }

class Trade {
    private final String symbol;
    private final Side side;
    private final int quantity;
    private final BigDecimal price;

    Trade(String symbol, Side side, int quantity, String price) {
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = new BigDecimal(price);
    }

    public String getSymbol()   { return symbol; }
    public Side getSide()       { return side; }
    public int getQuantity()    { return quantity; }
    public BigDecimal getPrice(){ return price; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Trade)) return false;
        Trade other = (Trade) o;
        return symbol.equals(other.symbol);          // BUG: only symbol
    }

    @Override
    public int hashCode() {
        return symbol.hashCode();                    // BUG: only symbol
    }

    @Override
    public String toString() {
        return symbol + "/" + side + "/" + quantity + "/" + price;
    }
}

class TradeReconciler {
    public List<Trade> reconcile(List<Trade> booked, List<Trade> settled) {
        Set<Trade> settledSet = new HashSet<>(settled);
        return booked.stream()
                .filter(t -> !settledSet.contains(t))
                .collect(Collectors.toList());
    }
}

class TradeReconcilerTest {
    public static void main(String[] args) {
        TradeReconciler r = new TradeReconciler();
        List<Trade> booked = Arrays.asList(
            new Trade("HSBC", Side.BUY,  100, "10.50"),
            new Trade("HSBC", Side.SELL, 100, "10.50"),
            new Trade("AAPL", Side.BUY,   50, "190.00")
        );
        List<Trade> settled = Arrays.asList(
            new Trade("HSBC", Side.BUY, 100, "10.50")
        );
        List<Trade> unreconciled = r.reconcile(booked, settled);
        // Expected 2, actual 1 — HSBC/SELL silently disappears
        System.out.println("unreconciled = " + unreconciled.size() + " (expected 2)");
        if (unreconciled.size() != 2) throw new AssertionError("Reconciliation failed");
    }
}
```

**Interviewer curveball after Round 1**
"One banking-specific detail: `BigDecimal.equals()` compares value **and scale**, so `"10.50"` and `"10.5"` are not equal. And `BigDecimal.hashCode()` is scale-sensitive too. If you fix `equals` with `compareTo` but forget `hashCode`, you reintroduce the same `HashSet` bug in a different form. How do you handle it?"

**Round 2 — Extension requirement (verbatim)**
"HSBC reconciliation isn't yes/no, it's **how many**. The same trade can appear multiple times in `booked` (partial fills) and in `settled`. `contains()` can't express counts.

Add:
```java
public Map<Trade, Integer> reconcileCounts(List<Trade> booked, List<Trade> settled)
```
Rules:
1. Return each `Trade`'s **unsettled count** = `booked occurrences − settled occurrences`; keep only entries **> 0**.
2. Fully matched (diff = 0) entries must NOT appear.
3. Trades in `settled` but not in `booked` → ignore. (If you think it should be an error, ask me — don't decide on your own.)
4. Reuse your fixed `equals/hashCode`; `"10.50"` and `"10.5"` must count as the same trade.
5. `booked` / `settled` null → return an empty Map, no exception.
6. Keep it **O(n + m)** and state the complexity out loud.
7. **Keep** the original `reconcile(...)` — don't break the existing contract.

**Follow-up (answer after coding)**
"If prices need a ±0.01 tolerance match (common in FX reconciliation), does your `equals/hashCode` approach still hold? Why or why not?"

---

## Part 2 — Final Code Implementation

```java
package karat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

enum Side { BUY, SELL }

class Trade {
    private final String symbol;
    private final Side side;
    private final int quantity;
    private final BigDecimal price;

    Trade(String symbol, Side side, int quantity, String price) {
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        // FIX (curveball): normalise scale at the boundary.
        // BigDecimal.equals()/hashCode() are scale-sensitive, so "10.5" and
        // "10.50" would otherwise be treated as two different trades.
        // Normalising here keeps equals/hashCode untouched and consistent.
        this.price = new BigDecimal(price).setScale(2, RoundingMode.HALF_UP);
    }

    public String getSymbol()   { return symbol; }
    public Side getSide()       { return side; }
    public int getQuantity()    { return quantity; }
    public BigDecimal getPrice(){ return price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // getClass() vs instanceof Trade: getClass() is stricter (subclass != parent).
        // Fine here since Trade is effectively final-in-practice; I'd mention the
        // trade-off to the interviewer rather than silently changing it.
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return quantity == trade.quantity
                && Objects.equals(symbol, trade.symbol)
                && side == trade.side
                && Objects.equals(price, trade.price);   // safe now: both are scale-2
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, side, quantity, price);  // MUST match equals' field set
    }

    @Override
    public String toString() {
        return symbol + "/" + side + "/" + quantity + "/" + price;
    }
}

class TradeReconciler {

    /** Round 1 contract — kept unchanged. */
    public List<Trade> reconcile(List<Trade> booked, List<Trade> settled) {
        if (booked == null) return Collections.emptyList();
        if (settled == null) settled = Collections.emptyList();
        Set<Trade> settledSet = new HashSet<>(settled);
        return booked.stream()
                .filter(t -> !settledSet.contains(t))
                .collect(Collectors.toList());
    }

    /** Round 2: count-based reconciliation. O(n + m). */
    public Map<Trade, Integer> reconcileCounts(List<Trade> booked, List<Trade> settled) {
        // Rule 5: null input -> empty map, no exception.
        // (I'd confirm with the interviewer: if ONLY settled is null, arguably
        //  we should still return the full booked counts. I follow the spec literally.)
        if (booked == null || settled == null) return new HashMap<>();

        Map<Trade, Integer> bookedFreq = new HashMap<>();
        Map<Trade, Integer> settledFreq = new HashMap<>();
        for (Trade t : booked)  bookedFreq.merge(t, 1, Integer::sum);
        for (Trade t : settled) settledFreq.merge(t, 1, Integer::sum);

        Map<Trade, Integer> result = new HashMap<>();
        // Iterate booked keys ONLY -> rule 3 (settled-only trades) is satisfied
        // automatically, we never even look at them.
        for (Map.Entry<Trade, Integer> e : bookedFreq.entrySet()) {
            int diff = e.getValue() - settledFreq.getOrDefault(e.getKey(), 0);
            if (diff > 0) result.put(e.getKey(), diff);   // rules 1 & 2
        }
        return result;
    }
}

class TradeReconcilerTest {
    public static void main(String[] args) {
        TradeReconciler r = new TradeReconciler();

        List<Trade> booked = Arrays.asList(
                new Trade("HSBC", Side.BUY,  100, "10.50"),
                new Trade("HSBC", Side.SELL, 100, "10.50"),
                new Trade("AAPL", Side.BUY,   50, "190.00")
        );
        List<Trade> settled = Arrays.asList(
                new Trade("HSBC", Side.BUY, 100, "10.50")
        );

        // Round 1 now passes: 2
        List<Trade> unreconciled = r.reconcile(booked, settled);
        System.out.println("unreconciled = " + unreconciled.size() + " (expected 2)");
        unreconciled.forEach(System.out::println);
        if (unreconciled.size() != 2) throw new AssertionError("Reconciliation failed");

        // Round 2 demo: partial fills + scale normalisation
        List<Trade> booked2 = Arrays.asList(
                new Trade("HSBC", Side.BUY, 100, "10.50"),
                new Trade("HSBC", Side.BUY, 100, "10.5"),   // same trade, different scale
                new Trade("HSBC", Side.BUY, 100, "10.50"),
                new Trade("AAPL", Side.BUY,  50, "190.00")
        );
        List<Trade> settled2 = Arrays.asList(
                new Trade("HSBC", Side.BUY, 100, "10.50"),
                new Trade("AAPL", Side.BUY,  50, "190.00"),
                new Trade("AAPL", Side.BUY,  50, "190.00")  // settled-only -> ignored
        );
        Map<Trade, Integer> counts = r.reconcileCounts(booked2, settled2);
        // Expected: {HSBC/BUY/100/10.50=2}   (3 booked - 1 settled; AAPL diff = -1 -> omitted)
        counts.forEach((k, v) -> System.out.println(k + " -> " + v));
        System.out.println("null input -> " + r.reconcileCounts(null, settled2));
    }
}
```

**Bug / decision checklist**

| # | Issue | Fix |
|---|---|---|
| 1 | `equals`/`hashCode` used only `symbol` → `HSBC BUY` and `HSBC SELL` collapse into one `HashSet` bucket | Cover all 4 fields in **both** methods |
| 2 | Only fixing `equals` (not `hashCode`) | Kept them as a pair via `Objects.hash(...)` |
| 3 | `BigDecimal.equals` is scale-sensitive (`"10.50" != "10.5"`) | Normalise with `setScale(2, HALF_UP)` in the constructor — equals/hashCode need no change |
| 4 | Tempting fix: `compareTo` in `equals` only | Rejected — `hashCode` would still be scale-sensitive and `HashSet.contains` breaks again |
| 5 | `reconcile` itself was fine | Left untouched (rule 7); only added null guards |
| 6 | `contains()` can't express multiplicity | Two frequency maps + `merge(..., Integer::sum)` |
| 7 | settled-only / diff=0 entries | Iterate `bookedFreq` keys only, keep `diff > 0` |
| 8 | `null` input → NPE | Early return `new HashMap<>()` |
| 9 | `new BigDecimal(String)` | Kept String constructor (never `double`) |

---

## Part 3 — Oral English Answer (Interview-Ready)

**Warm-ups**

"If two objects are equal per `equals()`, they **must** return the same `hashCode()` — that's the contract. If I override only `equals`, two equal objects can land in different hash buckets, so `HashSet.contains()` returns false: that's the mirror image of the bug we're about to fix. Overriding only `hashCode` isn't enough either — they collide into the same bucket but `equals` still says no.

Checked exceptions extend `Exception` but not `RuntimeException`, and the compiler forces me to catch or declare them — `IOException` or `SQLException`, for example. Unchecked ones extend `RuntimeException`, like `NullPointerException` or `IllegalArgumentException`, and the compiler doesn't enforce handling."

**Round 1 root cause**

"`reconcile()` itself is correct — I didn't change it. The bug is in `Trade`: both `equals` and `hashCode` were built from `symbol` alone. When I construct `new HashSet<>(settled)`, the settled `HSBC/BUY` trade occupies the bucket for hash of `"HSBC"`. Then the booked `HSBC/SELL` trade produces the **same hash**, and `equals` compares only the symbol and says true — so `contains()` reports a match and the filter drops it. That's exactly why the test prints 1 instead of 2. It's a silent data-loss bug, which is worse than a crash in a banking system.

I fixed it by making both methods cover `symbol`, `side`, `quantity` and `price`, using `Objects.equals` and `Objects.hash` so they stay consistent and null-safe. I also kept the `this == o` fast path and the `getClass()` check; I'd mention to the interviewer that `instanceof Trade` is the looser alternative if subclasses are ever allowed.

Then there's the banking-specific catch: `BigDecimal.equals()` compares **value and scale**, so `"10.50"` and `"10.5"` are different objects, and `BigDecimal.hashCode()` is scale-sensitive in the same way. The tempting fix is to switch `equals` to `price.compareTo(...) == 0` — but that would break the contract again, because equal objects would then produce different hashes and `HashSet.contains` would start missing. So instead I normalise at the boundary: in the constructor I do `new BigDecimal(price).setScale(2, HALF_UP)`. Now every stored price has scale 2, the plain `Objects.equals` / `Objects.hash` are correct, and `"10.50"` from upstream and `"10.5"` from downstream reconcile as one trade. That also matches the `BigDecimal` rule from the earlier questions."

**Round 2 design**

"For `reconcileCounts`, `contains()` is the wrong primitive — it answers yes/no, but the requirement is *how many*. I build two frequency maps with `merge(trade, 1, Integer::sum)`, one for `booked` and one for `settled`, then iterate **only the booked keys**, compute `diff = bookedCount − settledOrDefaultZero`, and keep entries where `diff > 0`.

That single design choice satisfies three rules at once: fully matched trades give zero and disappear, iterating only booked keys means settled-only trades are ignored automatically — and per your instruction, I wouldn't silently turn that into an error; I'd ask whether it should raise an exception or be logged as a break. Null inputs return an empty map up front, though I'd confirm one edge with you: if only `settled` is null, arguably we should still return the full booked counts, so I'm following the spec literally but flagging it.

Complexity is **O(n + m)** time — one pass per list plus one pass over distinct booked trades — and **O(n + m)** space for the two maps. The scale normalisation means rule 4 comes for free: `"10.50"` and `"10.5"` are the same key without touching `equals` or `hashCode` again. And I kept `reconcile(...)` intact so the existing contract isn't broken."

**Follow-up: ±0.01 tolerance**

"No — the `equals/hashCode` approach **does not survive** a tolerance. A hash-based lookup needs `equals` to be a proper equivalence relation: reflexive, symmetric and **transitive**. Approximate equality isn't transitive: 9.99 ≈ 10.00 and 10.00 ≈ 10.01, but 9.99 ≉ 10.01. More practically, there is no hash function that reliably puts 'nearly equal' prices in the same bucket — two values 0.01 apart will usually hash differently, so `HashMap` structurally cannot express 'close enough'.

I'd stop using hashing for that leg: group by exact fields (`symbol`, `side`, `quantity`), sort each group by price, then do a two-pointer or interval match within ±0.01, carrying the unmatched residual into a break queue. Bucketing or rounding prices to 0.01 is tempting but still leaks at bucket boundaries. In production I'd push it to a database tolerance join with an explicit audit trail, or run exact matching first and only apply nearest-match to the residual — never silently widen `equals`, because that would corrupt every `HashSet` and `HashMap` in the codebase."