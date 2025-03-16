package com.rephelper.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for income creation requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIncomeRequest {
    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private LocalDateTime incomeDate;

    @NotBlank(message = "Source is required")
    private String source;

    @NotNull(message = "Republic ID is required")
    private UUID republicId;
}