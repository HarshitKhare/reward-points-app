package com.example.reward_points_app.controller;

import com.example.reward_points_app.dto.CustomerRewardDTO;
import com.example.reward_points_app.dto.MonthlyRewardDTO;
import com.example.reward_points_app.dto.RewardsReportDTO;
import com.example.reward_points_app.exception.CustomerNotFoundException;
import com.example.reward_points_app.exception.InvalidDateRangeException;
import com.example.reward_points_app.exception.NoTransactionsFoundException;
import com.example.reward_points_app.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-layer tests for {@link RewardsController}.
 * Uses {@code @WebMvcTest} so only the web layer is loaded; the service is mocked.
 */
@WebMvcTest(RewardsController.class)
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardsService rewardsService;

    // -------------------------------------------------------------------------
    // GET /api/v1/rewards/customers/{customerId}
    // -------------------------------------------------------------------------

    /**
     * Valid request for a single customer should return 200 with reward data.
     */
    @Test
    void getCustomerRewards_validRequest_returns200() throws Exception {
        CustomerRewardDTO dto = CustomerRewardDTO.builder()
                .customerId(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .totalPoints(90)
                .monthlyBreakdown(List.of(
                        MonthlyRewardDTO.builder()
                                .month("JANUARY 2024")
                                .year(2024)
                                .monthNumber(1)
                                .points(90)
                                .transactions(List.of())
                                .build()
                ))
                .build();

        when(rewardsService.getRewardsForCustomer(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dto);

        mockMvc.perform(get("/api/v1/rewards/customers/1")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.totalPoints").value(90));
    }

    /**
     * Request for a non-existent customer should return 404.
     */
    @Test
    void getCustomerRewards_customerNotFound_returns404() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq(99L), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new CustomerNotFoundException(99L));

        mockMvc.perform(get("/api/v1/rewards/customers/99")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-03-31"))
                .andExpect(status().isNotFound());
    }

    /**
     * Missing date parameters should result in 400 Bad Request.
     */
    @Test
    void getCustomerRewards_missingDateParams_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/customers/1"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Invalid date format (not ISO) should return 400 Bad Request.
     */
    @Test
    void getCustomerRewards_invalidDateFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/customers/1")
                        .param("startDate", "01-01-2024")
                        .param("endDate", "03-31-2024"))
                .andExpect(status().isBadRequest());
    }

    /**
     * When no transactions exist for the customer in the range, expect 404.
     */
    @Test
    void getCustomerRewards_noTransactions_returns404() throws Exception {
        when(rewardsService.getRewardsForCustomer(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new NoTransactionsFoundException("No transactions found"));

        mockMvc.perform(get("/api/v1/rewards/customers/1")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-03-31"))
                .andExpect(status().isNotFound());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/rewards/customers
    // -------------------------------------------------------------------------

    /**
     * All-customers report should return 200 with the correct structure.
     */
    @Test
    void getAllCustomerRewards_validRequest_returns200() throws Exception {
        RewardsReportDTO report = RewardsReportDTO.builder()
                .periodStart("2024-01-01")
                .periodEnd("2024-03-31")
                .totalCustomers(2)
                .customers(List.of())
                .build();

        when(rewardsService.getRewardsForAllCustomers(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(report);

        mockMvc.perform(get("/api/v1/rewards/customers")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(2))
                .andExpect(jsonPath("$.periodStart").value("2024-01-01"));
    }

    /**
     * Invalid date range (start after end) should result in 400 Bad Request.
     */
    @Test
    void getAllCustomerRewards_startAfterEnd_returns400() throws Exception {
        when(rewardsService.getRewardsForAllCustomers(any(LocalDate.class), any(LocalDate.class)))
                .thenThrow(new InvalidDateRangeException("Start date must not be after end date."));

        mockMvc.perform(get("/api/v1/rewards/customers")
                        .param("startDate", "2024-03-31")
                        .param("endDate", "2024-01-01"))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/rewards/customers/last-three-months/{customerId}
    // -------------------------------------------------------------------------

    /**
     * Last-three-months endpoint should work without any date params and return 200.
     */
    @Test
    void getCustomerRewardsLastThreeMonths_validCustomer_returns200() throws Exception {
        CustomerRewardDTO dto = CustomerRewardDTO.builder()
                .customerId(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .totalPoints(50)
                .monthlyBreakdown(List.of())
                .build();

        when(rewardsService.getRewardsForCustomer(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dto);

        mockMvc.perform(get("/api/v1/rewards/customers/last-three-months/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1));
    }
}
