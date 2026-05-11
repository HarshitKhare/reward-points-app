package com.example.reward_points_app.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRewardDTO {
    private String month;
    private int year;
    private int monthNumber;
    private int points;
    private List<TransactionDTO> transactions;
}
