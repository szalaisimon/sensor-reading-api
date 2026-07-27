package io.github.szalaisimon.sensor_reading_api.data;

import io.github.szalaisimon.sensor_reading_api.domain.model.*;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.stream.Stream;

import static java.time.Instant.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasurementQueryRepositoryTest {

    private static final @NonNull Instant I_07_01 = parse("2026-07-01T00:00:00Z");
    private static final @NonNull Instant I_07_02 = parse("2026-07-02T00:00:00Z");
    private static final @NonNull Instant I_07_03 = parse("2026-07-03T00:00:00Z");

    private static final @NonNull Instant I_07_01_NOON = parse("2026-07-01T12:00:00Z");
    private static final @NonNull Instant I_07_02_NOON = parse("2026-07-02T12:00:00Z");

    private static final @NonNull LocalDate D_07_01 = LocalDate.of(2026, 7, 1);
    private static final @NonNull LocalDate D_07_02 = LocalDate.of(2026, 7, 2);
    private static final @NonNull LocalDate D_07_03 = LocalDate.of(2026, 7, 3);

    @Mock
    private @NonNull MeasurementSource measurementSource;

    @Test
    void sameDayMeasurementsAreAggregatedTest() {
        final @NonNull MeasurementQueryRepository measurementQueryRepository = repositoryOf(
                measurement(I_07_01, 20),
                measurement(I_07_01_NOON, 30)
        );

        final @NonNull NavigableMap<LocalDate, DailyStatistics> actualDailyStatistics
                = measurementQueryRepository.getDailyStatistics(1L, null, null);

        assertEquals(
                Map.of(D_07_01, new DailyStatistics(new MetricStatistics(2L, 50, 20, 30), MetricStatistics.empty())),
                actualDailyStatistics
        );
    }

    @Test
    void nullHumidityIsSkippedInAggregationTest() {
        final @NonNull MeasurementQueryRepository measurementQueryRepository = repositoryOf(
                measurement(I_07_01, 20, null),
                measurement(I_07_01_NOON, 30, 40),
                measurement(I_07_02, 20, 50),
                measurement(I_07_02_NOON, 30, null)
        );

        final @NonNull NavigableMap<LocalDate, DailyStatistics> actualDailyStatistics
                = measurementQueryRepository.getDailyStatistics(1L, D_07_01, D_07_02);

        assertEquals(
                Map.of(
                        D_07_01, new DailyStatistics(new MetricStatistics(2L, 50, 20, 30), new MetricStatistics(1L, 40, 40, 40)),
                        D_07_02, new DailyStatistics(new MetricStatistics(2L, 50, 20, 30), new MetricStatistics(1L, 50, 50, 50))
                ),
                actualDailyStatistics
        );
    }

    @Test
    void onlyLatestDayIsReturnedWhenFromAndToAreNullTest() {
        final @NonNull MeasurementQueryRepository measurementQueryRepository = repositoryOf(
                measurement(I_07_01, 10),
                measurement(I_07_02, 20),
                measurement(I_07_03, 30)
        );

        final @NonNull NavigableMap<LocalDate, DailyStatistics> actualDailyStatistics
                = measurementQueryRepository.getDailyStatistics(1L, null, null);

        assertEquals(
                Map.of(D_07_03, new DailyStatistics(MetricStatistics.from(30), MetricStatistics.empty())),
                actualDailyStatistics
        );
    }

    @Test
    void fromFiltersEarlierDaysWhenToIsNullTest() {
        final @NonNull MeasurementQueryRepository measurementQueryRepository = repositoryOf(
                measurement(I_07_01, 10),
                measurement(I_07_02, 20),
                measurement(I_07_03, 30)
        );

        final @NonNull NavigableMap<LocalDate, DailyStatistics> actualDailyStatistics
                = measurementQueryRepository.getDailyStatistics(1L, D_07_02, null);

        assertEquals(2, actualDailyStatistics.size());
        assertEquals(Map.of(
                D_07_02, new DailyStatistics(MetricStatistics.from(20), MetricStatistics.empty()),
                D_07_03, new DailyStatistics(MetricStatistics.from(30), MetricStatistics.empty())
        ), actualDailyStatistics);
    }

    @Test
    void toFiltersLaterDaysWhenFromIsNullTest() {
        final @NonNull MeasurementQueryRepository measurementQueryRepository = repositoryOf(
                measurement(I_07_01, 10),
                measurement(I_07_02, 20),
                measurement(I_07_03, 30)
        );

        final @NonNull NavigableMap<LocalDate, DailyStatistics> actualDailyStatistics
                = measurementQueryRepository.getDailyStatistics(1L, null, D_07_02);

        assertEquals(2, actualDailyStatistics.size());
        assertEquals(Map.of(
                D_07_01, new DailyStatistics(MetricStatistics.from(10), MetricStatistics.empty()),
                D_07_02, new DailyStatistics(MetricStatistics.from(20), MetricStatistics.empty())
        ), actualDailyStatistics);
    }

    @Test
    void singleDayIsReturnedWhenFromEqualsToTest() {
        final @NonNull MeasurementQueryRepository measurementQueryRepository = repositoryOf(
                measurement(I_07_01, 10),
                measurement(I_07_02, 20),
                measurement(I_07_03, 30)
        );

        final @NonNull NavigableMap<LocalDate, DailyStatistics> actualDailyStatistics
                = measurementQueryRepository.getDailyStatistics(1L, D_07_02, D_07_02);

        assertEquals(
                Map.of(D_07_02, new DailyStatistics(MetricStatistics.from(20), MetricStatistics.empty())),
                actualDailyStatistics
        );
    }

    @Test
    void emptyStatisticsMapWhenNoMeasurementsArePresentTest() {
        final @NonNull MeasurementQueryRepository measurementQueryRepository = repositoryOf();
        assertEquals(Map.of(), measurementQueryRepository.getDailyStatistics(1L, null, null));
    }

    @Test
    void emptyStatisticsMapWhenQueryingUnknownDeviceIdTest() {
        final @NonNull MeasurementQueryRepository measurementQueryRepository = repositoryOf(
                measurement(I_07_01, 20)
        );

        assertEquals(Map.of(), measurementQueryRepository.getDailyStatistics(2L, null, null));
    }

    @Test
    void allDeviceIdsAreReturnedTest() {
        final @NonNull MeasurementQueryRepository measurementQueryRepository = repositoryOf(
                measurement(1L, I_07_01, 10, null),
                measurement(2L, I_07_01, 20, null)
        );

        final @NonNull Set<Long> expectedDeviceIds = Set.of(1L, 2L);

        assertEquals(expectedDeviceIds.size(), measurementQueryRepository.getDeviceIds().size());
        assertEquals(expectedDeviceIds, measurementQueryRepository.getDeviceIds());
    }

    // =============== PRIVATE METHODS ===============

    private @NonNull MeasurementQueryRepository repositoryOf(final @NonNull Measurement... measurements) {
        when(measurementSource.getMeasurements()).thenReturn(Stream.of(measurements));
        return new MeasurementQueryRepository(measurementSource);
    }

    private static @NonNull Measurement measurement(final @NonNull Instant measureTime, final double temperature) {
        return measurement(1L, measureTime, temperature, null);
    }

    private static @NonNull Measurement measurement(final @NonNull Instant measureTime, final double temperature, final @Nullable Integer humidity) {
        return measurement(1L, measureTime, temperature, humidity);
    }

    private static @NonNull Measurement measurement(final long deviceId, final @NonNull Instant measureTime, final double temperature, final @Nullable Integer humidity) {
        return new Measurement(deviceId, measureTime, new Temperature(temperature, TempUnit.CELSIUS), humidity);
    }
}
