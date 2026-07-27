package io.github.szalaisimon.sensor_reading_api.domain.model;

import lombok.NonNull;

public record DailyStatistics(
        @NonNull MetricStatistics temperature,
        @NonNull MetricStatistics humidity
) {

    public static DailyStatistics from(final @NonNull Measurement measurement) {
        return new DailyStatistics(
                MetricStatistics.from(measurement.temperature().celsius()),
                measurement.humidity() != null ? MetricStatistics.from(measurement.humidity()) : MetricStatistics.empty()
        );
    }

    public void update(final @NonNull Measurement measurement) {
        temperature.update(measurement.temperature().celsius());

        if (measurement.humidity() != null) {
            humidity.update(measurement.humidity());
        }
    }
}
