package com.rephelper.application.dto.response;

import com.rephelper.domain.model.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for expense responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String category;
    private String receiptUrl;
    private Expense.ExpenseStatus status;
    private LocalDateTime approvalDate;
    private LocalDateTime reimbursementDate;
    private String rejectionReason;
    private UUID republicId;
    private String republicName;
    private UUID creatorId;
    private String creatorName;
    private String creatorProfilePictureUrl;
    private LocalDateTime createdAt;
}