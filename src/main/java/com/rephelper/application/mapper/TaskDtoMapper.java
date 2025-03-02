package com.rephelper.application.mapper;

import com.rephelper.application.dto.request.CreateTaskRequest;
import com.rephelper.application.dto.request.UpdateTaskRequest;
import com.rephelper.application.dto.response.TaskResponse;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TaskDtoMapper {

    @Autowired
    protected UserDtoMapper userDtoMapper;

    public Task toTask(CreateTaskRequest request) {
        if (request == null) return null;

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .category(request.getCategory())
                .status(Task.TaskStatus.PENDING)
                .build();

        if (request.getRepublicId() != null) {
            task = Task.builder()
                    .title(task.getTitle())
                    .description(task.getDescription())
                    .dueDate(task.getDueDate())
                    .category(task.getCategory())
                    .status(task.getStatus())
                    .republic(Republic.builder().id(request.getRepublicId()).build())
                    .build();
        }

        return task;
    }

    public TaskResponse toTaskResponse(Task task) {
        if (task == null) return null;

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .republicId(task.getRepublic() != null ? task.getRepublic().getId() : null)
                .republicName(task.getRepublic() != null ? task.getRepublic().getName() : null)
                .assignedUsers(task.getAssignedUsers() != null ?
                        userDtoMapper.toUserSummaryResponseSet(task.getAssignedUsers()) : null)
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .completedAt(task.getCompletedAt())
                .category(task.getCategory())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    public List<TaskResponse> toTaskResponseList(List<Task> tasks) {
        if (tasks == null) return null;

        return tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

}