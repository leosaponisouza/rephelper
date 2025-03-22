package com.rephelper.application.mapper;

import com.rephelper.application.dto.request.CreateUserRequest;
import com.rephelper.application.dto.response.UserResponse;
import com.rephelper.application.dto.response.UserSummaryResponse;
import com.rephelper.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserDtoMapper {

    public User toUser(CreateUserRequest request) {
        if (request == null) return null;

        return User.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .profilePictureUrl(request.getProfilePictureUrl())
                .firebaseUid(request.getFirebaseUid())
                .provider(request.getProvider())
                .status("active")
                .isAdmin(false)
                .build();
    }

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;

        return UserResponse.builder()
                .uid(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .currentRepublicId(user.getCurrentRepublic() != null ? user.getCurrentRepublic().getId() : null)
                .currentRepublicName(user.getCurrentRepublic() != null ? user.getCurrentRepublic().getName() : null)
                .isAdmin(user.getIsAdmin())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .status(user.getStatus())
                .firebaseUid(user.getFirebaseUid())
                .provider(user.getProvider())
                .entryDate(user.getEntryDate())
                .departureDate(user.getDepartureDate())
                .build();
    }

    public List<UserResponse> toUserResponseList(List<User> users) {
        if (users == null) return null;

        return users.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    public UserSummaryResponse toUserSummaryResponse(User user) {
        if (user == null) return null;

        return UserSummaryResponse.builder()
                .uid(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    public Set<UserSummaryResponse> toUserSummaryResponseSet(Set<User> users) {
        if (users == null) return null;

        return users.stream()
                .map(this::toUserSummaryResponse)
                .collect(Collectors.toSet());
    }
}