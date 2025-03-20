package com.rephelper.infrastructure.adapter.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.rephelper.application.dto.request.TaskFilterRequest;
import com.rephelper.domain.model.Task;
import com.rephelper.domain.port.out.TaskRepositoryPort;
import com.rephelper.infrastructure.adapter.persistence.specification.TaskSpecification;
import com.rephelper.infrastructure.adapter.persistence.util.QueryUtils;
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
        // Ensure createdAt is set for new tasks
        if (task.getId() == null && task.getCreatedAt() == null) {
            task = Task.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .republic(task.getRepublic())
                .assignedUsers(task.getAssignedUsers())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .category(task.getCategory())
                .createdAt(LocalDateTime.now())
                .updatedAt(task.getUpdatedAt())
                .isRecurring(task.isRecurring())
                .recurrenceType(task.getRecurrenceType())
                .recurrenceInterval(task.getRecurrenceInterval())
                .recurrenceEndDate(task.getRecurrenceEndDate())
                .parentTaskId(task.getParentTaskId())
                .build();
        }
        
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
        return taskJpaRepository.findByRepublicUuidAndStatus(republicId, status).stream()
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
    
    @Override
    public Page<Task> findWithFilters(UUID republicId, TaskFilterRequest filter, Pageable pageable) {
        // Criar o objeto Pageable com base nos parâmetros de paginação e ordenação
        // Garantir que os parâmetros de ordenação do filter sejam sempre respeitados
        Pageable pageRequest;
        
        if (filter != null && (filter.getSortBy() != null || filter.getSortDirection() != null)) {
            // Se o filter tem parâmetros de ordenação, usá-los
            pageRequest = QueryUtils.createPageRequest(filter);
            
            // Se pageable não for nulo, manter apenas a paginação (page, size) do pageable
            if (pageable != null) {
                pageRequest = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    pageRequest.getSort()
                );
            }
        } else {
            // Se não há parâmetros de ordenação no filter, usar o pageable ou um padrão
            pageRequest = pageable != null ? pageable : QueryUtils.createPageRequest(new TaskFilterRequest());
        }
        
        // Aplicar os filtros usando a especificação
        Page<TaskJpaEntity> taskEntities = taskJpaRepository.findAll(
                TaskSpecification.withFilters(republicId, filter),
                pageRequest
        );
        
        // Mapear os resultados para entidades de domínio
        return taskEntities.map(taskMapper::toDomainEntity);
    }
    
    @Override
    public Page<Task> findAssignedWithFilters(UUID userId, TaskFilterRequest filter, Pageable pageable) {
        // Criar o objeto Pageable com base nos parâmetros de paginação e ordenação
        // Garantir que os parâmetros de ordenação do filter sejam sempre respeitados
        Pageable pageRequest;
        
        if (filter != null && (filter.getSortBy() != null || filter.getSortDirection() != null)) {
            // Se o filter tem parâmetros de ordenação, usá-los
            pageRequest = QueryUtils.createPageRequest(filter);
            
            // Se pageable não for nulo, manter apenas a paginação (page, size) do pageable
            if (pageable != null) {
                pageRequest = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    pageRequest.getSort()
                );
            }
        } else {
            // Se não há parâmetros de ordenação no filter, usar o pageable ou um padrão
            pageRequest = pageable != null ? pageable : QueryUtils.createPageRequest(new TaskFilterRequest());
        }
        
        // Aplicar os filtros usando a especificação
        Page<TaskJpaEntity> taskEntities = taskJpaRepository.findAll(
                TaskSpecification.withAssignedFilters(userId, filter),
                pageRequest
        );
        
        // Mapear os resultados para entidades de domínio
        return taskEntities.map(taskMapper::toDomainEntity);
    }
}