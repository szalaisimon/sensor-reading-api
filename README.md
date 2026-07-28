# Sensor Reading API

A small REST API that answers statistics queries (minimum, maximum, average) about sensor readings from IoT devices. The readings come from a static CSV file that is loaded at startup; no database is used.

## Features

- One query endpoint that returns one statistic per metric for each device.
- Query options:
  - `deviceIds` – one or more devices; all devices if not given
  - `from` / `to` – date range (both inclusive); if neither is given, only the latest day of each device is used
  - `metrics` – `TEMPERATURE` and/or `HUMIDITY`; at least one is required
  - `statistic` – `MINIMUM`, `MAXIMUM` or `AVERAGE`; `AVERAGE` if not given
- Input validation and a uniform JSON error body for bad requests and unexpected errors.
- Tolerant CSV loading: malformed lines are logged and skipped, so one bad line does not lose the whole file.
- Fahrenheit readings are converted to Celsius, so mixed-unit devices can be compared.
- OpenAPI documentation (Swagger UI).

## Architecture

```
           web                      domain                        data
 ┌───────────────────────┐  ┌──────────────────────┐  ┌──────────────────────────┐
 │ MeasurementQuery-     │  │ MeasurementQuery-    │  │ MeasurementQuery-        │
 │ Controller            ├──► Service              ├──► Repository               │
 │                       │  │                      │  │ (per-device, per-day     │
 │ GlobalExceptionHandler│  │ defaults, aggregation│  │  summaries in memory)    │
 └───────────────────────┘  └──────────────────────┘  └────────────┬─────────────┘
                                                                   │ loads once
                                                                   │ at startup
                                                      ┌────────────▼─────────────┐
                                                      │ MeasurementSource        │
                                                      │ (CsvMeasurementSource,   │
                                                      │  readings.csv)           │
                                                      └──────────────────────────┘
```

- **web** – controller and error handling, turns HTTP requests into queries.
- **domain** – query service and models, fills in defaults and aggregates the statistics.
- **data** – CSV parsing and the in-memory store.

The domain and data classes are plain Java; a `@Configuration` class wires them together.

## Design decisions

- **Pre-aggregation into day summaries.** Readings are folded into per-device, per-day summaries (`count`, `sum`, `min`, `max` per metric) at startup. Queries only combine these summaries, so the raw readings are not kept in memory and the memory use stays bounded even for a large file. The task requires date-level filtering, so day is the natural bucket size.
- **Weighted average.** Because the summaries keep `sum` and `count`, the average over a range is weighted by the number of readings, not by days.
- **Celsius only.** Units are normalized at load time; the API always returns Celsius. Humidity is a percentage.
- **Missing values.** A reading without humidity still counts for temperature. A metric with no data in the queried range is left out of the response for that device.
- **Fail fast, but tolerate bad lines.** If the CSV cannot be opened, the application does not start. A single malformed line is only logged and skipped.
- **Echoed query.** The response contains the query with the resolved defaults, so the client can see what was actually executed.
- **Dates are calendar dates in UTC.**

## API

`GET /api/measurement`

Swagger UI: `http://localhost:8080/swagger-ui.html`

Average of both metrics for all devices, latest day of each device:

```bash
curl "http://localhost:8080/api/measurement?metrics=TEMPERATURE,HUMIDITY"
```

Minimum temperature of devices 1 and 2 in a date range:

```bash
curl "http://localhost:8080/api/measurement?deviceIds=1,2&from=2026-07-10&to=2026-07-11&metrics=TEMPERATURE&statistic=MINIMUM"
```

Example response:

```json
{
  "query": {
    "deviceIds": [1, 2],
    "from": "2026-07-10",
    "to": "2026-07-11",
    "metrics": ["TEMPERATURE"],
    "statistic": "MINIMUM"
  },
  "devices": [
    { "deviceId": 1, "temperature": { "MINIMUM": 20.0 } },
    { "deviceId": 2, "temperature": { "MINIMUM": 25.0 } }
  ]
}
```

Errors use a uniform body, for example:

```json
{
  "timestamp": "2026-07-28T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "metrics: at least one metric is required",
  "path": "/api/measurement"
}
```

## Project structure

```
src/main/java/.../sensor_reading_api
├── config      Spring wiring of the domain and data layers
├── data        CSV source and in-memory repository
├── domain
│   ├── model   measurements, statistics, query and result records
│   └── service query service (defaults, aggregation)
└── web         controller, error handling
src/main/resources
└── readings.csv
```

## Running

Requirements: Java 21 or newer. Maven is not needed; the project ships with the Maven wrapper.

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API starts on `http://localhost:8080`. The CSV location can be changed with the `app.readings.location` property.

## Testing

```bash
./mvnw test
```

Unit tests cover the CSV parsing, the repository and the query service, plus a context-load smoke test.

## Limitations

- The date range has day granularity, as required by the task.
- No controller (WebMvc) tests.

## Future improvements

- Separate web DTOs from the domain models with a mapper.
- Round the returned values to a fixed number of decimals
- Include the temperature unit in the response instead of the implicit Celsius.
- Support multiple statistics in one query (`statistic` as a set).
- Finer time granularity by using smaller buckets than a day.
