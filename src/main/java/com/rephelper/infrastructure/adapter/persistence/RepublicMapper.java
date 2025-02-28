package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Address;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.infrastructure.config.CommonMapperConfig;
import com.rephelper.infrastructure.entity.RepublicJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RepublicMapper {

    @Autowired
    private CommonMapperConfig commonMapperConfig;

    public Republic toDomainEntity(RepublicJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        Republic republic = Republic.builder()
                .id(jpaEntity.getUuid())
                .name(jpaEntity.getName())
                .code(jpaEntity.getCode())
                .address(toAddress(jpaEntity))
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                .build();

        // Map owner
        if (jpaEntity.getOwner() != null) {
            User owner = commonMapperConfig.mapUserWithoutRepublic(jpaEntity.getOwner());
            republic = Republic.builder()
                    .id(republic.getId())
                    .name(republic.getName())
                    .code(republic.getCode())
                    .address(republic.getAddress())
                    .owner(owner)
                    .createdAt(republic.getCreatedAt())
                    .updatedAt(republic.getUpdatedAt())
                    .build();
        }

        // Map members if needed
        if (jpaEntity.getMembers() != null && !jpaEntity.getMembers().isEmpty()) {
            List<User> members = jpaEntity.getMembers().stream()
                    .map(commonMapperConfig::mapUserWithoutRepublic)
                    .collect(Collectors.toList());
            republic = Republic.builder()
                    .id(republic.getId())
                    .name(republic.getName())
                    .code(republic.getCode())
                    .address(republic.getAddress())
                    .owner(republic.getOwner())
                    .members(members)
                    .createdAt(republic.getCreatedAt())
                    .updatedAt(republic.getUpdatedAt())
                    .build();
        }

        return republic;
    }

    public RepublicJpaEntity toJpaEntity(Republic domainEntity) {
        if (domainEntity == null) return null;

        RepublicJpaEntity entity = new RepublicJpaEntity();
        entity.setUuid(domainEntity.getId());
        entity.setName(domainEntity.getName());
        entity.setCode(domainEntity.getCode());

        if (domainEntity.getAddress() != null) {
            entity.setStreet(domainEntity.getAddress().getStreet());
            entity.setNumber(domainEntity.getAddress().getNumber());
            entity.setComplement(domainEntity.getAddress().getComplement());
            entity.setNeighborhood(domainEntity.getAddress().getNeighborhood());
            entity.setCity(domainEntity.getAddress().getCity());
            entity.setState(domainEntity.getAddress().getState());
            entity.setZipCode(domainEntity.getAddress().getZipCode());
        }

        if (domainEntity.getOwner() != null) {
            entity.setOwner(commonMapperConfig.mapUserEntityWithoutRepublic(domainEntity.getOwner()));
        }

        if (domainEntity.getMembers() != null && !domainEntity.getMembers().isEmpty()) {
            Set<UserJpaEntity> members = domainEntity.getMembers().stream()
                    .map(commonMapperConfig::mapUserEntityWithoutRepublic)
                    .collect(Collectors.toSet());
            entity.setMembers(members);
        }

        entity.setCreatedAt(domainEntity.getCreatedAt());
        entity.setUpdatedAt(domainEntity.getUpdatedAt());

        return entity;
    }

    public Republic toDomainEntityWithoutUsers(RepublicJpaEntity jpaEntity) {
        return commonMapperConfig.mapRepublicWithoutUsers(jpaEntity);
    }

    public RepublicJpaEntity toJpaEntityWithoutUsers(Republic domainEntity) {
        return commonMapperConfig.mapRepublicEntityWithoutUsers(domainEntity);
    }

    // Método para criar um objeto Address a partir dos campos do RepublicJpaEntity
    protected Address toAddress(RepublicJpaEntity entity) {
        if (entity == null) return null;

        return Address.builder()
                .street(entity.getStreet())
                .number(entity.getNumber())
                .complement(entity.getComplement())
                .neighborhood(entity.getNeighborhood())
                .city(entity.getCity())
                .state(entity.getState())
                .zipCode(entity.getZipCode())
                .build();
    }
}