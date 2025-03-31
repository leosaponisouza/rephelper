package com.rephelper.domain.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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
    private User createdBy;
    @Builder.Default
    private Set<User> assignedUsers = new HashSet<>();
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Campos para recorrência
    @Builder.Default
    private boolean isRecurring = false;
    private RecurrenceType recurrenceType;
    private Integer recurrenceInterval; // Intervalo de recorrência (ex: a cada 2 semanas)
    private LocalDateTime recurrenceEndDate; // Data final da recorrência (opcional)
    private Long parentTaskId; // ID da tarefa pai (para tarefas geradas por recorrência)

    /**
     * Status possíveis para uma tarefa
     */
    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, OVERDUE, CANCELLED
    }
    
    /**
     * Tipos de recorrência possíveis
     */
    public enum RecurrenceType {
        DAILY, WEEKLY, MONTHLY, YEARLY
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
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            this.title = request.getTitle();
        }

        if (request.getDescription() != null) {
            this.description = request.getDescription();
        }

        if (request.getDueDate() != null) {
            this.dueDate = request.getDueDate();
        }

        if (request.getCategory() != null) {
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
        
        // Atualiza informações de recorrência
        if (request.getRecurring() != null) {
            this.isRecurring = request.getRecurring();
        }
        
        if (request.getRecurrenceType() != null) {
            this.recurrenceType = RecurrenceType.valueOf(request.getRecurrenceType());
        }
        
        if (request.getRecurrenceInterval() != null) {
            this.recurrenceInterval = request.getRecurrenceInterval();
        }
        
        if (request.getRecurrenceEndDate() != null) {
            this.recurrenceEndDate = request.getRecurrenceEndDate();
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
    
    /**
     * Calcula a próxima data de vencimento com base na recorrência
     */
    public LocalDateTime calculateNextDueDate() {
        if (!isRecurring || dueDate == null || recurrenceType == null || recurrenceInterval == null) {
            return null;
        }
        
        LocalDateTime nextDueDate = null;
        
        switch (recurrenceType) {
            case DAILY:
                nextDueDate = dueDate.plusDays(recurrenceInterval);
                break;
            case WEEKLY:
                nextDueDate = dueDate.plusWeeks(recurrenceInterval);
                break;
            case MONTHLY:
                nextDueDate = dueDate.plusMonths(recurrenceInterval);
                break;
            case YEARLY:
                nextDueDate = dueDate.plusYears(recurrenceInterval);
                break;
        }
        
        return nextDueDate;
    }
    
    /**
     * Verifica se a recorrência deve continuar
     */
    public boolean shouldContinueRecurrence() {
        if (!isRecurring) {
            return false;
        }
        
        LocalDateTime nextDueDate = calculateNextDueDate();
        
        // Se não há próxima data, não continua
        if (nextDueDate == null) {
            return false;
        }
        
        // Se há data final de recorrência e a próxima data é depois, não continua
        if (recurrenceEndDate != null && nextDueDate.isAfter(recurrenceEndDate)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Cria uma nova instância de tarefa baseada nesta, para recorrência
     */
    public Task createRecurringInstance() {
        if (!shouldContinueRecurrence()) {
            return null;
        }
        
        LocalDateTime nextDueDate = calculateNextDueDate();
        
        Task newTask = Task.builder()
                .title(this.title)
                .description(this.description)
                .republic(this.republic)
                .status(TaskStatus.PENDING)
                .dueDate(nextDueDate)
                .category(this.category)
                .isRecurring(this.isRecurring)
                .recurrenceType(this.recurrenceType)
                .recurrenceInterval(this.recurrenceInterval)
                .recurrenceEndDate(this.recurrenceEndDate)
                .parentTaskId(this.id)
                .createdBy(this.createdBy)
                .build();
        
        // Copia os usuários atribuídos
        for (User user : this.assignedUsers) {
            newTask.assignTo(user);
        }
        
        return newTask;
    }

    public void setCreatedBy(User user) {
        this.createdBy = user;
    }
}