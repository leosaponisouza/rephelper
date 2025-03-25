package com.rephelper.domain.port.in;

import com.rephelper.domain.model.Notification;

import java.util.List;
import java.util.UUID;

/**
 * Port for the notification service
 */
public interface NotificationServicePort {
    /**
     * Creates a new notification
     */
    Notification createNotification(UUID recipientId, String title, String message,
                                    Notification.NotificationType type, String entityType, String entityId);

    /**
     * Gets a notification by ID
     */
    Notification getNotificationById(Long id);

    /**
     * Gets all notifications for a user
     */
    List<Notification> getNotificationsByUser(UUID userId);

    /**
     * Gets unread notifications for a user
     */
    List<Notification> getUnreadNotificationsByUser(UUID userId);

    /**
     * Counts unread notifications for a user
     */
    int countUnreadNotificationsByUser(UUID userId);

    /**
     * Marks a notification as read
     */
    Notification markNotificationAsRead(Long id);

    /**
     * Marks all notifications as read for a user
     */
    void markAllNotificationsAsRead(UUID userId);

    /**
     * Deletes a notification
     */
    void deleteNotification(Long id, UUID userId);

    /**
     * Deletes old notifications for a user
     */
    void deleteOldNotifications(UUID userId, int days);

    /**
     * Creates a task assignment notification
     */
    Notification notifyTaskAssigned(UUID recipientId, Long taskId, String taskTitle);

    /**
     * Creates a task completion notification
     */
    Notification notifyTaskCompleted(UUID recipientId, Long taskId, String taskTitle, UUID completedById);

    /**
     * Creates an expense creation notification
     */
    Notification notifyExpenseCreated(UUID recipientId, Long expenseId, String description, UUID creatorId);

    /**
     * Creates an expense approval notification
     */
    Notification notifyExpenseApproved(UUID recipientId, Long expenseId, String description);

    /**
     * Creates an expense rejection notification
     */
    Notification notifyExpenseRejected(UUID recipientId, Long expenseId, String description, String reason);

    /**
     * Creates an expense added notification
     */
    Notification notifyExpenseAdded(UUID recipientId, Long expenseId, String description, UUID creatorId);

    /**
     * Creates an expense paid notification
     */
    Notification notifyExpensePaid(UUID recipientId, Long expenseId, String description, UUID paidById);

    /**
     * Creates an event invitation notification
     */
    Notification notifyEventInvitation(UUID recipientId, Long eventId, String eventTitle);

    /**
     * Creates an event creation notification
     */
    Notification notifyEventCreated(UUID recipientId, Long eventId, String eventTitle, UUID creatorId);

    /**
     * Creates a republic invitation notification
     */
    Notification notifyRepublicInvitation(UUID recipientId, UUID republicId, String republicName, UUID invitedById);

    /**
     * Creates a republic left notification
     */
    Notification notifyRepublicLeft(UUID recipientId, UUID republicId, String republicName, UUID userId, String userName);

    /**
     * Creates a system notification
     */
    Notification notifySystemNotification(UUID recipientId, String title, String message);

    /**
     * Creates a task due soon notification
     */
    Notification notifyTaskDueSoon(UUID recipientId, Long taskId, String taskTitle, String message);

    /**
     * Creates a task overdue notification
     */
    Notification notifyTaskOverdue(UUID recipientId, Long taskId, String taskTitle, String message);
}