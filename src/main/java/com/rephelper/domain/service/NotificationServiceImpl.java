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
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + recipientId));

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
                .orElseThrow(() -> new ResourceNotFoundException("Notificação não encontrada com id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUser(UUID userId) {
        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + userId);
        }

        return notificationRepository.findByRecipientId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsByUser(UUID userId) {
        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + userId);
        }

        return notificationRepository.findUnreadByRecipientId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countUnreadNotificationsByUser(UUID userId) {
        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + userId);
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
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + userId);
        }

        notificationRepository.markAllAsReadForRecipient(userId);
    }

    @Override
    public void deleteNotification(Long id, UUID userId) {
        Notification notification = getNotificationById(id);

        // Check if the notification belongs to the user
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ForbiddenException("Você não tem permissão para excluir esta notificação");
        }

        notificationRepository.delete(notification);
    }

    @Override
    public void deleteOldNotifications(UUID userId, int days) {
        // Verify user exists
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + userId);
        }

        notificationRepository.deleteOldNotifications(userId, days);
    }

    @Override
    public Notification notifyTaskAssigned(UUID recipientId, Long taskId, String taskTitle) {
        String title = "Nova Tarefa Atribuída";
        String message = "Você foi designado para a tarefa: " + taskTitle;
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + completedById));

        String title = "Tarefa Concluída";
        String message = "A tarefa '" + taskTitle + "' foi marcada como concluída por " + (completedBy.getNickname() != null
                ? completedBy.getNickname() : completedBy.getName());
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
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + creatorId));

        String title = "Nova Despesa Criada";
        String message = "Uma nova despesa '" + description + "' foi criada por " + (creator.getNickname() != null
                ? creator.getNickname() : creator.getName());
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
        String title = "Despesa Aprovada";
        String message = "Sua despesa '" + description + "' foi aprovada";
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
        String title = "Despesa Rejeitada";
        String message = "Sua despesa '" + description + "' foi rejeitada. Motivo: " + reason;
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
    public Notification notifyExpenseAdded(UUID recipientId, Long expenseId, String description, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + creatorId));

        String title = "Nova Despesa Adicionada";
        String message = "Uma nova despesa '" + description + "' foi adicionada por " + (creator.getNickname() != null
                ? creator.getNickname() : creator.getName());;
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.EXPENSE_ADDED,
                "expense",
                expenseId.toString()
        );
    }

    @Override
    public Notification notifyEventInvitation(UUID recipientId, Long eventId, String eventTitle) {
        String title = "Novo Convite para Evento";
        String message = "Você foi convidado para o evento: " + eventTitle;
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.EVENT_INVITATION,
                "event",
                eventId.toString()
        );
    }

    @Override
    public Notification notifyExpensePaid(UUID recipientId, Long expenseId, String description, UUID paidById) {
        User paidBy = userRepository.findById(paidById)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + paidById));

        String title = "Despesa Paga";
        String message = "A despesa '" + description + "' foi paga por " + (paidBy.getNickname() != null
                ? paidBy.getNickname() : paidBy.getName());
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.EXPENSE_PAID,
                "expense",
                expenseId.toString()
        );
    }

    @Override
    public Notification notifyEventCreated(UUID recipientId, Long eventId, String eventTitle, UUID creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + creatorId));

        String title = "Novo Evento Criado";
        String message = "Um novo evento '" + eventTitle + "' foi criado por " + (creator.getNickname() != null
                ? creator.getNickname() : creator.getName());
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.EVENT_CREATED,
                "event",
                eventId.toString()
        );
    }

    @Override
    public Notification notifyRepublicInvitation(UUID recipientId, UUID republicId, String republicName, UUID invitedById) {
        User invitedBy = userRepository.findById(invitedById)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + invitedById));

        String title = "Convite para República";
        String message = "Você foi convidado por " + invitedBy.getName() + " para participar da república '" + republicName + "'";
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.REPUBLIC_INVITATION,
                "republic",
                republicId.toString()
        );
    }

    @Override
    public Notification notifyRepublicLeft(UUID recipientId, UUID republicId, String republicName, UUID userId, String userName) {
        String title = "Membro Saiu da República";
        String message = userName + " saiu da república '" + republicName + "'";
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.REPUBLIC_LEFT,
                "republic",
                republicId.toString()
        );
    }

    @Override
    public Notification notifySystemNotification(UUID recipientId, String title, String message) {
        return createNotification(
                recipientId,
                title,
                message,
                Notification.NotificationType.SYSTEM_NOTIFICATION,
                "system",
                null
        );
    }

    @Override
    public Notification notifyTaskDueSoon(UUID recipientId, Long taskId, String taskTitle, String message) {
        return createNotification(
                recipientId,
                "Tarefa com prazo próximo",
                message,
                Notification.NotificationType.TASK_DUE_SOON,
                "task",
                taskId.toString()
        );
    }
    
    @Override
    public Notification notifyTaskOverdue(UUID recipientId, Long taskId, String taskTitle, String message) {
        return createNotification(
                recipientId,
                "Tarefa atrasada",
                message,
                Notification.NotificationType.TASK_OVERDUE,
                "task",
                taskId.toString()
        );
    }
}