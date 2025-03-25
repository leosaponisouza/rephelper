package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Task;
import com.rephelper.infrastructure.entity.TaskJpaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para Tarefas
 */
@Repository
public interface TaskJpaRepository extends JpaRepository<TaskJpaEntity, Long>, JpaSpecificationExecutor<TaskJpaEntity> {
    List<TaskJpaEntity> findByRepublicUuid(UUID republicId);
    List<TaskJpaEntity> findByRepublicUuidAndCategory(UUID republicId, String category);
    List<TaskJpaEntity> findByRepublicUuidAndStatus(UUID republicId, Task.TaskStatus status);

    @Query("SELECT t FROM TaskJpaEntity t JOIN t.assignedUsers u WHERE u.uuid = :userId")
    List<TaskJpaEntity> findByAssignedUserId(@Param("userId") UUID userId);

    @Query("SELECT t FROM TaskJpaEntity t JOIN t.assignedUsers u WHERE u.uuid = :userId AND t.republic.uuid = :republicId")
    List<TaskJpaEntity> findByAssignedUserIdAndRepublicId(@Param("userId") UUID userId, @Param("republicId") UUID republicId);
    
    /**
     * Busca tarefas com paginação e ordenação
     * 
     * @param spec Especificação com os filtros
     * @param pageable Informações de paginação e ordenação
     * @return Página de tarefas
     */
    Page<TaskJpaEntity> findAll(Specification<TaskJpaEntity> spec, Pageable pageable);
    
    /**
     * Busca tarefas pendentes ou em progresso com prazo de vencimento nas próximas 24 horas
     * 
     * @return Lista de tarefas com prazo nas próximas 24 horas
     */
    @Query("SELECT t FROM TaskJpaEntity t " +
           "WHERE (t.status = 'PENDING' OR t.status = 'IN_PROGRESS') " +
           "AND t.dueDate > CURRENT_TIMESTAMP " +
           "AND t.dueDate <= :nextDay " +
           "ORDER BY t.dueDate ASC")
    List<TaskJpaEntity> findTasksDueWithinNextDay(@Param("nextDay") LocalDateTime nextDay);
    
    /**
     * Busca tarefas pendentes ou em progresso com prazo de vencimento em exatamente 3 dias
     * 
     * @return Lista de tarefas com prazo em 3 dias
     */
    @Query("SELECT t FROM TaskJpaEntity t " +
           "WHERE (t.status = 'PENDING' OR t.status = 'IN_PROGRESS') " +
           "AND t.dueDate > :startDay " +
           "AND t.dueDate < :endDay " +
           "ORDER BY t.dueDate ASC")
    List<TaskJpaEntity> findTasksDueInThreeDays(@Param("startDay") LocalDateTime startDay, 
                                                @Param("endDay") LocalDateTime endDay);
    
    /**
     * Busca tarefas pendentes ou em progresso que estão atrasadas há mais de 1 dia
     * 
     * @return Lista de tarefas atrasadas há mais de 1 dia
     */
    @Query("SELECT t FROM TaskJpaEntity t " +
           "WHERE (t.status = 'PENDING' OR t.status = 'IN_PROGRESS' OR t.status = 'OVERDUE') " +
           "AND t.dueDate < :oneDayAgo " +
           "ORDER BY t.dueDate ASC")
    List<TaskJpaEntity> findTasksOverdueMoreThanOneDay(@Param("oneDayAgo") LocalDateTime oneDayAgo);
    
    /**
     * Busca tarefas recorrentes vencidas que não foram concluídas
     * 
     * @return Lista de tarefas recorrentes vencidas
     */
    @Query("SELECT t FROM TaskJpaEntity t " +
           "WHERE t.isRecurring = true " +
           "AND (t.status = 'PENDING' OR t.status = 'IN_PROGRESS' OR t.status = 'OVERDUE') " +
           "AND t.dueDate < CURRENT_TIMESTAMP " +
           "ORDER BY t.dueDate ASC")
    List<TaskJpaEntity> findOverdueRecurringTasks();
    
    /**
     * Busca tarefas recorrentes concluídas
     * 
     * @return Lista de tarefas recorrentes concluídas
     */
    @Query("SELECT t FROM TaskJpaEntity t " +
           "WHERE t.isRecurring = true " +
           "AND t.status = 'COMPLETED' " +
           "ORDER BY t.completedAt DESC")
    List<TaskJpaEntity> findCompletedRecurringTasks();
    
    /**
     * Verifica se existe alguma tarefa com o ID pai especificado
     * 
     * @param parentTaskId ID da tarefa pai
     * @return true se existe alguma tarefa filha, false caso contrário
     */
    boolean existsByParentTaskId(Long parentTaskId);
}