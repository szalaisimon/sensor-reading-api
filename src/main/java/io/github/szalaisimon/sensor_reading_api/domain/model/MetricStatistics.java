package io.github.szalaisimon.sensor_reading_api.domain.model;

import lombok.NonNull;

/**
 * Aggregate of one metric's values, kept as count/sum/min/max so partial results
 * can be merged without keeping the individual readings around.
 */
public record MetricStatistics(
        long count,
        double sum,
        double min,
        double max
) {

    public static final MetricStatistics EMPTY = new MetricStatistics(0L, 0D, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);

    public static MetricStatistics from(final double value) {
        return new MetricStatistics(1L, value, value, value);
    }

    public @NonNull MetricStatistics merge(final @NonNull MetricStatistics other) {
        if (other.isEmpty()) {
            return this;
        }

        if (isEmpty()) {
            return other;
        }

        return new MetricStatistics(
                count + other.count,
                sum + other.sum,
                Math.min(min, other.min),
                Math.max(max, other.max)
        );
    }

    public boolean isEmpty() {
        return count == 0L;
    }

    public double getAverage() {
        return sum / count;
    }

    public double get(final @NonNull Statistic statistic) {
        return switch (statistic) {
            case MINIMUM -> min;
            case MAXIMUM -> max;
            case AVERAGE -> getAverage();
        };
    }

}
