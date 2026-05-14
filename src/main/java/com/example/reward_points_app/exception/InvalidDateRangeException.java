package com.example.reward_points_app.exception;

/**
 * Thrown when a date range is invalid — either a date is null,
 * or the start date is after the end date.
 */
public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException(String message) {
        super(message);
    }
}
