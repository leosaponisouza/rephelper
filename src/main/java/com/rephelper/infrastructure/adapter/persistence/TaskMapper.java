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
    public Task.TaskStatus mapToJpaTaskStatus(Task.TaskStatus domainStatus) {
        if (domainStatus == null) return null;

        switch (domainStatus) {
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
                throw new IllegalArgumentException("Unknown task status: " + domainStatus);
        }
    }

    public Task.TaskStatus mapToDomainTaskStatus(Task.TaskStatus jpaStatus) {
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
}