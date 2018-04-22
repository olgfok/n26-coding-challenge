package org.github.olgfok.n26.dto;

public class Transaction {
    private final double amount;
    private final long timestamp;


    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Transaction(long timestamp, double amount) {
        this.timestamp = timestamp;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "timestamp=" + timestamp +
                ", amount=" + amount +
                '}';
    }
}
