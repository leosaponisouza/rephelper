package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Notification;
import com.rephelper.domain.model.User;
import com.rephelper.infrastructure.entity.NotificationJpaEntity;
import com.rephelper.infrastructure.entity.UserJpaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Maps between Notification domain model and NotificationJpaEntity
 */
@Component
public class NotificationMapper {

    @Autowired
    private UserMapper userMapper;

    /**
     * Converts a JPA entity to a domain entity
     */
    public Notification toDomainEntity(NotificationJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        User recipient = null;
        if (jpaEntity.getRecipient() != null) {
            recipient = userMapper.toDomainEntityWithoutRepublic(jpaEntity.getRecipient());
        }

        return Notification.builder()
                .id(jpaEntity.getId())
                .recipient(recipient)
                .title(jpaEntity.getTitle())
                .message(jpaEntity.getMessage())
                .type(mapToDomainNotificationType(jpaEntity.getType()))
                .entityType(jpaEntity.getEntityType())
                .entityId(jpaEntity.getEntityId())
                .read(jpaEntity.isRead())
                .createdAt(jpaEntity.getCreatedAt())
                .readAt(jpaEntity.getReadAt())
                .build();
    }

    /**
     * Converts a domain entity to a JPA entity
     */
    public NotificationJpaEntity toJpaEntity(Notification domainEntity) {
        if (domainEntity == null) return null;

        UserJpaEntity recipientEntity = null;
        if (domainEntity.getRecipient() != null) {
            recipientEntity = userMapper.toJpaEntityWithoutRepublic(domainEntity.getRecipient());
        }

        return NotificationJpaEntity.builder()
                .id(domainEntity.getId())
                .recipient(recipientEntity)
                .title(domainEntity.getTitle())
                .message(domainEntity.getMessage())
                .type(mapToJpaNotificationType(domainEntity.getType()))
                .entityType(domainEntity.getEntityType())
                .entityId(domainEntity.getEntityId())
                .read(domainEntity.isRead())
                .createdAt(domainEntity.getCreatedAt())
                .readAt(domainEntity.getReadAt())
                .build();
    }

    /**
     * Maps domain notification type to JPA notification type
     */
    NotificationJpaEntity.NotificationTypeJpa mapToJpaNotificationType(Notification.NotificationType type) {
        if (type == null) return null;

        switch (type) {
            case TASK_ASSIGNED:
                return NotificationJpaEntity.NotificationTypeJpa.TASK_ASSIGNED;
            case TASK_COMPLETED:
                return NotificationJpaEntity.NotificationTypeJpa.TASK_COMPLETED;
            case TASK_DUE_SOON:
                return NotificationJpaEntity.NotificationTypeJpa.TASK_DUE_SOON;
            case TASK_OVERDUE:
                return NotificationJpaEntity.NotificationTypeJpa.TASK_OVERDUE;
            case EXPENSE_CREATED:
                return NotificationJpaEntity.NotificationTypeJpa.EXPENSE_CREATED;
            case EXPENSE_APPROVED:
                return NotificationJpaEntity.NotificationTypeJpa.EXPENSE_APPROVED;
            case EXPENSE_REJECTED:
                return NotificationJpaEntity.NotificationTypeJpa.EXPENSE_REJECTED;
            case EXPENSE_REIMBURSED:
                return NotificationJpaEntity.NotificationTypeJpa.EXPENSE_REIMBURSED;
            case EXPENSE_ADDED:
                return NotificationJpaEntity.NotificationTypeJpa.EXPENSE_ADDED;
            case EXPENSE_PAID:
                return NotificationJpaEntity.NotificationTypeJpa.EXPENSE_PAID;
            case INCOME_CREATED:
                return NotificationJpaEntity.NotificationTypeJpa.INCOME_CREATED;
            case EVENT_INVITATION:
                return NotificationJpaEntity.NotificationTypeJpa.EVENT_INVITATION;
            case EVENT_REMINDER:
                return NotificationJpaEntity.NotificationTypeJpa.EVENT_REMINDER;
            case EVENT_CREATED:
                return NotificationJpaEntity.NotificationTypeJpa.EVENT_CREATED;
            case REPUBLIC_JOINED:
                return NotificationJpaEntity.NotificationTypeJpa.REPUBLIC_JOINED;
            case REPUBLIC_LEFT:
                return NotificationJpaEntity.NotificationTypeJpa.REPUBLIC_LEFT;
            case REPUBLIC_INVITATION:
                return NotificationJpaEntity.NotificationTypeJpa.REPUBLIC_INVITATION;
            case ADMIN_ASSIGNED:
                return NotificationJpaEntity.NotificationTypeJpa.ADMIN_ASSIGNED;
            case SYSTEM_MESSAGE:
                return NotificationJpaEntity.NotificationTypeJpa.SYSTEM_MESSAGE;
            case SYSTEM_NOTIFICATION:
                return NotificationJpaEntity.NotificationTypeJpa.SYSTEM_NOTIFICATION;
            default:
                throw new IllegalArgumentException("Unknown notification type: " + type);
        }
    }

    /**
     * Maps JPA notification type to domain notification type
     */
    private Notification.NotificationType mapToDomainNotificationType(NotificationJpaEntity.NotificationTypeJpa type) {
        if (type == null) return null;

        return switch (type) {
            case TASK_ASSIGNED -> Notification.NotificationType.TASK_ASSIGNED;
            case TASK_COMPLETED -> Notification.NotificationType.TASK_COMPLETED;
            case TASK_DUE_SOON -> Notification.NotificationType.TASK_DUE_SOON;
            case TASK_OVERDUE -> Notification.NotificationType.TASK_OVERDUE;
            case EXPENSE_CREATED -> Notification.NotificationType.EXPENSE_CREATED;
            case EXPENSE_APPROVED -> Notification.NotificationType.EXPENSE_APPROVED;
            case EXPENSE_REJECTED -> Notification.NotificationType.EXPENSE_REJECTED;
            case EXPENSE_REIMBURSED -> Notification.NotificationType.EXPENSE_REIMBURSED;
            case EXPENSE_ADDED -> Notification.NotificationType.EXPENSE_ADDED;
            case EXPENSE_PAID -> Notification.NotificationType.EXPENSE_PAID;
            case INCOME_CREATED -> Notification.NotificationType.INCOME_CREATED;
            case EVENT_INVITATION -> Notification.NotificationType.EVENT_INVITATION;
            case EVENT_REMINDER -> Notification.NotificationType.EVENT_REMINDER;
            case EVENT_CREATED -> Notification.NotificationType.EVENT_CREATED;
            case REPUBLIC_JOINED -> Notification.NotificationType.REPUBLIC_JOINED;
            case REPUBLIC_LEFT -> Notification.NotificationType.REPUBLIC_LEFT;
            case REPUBLIC_INVITATION -> Notification.NotificationType.REPUBLIC_INVITATION;
            case ADMIN_ASSIGNED -> Notification.NotificationType.ADMIN_ASSIGNED;
            case SYSTEM_MESSAGE -> Notification.NotificationType.SYSTEM_MESSAGE;
            case SYSTEM_NOTIFICATION -> Notification.NotificationType.SYSTEM_NOTIFICATION;
            default -> throw new IllegalArgumentException("Unknown notification type: " + type);
        };
    }
}