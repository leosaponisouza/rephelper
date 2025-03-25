package com.rephelper.application.dto.response;

import com.rephelper.domain.model.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInvitationResponse {
    private UUID userId;
    private String userName;
    private String userEmail;
    private String nickName;
    private String profilePictureUrl;
    private Event.InvitationStatus status;
}
