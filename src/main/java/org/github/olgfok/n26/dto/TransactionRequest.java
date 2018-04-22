package org.github.olgfok.n26.dto;

/**
 * Transaction request params
 */
public class TransactionRequest {
    /**
     * transaction amount
     */
    private double amount;

    /**
     * transaction time in epoch in millis in UTC time zone (this is not current
     * timestamp)
     */
    private long timestamp;

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public TransactionRequest() {
    }

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
