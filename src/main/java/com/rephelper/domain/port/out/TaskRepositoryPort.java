package com.rephelper.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.rephelper.domain.model.Task;

/**
 * Porta de saída para operações de repositório relacionadas a tarefas.
 * Define o contrato que a camada de infraestrutura deve implementar.
 */
public interface TaskRepositoryPort {
    /**
     * Salva ou atualiza uma tarefa
     */
    Task save(Task task);

    /**
     * Busca uma tarefa pelo ID
     */
    Optional<Task> findById(Long id);

    /**
     * Busca todas as tarefas
     */
    List<Task> findAll();

    /**
     * Busca tarefas por república
     */
    List<Task> findByRepublicId(UUID republicId);

    /**
     * Busca tarefas por república e categoria
     */
    List<Task> findByRepublicIdAndCategory(UUID republicId, String category);

    /**
     * Busca tarefas por status
     */
    List<Task> findByRepublicIdAndStatus(UUID republicId, Task.TaskStatus status);

    /**
     * Busca tarefas atribuídas a um usuário
     */
    List<Task> findByAssignedUserId(UUID userId);

    /**
     * Busca tarefas atribuídas a um usuário em uma república
     */
    List<Task> findByAssignedUserIdAndRepublicId(UUID userId, UUID republicId);

    /**
     * Remove uma tarefa
     */
    void delete(Task task);
}