package com.example.reward_points_app.exception;

/**
 * Thrown when no transactions are found for a customer within the requested date range,
 * or when the system has no customers at all.
 */
public class NoTransactionsFoundException extends RuntimeException {

    public NoTransactionsFoundException(Long customerId) {
        super("No transactions found for customer with ID: " + customerId);
    }

    public NoTransactionsFoundException(String message) {
        super(message);
    }
}
