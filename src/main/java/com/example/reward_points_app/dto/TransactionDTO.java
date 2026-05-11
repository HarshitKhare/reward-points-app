package com.example.reward_points_app.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private Long id;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
    private int pointsEarned;
}
