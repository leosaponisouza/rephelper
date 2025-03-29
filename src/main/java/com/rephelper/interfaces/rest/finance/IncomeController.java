package com.rephelper.interfaces.rest.finance;

import com.rephelper.application.dto.request.CreateIncomeRequest;
import com.rephelper.application.dto.request.UpdateIncomeRequest;
import com.rephelper.application.dto.response.ApiResponse;
import com.rephelper.application.dto.response.IncomeResponse;
import com.rephelper.application.mapper.IncomeDtoMapper;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.Income;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.IncomeServicePort;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/incomes")
@RequiredArgsConstructor
@Tag(name = "Incomes", description = "Income management endpoints")
public class IncomeController {

    private final IncomeServicePort incomeService;
    private final UserServicePort userService;
    private final IncomeDtoMapper incomeDtoMapper;

    @PostMapping
    @Operation(summary = "Create a new income", description = "Creates a new income for a republic")
    public ResponseEntity<IncomeResponse> createIncome(
            @Valid @RequestBody CreateIncomeRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Convert request to domain object
        Income income = incomeDtoMapper.toIncome(request);

        // Create income
        Income createdIncome = incomeService.createIncome(income, currentUser.getUserId());

        return new ResponseEntity<>(incomeDtoMapper.toIncomeResponse(createdIncome), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all incomes for a republic", description = "Retrieves all incomes for the specified republic")
    public ResponseEntity<List<IncomeResponse>> getAllIncomes(
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
            throw new ValidationException("You can only view incomes for your own republic");
        }

        // Get incomes
        List<Income> incomes = incomeService.getIncomesByRepublicId(republicId);

        return ResponseEntity.ok(incomeDtoMapper.toIncomeResponseList(incomes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get income by ID", description = "Retrieves income details by ID")
    public ResponseEntity<IncomeResponse> getIncomeById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Income income = incomeService.getIncomeById(id);

        // Validate user has access to this income
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        if (!income.getRepublic().getId().equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view incomes for your own republic");
        }

        return ResponseEntity.ok(incomeDtoMapper.toIncomeResponse(income));
    }

    @GetMapping("/daterange")
    @Operation(summary = "Get incomes by date range", description = "Retrieves incomes by date range for a republic")
    public ResponseEntity<List<IncomeResponse>> getIncomesByDateRange(
            @RequestParam(required = true) UUID republicId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view incomes for your own republic");
        }

        // Get incomes by date range
        List<Income> incomes = incomeService.getIncomesByRepublicIdAndDateRange(republicId, startDate, endDate);

        return ResponseEntity.ok(incomeDtoMapper.toIncomeResponseList(incomes));
    }

    @GetMapping("/source/{source}")
    @Operation(summary = "Get incomes by source", description = "Retrieves incomes by source for a republic")
    public ResponseEntity<List<IncomeResponse>> getIncomesBySource(
            @PathVariable String source,
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
            throw new ValidationException("You can only view incomes for your own republic");
        }

        // Get incomes by source
        List<Income> incomes = incomeService.getIncomesByRepublicIdAndSource(republicId, source);

        return ResponseEntity.ok(incomeDtoMapper.toIncomeResponseList(incomes));
    }

    @GetMapping("/mine")
    @Operation(summary = "Get my contributions", description = "Retrieves incomes contributed by the current user")
    public ResponseEntity<List<IncomeResponse>> getMyContributions(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get incomes contributed by current user
        List<Income> incomes = incomeService.getIncomesByContributorId(currentUser.getUserId());

        return ResponseEntity.ok(incomeDtoMapper.toIncomeResponseList(incomes));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update income", description = "Updates income details")
    public ResponseEntity<IncomeResponse> updateIncome(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIncomeRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Update income
        Income updatedIncome = incomeService.updateIncome(
                id,
                request.getDescription(),
                request.getAmount(),
                request.getIncomeDate(),
                request.getSource(),
                currentUser.getUserId());

        return ResponseEntity.ok(incomeDtoMapper.toIncomeResponse(updatedIncome));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete income", description = "Deletes an income")
    public ResponseEntity<ApiResponse> deleteIncome(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Delete income
        incomeService.deleteIncome(id, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("Income deleted successfully")
                .build());
    }
}