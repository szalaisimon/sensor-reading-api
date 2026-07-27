package io.github.szalaisimon.sensor_reading_api.domain.service;

import io.github.szalaisimon.sensor_reading_api.data.MeasurementQueryRepository;
import io.github.szalaisimon.sensor_reading_api.domain.model.*;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasurementQueryServiceTest {

    private static final @NonNull LocalDate D_07_01 = LocalDate.of(2026, 7, 1);
    private static final @NonNull LocalDate D_07_02 = LocalDate.of(2026, 7, 2);

    @Mock
    private @NonNull MeasurementQueryRepository measurementQueryRepository;

    @InjectMocks
    private MeasurementQueryService measurementQueryService;

    @Test
    void defaultQueryParamsTest() {
        when(measurementQueryRepository.getDeviceIds()).thenReturn(Set.of(1L, 2L));
        when(measurementQueryRepository.getDailyStatistics(1L, D_07_01, D_07_01)).thenReturn(dailyStatisticsOf(D_07_01, day(20, 40)));
        when(measurementQueryRepository.getDailyStatistics(2L, D_07_01, D_07_01)).thenReturn(dailyStatisticsOf(D_07_01, day(30, 50)));

        final @NonNull MeasurementQueryResult result = measurementQueryService.query(
                new MeasurementQuery(null, D_07_01, D_07_01, Set.of(Metric.TEMPERATURE), null)
        );

        assertEquals(
                new MeasurementQuery(Set.of(1L, 2L), D_07_01, D_07_01, Set.of(Metric.TEMPERATURE), Statistic.AVERAGE),
                result.query()
        );

        assertEquals(
                Set.of(
                        new MeasurementQueryResult.DeviceStatistics(1L, Map.of(Statistic.AVERAGE, 20D), null),
                        new MeasurementQueryResult.DeviceStatistics(2L, Map.of(Statistic.AVERAGE, 30D), null)
                ),
                Set.copyOf(result.devices())
        );
    }

    @Test
    void fromAfterToThrowsTest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> measurementQueryService.query(new MeasurementQuery(null, D_07_02, D_07_01, Set.of(Metric.TEMPERATURE), null))
        );
    }

    @Test
    void explicitDeviceIdsAreUsedTest() {
        when(measurementQueryRepository.getDailyStatistics(2L, D_07_01, D_07_01)).thenReturn(dailyStatisticsOf(D_07_01, day(20)));

        final @NonNull MeasurementQuery query = new MeasurementQuery(Set.of(2L), D_07_01, D_07_01, Set.of(Metric.TEMPERATURE), Statistic.AVERAGE);
        final @NonNull MeasurementQueryResult result = measurementQueryService.query(query);

        assertEquals(
                new MeasurementQueryResult(query, List.of(
                        new MeasurementQueryResult.DeviceStatistics(2L, Map.of(Statistic.AVERAGE, 20D), null)
                )),
                result
        );
    }

    @Test
    void averageIsWeightedByMeasurementCountTest() {
        when(measurementQueryRepository.getDailyStatistics(1L, D_07_01, D_07_02)).thenReturn(dailyStatisticsOf(
                D_07_01, new DailyStatistics(new MetricStatistics(3L, 60, 10, 30), MetricStatistics.EMPTY),
                D_07_02, new DailyStatistics(new MetricStatistics(1L, 40, 40, 40), MetricStatistics.EMPTY)
        ));

        final @NonNull MeasurementQuery query = new MeasurementQuery(Set.of(1L), D_07_01, D_07_02, Set.of(Metric.TEMPERATURE), Statistic.AVERAGE);
        final @NonNull MeasurementQueryResult result = measurementQueryService.query(query);

        assertEquals(
                new MeasurementQueryResult(query, List.of(
                        new MeasurementQueryResult.DeviceStatistics(1L, Map.of(Statistic.AVERAGE, 25D), null)
                )),
                result
        );
    }

    @Test
    void minimumSpansAllDaysTest() {
        when(measurementQueryRepository.getDailyStatistics(1L, D_07_01, D_07_02)).thenReturn(dailyStatisticsOf(
                D_07_01, day(10),
                D_07_02, day(40)
        ));

        final @NonNull MeasurementQuery query = new MeasurementQuery(Set.of(1L), D_07_01, D_07_02, Set.of(Metric.TEMPERATURE), Statistic.MINIMUM);
        final @NonNull MeasurementQueryResult result = measurementQueryService.query(query);

        assertEquals(
                new MeasurementQueryResult(query, List.of(
                        new MeasurementQueryResult.DeviceStatistics(1L, Map.of(Statistic.MINIMUM, 10D), null)
                )),
                result
        );
    }

    @Test
    void maximumSpansAllDaysTest() {
        when(measurementQueryRepository.getDailyStatistics(1L, D_07_01, D_07_02)).thenReturn(dailyStatisticsOf(
                D_07_01, day(10),
                D_07_02, day(40)
        ));

        final @NonNull MeasurementQuery query = new MeasurementQuery(Set.of(1L), D_07_01, D_07_02, Set.of(Metric.TEMPERATURE), Statistic.MAXIMUM);
        final @NonNull MeasurementQueryResult result = measurementQueryService.query(query);

        assertEquals(
                new MeasurementQueryResult(query, List.of(
                        new MeasurementQueryResult.DeviceStatistics(1L, Map.of(Statistic.MAXIMUM, 40D), null)
                )),
                result
        );
    }

    @Test
    void requestedMetricWithoutDataIsNullTest() {
        when(measurementQueryRepository.getDailyStatistics(1L, D_07_01, D_07_01)).thenReturn(dailyStatisticsOf(D_07_01, day(20)));

        final @NonNull MeasurementQuery query = new MeasurementQuery(Set.of(1L), D_07_01, D_07_01, Set.of(Metric.TEMPERATURE, Metric.HUMIDITY), Statistic.AVERAGE);
        final @NonNull MeasurementQueryResult result = measurementQueryService.query(query);

        assertEquals(
                new MeasurementQueryResult(query, List.of(
                        new MeasurementQueryResult.DeviceStatistics(1L, Map.of(Statistic.AVERAGE, 20D), null)
                )),
                result
        );
    }

    @Test
    void deviceWithoutMeasurementsHasNoStatisticsTest() {
        when(measurementQueryRepository.getDailyStatistics(1L, D_07_01, D_07_01)).thenReturn(Collections.emptyNavigableMap());

        final @NonNull MeasurementQuery query = new MeasurementQuery(Set.of(1L), D_07_01, D_07_01, Set.of(Metric.TEMPERATURE, Metric.HUMIDITY), Statistic.AVERAGE);
        final @NonNull MeasurementQueryResult result = measurementQueryService.query(query);

        assertEquals(
                new MeasurementQueryResult(query, List.of(
                        new MeasurementQueryResult.DeviceStatistics(1L, null, null)
                )),
                result
        );
    }

    // =============== PRIVATE METHODS ===============

    private static @NonNull DailyStatistics day(final double temperature) {
        return new DailyStatistics(MetricStatistics.from(temperature), MetricStatistics.EMPTY);
    }

    private static @NonNull DailyStatistics day(final double temperature, final int humidity) {
        return new DailyStatistics(MetricStatistics.from(temperature), MetricStatistics.from(humidity));
    }

    private static @NonNull NavigableMap<LocalDate, DailyStatistics> dailyStatisticsOf(
            final @NonNull LocalDate date,
            final @NonNull DailyStatistics dailyStatistics
    ) {
        return new TreeMap<>(Map.of(date, dailyStatistics));
    }

    private static @NonNull NavigableMap<LocalDate, DailyStatistics> dailyStatisticsOf(
            final @NonNull LocalDate date1,
            final @NonNull DailyStatistics dailyStatistics1,
            final @NonNull LocalDate date2,
            final @NonNull DailyStatistics dailyStatistics2
    ) {
        return new TreeMap<>(Map.of(date1, dailyStatistics1, date2, dailyStatistics2));
    }
}
