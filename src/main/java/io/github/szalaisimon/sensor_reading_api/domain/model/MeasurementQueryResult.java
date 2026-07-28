package io.github.szalaisimon.sensor_reading_api.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

/**
 * Response of a statistics query: the statistics per device, along with the query that was
 * actually executed so clients can see the resolved defaults.
 */
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
