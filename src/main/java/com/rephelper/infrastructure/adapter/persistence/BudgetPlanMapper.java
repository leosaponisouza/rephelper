package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.BudgetPlan;
import com.rephelper.domain.model.Republic;
import com.rephelper.infrastructure.entity.BudgetPlanJpaEntity;
import com.rephelper.infrastructure.entity.RepublicJpaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Maps between BudgetPlan domain model and BudgetPlanJpaEntity
 */
@Component
public class BudgetPlanMapper {

    @Autowired
    private RepublicMapper republicMapper;

    /**
     * Converts a JPA entity to a domain entity
     */
    public BudgetPlan toDomainEntity(BudgetPlanJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        Republic republic = null;
        if (jpaEntity.getRepublic() != null) {
            republic = republicMapper.toDomainEntityWithoutUsers(jpaEntity.getRepublic());
        }

        return BudgetPlan.builder()
                .id(jpaEntity.getId())
                .republic(republic)
                .year(jpaEntity.getYear())
                .month(jpaEntity.getMonth())
                .category(jpaEntity.getCategory())
                .plannedAmount(jpaEntity.getPlannedAmount())
                .createdAt(jpaEntity.getCreatedAt())
                .build();
    }

    /**
     * Converts a domain entity to a JPA entity
     */
    public BudgetPlanJpaEntity toJpaEntity(BudgetPlan domainEntity) {
        if (domainEntity == null) return null;

        RepublicJpaEntity republicEntity = null;
        if (domainEntity.getRepublic() != null) {
            republicEntity = republicMapper.toJpaEntityWithoutUsers(domainEntity.getRepublic());
        }

        return BudgetPlanJpaEntity.builder()
                .id(domainEntity.getId())
                .republic(republicEntity)
                .year(domainEntity.getYear())
                .month(domainEntity.getMonth())
                .category(domainEntity.getCategory())
                .plannedAmount(domainEntity.getPlannedAmount())
                .createdAt(domainEntity.getCreatedAt())
                .build();
    }
}