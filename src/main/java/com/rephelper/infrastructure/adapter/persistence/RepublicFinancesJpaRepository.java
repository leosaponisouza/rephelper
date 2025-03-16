package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.infrastructure.entity.RepublicFinancesJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA Repository for Republic Finances
 */
@Repository
public interface RepublicFinancesJpaRepository extends JpaRepository<RepublicFinancesJpaEntity, Long> {

    Optional<RepublicFinancesJpaEntity> findByRepublicUuid(UUID republicId);
}