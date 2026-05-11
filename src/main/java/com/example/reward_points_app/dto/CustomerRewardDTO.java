package com.example.reward_points_app.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerRewardDTO {
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private List<MonthlyRewardDTO> monthlyBreakdown;
    private int totalPoints;
}
