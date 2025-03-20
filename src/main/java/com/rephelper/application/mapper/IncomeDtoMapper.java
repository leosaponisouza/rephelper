package com.rephelper.application.mapper;

import com.rephelper.application.dto.request.CreateIncomeRequest;
import com.rephelper.application.dto.request.UpdateIncomeRequest;
import com.rephelper.application.dto.response.IncomeResponse;
import com.rephelper.domain.model.Income;
import com.rephelper.domain.model.Republic;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Income DTOs
 */
@Component
public class IncomeDtoMapper {

    /**
     * Maps CreateIncomeRequest to Income domain object
     */
    public Income toIncome(CreateIncomeRequest request) {
        if (request == null) return null;

        return Income.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .incomeDate(request.getIncomeDate() != null ? request.getIncomeDate() : LocalDateTime.now())
                .source(request.getSource())
                .republic(Republic.builder().id(request.getRepublicId()).build())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Updates Income with data from UpdateIncomeRequest
     */
    public void updateIncomeFromRequest(Income income, UpdateIncomeRequest request) {
        if (income == null || request == null) return;

        income.updateDetails(
                request.getDescription(),
                request.getAmount(),
                request.getIncomeDate(),
                request.getSource()
        );
    }

    /**
     * Maps Income domain object to IncomeResponse
     */
    public IncomeResponse toIncomeResponse(Income income) {
        if (income == null) return null;

        return IncomeResponse.builder()
                .id(income.getId())
                .description(income.getDescription())
                .amount(income.getAmount())
                .incomeDate(income.getIncomeDate())
                .source(income.getSource())
                .republicId(income.getRepublic() != null ? income.getRepublic().getId() : null)
                .republicName(income.getRepublic() != null ? income.getRepublic().getName() : null)
                .contributorId(income.getContributor() != null ? income.getContributor().getId() : null)
                .contributorName(income.getContributor() != null ? ( income.getContributor().getNickname() != null ? income.getContributor().getNickname() : income.getContributor().getName()) : null)
                .contributorProfilePictureUrl(income.getContributor() != null ? income.getContributor().getProfilePictureUrl() : null)
                .createdAt(income.getCreatedAt())
                .build();
    }

    /**
     * Maps a list of Income domain objects to a list of IncomeResponse DTOs
     */
    public List<IncomeResponse> toIncomeResponseList(List<Income> incomes) {
        if (incomes == null) return null;

        return incomes.stream()
                .map(this::toIncomeResponse)
                .collect(Collectors.toList());
    }
}