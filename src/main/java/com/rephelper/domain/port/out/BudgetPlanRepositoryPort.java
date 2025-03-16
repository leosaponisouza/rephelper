package com.rephelper.domain.port.out;

import com.rephelper.domain.model.BudgetPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for the budget plan repository
 */
public interface BudgetPlanRepositoryPort {
    /**
     * Saves a budget plan
     */
    BudgetPlan save(BudgetPlan budgetPlan);

    /**
     * Finds a budget plan by its ID
     */
    Optional<BudgetPlan> findById(Long id);

    /**
     * Finds all budget plans
     */
    List<BudgetPlan> findAll();

    /**
     * Finds budget plans by republic ID
     */
    List<BudgetPlan> findByRepublicId(UUID republicId);

    /**
     * Finds budget plans by republic ID, year and month
     */
    List<BudgetPlan> findByRepublicIdAndYearAndMonth(UUID republicId, Integer year, Integer month);

    /**
     * Finds a specific budget plan by republic, year, month and category
     */
    Optional<BudgetPlan> findByRepublicIdAndYearAndMonthAndCategory(UUID republicId, Integer year, Integer month, String category);

    /**
     * Deletes a budget plan
     */
    void delete(BudgetPlan budgetPlan);
}