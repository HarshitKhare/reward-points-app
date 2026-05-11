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

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardsService {

    private static final BigDecimal LOWER_THRESHOLD = new BigDecimal("50");
    private static final BigDecimal UPPER_THRESHOLD = new BigDecimal("100");

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

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

    // Rewards for a Single Customer

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

    // Rewards for ALL Customers

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

    private TransactionDTO toTransactionDTO(Transaction t) {
        return TransactionDTO.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .transactionDate(t.getTransactionDate())
                .description(t.getDescription())
                .pointsEarned(calculatePoints(t.getAmount()))
                .build();
    }


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
