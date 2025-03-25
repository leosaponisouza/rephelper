package com.rephelper.application.dto.response;

import com.rephelper.domain.model.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for notification responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private UUID recipientId;
    private String recipientName;
    private String title;
    private String message;
    private String type; // Enum as string for the frontend
    private String entityType;
    private String entityId;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}