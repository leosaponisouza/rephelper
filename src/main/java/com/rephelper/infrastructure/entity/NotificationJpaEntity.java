package com.rephelper.infrastructure.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity for representing a notification
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifications_id_seq_gen")
    @SequenceGenerator(name = "notifications_id_seq_gen", sequenceName = "notifications_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private UserJpaEntity recipient;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationTypeJpa type;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(nullable = false)
    private boolean read;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Types of notifications
     */
    public enum NotificationTypeJpa {
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
}