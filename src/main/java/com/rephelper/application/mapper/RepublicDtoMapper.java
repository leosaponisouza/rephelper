package com.rephelper.application.mapper;

import com.rephelper.application.dto.request.CreateRepublicRequest;
import com.rephelper.application.dto.request.UpdateRepublicRequest;
import com.rephelper.application.dto.response.AddressResponse;
import com.rephelper.application.dto.response.RepublicResponse;
import com.rephelper.domain.model.Address;
import com.rephelper.domain.model.Republic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RepublicDtoMapper {

    @Autowired
    protected UserDtoMapper userDtoMapper;

    public Republic toRepublic(CreateRepublicRequest request) {
        if (request == null) return null;

        return Republic.builder()
                .name(request.getName())
                .address(toAddress(request))
                .build();
    }

    public RepublicResponse toRepublicResponse(Republic republic) {
        if (republic == null) return null;

        return RepublicResponse.builder()
                .id(republic.getId())
                .name(republic.getName())
                .code(republic.getCode())
                .address(toAddressResponse(republic.getAddress()))
                .ownerId(republic.getOwner() != null ? republic.getOwner().getId() : null)
                .ownerName(republic.getOwner() != null ? republic.getOwner().getName() : null)
                .createdAt(republic.getCreatedAt())
                .updatedAt(republic.getUpdatedAt())
                .build();
    }

    public List<RepublicResponse> toRepublicResponseList(List<Republic> republics) {
        if (republics == null) return null;

        return republics.stream()
                .map(this::toRepublicResponse)
                .collect(Collectors.toList());
    }

    public AddressResponse toAddressResponse(Address address) {
        if (address == null) return null;

        return AddressResponse.builder()
                .street(address.getStreet())
                .number(address.getNumber())
                .complement(address.getComplement())
                .neighborhood(address.getNeighborhood())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .fullAddress(address.getFullAddress())
                .build();
    }

    // Method to create an Address object from the creation DTO
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

    // Method to create an Address object from the update DTO
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