package com.rephelper.application.mapper;

import com.rephelper.application.dto.request.CreateEventRequest;
import com.rephelper.application.dto.response.EventInvitationResponse;
import com.rephelper.application.dto.response.EventResponse;
import com.rephelper.domain.model.Event;
import com.rephelper.domain.model.EventInvitation;
import com.rephelper.domain.model.Republic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EventDtoMapper {

    @Autowired
    private UserDtoMapper userDtoMapper;

    public Event toEvent(CreateEventRequest request) {
        if (request == null) return null;

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .build();

        if (request.getRepublicId() != null) {
            event = Event.builder()
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .location(event.getLocation())
                    .republic(Republic.builder().id(request.getRepublicId()).build())
                    .build();
        }

        return event;
    }

    public EventResponse toEventResponse(Event event) {
        if (event == null) return null;

        // Mapear os convites
        Set<EventInvitationResponse> invitationResponses = null;
        if (event.getInvitations() != null && !event.getInvitations().isEmpty()) {
            invitationResponses = event.getInvitations().stream()
                    .map(this::toEventInvitationResponse)
                    .collect(Collectors.toSet());
        }

        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .republicId(event.getRepublic() != null ? event.getRepublic().getId() : null)
                .republicName(event.getRepublic() != null ? event.getRepublic().getName() : null)
                .creatorId(event.getCreator() != null ? event.getCreator().getId() : null)
                .creatorName(event.getCreator() != null ? event.getCreator().getName() : null)
                .invitations(invitationResponses)
                .createdAt(event.getCreatedAt())
                .isFinished(event.hasFinished())
                .isHappening(event.isHappening())
                .build();
    }

    public List<EventResponse> toEventResponseList(List<Event> events) {
        if (events == null) return null;

        return events.stream()
                .map(this::toEventResponse)
                .collect(Collectors.toList());
    }

    public EventInvitationResponse toEventInvitationResponse(EventInvitation invitation) {
        if (invitation == null) return null;

        return EventInvitationResponse.builder()
                .userId(invitation.getUser().getId())
                .userName(invitation.getUser().getName())
                .userEmail(invitation.getUser().getEmail())
                .nickName(invitation.getUser().getNickname())
                .profilePictureUrl(invitation.getUser().getProfilePictureUrl())
                .status(invitation.getStatus())
                .build();
    }
}