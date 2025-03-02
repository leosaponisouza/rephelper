package com.rephelper.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.rephelper.application.dto.request.UpdateTaskRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidade de domínio que representa uma tarefa na república.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Long id;
    private String title;
    private String description;
    private Republic republic;
    @Builder.Default
    private Set<User> assignedUsers = new HashSet<>();
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Status possíveis para uma tarefa
     */
    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, OVERDUE, CANCELLED
    }

    /**
     * Marca a tarefa como concluída
     */
    public void complete() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marca a tarefa como em progresso
     */
    public void startProgress() {
        if (this.status == TaskStatus.PENDING || this.status == TaskStatus.OVERDUE) {
            this.status = TaskStatus.IN_PROGRESS;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Cancela a tarefa
     */
    public void cancel() {
        this.status = TaskStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Atualiza o status baseado na data de vencimento
     */
    public void updateStatus() {
        // Se já está completa ou cancelada, não faz nada
        if (this.status == TaskStatus.COMPLETED || this.status == TaskStatus.CANCELLED) {
            return;
        }

        // Verifica se a tarefa está atrasada
        if (this.dueDate != null && LocalDateTime.now().isAfter(this.dueDate)) {
            this.status = TaskStatus.OVERDUE;
        }
    }

    /**
     * Atualiza informações da tarefa
     */
    public void update(UpdateTaskRequest request) {
        if (title != null && !title.isBlank()) {
            this.title = request.getTitle();
        }

        if (description != null) {
            this.description = request.getDescription();
        }

        if (dueDate != null) {
            this.dueDate = request.getDueDate();
        }

        if (category != null) {
            this.category = request.getCategory();
        }
        if (request.getStatus() != null &&
                request.getStatus().equals(TaskStatus.IN_PROGRESS.toString())) {
            startProgress();
        }
        if (request.getStatus() != null &&
                request.getStatus().equals(TaskStatus.PENDING.toString())) {
            this.status = TaskStatus.PENDING;
        }

        this.updatedAt = LocalDateTime.now();

        // Atualiza o status com base na nova data de vencimento
        updateStatus();

    }

    /**
     * Atribui a tarefa a um usuário
     */
    public void assignTo(User user) {
        if (user != null) {
            this.assignedUsers.add(user);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Remove a atribuição da tarefa a um usuário
     */
    public void unassignFrom(User user) {
        if (user != null) {
            this.assignedUsers.removeIf(u -> u.getId().equals(user.getId()));
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Verifica se a tarefa está atribuída a um usuário
     */
    public boolean isAssignedTo(User user) {
        return user != null &&
                this.assignedUsers.stream()
                        .anyMatch(u -> u.getId().equals(user.getId()));
    }
}