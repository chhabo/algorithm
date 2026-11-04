package karaat;

import java.math.BigDecimal;
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
        this.price = new BigDecimal(price);
    }

    public String getSymbol()   { return symbol; }
    public Side getSide()       { return side; }
    public int getQuantity()    { return quantity; }
    public BigDecimal getPrice(){ return price; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return quantity == trade.quantity && Objects.equals(symbol, trade.symbol) && side == trade.side && Objects.equals(price, trade.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, side, quantity, price);
    }


    @Override
    public String toString() {
        return symbol + "/" + side + "/" + quantity + "/" + price;
    }
}

class TradeReconciler {
    /**
     * Return trades present in `booked` with NO identical counterpart in `settled`.
     * A match requires symbol, side, quantity and price to be identical.
     */
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
                new Trade("HCCC", Side.BUY,  100, "10.50"),
                new Trade("HCCC", Side.SELL, 100, "10.50"),
                new Trade("AAPL", Side.BUY,   50, "190.00")
        );

        List<Trade> settled = Arrays.asList(
                new Trade("HCCC", Side.BUY, 100, "10.50")
        );

        List<Trade> unreconciled = r.reconcile(booked, settled);

        // Expected: HCCC/SELL/100/10.50 and AAPL/BUY/50/190.00  -> size 2
        // Actual  : size 1 — the HCCC SELL trade silently disappears
        System.out.println("unreconciled = " + unreconciled.size() + " (expected 2)");
        unreconciled.forEach(System.out::println);
        if (unreconciled.size() != 2) throw new AssertionError("Reconciliation failed");
    }
}