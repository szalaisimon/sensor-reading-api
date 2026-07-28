package io.github.szalaisimon.sensor_reading_api.domain.model;

import jakarta.annotation.Nullable;
import lombok.NonNull;

import java.time.Instant;

/**
 * A single sensor reading as received from the source. Physically impossible values
 * are rejected at construction, so a measurement that exists is always valid.
 */
public record Measurement(
        long deviceId,
        @NonNull Instant measureTime,
        @NonNull Temperature temperature,
        @Nullable Integer humidity
) {

    public Measurement {
        if (deviceId <= 0) {
            throw new IllegalArgumentException("DeviceId must be greater than zero: " + deviceId);
        }

        if (humidity != null && (humidity < 0 || humidity > 100)) {
            throw new IllegalArgumentException("Humidity is out of range: " + humidity);
        }
    }
}
