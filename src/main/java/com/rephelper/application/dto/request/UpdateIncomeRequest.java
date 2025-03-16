package com.rephelper.application.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for income update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIncomeRequest {
    private String description;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private LocalDateTime incomeDate;

    private String source;
}