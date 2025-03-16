package com.rephelper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing the financial state of a republic
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepublicFinances {
    private Long id;
    private Republic republic;
    private BigDecimal currentBalance;
    private LocalDateTime lastUpdated;

    /**
     * Updates the balance by adding the specified amount
     * Use negative amount to decrease balance
     */
    public void updateBalance(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (this.currentBalance == null) {
            this.currentBalance = amount;
        } else {
            this.currentBalance = this.currentBalance.add(amount);
        }

        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Checks if there's enough balance for a withdrawal
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }

        return this.currentBalance != null && this.currentBalance.compareTo(amount) >= 0;
    }
}