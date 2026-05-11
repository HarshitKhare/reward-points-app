package com.example.reward_points_app.controller;

import com.example.reward_points_app.dto.CustomerRewardDTO;
import com.example.reward_points_app.dto.RewardsReportDTO;
import com.example.reward_points_app.service.RewardsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardsController {

    private final RewardsService rewardsService;

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<CustomerRewardDTO> getCustomerRewards(
            @PathVariable Long customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        CustomerRewardDTO result = rewardsService.getRewardsForCustomer(customerId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customers")
    public ResponseEntity<RewardsReportDTO> getAllCustomerRewards(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        RewardsReportDTO result = rewardsService.getRewardsForAllCustomers(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customers/last-three-months/{customerId}")
    public ResponseEntity<CustomerRewardDTO> getCustomerRewardsLastThreeMonths(
            @PathVariable Long customerId) {

        LocalDate endDate   = LocalDate.now().withDayOfMonth(1).minusDays(1);          // last day of prev month
        LocalDate startDate = endDate.withDayOfMonth(1).minusMonths(2);                // 1st of 3 months ago

        CustomerRewardDTO result = rewardsService.getRewardsForCustomer(customerId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/customers/last-three-months")
    public ResponseEntity<RewardsReportDTO> getAllCustomerRewardsLastThreeMonths() {

        LocalDate endDate   = LocalDate.now().withDayOfMonth(1).minusDays(1);
        LocalDate startDate = endDate.withDayOfMonth(1).minusMonths(2);

        RewardsReportDTO result = rewardsService.getRewardsForAllCustomers(startDate, endDate);
        return ResponseEntity.ok(result);
    }
}
