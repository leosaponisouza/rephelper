package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.infrastructure.entity.BudgetPlanJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Budget Plans
 */
@Repository
public interface BudgetPlanJpaRepository extends JpaRepository<BudgetPlanJpaEntity, Long> {

    List<BudgetPlanJpaEntity> findByRepublicUuid(UUID republicId);

    List<BudgetPlanJpaEntity> findByRepublicUuidAndYearAndMonth(UUID republicId, Integer year, Integer month);

    Optional<BudgetPlanJpaEntity> findByRepublicUuidAndYearAndMonthAndCategory(
            UUID republicId, Integer year, Integer month, String category);
}