package com.rephelper.infrastructure.adapter.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.rephelper.domain.model.Task;
import com.rephelper.domain.port.out.TaskRepositoryPort;
import com.rephelper.infrastructure.entity.TaskJpaEntity;

import lombok.RequiredArgsConstructor;

/**
 * Implementação do adaptador para o repositório de tarefas usando JPA.
 */
@Component
@RequiredArgsConstructor
public class TaskJpaAdapter implements TaskRepositoryPort {

    private final TaskJpaRepository taskJpaRepository;
    private final TaskMapper taskMapper;

    @Override
    public Task save(Task task) {
        TaskJpaEntity taskEntity = taskMapper.toJpaEntity(task);
        TaskJpaEntity savedEntity = taskJpaRepository.save(taskEntity);
        return taskMapper.toDomainEntity(savedEntity);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskJpaRepository.findById(id)
                .map(taskMapper::toDomainEntity);
    }

    @Override
    public List<Task> findAll() {
        return taskJpaRepository.findAll().stream()
                .map(taskMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByRepublicId(UUID republicId) {
        return taskJpaRepository.findByRepublicUuid(republicId).stream()
                .map(taskMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByRepublicIdAndCategory(UUID republicId, String category) {
        return taskJpaRepository.findByRepublicUuidAndCategory(republicId, category).stream()
                .map(taskMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByRepublicIdAndStatus(UUID republicId, Task.TaskStatus status) {
        Task.TaskStatus jpaStatus = taskMapper.mapToJpaTaskStatus(status);
        return taskJpaRepository.findByRepublicUuidAndStatus(republicId, jpaStatus).stream()
                .map(taskMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByAssignedUserId(UUID userId) {
        return taskJpaRepository.findByAssignedUserId(userId).stream()
                .map(taskMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> findByAssignedUserIdAndRepublicId(UUID userId, UUID republicId) {
        return taskJpaRepository.findByAssignedUserIdAndRepublicId(userId, republicId).stream()
                .map(taskMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Task task) {
        taskJpaRepository.deleteById(task.getId());
    }
}