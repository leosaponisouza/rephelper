package com.rephelper.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a notification in the system
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long id;
    private User recipient;
    private String title;
    private String message;
    private NotificationType type;
    private String entityType;
    private String entityId;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    /**
     * Types of notifications
     */
    public enum NotificationType {
        TASK_ASSIGNED,       // When a task is assigned to a user
        TASK_COMPLETED,      // When a task is marked as completed
        TASK_DUE_SOON,       // When a task is due soon
        TASK_OVERDUE,        // When a task is overdue
        EXPENSE_CREATED,     // When a new expense is created
        EXPENSE_APPROVED,    // When an expense is approved
        EXPENSE_REJECTED,    // When an expense is rejected
        EXPENSE_REIMBURSED,  // When an expense is reimbursed
        EXPENSE_ADDED,       // When a new expense is added
        EXPENSE_PAID,        // When an expense is paid
        INCOME_CREATED,      // When a new income is registered
        EVENT_INVITATION,    // When a user is invited to an event
        EVENT_REMINDER,      // When an event is happening soon
        EVENT_CREATED,       // When a new event is created
        REPUBLIC_JOINED,     // When a user joins a republic
        REPUBLIC_LEFT,       // When a user leaves a republic
        REPUBLIC_INVITATION, // When a user is invited to join a republic
        ADMIN_ASSIGNED,      // When a user is made admin
        SYSTEM_MESSAGE,      // For general system messages
        SYSTEM_NOTIFICATION  // For general system notifications
    }

    /**
     * Marks the notification as read
     */
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Marks the notification as unread
     */
    public void markAsUnread() {
        this.read = false;
        this.readAt = null;
    }
}