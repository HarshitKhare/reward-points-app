package com.example.reward_points_app.service;

import com.example.reward_points_app.dto.CustomerRewardDTO;
import com.example.reward_points_app.dto.RewardsReportDTO;
import com.example.reward_points_app.exception.CustomerNotFoundException;
import com.example.reward_points_app.exception.InvalidDateRangeException;
import com.example.reward_points_app.exception.NoTransactionsFoundException;
import com.example.reward_points_app.model.Customer;
import com.example.reward_points_app.model.Transaction;
import com.example.reward_points_app.repository.CustomerRepository;
import com.example.reward_points_app.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RewardsService}.
 * Mocks the repository layer to isolate service logic.
 */
@ExtendWith(MockitoExtension.class)
class RewardsServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardsService rewardsService;

    private Customer customer;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        startDate = LocalDate.of(2024, 1, 1);
        endDate   = LocalDate.of(2024, 3, 31);
    }

    // -------------------------------------------------------------------------
    // calculatePoints tests
    // -------------------------------------------------------------------------

    /**
     * Amount at or below $50 should earn zero points.
     */
    @Test
    void calculatePoints_belowOrAtFifty_returnsZero() {
        assertEquals(0, rewardsService.calculatePoints(new BigDecimal("50")));
        assertEquals(0, rewardsService.calculatePoints(new BigDecimal("30")));
    }

    /**
     * Null amount should return zero points without throwing.
     */
    @Test
    void calculatePoints_nullAmount_returnsZero() {
        assertEquals(0, rewardsService.calculatePoints(null));
    }

    /**
     * $120 spend: 2 pts × $20 above $100 = 40, plus 1 pt × $50 between $50-$100 = 50. Total = 90.
     */
    @Test
    void calculatePoints_aboveHundred_returnsCorrectPoints() {
        // $120: (120-100)*2 + 50*1 = 40 + 50 = 90
        assertEquals(90, rewardsService.calculatePoints(new BigDecimal("120")));
    }

    /**
     * Amount between $50 and $100 earns 1 point per dollar above $50.
     */
    @Test
    void calculatePoints_betweenFiftyAndHundred_returnsCorrectPoints() {
        // $75: (75-50)*1 = 25
        assertEquals(25, rewardsService.calculatePoints(new BigDecimal("75")));
    }

    /**
     * Boundary check: exactly $100 earns 1 pt per dollar above $50 (50 pts).
     */
    @Test
    void calculatePoints_exactlyHundred_returnsCorrectPoints() {
        // $100: (100-50)*1 = 50
        assertEquals(50, rewardsService.calculatePoints(new BigDecimal("100")));
    }

    // -------------------------------------------------------------------------
    // getRewardsForCustomer tests
    // -------------------------------------------------------------------------

    /**
     * Happy path: valid customer with transactions in the date range.
     */
    @Test
    void getRewardsForCustomer_validRequest_returnsDTO() {
        Transaction txn = Transaction.builder()
                .id(1L)
                .customer(customer)
                .amount(new BigDecimal("120"))
                .transactionDate(LocalDate.of(2024, 1, 15))
                .description("Online purchase")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findByCustomerIdAndDateRange(1L, startDate, endDate))
                .thenReturn(List.of(txn));

        CustomerRewardDTO result = rewardsService.getRewardsForCustomer(1L, startDate, endDate);

        assertNotNull(result);
        assertEquals(1L, result.getCustomerId());
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(90, result.getTotalPoints());
        assertEquals(1, result.getMonthlyBreakdown().size());
    }

    /**
     * Customer ID that doesn't exist should throw {@link CustomerNotFoundException}.
     */
    @Test
    void getRewardsForCustomer_customerNotFound_throwsException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> rewardsService.getRewardsForCustomer(99L, startDate, endDate));
    }

    /**
     * Customer exists but has no transactions in the given range.
     */
    @Test
    void getRewardsForCustomer_noTransactions_throwsException() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findByCustomerIdAndDateRange(1L, startDate, endDate))
                .thenReturn(Collections.emptyList());

        assertThrows(NoTransactionsFoundException.class,
                () -> rewardsService.getRewardsForCustomer(1L, startDate, endDate));
    }

    /**
     * Start date after end date should fail fast with {@link InvalidDateRangeException}.
     */
    @Test
    void getRewardsForCustomer_startAfterEnd_throwsException() {
        assertThrows(InvalidDateRangeException.class,
                () -> rewardsService.getRewardsForCustomer(1L, endDate, startDate));
    }

    /**
     * Transactions spread across multiple months should produce one monthly entry each.
     */
    @Test
    void getRewardsForCustomer_multipleMonths_groupedCorrectly() {
        Transaction jan = Transaction.builder().id(1L).customer(customer)
                .amount(new BigDecimal("120")).transactionDate(LocalDate.of(2024, 1, 10))
                .description("Jan purchase").build();
        Transaction feb = Transaction.builder().id(2L).customer(customer)
                .amount(new BigDecimal("75")).transactionDate(LocalDate.of(2024, 2, 20))
                .description("Feb purchase").build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(transactionRepository.findByCustomerIdAndDateRange(1L, startDate, endDate))
                .thenReturn(List.of(jan, feb));

        CustomerRewardDTO result = rewardsService.getRewardsForCustomer(1L, startDate, endDate);

        assertEquals(2, result.getMonthlyBreakdown().size());
        // Jan: 90 pts, Feb: 25 pts
        assertEquals(115, result.getTotalPoints());
    }

    // -------------------------------------------------------------------------
    // getRewardsForAllCustomers tests
    // -------------------------------------------------------------------------

    /**
     * All customers report should include one entry per customer.
     */
    @Test
    void getRewardsForAllCustomers_validRequest_returnsReport() {
        Customer customer2 = Customer.builder().id(2L).name("Jane Smith").email("jane@example.com").build();

        Transaction txn1 = Transaction.builder().id(1L).customer(customer)
                .amount(new BigDecimal("150")).transactionDate(LocalDate.of(2024, 2, 5))
                .description("Store purchase").build();

        when(customerRepository.findAll()).thenReturn(List.of(customer, customer2));
        when(transactionRepository.findByCustomerIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of(txn1));
        when(transactionRepository.findByCustomerIdAndDateRange(eq(2L), any(), any()))
                .thenReturn(Collections.emptyList());

        RewardsReportDTO report = rewardsService.getRewardsForAllCustomers(startDate, endDate);

        assertNotNull(report);
        assertEquals(2, report.getTotalCustomers());
        assertEquals(startDate.toString(), report.getPeriodStart());
        assertEquals(endDate.toString(), report.getPeriodEnd());
    }

    /**
     * No customers in the system should throw {@link NoTransactionsFoundException}.
     */
    @Test
    void getRewardsForAllCustomers_noCustomers_throwsException() {
        when(customerRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(NoTransactionsFoundException.class,
                () -> rewardsService.getRewardsForAllCustomers(startDate, endDate));
    }

    /**
     * Null dates should be rejected before any repository call is made.
     */
    @Test
    void getRewardsForAllCustomers_nullDates_throwsException() {
        assertThrows(InvalidDateRangeException.class,
                () -> rewardsService.getRewardsForAllCustomers(null, null));

        verifyNoInteractions(customerRepository);
    }
}
