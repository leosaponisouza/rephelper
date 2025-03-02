package com.rephelper.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private UUID republicId;
    private String republicName;
    private UUID creatorId;
    private String creatorName;
    private Set<EventInvitationResponse> invitations;
    private LocalDateTime createdAt;
    private boolean isFinished;
    private boolean isHappening;
}
