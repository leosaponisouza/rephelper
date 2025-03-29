package com.rephelper.interfaces.rest.finance;

import com.rephelper.application.dto.request.CreateBudgetPlanRequest;
import com.rephelper.application.dto.response.ApiResponse;
import com.rephelper.application.dto.response.BudgetPlanResponse;
import com.rephelper.application.mapper.BudgetPlanDtoMapper;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.BudgetPlan;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.BudgetPlanServicePort;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/budgetplans")
@RequiredArgsConstructor
@Tag(name = "Budget Plans", description = "Budget plan management endpoints")
public class BudgetPlanController {

    private final BudgetPlanServicePort budgetPlanService;
    private final UserServicePort userService;
    private final BudgetPlanDtoMapper budgetPlanDtoMapper;

    @PostMapping
    @Operation(summary = "Create or update a budget plan", description = "Creates or updates a budget plan for a specific month and category")
    public ResponseEntity<BudgetPlanResponse> createOrUpdateBudgetPlan(
            @Valid @RequestBody CreateBudgetPlanRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!request.getRepublicId().equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only create budget plans for your own republic");
        }

        // Create or update budget plan
        BudgetPlan budgetPlan = budgetPlanService.createOrUpdateBudgetPlan(
                request.getRepublicId(),
                request.getYear(),
                request.getMonth(),
                request.getCategory(),
                request.getPlannedAmount());

        return new ResponseEntity<>(budgetPlanDtoMapper.toBudgetPlanResponse(budgetPlan), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all budget plans for a republic", description = "Retrieves all budget plans for the specified republic")
    public ResponseEntity<List<BudgetPlanResponse>> getAllBudgetPlans(
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
            throw new ValidationException("You can only view budget plans for your own republic");
        }

        // Get budget plans
        List<BudgetPlan> budgetPlans = budgetPlanService.getBudgetPlansByRepublicId(republicId);

        return ResponseEntity.ok(budgetPlanDtoMapper.toBudgetPlanResponseList(budgetPlans));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get budget plan by ID", description = "Retrieves budget plan details by ID")
    public ResponseEntity<BudgetPlanResponse> getBudgetPlanById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        BudgetPlan budgetPlan = budgetPlanService.getBudgetPlanById(id);

        // Validate user has access to this budget plan
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        if (!budgetPlan.getRepublic().getId().equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view budget plans for your own republic");
        }

        return ResponseEntity.ok(budgetPlanDtoMapper.toBudgetPlanResponse(budgetPlan));
    }

    @GetMapping("/month")
    @Operation(summary = "Get budget plans by month", description = "Retrieves all budget plans for the specified month")
    public ResponseEntity<List<BudgetPlanResponse>> getBudgetPlansByMonth(
            @RequestParam(required = true) UUID republicId,
            @RequestParam(required = true) Integer year,
            @RequestParam(required = true) Integer month,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view budget plans for your own republic");
        }

        // Get budget plans for month
        List<BudgetPlan> budgetPlans = budgetPlanService.getBudgetPlansByYearAndMonth(republicId, year, month);

        return ResponseEntity.ok(budgetPlanDtoMapper.toBudgetPlanResponseList(budgetPlans));
    }

    @GetMapping("/category")
    @Operation(summary = "Get budget plan by category", description = "Retrieves budget plan for the specified category")
    public ResponseEntity<BudgetPlanResponse> getBudgetPlanByCategory(
            @RequestParam(required = true) UUID republicId,
            @RequestParam(required = true) Integer year,
            @RequestParam(required = true) Integer month,
            @RequestParam(required = true) String category,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view budget plans for your own republic");
        }

        // Get budget plan for category
        BudgetPlan budgetPlan = budgetPlanService.getBudgetPlanByYearMonthAndCategory(republicId, year, month, category);

        return ResponseEntity.ok(budgetPlanDtoMapper.toBudgetPlanResponse(budgetPlan));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget plan", description = "Deletes a budget plan")
    public ResponseEntity<ApiResponse> deleteBudgetPlan(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Delete budget plan
        budgetPlanService.deleteBudgetPlan(id, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("Budget plan deleted successfully")
                .build());
    }
}