package com.rephelper.domain.service;

import java.util.List;
import java.util.UUID;

import com.rephelper.application.dto.request.TaskFilterRequest;
import com.rephelper.application.dto.request.UpdateTaskRequest;
import com.rephelper.domain.model.NotificationType;
import com.rephelper.domain.port.in.NotificationServicePort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final NotificationServicePort notificationService;

    @Override
    public Task createTask(Task task, UUID creatorUserId) {
        // Validar usuário
        User user = userRepository.findById(creatorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + creatorUserId));

        // Verificar se o usuário pertence à república
        if (user.getCurrentRepublic() == null ||
                !user.getCurrentRepublic().getId().equals(task.getRepublic().getId())) {
            throw new ForbiddenException("Você só pode criar tarefas para sua própria república");
        }

        // Definir o criador da tarefa
        task.setCreatedBy(user);

        // Verificar se a república existe
        if (!republicRepository.findById(task.getRepublic().getId()).isPresent()) {
            throw new ResourceNotFoundException("República não encontrada com id: " + task.getRepublic().getId());
        }

        // Validar dados de recorrência
        if (task.isRecurring()) {
            if (task.getRecurrenceType() == null) {
                throw new ValidationException("O tipo de recorrência é obrigatório para tarefas recorrentes");
            }

            if (task.getRecurrenceInterval() == null || task.getRecurrenceInterval() <= 0) {
                throw new ValidationException("O intervalo de recorrência deve ser um número positivo");
            }

            if (task.getDueDate() == null) {
                throw new ValidationException("A data de vencimento é obrigatória para tarefas recorrentes");
            }
        }

        // Atualizar status baseado na data de vencimento
        task.updateStatus();

        Task savedTask = taskRepository.save(task);

        // Notify republic admins about the new task if it doesn't have assigned users yet
        if (savedTask.getAssignedUsers() == null || savedTask.getAssignedUsers().isEmpty()) {
            // Get republic admins
            List<User> republicMembers = userRepository.findByCurrentRepublicId(task.getRepublic().getId());
            for (User member : republicMembers) {
                if (member.isRepublicAdmin() && !member.getId().equals(creatorUserId)) {
                    notificationService.createNotification(
                            member.getId(),
                            "Nova Tarefa Não Atribuída",
                            "Uma nova tarefa '" + savedTask.getTitle() + "' foi criada e precisa ser atribuída",
                            com.rephelper.domain.model.Notification.NotificationType.TASK_ASSIGNED,
                            "task",
                            savedTask.getId().toString()
                    );
                }
            }
        }

        return savedTask;
    }


    @Override
    @Transactional(readOnly = true)
    public List<Task> getAllTasksByRepublicId(UUID republicId) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("República não encontrada com id: " + republicId);
        }

        // Use findWithFilters with default sorting to ensure ordering is applied
        TaskFilterRequest filter = new TaskFilterRequest();
        filter.setSortBy("dueDate");
        filter.setSortDirection("ASC");
        
        return taskRepository.findWithFilters(republicId, filter, null).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa não encontrada com id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByCategory(UUID republicId, String category) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("República não encontrada com id: " + republicId);
        }

        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("A categoria não pode estar vazia");
        }

        // Use findWithFilters with category filter and default sorting
        TaskFilterRequest filter = new TaskFilterRequest();
        filter.setCategory(category);
        filter.setSortBy("dueDate");
        filter.setSortDirection("ASC");
        
        return taskRepository.findWithFilters(republicId, filter, null).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByStatus(UUID republicId, Task.TaskStatus status) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("República não encontrada com id: " + republicId);
        }

        if (status == null) {
            throw new ValidationException("O status não pode ser nulo");
        }

        // Use findWithFilters with status filter and default sorting
        TaskFilterRequest filter = new TaskFilterRequest();
        filter.setStatus(status.name());
        filter.setSortBy("dueDate");
        filter.setSortDirection("ASC");
        
        return taskRepository.findWithFilters(republicId, filter, null).getContent();
    }

    @Override
    public Task updateTask(Long id, UpdateTaskRequest request, UUID modifierUserId) {
        Task task = getTaskById(id);

        // Validar usuário
        User user = userRepository.findById(modifierUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + modifierUserId));


        boolean isMember = user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isMember) {
            throw new ForbiddenException("Você não tem permissão para atualizar esta tarefa");
        }

        // Validar dados de recorrência
        if (request.getRecurring() != null && request.getRecurring()) {
            if (request.getRecurrenceType() == null && task.getRecurrenceType() == null) {
                throw new ValidationException("O tipo de recorrência é obrigatório para tarefas recorrentes");
            }
            
            if ((request.getRecurrenceInterval() == null || request.getRecurrenceInterval() <= 0) && 
                (task.getRecurrenceInterval() == null || task.getRecurrenceInterval() <= 0)) {
                throw new ValidationException("O intervalo de recorrência deve ser um número positivo");
            }
            
            if (request.getDueDate() == null && task.getDueDate() == null) {
                throw new ValidationException("A data de vencimento é obrigatória para tarefas recorrentes");
            }
        }

        // Atualizar tarefa
        task.update(request);

        return taskRepository.save(task);
    }


    @Override
    public Task completeTask(Long id, UUID userId) {
        Task task = getTaskById(id);

        // Validar usuário
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        boolean isMember = user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isMember) {
            throw new ForbiddenException("You do not have permission to complete this task");
        }

        // Completar tarefa
        task.complete();

        // Salvar a tarefa atualizada
        Task completedTask = taskRepository.save(task);

        // Notify all users who were assigned to this task, except the one who completed it
        if (task.getAssignedUsers() != null && !task.getAssignedUsers().isEmpty()) {
            for (User assignedUser : task.getAssignedUsers()) {
                if (!assignedUser.getId().equals(userId)) {
                    notificationService.notifyTaskCompleted(
                            assignedUser.getId(),
                            id,
                            task.getTitle(),
                            userId
                    );
                }
            }
        }

        // Notify republic admins about completed task
        List<User> republicMembers = userRepository.findByCurrentRepublicId(task.getRepublic().getId());
        for (User member : republicMembers) {
            if (member.isRepublicAdmin() && !member.getId().equals(userId) &&
                    (task.getAssignedUsers() == null || !task.getAssignedUsers().contains(member))) {
                notificationService.createNotification(
                        member.getId(),
                        "Tarefa Concluída",
                        "A tarefa '" + task.getTitle() + "' foi concluída por " + user.getName(),
                        com.rephelper.domain.model.Notification.NotificationType.TASK_COMPLETED,
                        "task",
                        id.toString()
                );
            }
        }

        // Se a tarefa for recorrente, criar a próxima instância
        if (task.isRecurring() && task.shouldContinueRecurrence()) {
            Task nextTask = task.createRecurringInstance();
            if (nextTask != null) {
                Task newTask = taskRepository.save(nextTask);

                // Notify assigned users about the new recurring task
                if (newTask.getAssignedUsers() != null) {
                    for (User assignedUser : newTask.getAssignedUsers()) {
                        notificationService.createNotification(
                                assignedUser.getId(),
                                "Nova Tarefa Recorrente",
                                "Uma nova tarefa recorrente '" + newTask.getTitle() + "' foi criada e atribuída a você",
                                com.rephelper.domain.model.Notification.NotificationType.TASK_ASSIGNED,
                                "task",
                                newTask.getId().toString()
                        );
                    }
                }
            }
        }

        return completedTask;
    }

    @Override
    public Task cancelTask(Long id, UUID userId) {
        Task task = getTaskById(id);

        // Validar usuário
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        boolean isMember = user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isMember) {
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


        boolean isMember = user.getCurrentRepublic() != null &&
                user.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isMember) {
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
        boolean isAssignerMember = assigner.getCurrentRepublic() != null &&
                assigner.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isAssignerMember) {
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

        Task savedTask = taskRepository.save(task);

        // Send notification to the assigned user
        notificationService.notifyTaskAssigned(userId, taskId, task.getTitle());

        return savedTask;
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
        boolean isUnassignerMember = unassigner.getCurrentRepublic() != null &&
                unassigner.getCurrentRepublic().getId().equals(task.getRepublic().getId());

        if (!isUnassignerMember) {
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

        // Use findAssignedWithFilters with default sorting
        TaskFilterRequest filter = new TaskFilterRequest();
        filter.setSortBy("dueDate");
        filter.setSortDirection("ASC");
        
        return taskRepository.findAssignedWithFilters(userId, filter, null).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksAssignedToUserInRepublic(UUID userId, UUID republicId) {
        // Verificar se o usuário existe
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + userId);
        }

        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("República não encontrada com id: " + republicId);
        }

        return taskRepository.findByAssignedUserIdAndRepublicId(userId, republicId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksCreatedByUser(UUID userId) {
        // Verificar se o usuário existe
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + userId);
        }

        return taskRepository.findByCreatedByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksCreatedByUserInRepublic(UUID userId, UUID republicId) {
        // Verificar se o usuário existe
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + userId);
        }

        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("República não encontrada com id: " + republicId);
        }

        return taskRepository.findByCreatedByUserIdAndRepublicId(userId, republicId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Task> findTasksWithFilters(UUID republicId, TaskFilterRequest filter, Pageable pageable) {
        // Verificar se a república existe
        if (!republicRepository.findById(republicId).isPresent()) {
            throw new ResourceNotFoundException("Republic not found with id: " + republicId);
        }
        
        return taskRepository.findWithFilters(republicId, filter, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Task> findTasksAssignedWithFilters(UUID userId, TaskFilterRequest filter, Pageable pageable) {
        // Validar usuário
        if (!userRepository.findById(userId).isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        return taskRepository.findAssignedWithFilters(userId, filter, pageable);
    }
}