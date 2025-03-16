package com.rephelper.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity for representing the financial state of a republic
 */
@Entity
@Table(name = "republic_finances")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepublicFinancesJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "republic_finances_id_seq_gen")
    @SequenceGenerator(name = "republic_finances_id_seq_gen", sequenceName = "republic_finances_id_seq", allocationSize = 1)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "republic_id", nullable = false, unique = true)
    private RepublicJpaEntity republic;

    @Column(name = "current_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}