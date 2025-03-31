package com.rephelper.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.rephelper.application.dto.request.TaskFilterRequest;
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
     * Busca tarefas criadas por um usuário
     */
    List<Task> findByCreatedByUserId(UUID userId);

    /**
     * Busca tarefas criadas por um usuário em uma república
     */
    List<Task> findByCreatedByUserIdAndRepublicId(UUID userId, UUID republicId);

    /**
     * Remove uma tarefa
     */
    void delete(Task task);
    
    /**
     * Busca tarefas com filtros e paginação
     * 
     * @param republicId ID da república
     * @param filter Filtros a serem aplicados
     * @param pageable Informações de paginação
     * @return Página de tarefas
     */
    Page<Task> findWithFilters(UUID republicId, TaskFilterRequest filter, Pageable pageable);
    
    /**
     * Busca tarefas atribuídas a um usuário com filtros e paginação
     * 
     * @param userId ID do usuário
     * @param filter Filtros a serem aplicados
     * @param pageable Informações de paginação
     * @return Página de tarefas
     */
    Page<Task> findAssignedWithFilters(UUID userId, TaskFilterRequest filter, Pageable pageable);
    
    /**
     * Busca tarefas pendentes ou em progresso com prazo de vencimento nas próximas 24 horas
     * 
     * @return Lista de tarefas com prazo nas próximas 24 horas
     */
    List<Task> findTasksDueWithinNextDay();
    
    /**
     * Busca tarefas pendentes ou em progresso com prazo de vencimento em 3 dias
     * 
     * @return Lista de tarefas com prazo em 3 dias
     */
    List<Task> findTasksDueInThreeDays();
    
    /**
     * Busca tarefas pendentes ou em progresso que estão atrasadas há mais de 1 dia
     * 
     * @return Lista de tarefas atrasadas há mais de 1 dia
     */
    List<Task> findTasksOverdueMoreThanOneDay();
    
    /**
     * Busca tarefas recorrentes vencidas que não foram concluídas
     * 
     * @return Lista de tarefas recorrentes vencidas
     */
    List<Task> findOverdueRecurringTasks();
    
    /**
     * Busca tarefas recorrentes concluídas
     * 
     * @return Lista de tarefas recorrentes concluídas
     */
    List<Task> findCompletedRecurringTasks();
    
    /**
     * Verifica se existe alguma tarefa com o ID pai especificado
     * 
     * @param parentTaskId ID da tarefa pai
     * @return true se existe alguma tarefa filha, false caso contrário
     */
    boolean existsByParentTaskId(Long parentTaskId);
}