package com.rephelper.interfaces.rest.event;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rephelper.application.dto.request.CreateEventRequest;
import com.rephelper.application.dto.request.EventInvitationRequest;
import com.rephelper.application.dto.request.UpdateEventRequest;
import com.rephelper.application.dto.request.UpdateInvitationStatusRequest;
import com.rephelper.application.dto.response.ApiResponse;
import com.rephelper.application.dto.response.EventResponse;
import com.rephelper.application.mapper.EventDtoMapper;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.Event;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.EventServicePort;
import com.rephelper.domain.port.in.UserServicePort;
import com.rephelper.infrastructure.adapter.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Event management endpoints")
public class EventController {

    private final EventServicePort eventService;
    private final UserServicePort userService;
    private final EventDtoMapper eventDtoMapper;

    @PostMapping
    @Operation(summary = "Create a new event", description = "Creates a new event for a republic")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Convert request to event
        Event event = eventDtoMapper.toEvent(request);

        // Create event
        Event createdEvent = eventService.createEvent(event, currentUser.getUserId());

        return new ResponseEntity<>(eventDtoMapper.toEventResponse(createdEvent), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all events for user's republic", description = "Retrieves all events for the current user's republic")
    public ResponseEntity<List<EventResponse>> getAllEvents(
            @RequestParam(required = false) UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to determine their republic
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // If republicId is provided, validate that it matches user's republic (unless admin)
        UUID targetRepublicId = republicId;
        if (targetRepublicId == null) {
            targetRepublicId = user.getCurrentRepublic().getId();
        } else if (!targetRepublicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view events for your own republic");
        }

        // Get events
        List<Event> events = eventService.getAllEventsByRepublicId(targetRepublicId);

        return ResponseEntity.ok(eventDtoMapper.toEventResponseList(events));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event by ID", description = "Retrieves event details by ID")
    public ResponseEntity<EventResponse> getEventById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Event event = eventService.getEventById(id);

        return ResponseEntity.ok(eventDtoMapper.toEventResponse(event));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming events", description = "Retrieves upcoming events for the current user's republic")
    public ResponseEntity<List<EventResponse>> getUpcomingEvents(
            @RequestParam(required = false) UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to determine their republic
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // If republicId is provided, validate that it matches user's republic (unless admin)
        UUID targetRepublicId = republicId;
        if (targetRepublicId == null) {
            targetRepublicId = user.getCurrentRepublic().getId();
        } else if (!targetRepublicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view events for your own republic");
        }

        // Get upcoming events
        List<Event> events = eventService.getUpcomingEventsByRepublicId(targetRepublicId);

        return ResponseEntity.ok(eventDtoMapper.toEventResponseList(events));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update event", description = "Updates event details")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEventRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Update event
        Event updatedEvent = eventService.updateEvent(
                id,
                request.getTitle(),
                request.getDescription(),
                request.getStartDate(),
                request.getEndDate(),
                request.getLocation(),
                currentUser.getUserId());

        return ResponseEntity.ok(eventDtoMapper.toEventResponse(updatedEvent));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete event", description = "Deletes an event")
    public ResponseEntity<ApiResponse> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Delete event
        eventService.deleteEvent(id, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("Event deleted successfully")
                .build());
    }

    @PostMapping("/{id}/invite")
    @Operation(summary = "Invite users to event", description = "Invites users to an event")
    public ResponseEntity<EventResponse> inviteUsers(
            @PathVariable Long id,
            @Valid @RequestBody EventInvitationRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Invite users
        Event event = eventService.inviteUsers(id, request.getUserIds(), currentUser.getUserId());

        return ResponseEntity.ok(eventDtoMapper.toEventResponse(event));
    }

    @PutMapping("/{id}/respond")
    @Operation(summary = "Respond to invitation", description = "Responds to an event invitation")
    public ResponseEntity<EventResponse> respondToInvitation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInvitationStatusRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Update invitation status
        Event event = eventService.updateInvitationStatus(id, currentUser.getUserId(), request.getStatus());

        return ResponseEntity.ok(eventDtoMapper.toEventResponse(event));
    }

    @GetMapping("/invited")
    @Operation(summary = "Get events I'm invited to", description = "Retrieves events the current user is invited to")
    public ResponseEntity<List<EventResponse>> getEventsInvitedTo(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get events user is invited to
        List<Event> events = eventService.getEventsByInvitedUser(currentUser.getUserId());

        return ResponseEntity.ok(eventDtoMapper.toEventResponseList(events));
    }

    @GetMapping("/confirmed")
    @Operation(summary = "Get events I've confirmed", description = "Retrieves events the current user has confirmed attendance")
    public ResponseEntity<List<EventResponse>> getEventsConfirmed(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get events user has confirmed
        List<Event> events = eventService.getEventsByConfirmedUser(currentUser.getUserId());

        return ResponseEntity.ok(eventDtoMapper.toEventResponseList(events));
    }
}