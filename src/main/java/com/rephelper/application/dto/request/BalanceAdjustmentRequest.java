package com.rephelper.application.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for balance adjustment requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceAdjustmentRequest {
    
    @NotNull(message = "Amount is required")
    private BigDecimal amount;
    
    private String description;
} 