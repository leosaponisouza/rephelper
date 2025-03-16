package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.RepublicFinances;
import com.rephelper.domain.port.out.RepublicFinancesRepositoryPort;
import com.rephelper.infrastructure.entity.RepublicFinancesJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of RepublicFinancesRepositoryPort using JPA
 */
@Component
@RequiredArgsConstructor
public class RepublicFinancesJpaAdapter implements RepublicFinancesRepositoryPort {

    private final RepublicFinancesJpaRepository republicFinancesJpaRepository;
    private final RepublicFinancesMapper republicFinancesMapper;

    @Override
    public RepublicFinances save(RepublicFinances republicFinances) {
        RepublicFinancesJpaEntity entity = republicFinancesMapper.toJpaEntity(republicFinances);
        RepublicFinancesJpaEntity savedEntity = republicFinancesJpaRepository.save(entity);
        return republicFinancesMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<RepublicFinances> findById(Long id) {
        return republicFinancesJpaRepository.findById(id)
                .map(republicFinancesMapper::toDomainEntity);
    }

    @Override
    public Optional<RepublicFinances> findByRepublicId(UUID republicId) {
        return republicFinancesJpaRepository.findByRepublicUuid(republicId)
                .map(republicFinancesMapper::toDomainEntity);
    }

    @Override
    public List<RepublicFinances> findAll() {
        return republicFinancesJpaRepository.findAll().stream()
                .map(republicFinancesMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(RepublicFinances republicFinances) {
        republicFinancesJpaRepository.deleteById(republicFinances.getId());
    }
}