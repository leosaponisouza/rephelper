package com.rephelper.infrastructure.adapter.persistence;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.infrastructure.entity.EventInvitationJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

import com.rephelper.domain.model.Event;
import com.rephelper.domain.port.out.EventRepositoryPort;
import com.rephelper.infrastructure.entity.EventJpaEntity;

import lombok.RequiredArgsConstructor;

import static com.rephelper.infrastructure.entity.EventInvitationJpaEntity.EventInvitationStatus.DECLINED;
import static com.rephelper.infrastructure.entity.EventInvitationJpaEntity.EventInvitationStatus.MAYBE;

/**
 * Implementação do adaptador para o repositório de eventos usando JPA.
 */
@Component
@RequiredArgsConstructor
public class EventJpaAdapter implements EventRepositoryPort {

    private final EventJpaRepository eventJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final EventMapper eventMapper;

    @Override
    public Event save(Event event) {
        EventJpaEntity eventEntity = eventMapper.toJpaEntity(event);
        EventJpaEntity savedEntity = eventJpaRepository.save(eventEntity);
        return eventMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return eventJpaRepository.findById(id)
                .map(eventMapper::toDomainEntity);
    }

    @Override
    public List<Event> findAll() {
        return eventJpaRepository.findAll().stream()
                .map(eventMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByRepublicId(UUID republicId) {
        return eventJpaRepository.findByRepublicUuid(republicId).stream()
                .map(eventMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findUpcomingByRepublicId(UUID republicId) {
        return eventJpaRepository.findUpcomingByRepublicId(republicId, LocalDateTime.now()).stream()
                .map(eventMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByInvitedUserId(UUID userId) {
        return eventJpaRepository.findByInvitedUserId(userId).stream()
                .map(eventMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Event> findByConfirmedUserId(UUID userId) {
        return eventJpaRepository.findByConfirmedUserId(userId).stream()
                .map(eventMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Event event) {
        eventJpaRepository.deleteById(event.getId());
    }

    @Override
    public Event inviteUserToEvent(Long eventId, UUID userId, Event.InvitationStatus status) {
        // Get managed references directly from repositories
        EventJpaEntity eventEntity = eventJpaRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        UserJpaEntity userEntity = userJpaRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create and add invitation using managed entities
        EventInvitationJpaEntity invitation = EventInvitationJpaEntity.builder()
                .event(eventEntity)
                .user(userEntity)
                .status(convertToJpaStatus(status)) // Using a local helper method
                .build();

        // Add to the set
        if (eventEntity.getInvitations() == null) {
            eventEntity.setInvitations(new HashSet<>());
        }
        eventEntity.getInvitations().add(invitation);

        // Save and return
        EventJpaEntity savedEntity = eventJpaRepository.save(eventEntity);
        return eventMapper.toDomainEntity(savedEntity);
    }

    // Helper method to convert domain status to JPA status
    private EventInvitationJpaEntity.EventInvitationStatus convertToJpaStatus(Event.InvitationStatus status) {
        switch (status) {
            case INVITED:
                return EventInvitationJpaEntity.EventInvitationStatus.INVITED;
            case CONFIRMED:
                return EventInvitationJpaEntity.EventInvitationStatus.CONFIRMED;
            case DECLINED:
                return EventInvitationJpaEntity.EventInvitationStatus.DECLINED;
            case MAYBE:
                return EventInvitationJpaEntity.EventInvitationStatus.MAYBE;
            default:
                throw new IllegalArgumentException("Unknown status: " + status);
        }
    }
}