package com.rephelper.application.mapper;

import java.util.List;
import java.util.Set;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.rephelper.application.dto.request.CreateUserRequest;

import com.rephelper.application.dto.response.UserResponse;
import com.rephelper.application.dto.response.UserSummaryResponse;
import com.rephelper.domain.model.User;
/**
 * Mapper para converter entre entidades de domínio e DTOs de usuário
 */
@Mapper(componentModel = "spring")
public abstract class UserDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentRepublic", ignore = true)
    @Mapping(target = "isAdmin", constant = "false")
    @Mapping(target = "isActiveResident", constant = "false")
    @Mapping(target = "entryDate", ignore = true)
    @Mapping(target = "departureDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    public abstract User toUser(CreateUserRequest request);

    @Mapping(target = "currentRepublicId", source = "currentRepublic.id")
    @Mapping(target = "currentRepublicName", source = "currentRepublic.name")
    public abstract UserResponse toUserResponse(User user);

    public abstract List<UserResponse> toUserResponseList(List<User> users);

    public abstract UserSummaryResponse toUserSummaryResponse(User user);

    public abstract Set<UserSummaryResponse> toUserSummaryResponseSet(Set<User> users);
}