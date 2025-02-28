package com.rephelper.domain.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.exception.ResourceNotFoundException;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.Task;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.TaskServicePort;
import com.rephelper.domain.port.out.RepublicRepositoryPort;
import com.rephelper.domain.port.out.TaskRepositoryPort;
import com.rephelper.domain.port.out.UserRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskServicePort {

    private final TaskRepositoryPort taskRepository;
    private final UserRepositoryPort userRepository;
    private final RepublicRepositoryPort republicRepository;

    @Override
    public Task createTask(Task task, UUID creatorUserId) {
        // Validar usuário
        User user = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + creatorUserId));

        // Verificar se o usuário pertence à república
        if (user.getCurrentRepublic() == null ||
                !user.getCurrentRepublic().getId().equals(task.getRepublic().getId())) {
            throw new ForbiddenException("You can only create tasks for your own republic");
        }

        // Verificar se a república existe
        if (!republicRepository.findById(task.getRepublic().getId()).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + task.getRepublic().getId());
        }

        // Atualizar status baseado na data de vencimento
        task.updateStatus();

        return taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getAllTasksByRepublicId(UUID republicId) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return taskRepository.findByRepublicId(republicId);
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByCategory(UUID republicId, String category) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category cannot be empty");
        }

        return taskRepository.findByRepublicIdAndCategory(republicId, category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(UUID republicId, Task.TaskStatus status) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        if (status == null) {
            throw new ValidationException("Status cannot be null");
        }

        return taskRepository.findByRepublicIdAndStatus(republicId, status);
    }

    @Override
    public Task updateTask(Long id, String title, String description, LocalDateTime dueDate, String category, UUID modifierUserId) {
        Task task = getTaskById(id);

        // Validar usuário
        User user = userRepository.findById(modifierUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + modifierUserId));

        // Verificar se o usuário pertence à república ou é admin
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;
        boolean isMember = user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isAdmin && !isMember) {
            throw new ForbiddenException("You do not have permission to update this task");
        }

        // Atualizar tarefa
        task.update(title, description, dueDate, category);

        return taskRepository.save(task);
    }

    @Override
    public Task completeTask(Long id, UUID userId) {
        Task task = getTaskById(id);

        // Validar usuário
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verificar se o usuário pertence à república ou é admin
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;
        boolean isMember = user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isAdmin && !isMember) {
            throw new ForbiddenException("You do not have permission to complete this task");
        }

        // Completar tarefa
        task.complete();

        return taskRepository.save(task);
    }

    @Override
    public Task cancelTask(Long id, UUID userId) {
        Task task = getTaskById(id);

        // Validar usuário
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verificar se o usuário pertence à república ou é admin
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;
        boolean isMember = user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isAdmin && !isMember) {
            throw new ForbiddenException("You do not have permission to cancel this task");
        }

        // Cancelar tarefa
        task.cancel();

        return taskRepository.save(task);
    }

    @Override
    public void deleteTask(Long id, UUID userId) {
        Task task = getTaskById(id);

        // Validar usuário
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Verificar se o usuário pertence à república ou é admin
        boolean isAdmin = user.getRole() == User.UserRole.ADMIN;
        boolean isMember = user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isAdmin && !isMember) {
            throw new ForbiddenException("You do not have permission to delete this task");
        }

        // Deletar tarefa
        taskRepository.delete(task);
    }

    @Override
    public Task assignTaskToUser(Long taskId, UUID userId, UUID assignerUserId) {
        Task task = getTaskById(taskId);

        // Validar usuário a ser atribuído
        User userToAssign = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Validar usuário que está atribuindo
        User assigner = userRepository.findById(assignerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigner user not found with id: " + assignerUserId));

        // Verificar se o atribuidor pertence à república ou é admin
        boolean isAssignerAdmin = assigner.getRole() == User.UserRole.ADMIN;
        boolean isAssignerMember = assigner.getCurrentRepublic() != null &&
                assigner.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isAssignerAdmin && !isAssignerMember) {
            throw new ForbiddenException("You do not have permission to assign tasks in this republic");
        }

        // Verificar se o usuário a ser atribuído pertence à república
        boolean isUserToAssignMember = userToAssign.getCurrentRepublic() != null &&
                userToAssign.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isUserToAssignMember) {
            throw new ForbiddenException("Can only assign tasks to members of the same republic");
        }

        // Atribuir tarefa
        task.assignTo(userToAssign);

        return taskRepository.save(task);
    }

    @Override
    public Task unassignTaskFromUser(Long taskId, UUID userId, UUID unassignerUserId) {
        Task task = getTaskById(taskId);

        // Validar usuário a ser removido
        User userToUnassign = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Validar usuário que está removendo
        User unassigner = userRepository.findById(unassignerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Unassigner user not found with id: " + unassignerUserId));

        // Verificar se o removedor pertence à república ou é admin
        boolean isUnassignerAdmin = unassigner.getRole() == User.UserRole.ADMIN;
        boolean isUnassignerMember = unassigner.getCurrentRepublic() != null &&
                unassigner.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isUnassignerAdmin && !isUnassignerMember) {
            throw new ForbiddenException("You do not have permission to unassign tasks in this republic");
        }

        // Verificar se a tarefa está realmente atribuída ao usuário
        if (!task.isAssignedTo(userToUnassign)) {
            throw new ValidationException("Task is not assigned to this user");
        }

        // Remover atribuição da tarefa
        task.unassignFrom(userToUnassign);

        return taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksAssignedToUser(UUID userId) {
        // Validar usuário
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return taskRepository.findByAssignedUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksAssignedToUserInRepublic(UUID userId, UUID republicId) {
        // Validar usuário
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        // Validar república
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }

        return taskRepository.findByAssignedUserIdAndRepublicId(userId, republicId);
    }
}