package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Notification;
import com.rephelper.domain.port.out.NotificationRepositoryPort;
import com.rephelper.infrastructure.entity.NotificationJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of NotificationRepositoryPort using JPA
 */
@Component
@RequiredArgsConstructor
public class NotificationJpaAdapter implements NotificationRepositoryPort {

    private final NotificationJpaRepository notificationJpaRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public Notification save(Notification notification) {
        NotificationJpaEntity entity = notificationMapper.toJpaEntity(notification);
        NotificationJpaEntity savedEntity = notificationJpaRepository.save(entity);
        return notificationMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return notificationJpaRepository.findById(id)
                .map(notificationMapper::toDomainEntity);
    }

    @Override
    public List<Notification> findAll() {
        return notificationJpaRepository.findAll().stream()
                .map(notificationMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findByRecipientId(UUID recipientId) {
        return notificationJpaRepository.findByRecipientUuidOrderByCreatedAtDesc(recipientId).stream()
                .map(notificationMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findUnreadByRecipientId(UUID recipientId) {
        return notificationJpaRepository.findUnreadByRecipientId(recipientId).stream()
                .map(notificationMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int countUnreadByRecipientId(UUID recipientId) {
        return notificationJpaRepository.countUnreadByRecipientId(recipientId);
    }

    @Override
    public List<Notification> findByTypeAndRecipientId(Notification.NotificationType type, UUID recipientId) {
        NotificationJpaEntity.NotificationTypeJpa jpaType = notificationMapper.mapToJpaNotificationType(type);
        return notificationJpaRepository.findByTypeAndRecipientUuid(jpaType, recipientId).stream()
                .map(notificationMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void markAllAsReadForRecipient(UUID recipientId) {
        notificationJpaRepository.markAllAsReadForRecipient(recipientId);
    }

    @Override
    public void delete(Notification notification) {
        notificationJpaRepository.deleteById(notification.getId());
    }

    @Override
    public void deleteOldNotifications(UUID recipientId, int days) {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
        notificationJpaRepository.deleteOldNotifications(recipientId, cutoffDate);
    }
}