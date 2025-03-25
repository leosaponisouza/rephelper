package com.rephelper.application.mapper;

import com.rephelper.application.dto.response.NotificationResponse;
import com.rephelper.domain.model.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for Notification DTOs
 */
@Component
public class NotificationDtoMapper {

    /**
     * Maps Notification domain object to NotificationResponse
     */
    public NotificationResponse toNotificationResponse(Notification notification) {
        if (notification == null) return null;

        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipient() != null ? notification.getRecipient().getId() : null)
                .recipientName(notification.getRecipient() != null ? notification.getRecipient().getName() : null)
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType() != null ? notification.getType().name() : null)
                .entityType(notification.getEntityType())
                .entityId(notification.getEntityId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    /**
     * Maps a list of Notification domain objects to a list of NotificationResponse DTOs
     */
    public List<NotificationResponse> toNotificationResponseList(List<Notification> notifications) {
        if (notifications == null) return null;

        return notifications.stream()
                .map(this::toNotificationResponse)
                .collect(Collectors.toList());
    }
}