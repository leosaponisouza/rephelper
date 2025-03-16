package com.rephelper.application.mapper;

import com.rephelper.application.dto.response.RepublicFinancesResponse;
import com.rephelper.domain.model.RepublicFinances;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Republic Finances DTOs
 */
@Component
public class RepublicFinancesDtoMapper {

    /**
     * Maps RepublicFinances domain object to RepublicFinancesResponse
     */
    public RepublicFinancesResponse toRepublicFinancesResponse(RepublicFinances finances) {
        if (finances == null) return null;

        return RepublicFinancesResponse.builder()
                .id(finances.getId())
                .republicId(finances.getRepublic() != null ? finances.getRepublic().getId() : null)
                .republicName(finances.getRepublic() != null ? finances.getRepublic().getName() : null)
                .currentBalance(finances.getCurrentBalance())
                .lastUpdated(finances.getLastUpdated())
                .build();
    }

    /**
     * Maps a list of RepublicFinances domain objects to a list of RepublicFinancesResponse DTOs
     */
    public List<RepublicFinancesResponse> toRepublicFinancesResponseList(List<RepublicFinances> financesList) {
        if (financesList == null) return null;

        return financesList.stream()
                .map(this::toRepublicFinancesResponse)
                .collect(Collectors.toList());
    }
}