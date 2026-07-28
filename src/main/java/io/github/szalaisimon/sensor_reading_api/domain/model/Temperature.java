package io.github.szalaisimon.sensor_reading_api.domain.model;

import lombok.NonNull;

/**
 * A temperature value together with the unit it was recorded in, so readings from
 * mixed-unit devices can be compared on a common Celsius scale.
 */
public record Temperature(
        double value,
        @NonNull TempUnit unit
) {

    public double celsius() {
        return unit.toCelsius(value);
    }
}
