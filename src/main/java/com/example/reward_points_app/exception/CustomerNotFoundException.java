package com.example.reward_points_app.exception;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with ID: " + customerId);
    }

    public CustomerNotFoundException(String message) {
        super(message);
    }
}
