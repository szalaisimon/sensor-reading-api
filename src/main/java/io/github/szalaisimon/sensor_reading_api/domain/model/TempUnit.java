package io.github.szalaisimon.sensor_reading_api.domain.model;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Temperature scales accepted from the source data. The API itself only ever exposes Celsius.
 */
@RequiredArgsConstructor
public enum TempUnit {
    FAHRENHEIT("F") {
        @Override
        public double toCelsius(final double value) {
            return (value - 32) * 5.0 / 9.0;
        }
    },
    CELSIUS("C") {
        @Override
        public double toCelsius(final double value) {
            return value;
        }
    };

    private final @NonNull String symbol;

    public static TempUnit from(final @NonNull String symbol) {
        final @NonNull String normalizedSymbol = symbol.trim().toUpperCase();

        for (final @NonNull TempUnit tempUnit : TempUnit.values()) {
            if (tempUnit.symbol.equals(normalizedSymbol)) {
                return tempUnit;
            }
        }

        throw new IllegalArgumentException("Unknown symbol: " + symbol);
    }

    public abstract double toCelsius(final double value);
}
