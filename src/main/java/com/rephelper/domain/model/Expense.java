package com.rephelper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing an expense in a republic
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    private Long id;
    private Republic republic;
    private User creator;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String category;
    private String receiptUrl;
    private ExpenseStatus status;
    private LocalDateTime approvalDate;
    private LocalDateTime reimbursementDate;
    private String rejectionReason;
    private LocalDateTime createdAt;

    /**
     * Possible statuses for an expense
     */
    public enum ExpenseStatus {
        PENDING, APPROVED, REJECTED, REIMBURSED
    }

    /**
     * Updates expense details
     */
    public void updateDetails(String description, BigDecimal amount, LocalDate expenseDate,
                              String category, String receiptUrl) {
        if (description != null && !description.isBlank()) {
            this.description = description;
        }

        if (amount != null) {
            this.amount = amount;
        }

        if (expenseDate != null) {
            this.expenseDate = expenseDate;
        }

        if (category != null) {
            this.category = category;
        }

        if (receiptUrl != null) {
            this.receiptUrl = receiptUrl;
        }
    }

    /**
     * Approves the expense
     */
    public void approve() {
        if (this.status == ExpenseStatus.PENDING) {
            this.status = ExpenseStatus.APPROVED;
            this.approvalDate = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Only pending expenses can be approved");
        }
    }

    /**
     * Rejects the expense with a reason
     */
    public void reject(String reason) {
        if (this.status == ExpenseStatus.PENDING) {
            this.status = ExpenseStatus.REJECTED;
            this.rejectionReason = reason;
        } else {
            throw new IllegalStateException("Only pending expenses can be rejected");
        }
    }

    /**
     * Marks the expense as reimbursed
     */
    public void reimburse() {
        if (this.status == ExpenseStatus.APPROVED) {
            this.status = ExpenseStatus.REIMBURSED;
            this.reimbursementDate = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Only approved expenses can be reimbursed");
        }
    }

    /**
     * Reset a rejected expense to pending status
     */
    public void resetToPending() {
        if (this.status == ExpenseStatus.REJECTED) {
            this.status = ExpenseStatus.PENDING;
            this.rejectionReason = null;
        } else {
            throw new IllegalStateException("Only rejected expenses can be reset to pending");
        }
    }
}