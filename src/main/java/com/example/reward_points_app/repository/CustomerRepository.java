package com.example.reward_points_app.repository;

import com.example.reward_points_app.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link Customer} entities.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Looks up a customer by their unique email address. */
    Optional<Customer> findByEmail(String email);
}
