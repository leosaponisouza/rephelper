package com.rephelper.interfaces.rest.finance;

import com.rephelper.application.dto.request.CreateExpenseRequest;
import com.rephelper.application.dto.request.RejectExpenseRequest;
import com.rephelper.application.dto.request.UpdateExpenseRequest;
import com.rephelper.application.dto.response.ApiResponse;
import com.rephelper.application.dto.response.ExpenseResponse;
import com.rephelper.application.mapper.ExpenseDtoMapper;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.Expense;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.ExpenseServicePort;
import com.rephelper.domain.port.in.UserServicePort;
import com.rephelper.infrastructure.adapter.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management endpoints")
public class ExpenseController {

    private final ExpenseServicePort expenseService;
    private final UserServicePort userService;
    private final ExpenseDtoMapper expenseDtoMapper;

    @PostMapping
    @Operation(summary = "Create a new expense", description = "Creates a new expense for a republic")
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Convert request to domain object
        Expense expense = expenseDtoMapper.toExpense(request);

        // Create expense
        Expense createdExpense = expenseService.createExpense(expense, currentUser.getUserId());

        return new ResponseEntity<>(expenseDtoMapper.toExpenseResponse(createdExpense), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all expenses for a republic", description = "Retrieves all expenses for the specified republic")
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses(
            @RequestParam(required = true) UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view expenses for your own republic");
        }

        // Get expenses
        List<Expense> expenses = expenseService.getExpensesByRepublicId(republicId);

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponseList(expenses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get expense by ID", description = "Retrieves expense details by ID")
    public ResponseEntity<ExpenseResponse> getExpenseById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Expense expense = expenseService.getExpenseById(id);

        // Validate user has access to this expense
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        if (!expense.getRepublic().getId().equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view expenses for your own republic");
        }

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponse(expense));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get expenses by status", description = "Retrieves expenses by status for a republic")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByStatus(
            @PathVariable Expense.ExpenseStatus status,
            @RequestParam(required = true) UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view expenses for your own republic");
        }

        // Get expenses by status
        List<Expense> expenses = expenseService.getExpensesByRepublicIdAndStatus(republicId, status);

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponseList(expenses));
    }

    @GetMapping("/daterange")
    @Operation(summary = "Get expenses by date range", description = "Retrieves expenses by date range for a republic")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByDateRange(
            @RequestParam(required = true) UUID republicId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view expenses for your own republic");
        }

        // Get expenses by date range
        List<Expense> expenses = expenseService.getExpensesByRepublicIdAndDateRange(republicId, startDate, endDate);

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponseList(expenses));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get expenses by category", description = "Retrieves expenses by category for a republic")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByCategory(
            @PathVariable String category,
            @RequestParam(required = true) UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view expenses for your own republic");
        }

        // Get expenses by category
        List<Expense> expenses = expenseService.getExpensesByRepublicIdAndCategory(republicId, category);

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponseList(expenses));
    }

    @GetMapping("/mine")
    @Operation(summary = "Get my expenses", description = "Retrieves expenses created by the current user")
    public ResponseEntity<List<ExpenseResponse>> getMyExpenses(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get expenses created by current user
        List<Expense> expenses = expenseService.getExpensesByCreatorId(currentUser.getUserId());

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponseList(expenses));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update expense", description = "Updates expense details")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody UpdateExpenseRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Update expense
        Expense updatedExpense = expenseService.updateExpense(
                id,
                request.getDescription(),
                request.getAmount(),
                request.getExpenseDate(),
                request.getCategory(),
                request.getReceiptUrl(),
                currentUser.getUserId());

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponse(updatedExpense));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve expense", description = "Approves an expense")
    public ResponseEntity<ExpenseResponse> approveExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Approve expense
        Expense approvedExpense = expenseService.approveExpense(id, currentUser.getUserId());

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponse(approvedExpense));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject expense", description = "Rejects an expense")
    public ResponseEntity<ExpenseResponse> rejectExpense(
            @PathVariable Long id,
            @Valid @RequestBody RejectExpenseRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Reject expense
        Expense rejectedExpense = expenseService.rejectExpense(id, request.getReason(), currentUser.getUserId());

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponse(rejectedExpense));
    }

    @PostMapping("/{id}/reimburse")
    @Operation(summary = "Reimburse expense", description = "Marks an expense as reimbursed")
    public ResponseEntity<ExpenseResponse> reimburseExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Reimburse expense
        Expense reimbursedExpense = expenseService.reimburseExpense(id, currentUser.getUserId());

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponse(reimbursedExpense));
    }

    @PostMapping("/{id}/reset")
    @Operation(summary = "Reset to pending", description = "Resets a rejected expense to pending status")
    public ResponseEntity<ExpenseResponse> resetToPending(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Reset expense to pending
        Expense resetExpense = expenseService.resetExpenseToPending(id, currentUser.getUserId());

        return ResponseEntity.ok(expenseDtoMapper.toExpenseResponse(resetExpense));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete expense", description = "Deletes an expense")
    public ResponseEntity<ApiResponse> deleteExpense(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Delete expense
        expenseService.deleteExpense(id, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("Expense deleted successfully")
                .build());
    }
}