package com.rephelper.application.dto.request;

import com.rephelper.domain.model.Event;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvitationStatusRequest {
    @NotNull(message = "Status is required")
    private Event.InvitationStatus status;
}
