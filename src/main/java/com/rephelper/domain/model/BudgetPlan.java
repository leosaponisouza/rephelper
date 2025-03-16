package com.rephelper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a budget plan for a specific month
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetPlan {
    private Long id;
    private Republic republic;
    private Integer year;
    private Integer month;
    private String category;
    private BigDecimal plannedAmount;
    private LocalDateTime createdAt;

    /**
     * Updates budget plan details
     */
    public void updateDetails(Integer year, Integer month, String category, BigDecimal plannedAmount) {
        if (year != null && year > 0) {
            this.year = year;
        }

        if (month != null && month >= 1 && month <= 12) {
            this.month = month;
        }

        if (category != null && !category.isBlank()) {
            this.category = category;
        }

        if (plannedAmount != null) {
            this.plannedAmount = plannedAmount;
        }
    }
}