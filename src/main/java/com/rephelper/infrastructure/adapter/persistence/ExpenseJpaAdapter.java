package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Expense;
import com.rephelper.domain.port.out.ExpenseRepositoryPort;
import com.rephelper.infrastructure.entity.ExpenseJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ExpenseRepositoryPort using JPA
 */
@Component
@RequiredArgsConstructor
public class ExpenseJpaAdapter implements ExpenseRepositoryPort {

    private final ExpenseJpaRepository expenseJpaRepository;
    private final ExpenseMapper expenseMapper;

    @Override
    public Expense save(Expense expense) {
        ExpenseJpaEntity expenseEntity = expenseMapper.toJpaEntity(expense);
        ExpenseJpaEntity savedEntity = expenseJpaRepository.save(expenseEntity);
        return expenseMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Expense> findById(Long id) {
        return expenseJpaRepository.findById(id)
                .map(expenseMapper::toDomainEntity);
    }

    @Override
    public List<Expense> findAll() {
        return expenseJpaRepository.findAll().stream()
                .map(expenseMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByRepublicId(UUID republicId) {
        return expenseJpaRepository.findByRepublicUuid(republicId).stream()
                .map(expenseMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByCreatorId(UUID creatorId) {
        return expenseJpaRepository.findByCreatorUuid(creatorId).stream()
                .map(expenseMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByStatus(Expense.ExpenseStatus status) {
        return expenseJpaRepository.findByStatus(status).stream()
                .map(expenseMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Expense> findByRepublicIdAndStatus(UUID republicId, Expense.ExpenseStatus status) {
        return expenseJpaRepository.findByRepublicUuidAndStatus(republicId, status).stream()
                .map(expenseMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
    @Override
    public List<Expense> findByRepublicIdAndDateRange(UUID republicId, LocalDate startDate, LocalDate endDate) {
        List<ExpenseJpaEntity> expenses;

        if (startDate != null && endDate != null) {
            // Both dates provided
            expenses = expenseJpaRepository.findByRepublicIdAndDateRangeBoth(republicId, startDate, endDate);
        } else if (startDate != null) {
            // Only start date provided
            expenses = expenseJpaRepository.findByRepublicIdAndStartDate(republicId, startDate);
        } else if (endDate != null) {
            // Only end date provided
            expenses = expenseJpaRepository.findByRepublicIdAndEndDate(republicId, endDate);
        } else {
            // No dates provided
            expenses = expenseJpaRepository.findByRepublicIdOnly(republicId);
        }

        return expenses.stream()
                .map(expenseMapper::toDomainEntity)
                .collect(Collectors.toList());
    }
    @Override
    public List<Expense> findByRepublicIdAndCategory(UUID republicId, String category) {
        return expenseJpaRepository.findByRepublicUuidAndCategory(republicId, category).stream()
                .map(expenseMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Expense expense) {
        expenseJpaRepository.deleteById(expense.getId());
    }
}