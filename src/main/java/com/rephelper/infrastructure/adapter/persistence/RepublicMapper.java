package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Address;
import com.rephelper.domain.model.Republic;
import com.rephelper.infrastructure.entity.RepublicJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper para converter entre Republic do domínio e RepublicJpaEntity
 */
@Mapper(componentModel = "spring")
public abstract class RepublicMapper {

    @Autowired
    protected UserMapper userMapper;

    @Mapping(target = "address", expression = "java(toAddress(jpaEntity))")
    @Mapping(target = "owner", source = "owner", qualifiedByName = "userWithoutRepublic")
    @Mapping(target = "members", expression = "java(jpaEntity.getMembers().stream().map(userMapper::toDomainEntityWithoutRepublic).collect(java.util.stream.Collectors.toList()))")
    public abstract Republic toDomainEntity(RepublicJpaEntity jpaEntity);

    @Named("toEntityWithoutUsers")
    @Mapping(target = "address", expression = "java(toAddress(jpaEntity))")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "members", ignore = true)
    public abstract Republic toDomainEntityWithoutUsers(RepublicJpaEntity jpaEntity);

    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "number", source = "address.number")
    @Mapping(target = "complement", source = "address.complement")
    @Mapping(target = "neighborhood", source = "address.neighborhood")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "state", source = "address.state")
    @Mapping(target = "zipCode", source = "address.zipCode")
    @Mapping(target = "owner", source = "owner", qualifiedByName = "userJpaWithoutRepublic")
    @Mapping(target = "members", expression = "java(domainEntity.getMembers().stream().map(userMapper::toJpaEntityWithoutRepublic).collect(java.util.stream.Collectors.toSet()))")
    public abstract RepublicJpaEntity toJpaEntity(Republic domainEntity);

    @Named("toJpaEntityWithoutUsers")
    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "number", source = "address.number")
    @Mapping(target = "complement", source = "address.complement")
    @Mapping(target = "neighborhood", source = "address.neighborhood")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "state", source = "address.state")
    @Mapping(target = "zipCode", source = "address.zipCode")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "members", ignore = true)
    public abstract RepublicJpaEntity toJpaEntityWithoutUsers(Republic domainEntity);

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
