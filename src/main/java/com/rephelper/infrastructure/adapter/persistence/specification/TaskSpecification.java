package com.rephelper.infrastructure.adapter.persistence.specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.rephelper.application.dto.request.TaskFilterRequest;
import com.rephelper.domain.model.Task;
import com.rephelper.infrastructure.entity.TaskJpaEntity;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

/**
 * Classe utilitária para criar especificações de consulta para tarefas
 */
public class TaskSpecification {

    /**
     * Cria uma especificação para filtrar tarefas com base nos critérios fornecidos
     * 
     * @param republicId ID da república
     * @param filter Filtros a serem aplicados
     * @return Especificação para consulta de tarefas
     */
    public static Specification<TaskJpaEntity> withFilters(UUID republicId, TaskFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filtro por república (sempre aplicado)
            predicates.add(criteriaBuilder.equal(root.get("republic").get("uuid"), republicId));
            
            // Filtro por status
            if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                try {
                    Task.TaskStatus status = Task.TaskStatus.valueOf(filter.getStatus());
                    predicates.add(criteriaBuilder.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Ignora status inválido
                }
            }
            
            // Filtro por categoria
            if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), filter.getCategory()));
            }
            
            // Filtro por recorrência
            if (filter.getIsRecurring() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isRecurring"), filter.getIsRecurring()));
            }
            
            // Filtro por tarefas atrasadas
            if (filter.getIsOverdue() != null && filter.getIsOverdue()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), Task.TaskStatus.OVERDUE));
            }
            
            // Filtros de data
            if (filter.getDueDateFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
            }
            
            if (filter.getDueDateTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
            }
            
            if (filter.getCreatedAtFrom() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom()));
            }
            
            if (filter.getCreatedAtTo() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo()));
            }
            
            // Filtro por usuário atribuído
            if (filter.getAssignedUserId() != null) {
                Join<Object, Object> userJoin = root.join("assignedUsers", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(userJoin.get("uuid"), filter.getAssignedUserId()));
            }
            
            // Filtro por tarefas não atribuídas
            if (filter.getUnassigned() != null && filter.getUnassigned()) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<TaskJpaEntity> subRoot = subquery.from(TaskJpaEntity.class);
                subquery.select(subRoot.get("id"));
                Join<Object, Object> userJoin = subRoot.join("assignedUsers", JoinType.INNER);
                subquery.where(criteriaBuilder.equal(subRoot.get("id"), root.get("id")));
                
                predicates.add(criteriaBuilder.not(criteriaBuilder.exists(subquery)));
            }
            
            // Filtro por termo de busca (título e descrição)
            if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
                String searchTerm = "%" + filter.getSearchTerm().toLowerCase() + "%";
                Predicate titlePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), searchTerm);
                Predicate descriptionPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchTerm);
                
                predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Cria uma especificação para filtrar tarefas atribuídas a um usuário específico
     * 
     * @param userId ID do usuário
     * @param filter Filtros a serem aplicados
     * @return Especificação para consulta de tarefas
     */
    public static Specification<TaskJpaEntity> withAssignedFilters(UUID userId, TaskFilterRequest filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filtro por usuário atribuído (sempre aplicado)
            Join<Object, Object> userJoin = root.join("assignedUsers", JoinType.INNER);
            predicates.add(criteriaBuilder.equal(userJoin.get("uuid"), userId));
            
            // Aplicar os demais filtros comuns
            if (filter != null) {
                // Filtro por status
                if (filter.getStatus() != null && !filter.getStatus().isEmpty()) {
                    try {
                        Task.TaskStatus status = Task.TaskStatus.valueOf(filter.getStatus());
                        predicates.add(criteriaBuilder.equal(root.get("status"), status));
                    } catch (IllegalArgumentException e) {
                        // Ignora status inválido
                    }
                }
                
                // Filtro por categoria
                if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("category"), filter.getCategory()));
                }
                
                // Filtro por recorrência
                if (filter.getIsRecurring() != null) {
                    predicates.add(criteriaBuilder.equal(root.get("isRecurring"), filter.getIsRecurring()));
                }
                
                // Filtro por tarefas atrasadas
                if (filter.getIsOverdue() != null && filter.getIsOverdue()) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), Task.TaskStatus.OVERDUE));
                }
                
                // Filtros de data
                if (filter.getDueDateFrom() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("dueDate"), filter.getDueDateFrom()));
                }
                
                if (filter.getDueDateTo() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("dueDate"), filter.getDueDateTo()));
                }
                
                if (filter.getCreatedAtFrom() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtFrom()));
                }
                
                if (filter.getCreatedAtTo() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getCreatedAtTo()));
                }
                
                // Filtro por termo de busca (título e descrição)
                if (filter.getSearchTerm() != null && !filter.getSearchTerm().isEmpty()) {
                    String searchTerm = "%" + filter.getSearchTerm().toLowerCase() + "%";
                    Predicate titlePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")), searchTerm);
                    Predicate descriptionPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")), searchTerm);
                    
                    predicates.add(criteriaBuilder.or(titlePredicate, descriptionPredicate));
                }
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}