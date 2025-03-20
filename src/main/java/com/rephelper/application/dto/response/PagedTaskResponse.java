package com.rephelper.application.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta paginada de tarefas
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedTaskResponse {
    private List<TaskResponse> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;
}