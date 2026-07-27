package io.github.szalaisimon.sensor_reading_api.data;

import io.github.szalaisimon.sensor_reading_api.domain.model.Measurement;
import lombok.NonNull;

import java.util.stream.Stream;

public interface MeasurementSource {

    @NonNull
    Stream<Measurement> getMeasurements();

}
