package com.rephelper.application.dto.request;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para filtrar tarefas no servidor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskFilterRequest {
    // Filtros básicos
    private String status;
    private String category;
    private Boolean isRecurring;
    private Boolean isOverdue;
    
    // Filtros de data
    private LocalDateTime dueDateFrom;
    private LocalDateTime dueDateTo;
    private LocalDateTime createdAtFrom;
    private LocalDateTime createdAtTo;
    
    // Filtros de usuário
    private UUID assignedUserId;
    private Boolean unassigned;
    
    // Filtros de texto
    private String searchTerm; // Busca no título e descrição
    
    // Paginação
    private Integer page;
    private Integer size;
    
    // Ordenação
    private String sortBy;
    private String sortDirection;
}