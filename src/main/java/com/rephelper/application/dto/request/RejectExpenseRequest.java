package com.rephelper.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for expense rejection requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectExpenseRequest {
    @NotBlank(message = "Rejection reason is required")
    private String reason;
}