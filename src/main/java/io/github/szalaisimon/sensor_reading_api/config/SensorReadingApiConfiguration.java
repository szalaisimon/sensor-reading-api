package io.github.szalaisimon.sensor_reading_api.config;

import io.github.szalaisimon.sensor_reading_api.data.CsvMeasurementSource;
import io.github.szalaisimon.sensor_reading_api.data.MeasurementQueryRepository;
import io.github.szalaisimon.sensor_reading_api.data.MeasurementSource;
import io.github.szalaisimon.sensor_reading_api.domain.service.MeasurementQueryService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class SensorReadingApiConfiguration {

    @Bean
    public MeasurementSource measurementSource(final @Value("${app.readings.location}") @NonNull Resource readings) {
        return new CsvMeasurementSource(readings);
    }

    @Bean
    public MeasurementQueryService measurementQueryService(final @NonNull MeasurementQueryRepository measurementQueryRepository) {
        return new MeasurementQueryService(measurementQueryRepository);
    }

    @Bean
    public MeasurementQueryRepository measurementQueryRepository(final @NonNull MeasurementSource measurementSource) {
        return new MeasurementQueryRepository(measurementSource);
    }
}
