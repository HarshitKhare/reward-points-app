package com.example.reward_points_app.controller;

import com.example.reward_points_app.dto.CustomerRewardDTO;
import com.example.reward_points_app.dto.RewardsReportDTO;
import com.example.reward_points_app.service.RewardsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST controller exposing endpoints to query customer reward points.
 * All endpoints are prefixed with {@code /api/v1/rewards}.
 */
@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardsController {

    private final RewardsService rewardsService;

    /**
     * Returns reward points for a single customer within a custom date range.
     *
     * @param customerId the customer's ID
     * @param startDate  start of the period (ISO format: yyyy-MM-dd)
     * @param endDate    end of the period (ISO format: yyyy-MM-dd)
     * @return 200 with a {@link CustomerRewardDTO}, or 404 if the customer / transactions are not found
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerRewardDTO> getCustomerRewards(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        CustomerRewardDTO result = rewardsService.getRewardsForCustomer(customerId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    /**
     * Returns reward points for all customers within a custom date range.
     *
     * @param startDate start of the period (ISO format: yyyy-MM-dd)
     * @param endDate   end of the period (ISO format: yyyy-MM-dd)
     * @return 200 with a {@link RewardsReportDTO}
     */
    @GetMapping("/customers")
    public ResponseEntity<RewardsReportDTO> getAllCustomerRewards(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        RewardsReportDTO result = rewardsService.getRewardsForAllCustomers(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    /**
     * Returns reward points for a single customer covering the last three complete months.
     * The period runs from the 1st of three months ago through the last day of the previous month.
     *
     * @param customerId the customer's ID
     * @return 200 with a {@link CustomerRewardDTO}
     */
    @GetMapping("/customers/last-three-months/{customerId}")
    public ResponseEntity<CustomerRewardDTO> getCustomerRewardsLastThreeMonths(
            @PathVariable Long customerId) {

        LocalDate endDate   = LocalDate.now().withDayOfMonth(1).minusDays(1);          // last day of prev month
        LocalDate startDate = endDate.withDayOfMonth(1).minusMonths(2);                // 1st of 3 months ago

        CustomerRewardDTO result = rewardsService.getRewardsForCustomer(customerId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    /**
     * Returns reward points for all customers covering the last three complete months.
     *
     * @return 200 with a {@link RewardsReportDTO}
     */
    @GetMapping("/customers/last-three-months")
    public ResponseEntity<RewardsReportDTO> getAllCustomerRewardsLastThreeMonths() {

        LocalDate endDate   = LocalDate.now().withDayOfMonth(1).minusDays(1);
        LocalDate startDate = endDate.withDayOfMonth(1).minusMonths(2);

        RewardsReportDTO result = rewardsService.getRewardsForAllCustomers(startDate, endDate);
        return ResponseEntity.ok(result);
    }
}
