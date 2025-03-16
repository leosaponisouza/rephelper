package com.rephelper.domain.service;

import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.BudgetPlan;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.BudgetPlanServicePort;
import com.rephelper.domain.port.out.BudgetPlanRepositoryPort;
import com.rephelper.domain.port.out.RepublicRepositoryPort;
import com.rephelper.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BudgetPlanServiceImpl implements BudgetPlanServicePort {

    private final BudgetPlanRepositoryPort budgetPlanRepository;
    private final RepublicRepositoryPort republicRepository;
    private final UserRepositoryPort userRepository;

    @Override
    public BudgetPlan createOrUpdateBudgetPlan(UUID republicId, Integer year, Integer month,
                                               String category, BigDecimal plannedAmount) {
        // Validate inputs
        if (year == null || year <= 0) {
            throw new ValidationException("Year must be a positive number");
        }

        if (month == null || month < 1 || month > 12) {
            throw new ValidationException("Month must be between 1 and 12");
        }

        if (category == null || category.isBlank()) {
            throw new ValidationException("Category is required");
        }

        if (plannedAmount == null) {
            throw new ValidationException("Planned amount is required");
        }

        // Get republic
        Republic republic = republicRepository.findById(republicId)
                .orElseThrow(() -> new ResourceNotFoundException("Republic not found with id: " + republicId));

        // Check if budget plan already exists for this category in this month
        Optional<BudgetPlan> existingPlan = budgetPlanRepository.findByRepublicIdAndYearAndMonthAndCategory(
                republicId, year, month, category);

        if (existingPlan.isPresent()) {
            // Update existing plan
            BudgetPlan plan = existingPlan.get();
            plan.updateDetails(year, month, category, plannedAmount);
            return budgetPlanRepository.save(plan);
        } else {
            // Create new plan
            BudgetPlan newPlan = BudgetPlan.builder()
                    .republic(republic)
                    .year(year)
                    .month(month)
                    .category(category)
                    .plannedAmount(plannedAmount)
                    .createdAt(LocalDateTime.now())
                    .build();

            return budgetPlanRepository.save(newPlan);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetPlan getBudgetPlanById(Long id) {
        return budgetPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget plan not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetPlan> getBudgetPlansByRepublicId(UUID republicId) {
        // Verify republic exists
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return budgetPlanRepository.findByRepublicId(republicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetPlan> getBudgetPlansByYearAndMonth(UUID republicId, Integer year, Integer month) {
        // Verify republic exists
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        // Validate inputs
        if (year == null || year <= 0) {
            throw new ValidationException("Year must be a positive number");
        }

        if (month == null || month < 1 || month > 12) {
            throw new ValidationException("Month must be between 1 and 12");
        }

        return budgetPlanRepository.findByRepublicIdAndYearAndMonth(republicId, year, month);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetPlan getBudgetPlanByYearMonthAndCategory(UUID republicId, Integer year, Integer month, String category) {
        // Verify republic exists
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        // Validate inputs
        if (year == null || year <= 0) {
            throw new ValidationException("Year must be a positive number");
        }

        if (month == null || month < 1 || month > 12) {
            throw new ValidationException("Month must be between 1 and 12");
        }

        if (category == null || category.isBlank()) {
            throw new ValidationException("Category is required");
        }

        return budgetPlanRepository.findByRepublicIdAndYearAndMonthAndCategory(republicId, year, month, category)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Budget plan not found for republic " + republicId + ", year " + year +
                                ", month " + month + ", and category " + category));
    }

    @Override
    public void deleteBudgetPlan(Long id, UUID deleterId) {
        // Get budget plan
        BudgetPlan budgetPlan = getBudgetPlanById(id);

        // Validate user
        User deleter = userRepository.findById(deleterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + deleterId));

        // Check if user is admin of the republic
        boolean isRepublicAdmin = deleter.getCurrentRepublic() != null &&
                deleter.getCurrentRepublic().getId().equals(budgetPlan.getRepublic().getId()) &&
                deleter.isRepublicAdmin();

        if (!isRepublicAdmin) {
            throw new ForbiddenException("You do not have permission to delete budget plans");
        }

        // Delete budget plan
        budgetPlanRepository.delete(budgetPlan);
    }
}