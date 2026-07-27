package io.github.szalaisimon.sensor_reading_api.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

public record MeasurementQueryResult(
        @NonNull MeasurementQuery query,
        @NonNull List<DeviceStatistics> devices
) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DeviceStatistics(
            long deviceId,
            @Nullable Map<Statistic, Double> temperature,
            @Nullable Map<Statistic, Double> humidity
    ) {
    }
}
