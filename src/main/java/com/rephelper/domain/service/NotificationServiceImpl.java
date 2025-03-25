package com.rephelper.domain.service;

import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.model.Notification;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.NotificationServicePort;
import com.rephelper.domain.port.out.NotificationRepositoryPort;
import com.rephelper.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationServicePort {

    private final NotificationRepositoryPort notificationRepository;
    private final UserRepositoryPort userRepository;

    @Override
    public Notification createNotification(UUID recipientId, String title, String message,
                                           Notification.NotificationType type, String entityType, String entityId) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + recipientId));

        Notification notification = Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .type(type)
                .entityType(entityType)
                .entityId(entityId)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUser(UUID userId) {
        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return notificationRepository.findByRecipientId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsByUser(UUID userId) {
        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return notificationRepository.findUnreadByRecipientId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countUnreadNotificationsByUser(UUID userId) {
        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return notificationRepository.countUnreadByRecipientId(userId);
    }

    @Override
    public Notification markNotificationAsRead(Long id) {
        Notification notification = getNotificationById(id);
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Override
    public void markAllNotificationsAsRead(UUID userId) {
        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        notificationRepository.markAllAsReadForRecipient(userId);
    }

    @Override
    public void deleteNotification(Long id, UUID userId) {
        Notification notification = getNotificationById(id);

        // Check if the notification belongs to the user
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    @Override
    public void deleteOldNotifications(UUID userId, int days) {
        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        notificationRepository.deleteOldNotifications(userId, days);
    }

    @Override
    public Notification notifyTaskAssigned(UUID recipientId, Long taskId, String taskTitle) {
        String title = "New Task Assigned";
        String message = "You have been assigned to the task: " + taskTitle;
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.TASK_ASSIGNED,
                "task",
                taskId.toString()
        );
    }

    @Override
    public Notification notifyTaskCompleted(UUID recipientId, Long taskId, String taskTitle, UUID completedById) {
        User completedBy = userRepository.findById(completedById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + completedById));

        String title = "Task Completed";
        String message = "Task '" + taskTitle + "' has been marked as completed by " + completedBy.getName();
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.TASK_COMPLETED,
                "task",
                taskId.toString()
        );
    }

    @Override
    public Notification notifyExpenseCreated(UUID recipientId, Long expenseId, String description, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorId));

        String title = "New Expense Created";
        String message = "A new expense '" + description + "' has been created by " + creator.getName();
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.EXPENSE_CREATED,
                "expense",
                expenseId.toString()
        );
    }

    @Override
    public Notification notifyExpenseApproved(UUID recipientId, Long expenseId, String description) {
        String title = "Expense Approved";
        String message = "Your expense '" + description + "' has been approved";
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.EXPENSE_APPROVED,
                "expense",
                expenseId.toString()
        );
    }

    @Override
    public Notification notifyExpenseRejected(UUID recipientId, Long expenseId, String description, String reason) {
        String title = "Expense Rejected";
        String message = "Your expense '" + description + "' has been rejected. Reason: " + reason;
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.EXPENSE_REJECTED,
                "expense",
                expenseId.toString()
        );
    }

    @Override
    public Notification notifyEventInvitation(UUID recipientId, Long eventId, String eventTitle) {
        String title = "New Event Invitation";
        String message = "You have been invited to the event: " + eventTitle;
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.EVENT_INVITATION,
                "event",
                eventId.toString()
        );
    }
}