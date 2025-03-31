package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Task;
import com.rephelper.infrastructure.config.CommonMapperConfig;
import com.rephelper.infrastructure.entity.TaskJpaEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    @Autowired
    private CommonMapperConfig commonMapperConfig;

    @Autowired
    private RepublicMapper republicMapper;

    @Autowired
    private UserMapper userMapper;

    public Task toDomainEntity(TaskJpaEntity jpaEntity) {
        if (jpaEntity == null) return null;

        Task task = Task.builder()
                .id(jpaEntity.getId())
                .title(jpaEntity.getTitle())
                .description(jpaEntity.getDescription())
                .status(mapToDomainTaskStatus(jpaEntity.getStatus()))
                .dueDate(jpaEntity.getDueDate())
                .completedAt(jpaEntity.getCompletedAt())
                .category(jpaEntity.getCategory())
                .createdAt(jpaEntity.getCreatedAt())
                .updatedAt(jpaEntity.getUpdatedAt())
                // Campos de recorrência
                .isRecurring(jpaEntity.isRecurring())
                .recurrenceType(mapToDomainRecurrenceType(jpaEntity.getRecurrenceType()))
                .recurrenceInterval(jpaEntity.getRecurrenceInterval())
                .recurrenceEndDate(jpaEntity.getRecurrenceEndDate())
                .parentTaskId(jpaEntity.getParentTaskId())
                .createdBy(jpaEntity.getCreatedBy() != null ? userMapper.toDomainEntityWithoutRepublic(jpaEntity.getCreatedBy()) : null)
                .build();

        // Map republic if present
        if (jpaEntity.getRepublic() != null) {
            task = Task.builder()
                    .id(task.getId())
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .republic(republicMapper.toDomainEntityWithoutUsers(jpaEntity.getRepublic()))
                    .status(task.getStatus())
                    .dueDate(task.getDueDate())
                    .completedAt(task.getCompletedAt())
                    .category(task.getCategory())
                    .createdAt(task.getCreatedAt())
                    .updatedAt(task.getUpdatedAt())
                    // Mantém os campos de recorrência
                    .isRecurring(task.isRecurring())
                    .recurrenceType(task.getRecurrenceType())
                    .recurrenceInterval(task.getRecurrenceInterval())
                    .recurrenceEndDate(task.getRecurrenceEndDate())
                    .parentTaskId(task.getParentTaskId())
                    .createdBy(task.getCreatedBy())
                    .build();
        }

        // Map assigned users if present
        if (jpaEntity.getAssignedUsers() != null && !jpaEntity.getAssignedUsers().isEmpty()) {
            task = Task.builder()
                    .id(task.getId())
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .republic(task.getRepublic())
                    .assignedUsers(jpaEntity.getAssignedUsers().stream()
                            .map(userMapper::toDomainEntityWithoutRepublic)
                            .collect(Collectors.toSet()))
                    .status(task.getStatus())
                    .dueDate(task.getDueDate())
                    .completedAt(task.getCompletedAt())
                    .category(task.getCategory())
                    .createdAt(task.getCreatedAt())
                    .updatedAt(task.getUpdatedAt())
                    // Mantém os campos de recorrência
                    .isRecurring(task.isRecurring())
                    .recurrenceType(task.getRecurrenceType())
                    .recurrenceInterval(task.getRecurrenceInterval())
                    .recurrenceEndDate(task.getRecurrenceEndDate())
                    .parentTaskId(task.getParentTaskId())
                    .createdBy(task.getCreatedBy())
                    .build();
        }

        return task;
    }

    public TaskJpaEntity toJpaEntity(Task domainEntity) {
        if (domainEntity == null) return null;

        TaskJpaEntity entity = TaskJpaEntity.builder()
                .id(domainEntity.getId())
                .title(domainEntity.getTitle())
                .description(domainEntity.getDescription())
                .status(mapToJpaTaskStatus(domainEntity.getStatus()))
                .dueDate(domainEntity.getDueDate())
                .completedAt(domainEntity.getCompletedAt())
                .category(domainEntity.getCategory())
                .createdAt(domainEntity.getCreatedAt())
                .updatedAt(domainEntity.getUpdatedAt())
                // Campos de recorrência
                .isRecurring(domainEntity.isRecurring())
                .recurrenceType(mapToJpaRecurrenceType(domainEntity.getRecurrenceType()))
                .recurrenceInterval(domainEntity.getRecurrenceInterval())
                .recurrenceEndDate(domainEntity.getRecurrenceEndDate())
                .parentTaskId(domainEntity.getParentTaskId())
                .createdBy(domainEntity.getCreatedBy() != null ? userMapper.toJpaEntityWithoutRepublic(domainEntity.getCreatedBy()) : null)
                .build();

        // Map republic if present
        if (domainEntity.getRepublic() != null) {
            entity.setRepublic(republicMapper.toJpaEntityWithoutUsers(domainEntity.getRepublic()));
        }

        // Map assigned users if present
        if (domainEntity.getAssignedUsers() != null && !domainEntity.getAssignedUsers().isEmpty()) {
            entity.setAssignedUsers(domainEntity.getAssignedUsers().stream()
                    .map(userMapper::toJpaEntityWithoutRepublic)
                    .collect(Collectors.toSet()));
        } else {
            entity.setAssignedUsers(new HashSet<>());
        }

        return entity;
    }

    // Status mapping methods
    public TaskJpaEntity.TaskStatusJpa mapToJpaTaskStatus(Task.TaskStatus domainStatus) {
        if (domainStatus == null) return null;

        switch (domainStatus) {
            case PENDING:
                return TaskJpaEntity.TaskStatusJpa.PENDING;
            case IN_PROGRESS:
                return TaskJpaEntity.TaskStatusJpa.IN_PROGRESS;
            case COMPLETED:
                return TaskJpaEntity.TaskStatusJpa.COMPLETED;
            case OVERDUE:
                return TaskJpaEntity.TaskStatusJpa.OVERDUE;
            case CANCELLED:
                return TaskJpaEntity.TaskStatusJpa.CANCELLED;
            default:
                throw new IllegalArgumentException("Unknown task status: " + domainStatus);
        }
    }

    public Task.TaskStatus mapToDomainTaskStatus(TaskJpaEntity.TaskStatusJpa jpaStatus) {
        if (jpaStatus == null) return null;

        switch (jpaStatus) {
            case PENDING:
                return Task.TaskStatus.PENDING;
            case IN_PROGRESS:
                return Task.TaskStatus.IN_PROGRESS;
            case COMPLETED:
                return Task.TaskStatus.COMPLETED;
            case OVERDUE:
                return Task.TaskStatus.OVERDUE;
            case CANCELLED:
                return Task.TaskStatus.CANCELLED;
            default:
                throw new IllegalArgumentException("Unknown task status: " + jpaStatus);
        }
    }

    // Recurrence type mapping methods
    public TaskJpaEntity.RecurrenceTypeJpa mapToJpaRecurrenceType(Task.RecurrenceType domainType) {
        if (domainType == null) return null;

        switch (domainType) {
            case DAILY:
                return TaskJpaEntity.RecurrenceTypeJpa.DAILY;
            case WEEKLY:
                return TaskJpaEntity.RecurrenceTypeJpa.WEEKLY;
            case MONTHLY:
                return TaskJpaEntity.RecurrenceTypeJpa.MONTHLY;
            case YEARLY:
                return TaskJpaEntity.RecurrenceTypeJpa.YEARLY;
            default:
                throw new IllegalArgumentException("Unknown recurrence type: " + domainType);
        }
    }

    public Task.RecurrenceType mapToDomainRecurrenceType(TaskJpaEntity.RecurrenceTypeJpa jpaType) {
        if (jpaType == null) return null;

        switch (jpaType) {
            case DAILY:
                return Task.RecurrenceType.DAILY;
            case WEEKLY:
                return Task.RecurrenceType.WEEKLY;
            case MONTHLY:
                return Task.RecurrenceType.MONTHLY;
            case YEARLY:
                return Task.RecurrenceType.YEARLY;
            default:
                throw new IllegalArgumentException("Unknown recurrence type: " + jpaType);
        }
    }
}