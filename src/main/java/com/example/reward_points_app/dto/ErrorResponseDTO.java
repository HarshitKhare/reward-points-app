package com.example.reward_points_app.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponseDTO {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
