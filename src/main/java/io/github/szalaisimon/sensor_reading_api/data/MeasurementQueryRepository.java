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

/**
 * In-memory store of per-device, per-day statistics. All readings are folded into daily summaries
 * once at startup, so queries never have to touch the raw measurements again.
 */
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
                final @NonNull NavigableMap<LocalDate, DailyStatistics> deviceDays
                        = result.computeIfAbsent(measurement.deviceId(), deviceId -> new TreeMap<>());

                final @NonNull LocalDate date = LocalDate.ofInstant(measurement.measureTime(), ZoneOffset.UTC);
                final @Nullable DailyStatistics existing = deviceDays.get(date);
                final @NonNull DailyStatistics fromMeasurement = DailyStatistics.from(measurement);

                deviceDays.put(date, existing == null ? fromMeasurement : existing.merge(fromMeasurement));
            });
        }

        return result;
    }

    public @NonNull Set<Long> getDeviceIds() {
        return Collections.unmodifiableSet(measurements.keySet());
    }

    /**
     * Returns the device's daily statistics within the given closed date range.
     * An open end means unbounded; when no range is given at all, only the device's latest day is returned.
     */
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
            return Collections.unmodifiableNavigableMap(dailyStatisticsMap.tailMap(dailyStatisticsMap.lastKey(), true));
        }

        return Collections.unmodifiableNavigableMap(dailyStatisticsMap.subMap(
                from != null ? from : LocalDate.MIN, true,
                to != null ? to : LocalDate.MAX, true
        ));
    }
}
