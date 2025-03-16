package com.rephelper.application.mapper;

import com.rephelper.application.dto.request.CreateExpenseRequest;
import com.rephelper.application.dto.request.UpdateExpenseRequest;
import com.rephelper.application.dto.response.ExpenseResponse;
import com.rephelper.domain.model.Expense;
import com.rephelper.domain.model.Republic;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Expense DTOs
 */
@Component
public class ExpenseDtoMapper {

    /**
     * Maps CreateExpenseRequest to Expense domain object
     */
    public Expense toExpense(CreateExpenseRequest request) {
        if (request == null) return null;

        return Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .expenseDate(request.getExpenseDate())
                .category(request.getCategory())
                .receiptUrl(request.getReceiptUrl())
                .republic(Republic.builder().id(request.getRepublicId()).build())
                .status(Expense.ExpenseStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates Expense with data from UpdateExpenseRequest
     */
    public void updateExpenseFromRequest(Expense expense, UpdateExpenseRequest request) {
        if (expense == null || request == null) return;

        expense.updateDetails(
                request.getDescription(),
                request.getAmount(),
                request.getExpenseDate(),
                request.getCategory(),
                request.getReceiptUrl()
        );
    }

    /**
     * Maps Expense domain object to ExpenseResponse
     */
    public ExpenseResponse toExpenseResponse(Expense expense) {
        if (expense == null) return null;

        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .category(expense.getCategory())
                .receiptUrl(expense.getReceiptUrl())
                .status(expense.getStatus())
                .approvalDate(expense.getApprovalDate())
                .reimbursementDate(expense.getReimbursementDate())
                .rejectionReason(expense.getRejectionReason())
                .republicId(expense.getRepublic() != null ? expense.getRepublic().getId() : null)
                .republicName(expense.getRepublic() != null ? expense.getRepublic().getName() : null)
                .creatorId(expense.getCreator() != null ? expense.getCreator().getId() : null)
                .creatorName(expense.getCreator() != null ? expense.getCreator().getName() : null)
                .creatorProfilePictureUrl(expense.getCreator() != null ? expense.getCreator().getProfilePictureUrl() : null)
                .createdAt(expense.getCreatedAt())
                .build();
    }

    /**
     * Maps a list of Expense domain objects to a list of ExpenseResponse DTOs
     */
    public List<ExpenseResponse> toExpenseResponseList(List<Expense> expenses) {
        if (expenses == null) return null;

        return expenses.stream()
                .map(this::toExpenseResponse)
                .collect(Collectors.toList());
    }
}