package io.github.szalaisimon.sensor_reading_api.data;

import io.github.szalaisimon.sensor_reading_api.domain.model.Measurement;
import io.github.szalaisimon.sensor_reading_api.domain.model.TempUnit;
import io.github.szalaisimon.sensor_reading_api.domain.model.Temperature;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Reads measurements from a CSV resource. A single bad line should not lose a whole file of data,
 * so malformed lines are logged and skipped instead of failing the load.
 */
@Slf4j
@RequiredArgsConstructor
public class CsvMeasurementSource implements MeasurementSource {

    private static final int COLUMN_COUNT = 5;

    private final @NonNull Resource resource;

    @Override
    public @NonNull Stream<Measurement> getMeasurements() {
        try {
            final @NonNull BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            log.debug("Skipping CSV header: '{}'", reader.readLine());

            return reader.lines()
                    .onClose(() -> close(reader))
                    .map(this::parseLine)
                    .filter(Optional::isPresent)
                    .map(Optional::get);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to open readings CSV: " + resource, e);
        }
    }

    private @NonNull Optional<Measurement> parseLine(final @NonNull String line) {
        if (line.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(from(line));
        } catch (final RuntimeException e) {
            log.warn("Skipping malformed CSV line: '{}' ({})", line, e.getMessage());
            return Optional.empty();
        }
    }

    private @NonNull Measurement from(final @NonNull String line) {
        final @NonNull String[] columns = line.split(",", -1);
        if (columns.length != COLUMN_COUNT) {
            throw new IllegalArgumentException("Expected " + COLUMN_COUNT + " columns but got " + columns.length);
        }

        final long deviceId = Long.parseLong(columns[0].trim());
        final @NonNull Instant timestamp = Instant.parse(columns[1].trim());
        final @NonNull Temperature temperature = new Temperature(Double.parseDouble(columns[2].trim()), TempUnit.from(columns[3].trim()));
        final @Nullable Integer humidity = columns[4].isBlank() ? null : Integer.valueOf(columns[4].trim());

        return new Measurement(deviceId, timestamp, temperature, humidity);
    }

    private void close(final @NonNull BufferedReader reader) {
        try {
            reader.close();
        } catch (final IOException e) {
            log.warn("Failed to close readings CSV reader", e);
        }
    }
}
