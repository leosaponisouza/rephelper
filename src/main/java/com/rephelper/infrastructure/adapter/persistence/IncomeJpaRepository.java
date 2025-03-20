package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.infrastructure.entity.IncomeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for Incomes
 */
@Repository
public interface IncomeJpaRepository extends JpaRepository<IncomeJpaEntity, Long> {

    List<IncomeJpaEntity> findByRepublicUuid(UUID republicId);

    List<IncomeJpaEntity> findByContributorUuid(UUID contributorId);

    @Query("SELECT i FROM IncomeJpaEntity i WHERE i.republic.uuid = :republicId AND i.incomeDate <= :endDate")
    List<IncomeJpaEntity> findByRepublicIdAndEndDate(
            @Param("republicId") UUID republicId,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT i FROM IncomeJpaEntity i WHERE i.republic.uuid = :republicId AND i.incomeDate >= :startDate")
    List<IncomeJpaEntity> findByRepublicIdAndStartDate(
            @Param("republicId") UUID republicId,
            @Param("startDate") LocalDateTime startDate);

    @Query("SELECT i FROM IncomeJpaEntity i WHERE i.republic.uuid = :republicId AND i.incomeDate >= :startDate AND i.incomeDate <= :endDate")
    List<IncomeJpaEntity> findByRepublicIdAndStartDateAndEndDate(
            @Param("republicId") UUID republicId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    List<IncomeJpaEntity> findByRepublicUuidAndSource(UUID republicId, String source);
}