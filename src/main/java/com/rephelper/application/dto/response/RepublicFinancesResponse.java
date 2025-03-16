package com.rephelper.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for republic finances responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepublicFinancesResponse {
    private Long id;
    private UUID republicId;
    private String republicName;
    private BigDecimal currentBalance;
    private LocalDateTime lastUpdated;
}