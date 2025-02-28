package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Task;
import com.rephelper.infrastructure.entity.TaskJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Mapper para converter entre Task do domínio e TaskJpaEntity
 */
@Mapper(componentModel = "spring", uses = {RepublicMapper.class, UserMapper.class})
public abstract class TaskMapper {

    @Autowired
    protected RepublicMapper republicMapper;

    @Autowired
    protected UserMapper userMapper;

    @Mapping(target = "republic", source = "republic", qualifiedByName = "toEntityWithoutUsers")
    @Mapping(target = "assignedUsers", expression = "java(jpaEntity.getAssignedUsers().stream().map(userMapper::toDomainEntityWithoutRepublic).collect(java.util.stream.Collectors.toSet()))")
    public abstract Task toDomainEntity(TaskJpaEntity jpaEntity);

    @Mapping(target = "republic", source = "republic", qualifiedByName = "toJpaEntityWithoutUsers")
    @Mapping(target = "assignedUsers", expression = "java(domainEntity.getAssignedUsers().stream().map(userMapper::toJpaEntityWithoutRepublic).collect(java.util.stream.Collectors.toSet()))")
    public abstract TaskJpaEntity toJpaEntity(Task domainEntity);

    // Método para mapear enums de status
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
