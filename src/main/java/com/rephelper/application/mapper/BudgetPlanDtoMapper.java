package com.rephelper.application.mapper;

import com.rephelper.application.dto.request.CreateBudgetPlanRequest;
import com.rephelper.application.dto.response.BudgetPlanResponse;
import com.rephelper.domain.model.BudgetPlan;
import com.rephelper.domain.model.Republic;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Budget Plan DTOs
 */
@Component
public class BudgetPlanDtoMapper {

    /**
     * Maps CreateBudgetPlanRequest to BudgetPlan domain object
     */
    public BudgetPlan toBudgetPlan(CreateBudgetPlanRequest request) {
        if (request == null) return null;

        return BudgetPlan.builder()
                .year(request.getYear())
                .month(request.getMonth())
                .category(request.getCategory())
                .plannedAmount(request.getPlannedAmount())
                .republic(Republic.builder().id(request.getRepublicId()).build())
                .build();
    }

    /**
     * Maps BudgetPlan domain object to BudgetPlanResponse
     */
    public BudgetPlanResponse toBudgetPlanResponse(BudgetPlan budgetPlan) {
        if (budgetPlan == null) return null;

        return BudgetPlanResponse.builder()
                .id(budgetPlan.getId())
                .republicId(budgetPlan.getRepublic() != null ? budgetPlan.getRepublic().getId() : null)
                .republicName(budgetPlan.getRepublic() != null ? budgetPlan.getRepublic().getName() : null)
                .year(budgetPlan.getYear())
                .month(budgetPlan.getMonth())
                .category(budgetPlan.getCategory())
                .plannedAmount(budgetPlan.getPlannedAmount())
                .createdAt(budgetPlan.getCreatedAt())
                .build();
    }

    /**
     * Maps a list of BudgetPlan domain objects to a list of BudgetPlanResponse DTOs
     */
    public List<BudgetPlanResponse> toBudgetPlanResponseList(List<BudgetPlan> budgetPlans) {
        if (budgetPlans == null) return null;

        return budgetPlans.stream()
                .map(this::toBudgetPlanResponse)
                .collect(Collectors.toList());
    }
}