package com.example.reward_points_app.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardsReportDTO {
    private String periodStart;
    private String periodEnd;
    private int totalCustomers;
    private List<CustomerRewardDTO> customers;
}
