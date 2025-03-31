package com.rephelper.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.rephelper.domain.model.Task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de tarefa
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private UUID republicId;
    private String republicName;
    private Set<UserSummaryResponse> assignedUsers;
    private Task.TaskStatus status;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private String category;
    private UserSummaryResponse createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Campos para recorrência
    private boolean isRecurring;
    private String recurrenceType;
    private Integer recurrenceInterval;
    private LocalDateTime recurrenceEndDate;
    private Long parentTaskId;
}