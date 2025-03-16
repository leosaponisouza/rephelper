package com.rephelper.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for representing a budget plan for a specific month
 */
@Entity
@Table(name = "budget_plans",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"republic_id", "year", "month", "category"},
                name = "uk_budget_plan_month"
        ))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BudgetPlanJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "budget_plans_id_seq_gen")
    @SequenceGenerator(name = "budget_plans_id_seq_gen", sequenceName = "budget_plans_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "republic_id", nullable = false)
    private RepublicJpaEntity republic;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private String category;

    @Column(name = "planned_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal plannedAmount;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}