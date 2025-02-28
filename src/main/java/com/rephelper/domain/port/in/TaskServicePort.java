package com.rephelper.domain.port.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.rephelper.domain.model.Task;

/**
 * Porta de entrada definindo os casos de uso relacionados a tarefas.
 * Esta interface será implementada pelo serviço de domínio.
 */
public interface TaskServicePort {
    /**
     * Cria uma nova tarefa
     */
    Task createTask(Task task, UUID creatorUserId);

    /**
     * Obtém todas as tarefas de uma república
     */
    List<Task> getAllTasksByRepublicId(UUID republicId);

    /**
     * Obtém uma tarefa pelo ID
     */
    Task getTaskById(UUID id);

    /**
     * Obtém tarefas por categoria em uma república
     */
    List<Task> getTasksByCategory(UUID republicId, String category);

    /**
     * Obtém tarefas por status em uma república
     */
    List<Task> getTasksByStatus(UUID republicId, Task.TaskStatus status);

    /**
     * Atualiza uma tarefa existente
     */
    Task updateTask(UUID id, String title, String description, LocalDateTime dueDate, String category, UUID modifierUserId);

    /**
     * Marca uma tarefa como concluída
     */
    Task completeTask(UUID id, UUID userId);

    /**
     * Cancela uma tarefa
     */
    Task cancelTask(UUID id, UUID userId);

    /**
     * Remove uma tarefa
     */
    void deleteTask(UUID id, UUID userId);

    /**
     * Atribui uma tarefa a um usuário
     */
    Task assignTaskToUser(UUID taskId, UUID userId, UUID assignerUserId);

    /**
     * Remove a atribuição de uma tarefa a um usuário
     */
    Task unassignTaskFromUser(UUID taskId, UUID userId, UUID unassignerUserId);

    /**
     * Obtém tarefas atribuídas a um usuário
     */
    List<Task> getTasksAssignedToUser(UUID userId);

    /**
     * Obtém tarefas atribuídas a um usuário em uma república
     */
    List<Task> getTasksAssignedToUserInRepublic(UUID userId, UUID republicId);
}