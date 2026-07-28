package io.github.szalaisimon.sensor_reading_api.data;

import io.github.szalaisimon.sensor_reading_api.domain.model.Measurement;
import lombok.NonNull;

import java.util.stream.Stream;

/**
 * Supplies raw sensor readings, hiding where and in what format they are stored.
 */
public interface MeasurementSource {

    /**
     * The stream may hold an open resource, so the caller must close it.
     */
    @NonNull
    Stream<Measurement> getMeasurements();

}
