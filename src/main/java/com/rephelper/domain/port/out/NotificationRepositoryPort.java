package com.rephelper.domain.port.out;

import com.rephelper.domain.model.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port for the notification repository
 */
public interface NotificationRepositoryPort {
    /**
     * Saves a notification
     */
    Notification save(Notification notification);

    /**
     * Finds a notification by its ID
     */
    Optional<Notification> findById(Long id);

    /**
     * Finds all notifications
     */
    List<Notification> findAll();

    /**
     * Finds notifications by recipient ID
     */
    List<Notification> findByRecipientId(UUID recipientId);

    /**
     * Finds unread notifications by recipient ID
     */
    List<Notification> findUnreadByRecipientId(UUID recipientId);

    /**
     * Counts unread notifications by recipient ID
     */
    int countUnreadByRecipientId(UUID recipientId);

    /**
     * Finds notifications by type and recipient ID
     */
    List<Notification> findByTypeAndRecipientId(Notification.NotificationType type, UUID recipientId);

    /**
     * Marks all notifications as read for a recipient
     */
    void markAllAsReadForRecipient(UUID recipientId);

    /**
     * Deletes a notification
     */
    void delete(Notification notification);

    /**
     * Deletes notifications older than specified days for a recipient
     */
    void deleteOldNotifications(UUID recipientId, int days);
}