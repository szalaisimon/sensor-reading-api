package io.github.szalaisimon.sensor_reading_api.web;

import io.github.szalaisimon.sensor_reading_api.domain.model.MeasurementQuery;
import io.github.szalaisimon.sensor_reading_api.domain.model.MeasurementQueryResult;
import io.github.szalaisimon.sensor_reading_api.domain.service.MeasurementQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Measurements", description = "Sensor measurement statistics")
public class MeasurementQueryController {

    private final @NonNull MeasurementQueryService measurementQueryService;

    @Operation(
            summary = "Get measurement statistics per device",
            description = """
                    Returns one statistic (MINIMUM, MAXIMUM or AVERAGE) of the asked metrics for each device.                                                                                                                       \s
                    
                    Good to know:                                                                                                                                                                                                   \s
                    * Values have no unit: temperature is in Celsius, humidity is a percent.                                                                                                                                        \s
                    * If neither `from` nor `to` is given, only the last day of each device is used.                                                                                                                                \s
                    * A metric is left out for a device when it was not asked for or has no data.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statistics per device"),
            @ApiResponse(responseCode = "400", description = "Bad query, for example no metric, or 'from' is after 'to'"),
            @ApiResponse(responseCode = "500", description = "Unexpected error")
    })
    @GetMapping
    public @NonNull ResponseEntity<MeasurementQueryResult> getStatistics(final @Valid @NonNull MeasurementQuery query) {
        return ResponseEntity.ok(measurementQueryService.query(query));
    }
}
