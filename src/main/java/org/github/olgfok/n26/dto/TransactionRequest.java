package org.github.olgfok.n26.dto;


public class TransactionRequest {
    /**
     * transaction amount
     */
    private double amount;

    /**
     *
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

    public TransactionRequest(long timestamp, double amount) {
        this.amount = amount;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "amount=" + amount +
                ", timestamp=" + timestamp +
                '}';
    }
}
