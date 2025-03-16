package com.rephelper.application.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for expense update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateExpenseRequest {
    private String description;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private LocalDate expenseDate;

    private String category;

    private String receiptUrl;
}