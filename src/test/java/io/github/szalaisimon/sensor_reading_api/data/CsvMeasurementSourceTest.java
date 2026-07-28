package io.github.szalaisimon.sensor_reading_api.data;

import io.github.szalaisimon.sensor_reading_api.domain.model.Measurement;
import io.github.szalaisimon.sensor_reading_api.domain.model.TempUnit;
import io.github.szalaisimon.sensor_reading_api.domain.model.Temperature;
import lombok.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static java.time.Instant.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvMeasurementSourceTest {

    private static final String HEADER = "DeviceId,MeasureTime,Temperature,TempUnit,Humidity";

    private static final @NonNull Instant I_07_01 = parse("2026-07-01T00:00:00Z");
    private static final @NonNull Instant I_07_02 = parse("2026-07-02T00:00:00Z");

    @Test
    void skipsHeaderAndParsesValidRowsTest() {
        final @NonNull List<Measurement> result = readAll(sourceOf("""
                %s
                1,2026-07-01T00:00:00Z,21.5,C,40
                2,2026-07-02T00:00:00Z,70.0,F,55
                """.formatted(HEADER)));

        assertEquals(
                List.of(
                        new Measurement(1L, I_07_01, new Temperature(21.5, TempUnit.CELSIUS), 40),
                        new Measurement(2L, I_07_02, new Temperature(70.0, TempUnit.FAHRENHEIT), 55)),
                result);
    }

    @Test
    void missingHumidityIsNullTest() {
        final @NonNull List<Measurement> result = readAll(sourceOf("""
                %s
                1,2026-07-01T00:00:00Z,21.5,C,
                """.formatted(HEADER)));

        assertEquals(List.of(new Measurement(1L, I_07_01, new Temperature(21.5, TempUnit.CELSIUS), null)), result);
    }

    @Test
    void skipsBlankLinesTest() {
        final @NonNull List<Measurement> result = readAll(sourceOf("""
                %s
                
                1,2024-05-01T10:15:30Z,21.5,C,40
                
                """.formatted(HEADER)));

        assertEquals(1, result.size());
    }

    @Test
    void skipsMalformedLinesButKeepsValidOnes() {
        final @NonNull List<Measurement> result = readAll(sourceOf("""
                %s
                1,2024-05-01T10:15:30Z,21.5,C
                nem-szam,2024-05-01T10:15:30Z,21.5,C,40
                3,nem-idobelyeg,21.5,C,40
                4,2024-05-01T10:15:30Z,nem-szam,C,40
                5,2024-05-01T10:15:30Z,21.5,C,40
                """.formatted(HEADER)));

        assertEquals(1, result.size());
        assertEquals(5L, result.getFirst().deviceId());
    }

    // =============== PRIVATE METHODS ===============

    private @NonNull CsvMeasurementSource sourceOf(final @NonNull String csv) {
        return new CsvMeasurementSource(new ByteArrayResource(csv.getBytes()));
    }

    private @NonNull List<Measurement> readAll(final @NonNull CsvMeasurementSource source) {
        try (final @NonNull Stream<Measurement> measurements = source.getMeasurements()) {
            return measurements.toList();
        }
    }
}