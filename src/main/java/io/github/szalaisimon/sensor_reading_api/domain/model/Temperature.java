package io.github.szalaisimon.sensor_reading_api.domain.model;

import lombok.NonNull;

public record Temperature(
        double value,
        @NonNull TempUnit unit
) {

    public double celsius() {
        return unit.toCelsius(value);
    }
}
