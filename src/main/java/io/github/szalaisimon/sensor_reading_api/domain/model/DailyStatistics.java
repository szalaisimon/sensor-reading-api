package io.github.szalaisimon.sensor_reading_api.domain.model;

import lombok.NonNull;

public record DailyStatistics(
        @NonNull MetricStatistics temperature,
        @NonNull MetricStatistics humidity
) {

    public static DailyStatistics from(final @NonNull Measurement measurement) {
        return new DailyStatistics(
                MetricStatistics.from(measurement.temperature().celsius()),
                measurement.humidity() != null ? MetricStatistics.from(measurement.humidity()) : MetricStatistics.EMPTY
        );
    }

    public @NonNull DailyStatistics merge(final @NonNull DailyStatistics other) {
        return new DailyStatistics(
                temperature.merge(other.temperature()),
                humidity.merge(other.humidity())
        );
    }
}
