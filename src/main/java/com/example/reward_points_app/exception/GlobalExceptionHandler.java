package com.example.reward_points_app.exception;

import com.example.reward_points_app.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

/**
 * Centralised exception handler for all REST controllers.
 * Converts application exceptions into a consistent {@link ErrorResponseDTO} structure.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleCustomerNotFound(
            CustomerNotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "Customer Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(NoTransactionsFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoTransactionsFound(
            NoTransactionsFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "No Transactions Found", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidDateRange(
            InvalidDateRangeException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid Date Range", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return buildError(HttpStatus.BAD_REQUEST, "Bad Request", message, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingParams(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Missing Parameter",
                "Required parameter '" + ex.getParameterName() + "' is missing", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, "Not Found",
                "No endpoint found for: " + request.getMethod() + " " + request.getRequestURI(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(), request);
    }

    /** Builds a standard error response with the given HTTP status and message details. */
    private ResponseEntity<ErrorResponseDTO> buildError(
            HttpStatus status, String error, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(
                ErrorResponseDTO.builder()
                        .status(status.value())
                        .error(error)
                        .message(message)
                        .path(request.getRequestURI())
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}