package com.rephelper.application.dto.request;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String name;

    private String nickname;

    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;

    private String profilePictureUrl;
}