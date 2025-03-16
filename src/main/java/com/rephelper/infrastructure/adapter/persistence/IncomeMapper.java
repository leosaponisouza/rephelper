package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Income;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.infrastructure.entity.IncomeJpaEntity;
import com.rephelper.infrastructure.entity.RepublicJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Maps between Income domain model and IncomeJpaEntity
 */
@Component
public class IncomeMapper {

    @Autowired
    private RepublicMapper republicMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * Converts a JPA entity to a domain entity
     */
    public Income toDomainEntity(IncomeJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        Republic republic = null;
        if (jpaEntity.getRepublic() != null) {
            republic = republicMapper.toDomainEntityWithoutUsers(jpaEntity.getRepublic());
        }

        User contributor = null;
        if (jpaEntity.getContributor() != null) {
            contributor = userMapper.toDomainEntityWithoutRepublic(jpaEntity.getContributor());
        }

        return Income.builder()
                .id(jpaEntity.getId())
                .republic(republic)
                .contributor(contributor)
                .description(jpaEntity.getDescription())
                .amount(jpaEntity.getAmount())
                .incomeDate(jpaEntity.getIncomeDate())
                .source(jpaEntity.getSource())
                .createdAt(jpaEntity.getCreatedAt())
                .build();
    }

    /**
     * Converts a domain entity to a JPA entity
     */
    public IncomeJpaEntity toJpaEntity(Income domainEntity) {
        if (domainEntity == null) return null;

        RepublicJpaEntity republicEntity = null;
        if (domainEntity.getRepublic() != null) {
            republicEntity = republicMapper.toJpaEntityWithoutUsers(domainEntity.getRepublic());
        }

        UserJpaEntity contributorEntity = null;
        if (domainEntity.getContributor() != null) {
            contributorEntity = userMapper.toJpaEntityWithoutRepublic(domainEntity.getContributor());
        }

        return IncomeJpaEntity.builder()
                .id(domainEntity.getId())
                .republic(republicEntity)
                .contributor(contributorEntity)
                .description(domainEntity.getDescription())
                .amount(domainEntity.getAmount())
                .incomeDate(domainEntity.getIncomeDate())
                .source(domainEntity.getSource())
                .createdAt(domainEntity.getCreatedAt())
                .build();
    }
}