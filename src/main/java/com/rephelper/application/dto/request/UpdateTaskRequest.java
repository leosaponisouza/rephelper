package com.rephelper.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskRequest {
    private String title;

    private String description;

    private LocalDateTime dueDate;

    private String status;

    private String category;
    
    // Campos para recorrência
    private Boolean recurring;
    
    private String recurrenceType; // DAILY, WEEKLY, MONTHLY, YEARLY
    
    private Integer recurrenceInterval;
    
    private LocalDateTime recurrenceEndDate;
}