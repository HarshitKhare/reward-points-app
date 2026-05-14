package com.example.reward_points_app.repository;

import com.example.reward_points_app.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for {@link Transaction} entities.
 * Provides date-range query support on top of the standard JPA operations.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** Returns all transactions belonging to the given customer, unfiltered. */
    List<Transaction> findByCustomerId(Long customerId);

    /**
     * Returns transactions for a customer that fall within the given date range (inclusive),
     * ordered by transaction date ascending.
     */
    @Query("SELECT t FROM Transaction t WHERE t.customer.id = :customerId " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate ASC")
    List<Transaction> findByCustomerIdAndDateRange(
            @Param("customerId") Long customerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Returns all transactions across all customers within the given date range,
     * ordered by customer ID then transaction date.
     */
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.customer.id ASC, t.transactionDate ASC")
    List<Transaction> findAllByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
