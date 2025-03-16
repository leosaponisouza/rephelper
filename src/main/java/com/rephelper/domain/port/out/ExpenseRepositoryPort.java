package com.rephelper.domain.port.out;

import com.rephelper.domain.model.Expense;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for the expense repository
 */
public interface ExpenseRepositoryPort {
    /**
     * Saves an expense
     */
    Expense save(Expense expense);

    /**
     * Finds an expense by its ID
     */
    Optional<Expense> findById(Long id);

    /**
     * Finds all expenses
     */
    List<Expense> findAll();

    /**
     * Finds expenses by republic ID
     */
    List<Expense> findByRepublicId(UUID republicId);

    /**
     * Finds expenses by creator ID
     */
    List<Expense> findByCreatorId(UUID creatorId);

    /**
     * Finds expenses by status
     */
    List<Expense> findByStatus(Expense.ExpenseStatus status);

    /**
     * Finds expenses by republic ID and status
     */
    List<Expense> findByRepublicIdAndStatus(UUID republicId, Expense.ExpenseStatus status);

    /**
     * Finds expenses by republic ID and date range
     */
    List<Expense> findByRepublicIdAndDateRange(UUID republicId, LocalDate startDate, LocalDate endDate);

    /**
     * Finds expenses by republic ID and category
     */
    List<Expense> findByRepublicIdAndCategory(UUID republicId, String category);

    /**
     * Deletes an expense
     */
    void delete(Expense expense);
}