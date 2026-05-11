package com.example.reward_points_app.exception;

public class NoTransactionsFoundException extends RuntimeException {

    public NoTransactionsFoundException(Long customerId) {
        super("No transactions found for customer with ID: " + customerId);
    }

    public NoTransactionsFoundException(String message) {
        super(message);
    }
}
