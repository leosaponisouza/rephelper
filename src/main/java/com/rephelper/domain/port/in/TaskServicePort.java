package com.rephelper.domain.port.in;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.rephelper.application.dto.request.TaskFilterRequest;
import com.rephelper.application.dto.request.UpdateTaskRequest;
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
    Task getTaskById(Long id);

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
    Task updateTask(Long id, UpdateTaskRequest request, UUID modifierUserId);

    /**
     * Marca uma tarefa como concluída
     */
    Task completeTask(Long id, UUID userId);

    /**
     * Cancela uma tarefa
     */
    Task cancelTask(Long id, UUID userId);

    /**
     * Remove uma tarefa
     */
    void deleteTask(Long id, UUID userId);

    /**
     * Atribui uma tarefa a um usuário
     */
    Task assignTaskToUser(Long taskId, UUID userId, UUID assignerUserId);

    /**
     * Remove a atribuição de uma tarefa a um usuário
     */
    Task unassignTaskFromUser(Long taskId, UUID userId, UUID unassignerUserId);

    /**
     * Obtém tarefas atribuídas a um usuário
     */
    List<Task> getTasksAssignedToUser(UUID userId);

    /**
     * Obtém tarefas atribuídas a um usuário em uma república
     */
    List<Task> getTasksAssignedToUserInRepublic(UUID userId, UUID republicId);
    
    /**
     * Obtém tarefas criadas por um usuário
     */
    List<Task> getTasksCreatedByUser(UUID userId);

    /**
     * Obtém tarefas criadas por um usuário em uma república
     */
    List<Task> getTasksCreatedByUserInRepublic(UUID userId, UUID republicId);
    
    /**
     * Busca tarefas com filtros e paginação
     * 
     * @param republicId ID da república
     * @param filter Filtros a serem aplicados
     * @param pageable Informações de paginação
     * @return Página de tarefas
     */
    Page<Task> findTasksWithFilters(UUID republicId, TaskFilterRequest filter, Pageable pageable);
    
    /**
     * Busca tarefas atribuídas a um usuário com filtros e paginação
     * 
     * @param userId ID do usuário
     * @param filter Filtros a serem aplicados
     * @param pageable Informações de paginação
     * @return Página de tarefas
     */
    Page<Task> findTasksAssignedWithFilters(UUID userId, TaskFilterRequest filter, Pageable pageable);
}