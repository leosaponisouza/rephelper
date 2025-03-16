package com.rephelper.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for income responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDateTime incomeDate;
    private String source;
    private UUID republicId;
    private String republicName;
    private UUID contributorId;
    private String contributorName;
    private String contributorProfilePictureUrl;
    private LocalDateTime createdAt;
}