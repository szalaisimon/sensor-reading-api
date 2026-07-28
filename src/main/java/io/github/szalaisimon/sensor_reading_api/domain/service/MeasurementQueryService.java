package io.github.szalaisimon.sensor_reading_api.domain.service;

import io.github.szalaisimon.sensor_reading_api.data.MeasurementQueryRepository;
import io.github.szalaisimon.sensor_reading_api.domain.model.*;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Answers statistics queries: fills in the documented defaults, aggregates each device's
 * daily statistics over the requested days and keeps only the asked metrics.
 */
@RequiredArgsConstructor
@Slf4j
public class MeasurementQueryService {

    private final @NonNull MeasurementQueryRepository measurementQueryRepository;

    public @NonNull MeasurementQueryResult query(final @NonNull MeasurementQuery request) {
        final @NonNull MeasurementQuery query = normalize(request);
        final @NonNull List<MeasurementQueryResult.DeviceStatistics> deviceStatistics = new ArrayList<>();

        query.deviceIds().forEach(deviceId -> {
            final @NonNull Map<LocalDate, DailyStatistics> dailyStatistics = measurementQueryRepository.getDailyStatistics(deviceId, query.from(), query.to());

            //aggregate
            final @NonNull MetricStatistics aggregatedTemperature = dailyStatistics.values().stream()
                    .map(DailyStatistics::temperature)
                    .reduce(MetricStatistics.EMPTY, MetricStatistics::merge);

            final @NonNull MetricStatistics aggregatedHumidity = dailyStatistics.values().stream()
                    .map(DailyStatistics::humidity)
                    .reduce(MetricStatistics.EMPTY, MetricStatistics::merge);

            deviceStatistics.add(new MeasurementQueryResult.DeviceStatistics(
                    deviceId,
                    toStatisticMap(query.metrics(), query.statistic(), Metric.TEMPERATURE, aggregatedTemperature),
                    toStatisticMap(query.metrics(), query.statistic(), Metric.HUMIDITY, aggregatedHumidity)
            ));
        });
        return new MeasurementQueryResult(query, deviceStatistics);
    }

    /**
     * Rejects an inverted date range and resolves the defaults (all devices, AVERAGE), so the rest
     * of the query logic — and the {@code query} echoed in the response — works with explicit values.
     */
    private @NonNull MeasurementQuery normalize(final @NonNull MeasurementQuery request) {
        if (request.from() != null && request.to() != null && request.from().isAfter(request.to())) {
            throw new IllegalArgumentException("'from' (" + request.from() + ") must not be after 'to' (" + request.to() + ")");
        }

        return new MeasurementQuery(
                request.deviceIds() == null || request.deviceIds().isEmpty()
                        ? measurementQueryRepository.getDeviceIds()
                        : request.deviceIds(),
                request.from(),
                request.to(),
                request.metrics(),
                request.statistic() == null ? Statistic.AVERAGE : request.statistic()
        );
    }

    /**
     * Null when the metric was not asked for or the device has no such data,
     * which leaves the metric out of the JSON response entirely.
     */
    private @Nullable Map<Statistic, Double> toStatisticMap(
            final @NonNull Set<Metric> metrics,
            final @NonNull Statistic statistic,
            final @NonNull Metric metric,
            final @NonNull MetricStatistics statistics
    ) {
        if (!metrics.contains(metric) || statistics.isEmpty()) {
            return null;
        }

        return Map.of(statistic, statistics.get(statistic));
    }
}
