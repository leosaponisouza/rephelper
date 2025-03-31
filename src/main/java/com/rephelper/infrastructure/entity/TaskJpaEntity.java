package com.rephelper.infrastructure.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidade JPA para persistência de tarefas no banco de dados.
 */
@Entity
@Table(name = "tasks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TaskJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tasks_id_seq_gen")
    @SequenceGenerator(name = "tasks_id_seq_gen", sequenceName = "tasks_id_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "republic_id", nullable = false)
    private RepublicJpaEntity republic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserJpaEntity createdBy;

    @ManyToMany
    @JoinTable(
            name = "user_tasks",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<UserJpaEntity> assignedUsers = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatusJpa status;  // Alterado para usar TaskStatusJpa

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    private String category;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Campos para recorrência
    @Column(name = "is_recurring")
    private boolean isRecurring;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type")
    private RecurrenceTypeJpa recurrenceType;  // Alterado para usar RecurrenceTypeJpa

    @Column(name = "recurrence_interval")
    private Integer recurrenceInterval;

    @Column(name = "recurrence_end_date")
    private LocalDateTime recurrenceEndDate;

    @Column(name = "parent_task_id")
    private Long parentTaskId;

    /**
     * Status possíveis para uma tarefa na camada de persistência
     */
    public enum TaskStatusJpa {
        PENDING, IN_PROGRESS, COMPLETED, OVERDUE, CANCELLED
    }

    /**
     * Tipos de recorrência possíveis na camada de persistência
     */
    public enum RecurrenceTypeJpa {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}