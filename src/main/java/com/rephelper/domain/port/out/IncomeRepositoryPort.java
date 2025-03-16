package com.rephelper.domain.port.out;

import com.rephelper.domain.model.Income;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for the income repository
 */
public interface IncomeRepositoryPort {
    /**
     * Saves an income
     */
    Income save(Income income);

    /**
     * Finds an income by its ID
     */
    Optional<Income> findById(Long id);

    /**
     * Finds all incomes
     */
    List<Income> findAll();

    /**
     * Finds incomes by republic ID
     */
    List<Income> findByRepublicId(UUID republicId);

    /**
     * Finds incomes by contributor ID
     */
    List<Income> findByContributorId(UUID contributorId);

    /**
     * Finds incomes by republic ID and date range
     */
    List<Income> findByRepublicIdAndDateRange(UUID republicId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds incomes by republic ID and source
     */
    List<Income> findByRepublicIdAndSource(UUID republicId, String source);

    /**
     * Deletes an income
     */
    void delete(Income income);
}