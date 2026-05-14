package com.example.reward_points_app.integration;

import com.example.reward_points_app.model.Customer;
import com.example.reward_points_app.model.Transaction;
import com.example.reward_points_app.repository.CustomerRepository;
import com.example.reward_points_app.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests that boot the full Spring context with the H2 in-memory DB.
 * These tests verify the complete request-response flow across all layers.
 */
@SpringBootTest
@AutoConfigureMockMvc
class RewardsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        customerRepository.deleteAll();

        savedCustomer = customerRepository.save(
                Customer.builder()
                        .name("Alice Test")
                        .email("alice@test.com")
                        .build()
        );
    }

    /**
     * Full flow: a customer with a $120 transaction should earn 90 points.
     */
    @Test
    void getCustomerRewards_fullFlow_returnsCorrectPoints() throws Exception {
        transactionRepository.save(Transaction.builder()
                .customer(savedCustomer)
                .amount(new BigDecimal("120"))
                .transactionDate(LocalDate.of(2024, 1, 15))
                .description("Test purchase")
                .build());

        mockMvc.perform(get("/api/v1/rewards/customers/" + savedCustomer.getId())
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(90))
                .andExpect(jsonPath("$.customerName").value("Alice Test"))
                .andExpect(jsonPath("$.monthlyBreakdown.length()").value(1));
    }

    /**
     * A transaction outside the requested date range should not appear in results.
     */
    @Test
    void getCustomerRewards_transactionOutsideRange_notIncluded() throws Exception {
        // This one falls inside the range
        transactionRepository.save(Transaction.builder()
                .customer(savedCustomer)
                .amount(new BigDecimal("80"))
                .transactionDate(LocalDate.of(2024, 2, 10))
                .description("Feb purchase")
                .build());

        // This one falls outside the range
        transactionRepository.save(Transaction.builder()
                .customer(savedCustomer)
                .amount(new BigDecimal("200"))
                .transactionDate(LocalDate.of(2024, 5, 1))
                .description("May purchase - out of range")
                .build());

        mockMvc.perform(get("/api/v1/rewards/customers/" + savedCustomer.getId())
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                // Only the $80 transaction is counted (30 points)
                .andExpect(jsonPath("$.totalPoints").value(30));
    }

    /**
     * Requesting rewards for a customer with no transactions in range returns 404.
     */
    @Test
    void getCustomerRewards_noTransactionsInRange_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/customers/" + savedCustomer.getId())
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-03-31"))
                .andExpect(status().isNotFound());
    }

    /**
     * A non-existent customer ID should return 404 with an error body.
     */
    @Test
    void getCustomerRewards_unknownCustomer_returns404WithError() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/customers/9999")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-03-31"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Customer Not Found"));
    }

    /**
     * All-customers report endpoint should aggregate across every customer in the DB.
     */
    @Test
    void getAllCustomerRewards_multipleCustomers_returnsAllInReport() throws Exception {
        Customer second = customerRepository.save(
                Customer.builder().name("Bob Test").email("bob@test.com").build()
        );

        transactionRepository.save(Transaction.builder()
                .customer(savedCustomer)
                .amount(new BigDecimal("110"))
                .transactionDate(LocalDate.of(2024, 3, 5))
                .description("Alice purchase")
                .build());

        transactionRepository.save(Transaction.builder()
                .customer(second)
                .amount(new BigDecimal("60"))
                .transactionDate(LocalDate.of(2024, 3, 12))
                .description("Bob purchase")
                .build());

        mockMvc.perform(get("/api/v1/rewards/customers")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(2));
    }
}
