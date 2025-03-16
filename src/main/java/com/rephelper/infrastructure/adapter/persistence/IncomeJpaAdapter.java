package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Income;
import com.rephelper.domain.port.out.IncomeRepositoryPort;
import com.rephelper.infrastructure.entity.IncomeJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of IncomeRepositoryPort using JPA
 */
@Component
@RequiredArgsConstructor
public class IncomeJpaAdapter implements IncomeRepositoryPort {

    private final IncomeJpaRepository incomeJpaRepository;
    private final IncomeMapper incomeMapper;

    @Override
    public Income save(Income income) {
        IncomeJpaEntity incomeEntity = incomeMapper.toJpaEntity(income);
        IncomeJpaEntity savedEntity = incomeJpaRepository.save(incomeEntity);
        return incomeMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Income> findById(Long id) {
        return incomeJpaRepository.findById(id)
                .map(incomeMapper::toDomainEntity);
    }

    @Override
    public List<Income> findAll() {
        return incomeJpaRepository.findAll().stream()
                .map(incomeMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> findByRepublicId(UUID republicId) {
        return incomeJpaRepository.findByRepublicUuid(republicId).stream()
                .map(incomeMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> findByContributorId(UUID contributorId) {
        return incomeJpaRepository.findByContributorUuid(contributorId).stream()
                .map(incomeMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> findByRepublicIdAndDateRange(UUID republicId, LocalDateTime startDate, LocalDateTime endDate) {
        return incomeJpaRepository.findByRepublicIdAndDateRange(republicId, startDate, endDate).stream()
                .map(incomeMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Income> findByRepublicIdAndSource(UUID republicId, String source) {
        return incomeJpaRepository.findByRepublicUuidAndSource(republicId, source).stream()
                .map(incomeMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Income income) {
        incomeJpaRepository.deleteById(income.getId());
    }
}