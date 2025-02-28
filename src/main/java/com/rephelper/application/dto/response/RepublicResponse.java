package com.rephelper.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID; /**
 * DTO para resposta de república
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepublicResponse {
    private UUID id;
    private String name;
    private String code;
    private AddressResponse address;
    private UUID ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
