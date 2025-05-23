package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Expense;
import com.rephelper.infrastructure.entity.ExpenseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for Expenses
 */
@Repository
public interface ExpenseJpaRepository extends JpaRepository<ExpenseJpaEntity, Long> {

    List<ExpenseJpaEntity> findByRepublicUuid(UUID republicId);

    List<ExpenseJpaEntity> findByCreatorUuid(UUID creatorId);

    List<ExpenseJpaEntity> findByStatus(Expense.ExpenseStatus status);

    List<ExpenseJpaEntity> findByRepublicUuidAndStatus(UUID republicId, Expense.ExpenseStatus status);

    @Query("SELECT e FROM ExpenseJpaEntity e WHERE e.republic.uuid = :republicId AND " +
            "e.expenseDate >= :startDate AND e.expenseDate <= :endDate")
    List<ExpenseJpaEntity> findByRepublicIdAndDateRangeBoth(
            @Param("republicId") UUID republicId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM ExpenseJpaEntity e WHERE e.republic.uuid = :republicId AND " +
            "e.expenseDate >= :startDate")
    List<ExpenseJpaEntity> findByRepublicIdAndStartDate(
            @Param("republicId") UUID republicId,
            @Param("startDate") LocalDate startDate);

    @Query("SELECT e FROM ExpenseJpaEntity e WHERE e.republic.uuid = :republicId AND " +
            "e.expenseDate <= :endDate")
    List<ExpenseJpaEntity> findByRepublicIdAndEndDate(
            @Param("republicId") UUID republicId,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM ExpenseJpaEntity e WHERE e.republic.uuid = :republicId")
    List<ExpenseJpaEntity> findByRepublicIdOnly(
            @Param("republicId") UUID republicId);

    List<ExpenseJpaEntity> findByRepublicUuidAndCategory(UUID republicId, String category);
}