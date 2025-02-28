package com.rephelper.infrastructure.adapter.persistence;

import com.rephelper.domain.model.Task;
import com.rephelper.infrastructure.entity.TaskJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para Tarefas
 */
@Repository
public interface TaskJpaRepository extends JpaRepository<TaskJpaEntity, Long> {
    List<TaskJpaEntity> findByRepublicUuid(UUID republicId);
    List<TaskJpaEntity> findByRepublicUuidAndCategory(UUID republicId, String category);
    List<TaskJpaEntity> findByRepublicUuidAndStatus(UUID republicId, Task.TaskStatus status);

    @Query("SELECT t FROM TaskJpaEntity t JOIN t.assignedUsers u WHERE u.uuid = :userId")
    List<TaskJpaEntity> findByAssignedUserId(@Param("userId") UUID userId);

    @Query("SELECT t FROM TaskJpaEntity t JOIN t.assignedUsers u WHERE u.uuid = :userId AND t.republic.uuid = :republicId")
    List<TaskJpaEntity> findByAssignedUserIdAndRepublicId(@Param("userId") UUID userId, @Param("republicId") UUID republicId);
}
