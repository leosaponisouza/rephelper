package com.rephelper.domain.port.in;

import com.rephelper.domain.model.Income;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Port for the income service
 */
public interface IncomeServicePort {
    /**
     * Creates a new income
     */
    Income createIncome(Income income, UUID contributorId);

    /**
     * Updates an existing income
     */
    Income updateIncome(Long id, String description, BigDecimal amount,
                        LocalDateTime incomeDate, String source, UUID modifierId);

    /**
     * Gets an income by ID
     */
    Income getIncomeById(Long id);

    /**
     * Gets all incomes for a republic
     */
    List<Income> getIncomesByRepublicId(UUID republicId);

    /**
     * Gets incomes by date range for a republic
     */
    List<Income> getIncomesByRepublicIdAndDateRange(UUID republicId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Gets incomes by source for a republic
     */
    List<Income> getIncomesByRepublicIdAndSource(UUID republicId, String source);

    /**
     * Gets incomes contributed by a user
     */
    List<Income> getIncomesByContributorId(UUID contributorId);

    /**
     * Deletes an income
     */
    void deleteIncome(Long id, UUID deleterId);
}