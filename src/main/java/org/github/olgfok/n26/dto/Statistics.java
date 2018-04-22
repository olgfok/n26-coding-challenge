package org.github.olgfok.n26.dto;

import java.util.Objects;

public class Statistics {
    private final double sum;
    private final double avg;
    private final Double min;
    private final double max;
    private final long count;

    public double getSum() {
        return sum;
    }

    public Double getMin() {
        return min;
    }


    public double getMax() {
        return max;
    }


    public long getCount() {
        return count;
    }


    public Statistics(double sum, Double min, double max, long count) {
        this.sum = sum;
        this.min = min;
        this.max = max;
        this.count = count;
        this.avg = count == 0 ? 0 : sum / count;

    }

    @Override
    public String toString() {
        return "Statistics{" +
                "sum=" + sum +
                ", avg=" + avg +
                ", min=" + min +
                ", max=" + max +
                ", count=" + count +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Statistics that = (Statistics) o;
        return Double.compare(that.sum, sum) == 0 &&
                Double.compare(that.avg, avg) == 0 &&
                Double.compare(that.max, max) == 0 &&
                count == that.count &&
                Objects.equals(min, that.min);
    }

    @Override
    public int hashCode() {

        return Objects.hash(sum, avg, min, max, count);
    }
}
