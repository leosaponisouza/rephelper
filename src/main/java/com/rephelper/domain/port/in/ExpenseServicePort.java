package com.rephelper.domain.port.in;

import com.rephelper.domain.model.Expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Port for the expense service
 */

public interface ExpenseServicePort {
    /**
     * Creates a new expense
     */
    Expense createExpense(Expense expense, UUID creatorId);

    /**
     * Updates an existing expense
     */
    Expense updateExpense(Long id, String description, BigDecimal amount,
                          LocalDate expenseDate, String category,
                          String receiptUrl, UUID modifierId);

    /**
     * Gets an expense by ID
     */
    Expense getExpenseById(Long id);

    /**
     * Gets all expenses for a republic
     */
    List<Expense> getExpensesByRepublicId(UUID republicId);

    /**
     * Gets expenses by status for a republic
     */
    List<Expense> getExpensesByRepublicIdAndStatus(UUID republicId, Expense.ExpenseStatus status);

    /**
     * Gets expenses by date range for a republic
     */
    List<Expense> getExpensesByRepublicIdAndDateRange(UUID republicId, LocalDate startDate, LocalDate endDate);

    /**
     * Gets expenses by category for a republic
     */
    List<Expense> getExpensesByRepublicIdAndCategory(UUID republicId, String category);

    /**
     * Gets expenses created by a user
     */
    List<Expense> getExpensesByCreatorId(UUID creatorId);

    /**
     * Approves an expense
     */
    Expense approveExpense(Long id, UUID approverId);

    /**
     * Rejects an expense
     */
    Expense rejectExpense(Long id, String reason, UUID rejecterId);

    /**
     * Marks an expense as reimbursed
     */
    Expense reimburseExpense(Long id, UUID reimburserId);

    /**
     * Resets a rejected expense to pending
     */
    Expense resetExpenseToPending(Long id, UUID modifierId);

    /**
     * Deletes an expense
     */
    void deleteExpense(Long id, UUID deleterId);
}