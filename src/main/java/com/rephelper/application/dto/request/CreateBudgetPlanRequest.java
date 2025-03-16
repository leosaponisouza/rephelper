package com.rephelper.application.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for budget plan creation/update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBudgetPlanRequest {
    @NotNull(message = "Republic ID is required")
    private UUID republicId;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be 2000 or later")
    private Integer year;

    @NotNull(message = "Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer month;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Planned amount is required")
    @Positive(message = "Planned amount must be positive")
    private BigDecimal plannedAmount;
}