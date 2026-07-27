package io.github.szalaisimon.sensor_reading_api.web;

import lombok.NonNull;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ApiError(
        @NonNull Instant timestamp,
        int status,
        @NonNull String error,
        @NonNull String message,
        @NonNull String path
) {

    public static ApiError of(final @NonNull HttpStatus status, final @NonNull String message, final @NonNull String path) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
    }
}
