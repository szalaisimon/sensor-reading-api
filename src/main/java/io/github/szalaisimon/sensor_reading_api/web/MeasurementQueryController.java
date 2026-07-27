package io.github.szalaisimon.sensor_reading_api.web;

import io.github.szalaisimon.sensor_reading_api.domain.model.MeasurementQuery;
import io.github.szalaisimon.sensor_reading_api.domain.model.MeasurementQueryResult;
import io.github.szalaisimon.sensor_reading_api.domain.service.MeasurementQueryService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/measurement")
@RequiredArgsConstructor
public class MeasurementQueryController {

    private final @NonNull MeasurementQueryService measurementQueryService;

    @GetMapping
    public @NonNull ResponseEntity<MeasurementQueryResult> getStatistics(final @Valid @NonNull MeasurementQuery query) {
        return ResponseEntity.ok(measurementQueryService.query(query));
    }
}
