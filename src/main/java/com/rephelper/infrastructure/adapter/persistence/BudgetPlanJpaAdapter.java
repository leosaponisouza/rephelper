package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.BudgetPlan;
import com.rephelper.domain.port.out.BudgetPlanRepositoryPort;
import com.rephelper.infrastructure.entity.BudgetPlanJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of BudgetPlanRepositoryPort using JPA
 */
@Component
@RequiredArgsConstructor
public class BudgetPlanJpaAdapter implements BudgetPlanRepositoryPort {

    private final BudgetPlanJpaRepository budgetPlanJpaRepository;
    private final BudgetPlanMapper budgetPlanMapper;

    @Override
    public BudgetPlan save(BudgetPlan budgetPlan) {
        BudgetPlanJpaEntity entity = budgetPlanMapper.toJpaEntity(budgetPlan);
        BudgetPlanJpaEntity savedEntity = budgetPlanJpaRepository.save(entity);
        return budgetPlanMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<BudgetPlan> findById(Long id) {
        return budgetPlanJpaRepository.findById(id)
                .map(budgetPlanMapper::toDomainEntity);
    }

    @Override
    public List<BudgetPlan> findAll() {
        return budgetPlanJpaRepository.findAll().stream()
                .map(budgetPlanMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetPlan> findByRepublicId(UUID republicId) {
        return budgetPlanJpaRepository.findByRepublicUuid(republicId).stream()
                .map(budgetPlanMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetPlan> findByRepublicIdAndYearAndMonth(UUID republicId, Integer year, Integer month) {
        return budgetPlanJpaRepository.findByRepublicUuidAndYearAndMonth(republicId, year, month).stream()
                .map(budgetPlanMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BudgetPlan> findByRepublicIdAndYearAndMonthAndCategory(UUID republicId, Integer year, Integer month, String category) {
        return budgetPlanJpaRepository.findByRepublicUuidAndYearAndMonthAndCategory(republicId, year, month, category)
                .map(budgetPlanMapper::toDomainEntity);
    }

    @Override
    public void delete(BudgetPlan budgetPlan) {
        budgetPlanJpaRepository.deleteById(budgetPlan.getId());
    }
}