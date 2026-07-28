package io.github.szalaisimon.sensor_reading_api.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MeasurementQuery(
        @Schema(description = "Devices to ask for, all devices if empty", example = "1,2")
        @Nullable Set<@Positive Long> deviceIds,

        @Schema(description = "First day to use, included. If neither 'from' nor 'to' is given, only the last day of each device is used.", example = "2026-07-10")
        @Nullable LocalDate from,

        @Schema(description = "Last day to use, included. If empty, all days from 'from' are used.", example = "2026-07-12")
        @Nullable LocalDate to,

        @Schema(description = "Metrics to return, at least one is needed", example = "TEMPERATURE,HUMIDITY")
        @NotEmpty(message = "at least one metric is required") Set<Metric> metrics,

        @Schema(description = "Statistic to compute, AVERAGE if empty")
        @Nullable Statistic statistic
) {
}
