package com.rephelper.application.mapper;

import com.rephelper.application.dto.request.CreateTaskRequest;
import com.rephelper.application.dto.request.UpdateTaskRequest;
import com.rephelper.application.dto.response.TaskResponse;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.mapstruct.Named;
import java.util.List;

/**
 * Mapper para converter entre entidades de domínio e DTOs de tarefa
 */
@Mapper(componentModel = "spring")
public abstract class TaskDtoMapper {

    @Autowired
    protected UserDtoMapper userDtoMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "republic", expression = "java(toRepublic(request))")
    @Mapping(target = "assignedUsers", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Task toTask(CreateTaskRequest request);

    @Mapping(target = "republicId", source = "republic.id")
    @Mapping(target = "republicName", source = "republic.name")
    @Mapping(target = "assignedUsers", expression = "java(userDtoMapper.toUserSummaryResponseSet(task.getAssignedUsers()))")
    public abstract TaskResponse toTaskResponse(Task task);

    public abstract List<TaskResponse> toTaskResponseList(List<Task> tasks);

    // Método para criar um objeto Republic com apenas o ID preenchido
    protected Republic toRepublic(CreateTaskRequest request) {
        if (request == null || request.getRepublicId() == null) return null;

        return Republic.builder()
                .id(request.getRepublicId())
                .build();
    }

    // Método para aplicar atualizações de uma tarefa
    @Named("applyUpdates")
    public Task applyUpdates(Task task, UpdateTaskRequest request) {
        if (request == null) return task;

        if (request.getTitle() != null) {
            task.update(
                    request.getTitle(),
                    request.getDescription(),
                    request.getDueDate(),
                    request.getCategory()
            );
        }

        return task;
    }
}
