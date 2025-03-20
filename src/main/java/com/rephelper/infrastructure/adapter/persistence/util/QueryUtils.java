package com.rephelper.infrastructure.adapter.persistence.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.rephelper.application.dto.request.TaskFilterRequest;

/**
 * Classe utilitária para construção de consultas
 */
public class QueryUtils {

    /**
     * Cria um objeto Pageable com base nos parâmetros de paginação e ordenação
     * 
     * @param filter Filtros com informações de paginação e ordenação
     * @return Objeto Pageable configurado
     */
    public static Pageable createPageRequest(TaskFilterRequest filter) {
        // Valores padrão
        int page = 0;
        int size = 20;
        String sortBy = "dueDate";
        Direction direction = Direction.ASC;
        
        // Aplicar valores do filtro, se fornecidos
        if (filter != null) {
            if (filter.getPage() != null && filter.getPage() >= 0) {
                page = filter.getPage();
            }
            
            if (filter.getSize() != null && filter.getSize() > 0) {
                size = filter.getSize();
            }
            
            if (filter.getSortBy() != null && !filter.getSortBy().trim().isEmpty()) {
                sortBy = filter.getSortBy().trim();
            }
            
            if (filter.getSortDirection() != null && !filter.getSortDirection().trim().isEmpty()) {
                String directionStr = filter.getSortDirection().trim().toUpperCase();
                if (directionStr.equals("DESC")) {
                    direction = Direction.DESC;
                } else if (directionStr.equals("ASC")) {
                    direction = Direction.ASC;
                }
            }
        }
        
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
    
    /**
     * Verifica se uma string não é nula e não está vazia
     * 
     * @param str String a ser verificada
     * @return true se a string não for nula e não estiver vazia
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}