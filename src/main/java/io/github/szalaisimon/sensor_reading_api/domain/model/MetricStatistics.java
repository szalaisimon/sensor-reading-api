package io.github.szalaisimon.sensor_reading_api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class MetricStatistics {

    private long count;
    private double sum;
    private double min;
    private double max;

    public static MetricStatistics from(double value) {
        return new MetricStatistics(1L, value, value, value);
    }

    public static MetricStatistics empty() {
        return new MetricStatistics(0L, 0D, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
    }

    public void update(double value) {
        this.sum += value;
        this.count += 1L;

        if (value > max) {
            this.max = value;
        }

        if (value < min) {
            this.min = value;
        }
    }

    /**
     * Két részaggregátum összefésülése (pl. napi statisztikákból időszakos statisztika).
     */
    public void update(final @NonNull MetricStatistics other) {
        if (other.isEmpty()) {
            return;
        }

        this.sum += other.sum;
        this.count += other.count;

        if (other.max > max) {
            this.max = other.max;
        }

        if (other.min < min) {
            this.min = other.min;
        }
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
