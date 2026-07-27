package io.github.szalaisimon.sensor_reading_api.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeasurementQuery(
        @Nullable Set<@Positive Long> deviceIds,
        @Nullable LocalDate from,
        @Nullable LocalDate to,
        @NotEmpty(message = "at least one metric is required") Set<Metric> metrics,
        @Nullable Statistic statistic
) {
}
