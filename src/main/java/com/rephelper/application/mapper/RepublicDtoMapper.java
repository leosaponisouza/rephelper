package com.rephelper.application.mapper;

import com.rephelper.application.dto.request.CreateRepublicRequest;
import com.rephelper.application.dto.request.UpdateRepublicRequest;
import com.rephelper.application.dto.response.AddressResponse;
import com.rephelper.application.dto.response.RepublicResponse;
import com.rephelper.domain.model.Address;
import com.rephelper.domain.model.Republic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Mapper para converter entre entidades de domínio e DTOs de república
 */
@Mapper(componentModel = "spring")
public abstract class RepublicDtoMapper {

    @Autowired
    protected UserDtoMapper userDtoMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "address", expression = "java(toAddress(request))")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Republic toRepublic(CreateRepublicRequest request);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "ownerName", source = "owner.name")
    public abstract RepublicResponse toRepublicResponse(Republic republic);

    public abstract List<RepublicResponse> toRepublicResponseList(List<Republic> republics);

    @Mapping(target = "fullAddress", expression = "java(address.getFullAddress())")
    public abstract AddressResponse toAddressResponse(Address address);

    // Método para criar um objeto Address a partir do DTO de criação
    protected Address toAddress(CreateRepublicRequest request) {
        if (request == null) return null;

        return Address.builder()
                .street(request.getStreet())
                .number(request.getNumber())
                .complement(request.getComplement())
                .neighborhood(request.getNeighborhood())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .build();
    }

    // Método para criar um objeto Address a partir do DTO de atualização
    public Address toAddress(UpdateRepublicRequest request) {
        if (request == null) return null;

        return Address.builder()
                .street(request.getStreet())
                .number(request.getNumber())
                .complement(request.getComplement())
                .neighborhood(request.getNeighborhood())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .build();
    }
}
