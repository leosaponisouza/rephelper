package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.infrastructure.entity.NotificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * JPA Repository for Notifications
 */
@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationJpaEntity, Long> {

    List<NotificationJpaEntity> findByRecipientUuidOrderByCreatedAtDesc(UUID recipientId);

    @Query("SELECT n FROM NotificationJpaEntity n WHERE n.recipient.uuid = :recipientId AND n.read = false ORDER BY n.createdAt DESC")
    List<NotificationJpaEntity> findUnreadByRecipientId(@Param("recipientId") UUID recipientId);

    @Query("SELECT COUNT(n) FROM NotificationJpaEntity n WHERE n.recipient.uuid = :recipientId AND n.read = false")
    int countUnreadByRecipientId(@Param("recipientId") UUID recipientId);

    List<NotificationJpaEntity> findByTypeAndRecipientUuid(NotificationJpaEntity.NotificationTypeJpa type, UUID recipientId);

    @Modifying
    @Query("UPDATE NotificationJpaEntity n SET n.read = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipient.uuid = :recipientId AND n.read = false")
    void markAllAsReadForRecipient(@Param("recipientId") UUID recipientId);

    @Modifying
    @Query("DELETE FROM NotificationJpaEntity n WHERE n.recipient.uuid = :recipientId AND n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("recipientId") UUID recipientId, @Param("cutoffDate") LocalDateTime cutoffDate);
}