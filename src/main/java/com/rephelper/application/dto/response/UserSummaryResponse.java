package com.rephelper.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID; /**
 * DTO para resposta resumida de usuário (usado em listas)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private UUID uid;
    private String name;
    private String nickname;
    private String email;
    private String profilePictureUrl;
}