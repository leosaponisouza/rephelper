package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Event;
import com.rephelper.domain.model.EventInvitation;
import com.rephelper.infrastructure.config.CommonMapperConfig;
import com.rephelper.infrastructure.entity.EventInvitationJpaEntity;
import com.rephelper.infrastructure.entity.EventJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre entidades de domínio e entidades JPA para eventos.
 */
@Component
public class EventMapper {

    @Autowired
    private CommonMapperConfig commonMapperConfig;

    @Autowired
    private RepublicMapper republicMapper;

    @Autowired
    private UserMapper userMapper;

    public Event toDomainEntity(EventJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        Event event = Event.builder()
                .id(jpaEntity.getId())
                .title(jpaEntity.getTitle())
                .description(jpaEntity.getDescription())
                .startDate(jpaEntity.getStartDate())
                .endDate(jpaEntity.getEndDate())
                .location(jpaEntity.getLocation())
                .createdAt(jpaEntity.getCreatedAt())
                .build();

        // Map republic if present
        if (jpaEntity.getRepublic() != null) {
            event = Event.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .location(event.getLocation())
                    .republic(republicMapper.toDomainEntityWithoutUsers(jpaEntity.getRepublic()))
                    .createdAt(event.getCreatedAt())
                    .build();
        }

        // Map creator if present
        if (jpaEntity.getCreator() != null) {
            event = Event.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .location(event.getLocation())
                    .republic(event.getRepublic())
                    .creator(userMapper.toDomainEntityWithoutRepublic(jpaEntity.getCreator()))
                    .createdAt(event.getCreatedAt())
                    .build();
        }

        // Map invitations if present
        if (jpaEntity.getInvitations() != null && !jpaEntity.getInvitations().isEmpty()) {
            Set<EventInvitation> invitations = jpaEntity.getInvitations().stream()
                    .map(this::toDomainInvitation)
                    .collect(Collectors.toSet());

            event = Event.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .description(event.getDescription())
                    .startDate(event.getStartDate())
                    .endDate(event.getEndDate())
                    .location(event.getLocation())
                    .republic(event.getRepublic())
                    .creator(event.getCreator())
                    .invitations(invitations)
                    .createdAt(event.getCreatedAt())
                    .build();
        }

        return event;
    }

    public EventJpaEntity toJpaEntity(Event domainEntity) {
        if (domainEntity == null) return null;

        EventJpaEntity entity = EventJpaEntity.builder()
                .id(domainEntity.getId())
                .title(domainEntity.getTitle())
                .description(domainEntity.getDescription())
                .startDate(domainEntity.getStartDate())
                .endDate(domainEntity.getEndDate())
                .location(domainEntity.getLocation())
                .creator(userMapper.toJpaEntity(domainEntity.getCreator()))
                .republic(republicMapper.toJpaEntity(domainEntity.getRepublic()))
                .createdAt(domainEntity.getCreatedAt())
                .build();

        // Map invitations if present, but only use IDs
        if (domainEntity.getInvitations() != null && !domainEntity.getInvitations().isEmpty()) {
            Set<EventInvitationJpaEntity> invitationEntities = new HashSet<>();

            for (EventInvitation invitation : domainEntity.getInvitations()) {
                // Create invitation with just the ID reference, not the full entity
                EventInvitationJpaEntity invitationEntity = EventInvitationJpaEntity.builder()
                        .event(entity)
                        .build();

                // Instead of mapping the full User entity, just set the ID
                if (invitation.getUser() != null) {
                    UserJpaEntity userReference = new UserJpaEntity();
                    userReference.setUuid(invitation.getUser().getId());
                    invitationEntity.setUser(userReference);
                }

                invitationEntity.setStatus(mapToJpaInvitationStatus(invitation.getStatus()));
                invitationEntities.add(invitationEntity);
            }

            entity.setInvitations(invitationEntities);
        }

        return entity;
    }
    private EventInvitation toDomainInvitation(EventInvitationJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        return EventInvitation.builder()
                .user(userMapper.toDomainEntityWithoutRepublic(jpaEntity.getUser()))
                .status(mapToDomainInvitationStatus(jpaEntity.getStatus()))
                .build();
    }

    private EventInvitationJpaEntity toJpaInvitation(EventInvitation domainEntity, EventJpaEntity eventEntity) {
        if (domainEntity == null) return null;

        return EventInvitationJpaEntity.builder()
                .user(userMapper.toJpaEntityWithoutRepublic(domainEntity.getUser()))
                .event(eventEntity)
                .status(mapToJpaInvitationStatus(domainEntity.getStatus()))
                .build();
    }

    // Status mapping methods
    private Event.InvitationStatus mapToDomainInvitationStatus(EventInvitationJpaEntity.EventInvitationStatus jpaStatus) {
        if (jpaStatus == null) return null;

        switch (jpaStatus) {
            case INVITED:
                return Event.InvitationStatus.INVITED;
            case CONFIRMED:
                return Event.InvitationStatus.CONFIRMED;
            case DECLINED:
                return Event.InvitationStatus.DECLINED;
            default:
                throw new IllegalArgumentException("Unknown invitation status: " + jpaStatus);
        }
    }

    private EventInvitationJpaEntity.EventInvitationStatus mapToJpaInvitationStatus(Event.InvitationStatus domainStatus) {
        if (domainStatus == null) return null;

        switch (domainStatus) {
            case INVITED:
                return EventInvitationJpaEntity.EventInvitationStatus.INVITED;
            case CONFIRMED:
                return EventInvitationJpaEntity.EventInvitationStatus.CONFIRMED;
            case DECLINED:
                return EventInvitationJpaEntity.EventInvitationStatus.DECLINED;
            default:
                throw new IllegalArgumentException("Unknown invitation status: " + domainStatus);
        }
    }
}
