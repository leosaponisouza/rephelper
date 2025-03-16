package com.rephelper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an income entry for a republic
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Income {
    private Long id;
    private Republic republic;
    private User contributor;
    private String description;
    private BigDecimal amount;
    private LocalDateTime incomeDate;
    private String source;
    private LocalDateTime createdAt;

    /**
     * Updates income details
     */
    public void updateDetails(String description, BigDecimal amount, LocalDateTime incomeDate, String source) {
        if (description != null && !description.isBlank()) {
            this.description = description;
        }

        if (amount != null) {
            this.amount = amount;
        }

        if (incomeDate != null) {
            this.incomeDate = incomeDate;
        }

        if (source != null && !source.isBlank()) {
            this.source = source;
        }
    }
}