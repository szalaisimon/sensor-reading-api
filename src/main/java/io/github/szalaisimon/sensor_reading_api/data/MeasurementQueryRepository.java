package io.github.szalaisimon.sensor_reading_api.data;

import io.github.szalaisimon.sensor_reading_api.domain.model.DailyStatistics;
import io.github.szalaisimon.sensor_reading_api.domain.model.Measurement;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class MeasurementQueryRepository {

    private final @NonNull Map<Long, NavigableMap<LocalDate, DailyStatistics>> measurements;

    public MeasurementQueryRepository(final @NonNull MeasurementSource measurementSource) {
        this.measurements = load(measurementSource);
    }

    private @NonNull Map<Long, NavigableMap<LocalDate, DailyStatistics>> load(final @NonNull MeasurementSource measurementSource) {
        final @NonNull Map<Long, NavigableMap<LocalDate, DailyStatistics>> result = new HashMap<>();

        try (final @NonNull Stream<Measurement> measurementStream = measurementSource.getMeasurements()) {
            measurementStream.forEach(measurement -> {
                final @Nullable NavigableMap<LocalDate, DailyStatistics> dailyStatisticsMap = result.get(measurement.deviceId());
                final @NonNull LocalDate currentLocalDate = LocalDate.ofInstant(measurement.measureTime(), ZoneOffset.UTC);

                if (dailyStatisticsMap == null) {
                    // new device
                    final @NonNull NavigableMap<LocalDate, DailyStatistics> newDailyStatistics = new TreeMap<>();
                    newDailyStatistics.put(currentLocalDate, DailyStatistics.from(measurement));
                    result.put(measurement.deviceId(), newDailyStatistics);
                } else {
                    // device already set
                    final @Nullable DailyStatistics dailyStatistics = dailyStatisticsMap.get(currentLocalDate);

                    if (dailyStatistics == null) {
                        // new LocalDate
                        dailyStatisticsMap.put(currentLocalDate, DailyStatistics.from(measurement));
                    } else {
                        dailyStatistics.update(measurement);
                    }
                }
            });
        }

        return result;
    }

    public @NonNull Set<Long> getDeviceIds() {
        return Collections.unmodifiableSet(measurements.keySet());
    }

    public @NonNull NavigableMap<LocalDate, DailyStatistics> getDailyStatistics(
            final @NonNull Long deviceId,
            final @Nullable LocalDate from,
            final @Nullable LocalDate to
    ) {
        final @Nullable NavigableMap<LocalDate, DailyStatistics> dailyStatisticsMap = measurements.get(deviceId);

        if (dailyStatisticsMap == null || dailyStatisticsMap.isEmpty()) {
            return Collections.emptyNavigableMap();
        }

        if (from == null && to == null) {
            return dailyStatisticsMap.tailMap(dailyStatisticsMap.lastKey(), true);
        }

        return dailyStatisticsMap.subMap(
                from != null ? from : LocalDate.MIN, true,
                to != null ? to : LocalDate.MAX, true
        );
    }
}
