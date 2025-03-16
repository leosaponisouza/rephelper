package com.rephelper.domain.port.in;

import com.rephelper.domain.model.BudgetPlan;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Port for the budget plan service
 */
public interface BudgetPlanServicePort {
    /**
     * Creates or updates a budget plan
     */
    BudgetPlan createOrUpdateBudgetPlan(UUID republicId, Integer year, Integer month,
                                        String category, BigDecimal plannedAmount);

    /**
     * Gets a budget plan by ID
     */
    BudgetPlan getBudgetPlanById(Long id);

    /**
     * Gets all budget plans for a republic
     */
    List<BudgetPlan> getBudgetPlansByRepublicId(UUID republicId);

    /**
     * Gets budget plans for a specific month
     */
    List<BudgetPlan> getBudgetPlansByYearAndMonth(UUID republicId, Integer year, Integer month);

    /**
     * Gets budget plan for a specific category in a month
     */
    BudgetPlan getBudgetPlanByYearMonthAndCategory(UUID republicId, Integer year, Integer month, String category);

    /**
     * Deletes a budget plan
     */
    void deleteBudgetPlan(Long id, UUID deleterId);
}