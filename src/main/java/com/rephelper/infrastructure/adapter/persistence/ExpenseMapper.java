package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Expense;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.infrastructure.entity.ExpenseJpaEntity;
import com.rephelper.infrastructure.entity.RepublicJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Maps between Expense domain model and ExpenseJpaEntity
 */
@Component
public class ExpenseMapper {

    @Autowired
    private RepublicMapper republicMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * Converts a JPA entity to a domain entity
     */
    public Expense toDomainEntity(ExpenseJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        Republic republic = null;
        if (jpaEntity.getRepublic() != null) {
            republic = republicMapper.toDomainEntityWithoutUsers(jpaEntity.getRepublic());
        }

        User creator = null;
        if (jpaEntity.getCreator() != null) {
            creator = userMapper.toDomainEntityWithoutRepublic(jpaEntity.getCreator());
        }

        return Expense.builder()
                .id(jpaEntity.getId())
                .republic(republic)
                .creator(creator)
                .description(jpaEntity.getDescription())
                .amount(jpaEntity.getAmount())
                .expenseDate(jpaEntity.getExpenseDate())
                .category(jpaEntity.getCategory())
                .receiptUrl(jpaEntity.getReceiptUrl())
                .status(jpaEntity.getStatus())
                .approvalDate(jpaEntity.getApprovalDate())
                .reimbursementDate(jpaEntity.getReimbursementDate())
                .rejectionReason(jpaEntity.getRejectionReason())
                .createdAt(jpaEntity.getCreatedAt())
                .build();
    }

    /**
     * Converts a domain entity to a JPA entity
     */
    public ExpenseJpaEntity toJpaEntity(Expense domainEntity) {
        if (domainEntity == null) return null;

        RepublicJpaEntity republicEntity = null;
        if (domainEntity.getRepublic() != null) {
            republicEntity = republicMapper.toJpaEntityWithoutUsers(domainEntity.getRepublic());
        }

        UserJpaEntity creatorEntity = null;
        if (domainEntity.getCreator() != null) {
            creatorEntity = userMapper.toJpaEntityWithoutRepublic(domainEntity.getCreator());
        }

        return ExpenseJpaEntity.builder()
                .id(domainEntity.getId())
                .republic(republicEntity)
                .creator(creatorEntity)
                .description(domainEntity.getDescription())
                .amount(domainEntity.getAmount())
                .expenseDate(domainEntity.getExpenseDate())
                .category(domainEntity.getCategory())
                .receiptUrl(domainEntity.getReceiptUrl())
                .status(domainEntity.getStatus())
                .approvalDate(domainEntity.getApprovalDate())
                .reimbursementDate(domainEntity.getReimbursementDate())
                .rejectionReason(domainEntity.getRejectionReason())
                .createdAt(domainEntity.getCreatedAt())
                .build();
    }
}