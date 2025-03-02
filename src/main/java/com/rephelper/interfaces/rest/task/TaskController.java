package com.rephelper.interfaces.rest.task;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rephelper.application.dto.request.CreateTaskRequest;
import com.rephelper.application.dto.request.TaskAssignmentRequest;
import com.rephelper.application.dto.request.UpdateTaskRequest;
import com.rephelper.application.dto.response.ApiResponse;
import com.rephelper.application.dto.response.TaskResponse;
import com.rephelper.application.mapper.TaskDtoMapper;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.Task;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.TaskServicePort;
import com.rephelper.domain.port.in.UserServicePort;
import com.rephelper.infrastructure.adapter.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskServicePort taskService;
    private final UserServicePort userService;
    private final TaskDtoMapper taskDtoMapper;

    @PostMapping
    @Operation(summary = "Create a new task", description = "Creates a new task for a republic")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Convert request to task
        Task task = taskDtoMapper.toTask(request);

        // Create task
        Task createdTask = taskService.createTask(task, currentUser.getUserId());

        return new ResponseEntity<>(taskDtoMapper.toTaskResponse(createdTask), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all tasks for user's republic", description = "Retrieves all tasks for the current user's republic")
    public ResponseEntity<List<TaskResponse>> getAllTasks(
            @RequestParam(required = false) UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to determine their republic
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // If republicId is provided, validate that it matches user's republic (unless admin)
        UUID targetRepublicId = republicId;
        if (targetRepublicId == null) {
            targetRepublicId = user.getCurrentRepublic().getId();
        } else if (!targetRepublicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view tasks for your own republic");
        }

        // Get tasks
        List<Task> tasks = taskService.getAllTasksByRepublicId(targetRepublicId);

        return ResponseEntity.ok(taskDtoMapper.toTaskResponseList(tasks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID", description = "Retrieves task details by ID")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        Task task = taskService.getTaskById(id);

        return ResponseEntity.ok(taskDtoMapper.toTaskResponse(task));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get tasks by category", description = "Retrieves tasks by category for a republic")
    public ResponseEntity<List<TaskResponse>> getTasksByCategory(
            @PathVariable String category,
            @RequestParam(required = false) UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to determine their republic
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // If republicId is provided, validate that it matches user's republic (unless admin)
        UUID targetRepublicId = republicId;
        if (targetRepublicId == null) {
            targetRepublicId = user.getCurrentRepublic().getId();
        } else if (!targetRepublicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view tasks for your own republic");
        }

        // Get tasks by category
        List<Task> tasks = taskService.getTasksByCategory(targetRepublicId, category);

        return ResponseEntity.ok(taskDtoMapper.toTaskResponseList(tasks));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status", description = "Retrieves tasks by status for a republic")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(
            @PathVariable Task.TaskStatus status,
            @RequestParam(required = false) UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to determine their republic
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // If republicId is provided, validate that it matches user's republic (unless admin)
        UUID targetRepublicId = republicId;
        if (targetRepublicId == null) {
            targetRepublicId = user.getCurrentRepublic().getId();
        } else if (!targetRepublicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view tasks for your own republic");
        }

        // Get tasks by status
        List<Task> tasks = taskService.getTasksByStatus(targetRepublicId, status);

        return ResponseEntity.ok(taskDtoMapper.toTaskResponseList(tasks));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task", description = "Updates task details")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Update task
        Task updatedTask = taskService.updateTask(
                id,
                request,
                currentUser.getUserId());

        return ResponseEntity.ok(taskDtoMapper.toTaskResponse(updatedTask));
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete task", description = "Marks a task as completed")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Complete task
        Task completedTask = taskService.completeTask(id, currentUser.getUserId());

        return ResponseEntity.ok(taskDtoMapper.toTaskResponse(completedTask));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel task", description = "Cancels a task")
    public ResponseEntity<TaskResponse> cancelTask(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Cancel task
        Task canceledTask = taskService.cancelTask(id, currentUser.getUserId());

        return ResponseEntity.ok(taskDtoMapper.toTaskResponse(canceledTask));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete task", description = "Deletes a task")
    public ResponseEntity<ApiResponse> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Delete task
        taskService.deleteTask(id, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("Task deleted successfully")
                .build());
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign task", description = "Assigns a task to a user")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskAssignmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Assign task
        Task task = taskService.assignTaskToUser(id, request.getUserId(), currentUser.getUserId());

        return ResponseEntity.ok(taskDtoMapper.toTaskResponse(task));
    }

    @PostMapping("/{id}/unassign")
    @Operation(summary = "Unassign task", description = "Removes task assignment from a user")
    public ResponseEntity<TaskResponse> unassignTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskAssignmentRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Unassign task
        Task task = taskService.unassignTaskFromUser(id, request.getUserId(), currentUser.getUserId());

        return ResponseEntity.ok(taskDtoMapper.toTaskResponse(task));
    }

    @GetMapping("/assigned")
    @Operation(summary = "Get assigned tasks", description = "Gets tasks assigned to the current user")
    public ResponseEntity<List<TaskResponse>> getAssignedTasks(
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get tasks assigned to user
        List<Task> tasks = taskService.getTasksAssignedToUser(currentUser.getUserId());

        return ResponseEntity.ok(taskDtoMapper.toTaskResponseList(tasks));
    }
}