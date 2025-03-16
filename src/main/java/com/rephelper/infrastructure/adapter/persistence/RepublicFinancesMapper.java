package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.RepublicFinances;
import com.rephelper.infrastructure.entity.RepublicFinancesJpaEntity;
import com.rephelper.infrastructure.entity.RepublicJpaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Maps between RepublicFinances domain model and RepublicFinancesJpaEntity
 */
@Component
public class RepublicFinancesMapper {

    @Autowired
    private RepublicMapper republicMapper;

    /**
     * Converts a JPA entity to a domain entity
     */
    public RepublicFinances toDomainEntity(RepublicFinancesJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        Republic republic = null;
        if (jpaEntity.getRepublic() != null) {
            republic = republicMapper.toDomainEntityWithoutUsers(jpaEntity.getRepublic());
        }

        return RepublicFinances.builder()
                .id(jpaEntity.getId())
                .republic(republic)
                .currentBalance(jpaEntity.getCurrentBalance())
                .lastUpdated(jpaEntity.getLastUpdated())
                .build();
    }

    /**
     * Converts a domain entity to a JPA entity
     */
    public RepublicFinancesJpaEntity toJpaEntity(RepublicFinances domainEntity) {
        if (domainEntity == null) return null;

        RepublicJpaEntity republicEntity = null;
        if (domainEntity.getRepublic() != null) {
            republicEntity = republicMapper.toJpaEntityWithoutUsers(domainEntity.getRepublic());
        }

        return RepublicFinancesJpaEntity.builder()
                .id(domainEntity.getId())
                .republic(republicEntity)
                .currentBalance(domainEntity.getCurrentBalance())
                .lastUpdated(domainEntity.getLastUpdated())
                .build();
    }
}