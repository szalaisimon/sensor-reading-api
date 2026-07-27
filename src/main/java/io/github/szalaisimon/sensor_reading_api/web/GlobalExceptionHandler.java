package io.github.szalaisimon.sensor_reading_api.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({BindException.class, IllegalArgumentException.class})
    public @NonNull ResponseEntity<ApiError> handleBadRequest(final @NonNull Exception e, final @NonNull HttpServletRequest request) {
        return ResponseEntity
                .badRequest()
                .body(ApiError.of(HttpStatus.BAD_REQUEST, message(e), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public @NonNull ResponseEntity<ApiError> handleUnexpected(final @NonNull Exception e, final @NonNull HttpServletRequest request) {
        log.error("Unexpected error while handling {}", request.getRequestURI(), e);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request.getRequestURI()));
    }

    private @NonNull String message(final @NonNull Exception e) {
        if (e instanceof BindException bindException && bindException.getFieldError() != null) {
            final @NonNull FieldError fieldError = bindException.getFieldError();
            return fieldError.getField() + ": " + (fieldError.isBindingFailure() ? "invalid value" : fieldError.getDefaultMessage());
        }

        return e.getMessage() != null ? e.getMessage() : "Invalid request";
    }
}
