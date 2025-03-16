package com.rephelper.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for budget plan responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetPlanResponse {
    private Long id;
    private UUID republicId;
    private String republicName;
    private Integer year;
    private Integer month;
    private String category;
    private BigDecimal plannedAmount;
    private LocalDateTime createdAt;
}