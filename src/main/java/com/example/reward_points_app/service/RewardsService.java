package com.example.reward_points_app.service;

import com.example.reward_points_app.dto.CustomerRewardDTO;
import com.example.reward_points_app.dto.MonthlyRewardDTO;
import com.example.reward_points_app.dto.RewardsReportDTO;
import com.example.reward_points_app.dto.TransactionDTO;
import com.example.reward_points_app.exception.CustomerNotFoundException;
import com.example.reward_points_app.exception.InvalidDateRangeException;
import com.example.reward_points_app.exception.NoTransactionsFoundException;
import com.example.reward_points_app.model.Customer;
import com.example.reward_points_app.model.Transaction;
import com.example.reward_points_app.repository.CustomerRepository;
import com.example.reward_points_app.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service responsible for calculating and aggregating reward points
 * earned by customers based on their transaction history.
 *
 * <p>Points are awarded as follows:
 * <ul>
 *   <li>No points for purchases at or below $50</li>
 *   <li>1 point per dollar spent between $50 and $100</li>
 *   <li>2 points per dollar spent above $100 (plus the 1 pt/dollar tier)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RewardsService {

    private static final BigDecimal LOWER_THRESHOLD = new BigDecimal("50");
    private static final BigDecimal UPPER_THRESHOLD = new BigDecimal("100");

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    /**
     * Calculates the reward points earned for a single transaction amount.
     *
     * @param amount the transaction amount; {@code null} is treated as zero
     * @return the number of points earned
     */
    public int calculatePoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(LOWER_THRESHOLD) <= 0) {
            return 0;
        }

        int points = 0;

        if (amount.compareTo(UPPER_THRESHOLD) > 0) {
            // Points for the expenditure above $100 (2 points per dollar)
            BigDecimal aboveHundred = amount.subtract(UPPER_THRESHOLD);
            points += aboveHundred.intValue() * 2;

            // Points for the expenditure between $50 and $100 (1 point per dollar)
            points += LOWER_THRESHOLD.intValue();
        }
        else {
            // Amount between $50 and $100: 1 point per dollar above $50
            BigDecimal aboveFifty = amount.subtract(LOWER_THRESHOLD);
            points += aboveFifty.intValue();
        }

        return points;
    }

    /**
     * Returns reward points earned by a specific customer within a date range,
     * broken down by month.
     *
     * @param customerId the ID of the customer
     * @param startDate  the start of the period (inclusive)
     * @param endDate    the end of the period (inclusive)
     * @return a {@link CustomerRewardDTO} with the monthly breakdown and total points
     * @throws CustomerNotFoundException    if the customer ID does not exist
     * @throws NoTransactionsFoundException if there are no transactions in the given range
     * @throws InvalidDateRangeException    if the date range is null or start is after end
     */
    public CustomerRewardDTO getRewardsForCustomer(Long customerId, LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        List<Transaction> transactions = transactionRepository
                .findByCustomerIdAndDateRange(customerId, startDate, endDate);

        if (transactions.isEmpty()) {
            throw new NoTransactionsFoundException(
                    "No transactions found for customer ID " + customerId +
                    " between " + startDate + " and " + endDate);
        }

        return buildCustomerRewardDTO(customer, transactions);
    }

    /**
     * Returns reward points for all customers in the system within a date range.
     * Customers with no transactions in the period will have zero points.
     *
     * @param startDate the start of the period (inclusive)
     * @param endDate   the end of the period (inclusive)
     * @return a {@link RewardsReportDTO} containing one entry per customer
     * @throws NoTransactionsFoundException if no customers exist in the system
     * @throws InvalidDateRangeException    if the date range is null or start is after end
     */
    public RewardsReportDTO getRewardsForAllCustomers(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);

        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            throw new NoTransactionsFoundException("No customers found in the system.");
        }

        List<CustomerRewardDTO> customerRewards = customers.stream()
                .map(customer -> {
                    List<Transaction> txns = transactionRepository
                            .findByCustomerIdAndDateRange(customer.getId(), startDate, endDate);
                    return buildCustomerRewardDTO(customer, txns);
                })
                .collect(Collectors.toList());

        return RewardsReportDTO.builder()
                .periodStart(startDate.toString())
                .periodEnd(endDate.toString())
                .totalCustomers(customerRewards.size())
                .customers(customerRewards)
                .build();
    }

    /**
     * Builds a {@link CustomerRewardDTO} by grouping the given transactions by month
     * and computing points for each.
     */
    private CustomerRewardDTO buildCustomerRewardDTO(Customer customer, List<Transaction> transactions) {
        // Group transactions by year-month
        Map<String, List<Transaction>> byMonth = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().getYear() + "-" +
                             String.format("%02d", t.getTransactionDate().getMonthValue()),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<MonthlyRewardDTO> monthlyBreakdown = byMonth.entrySet().stream()
                .map(entry -> {
                    List<TransactionDTO> txnDTOs = entry.getValue().stream()
                            .map(this::toTransactionDTO)
                            .collect(Collectors.toList());

                    int monthPoints = txnDTOs.stream().mapToInt(TransactionDTO::getPointsEarned).sum();

                    // Parse year and month from key "YYYY-MM"
                    String[] parts = entry.getKey().split("-");
                    int year  = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    LocalDate sample = LocalDate.of(year, month, 1);
                    String label = sample.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase()
                                   + " " + year;

                    return MonthlyRewardDTO.builder()
                            .month(label)
                            .year(year)
                            .monthNumber(month)
                            .points(monthPoints)
                            .transactions(txnDTOs)
                            .build();
                })
                .collect(Collectors.toList());

        int totalPoints = monthlyBreakdown.stream().mapToInt(MonthlyRewardDTO::getPoints).sum();

        return CustomerRewardDTO.builder()
                .customerId(customer.getId())
                .customerName(customer.getName())
                .customerEmail(customer.getEmail())
                .monthlyBreakdown(monthlyBreakdown)
                .totalPoints(totalPoints)
                .build();
    }

    /** Converts a {@link Transaction} entity to its DTO representation, including points earned. */
    private TransactionDTO toTransactionDTO(Transaction t) {
        return TransactionDTO.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .transactionDate(t.getTransactionDate())
                .description(t.getDescription())
                .pointsEarned(calculatePoints(t.getAmount()))
                .build();
    }

    /**
     * Validates that both dates are non-null and that start is not after end.
     *
     * @throws InvalidDateRangeException if validation fails
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new InvalidDateRangeException("Start date and end date must not be null.");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(
                    "Start date (" + startDate + ") must not be after end date (" + endDate + ").");
        }
    }
}
