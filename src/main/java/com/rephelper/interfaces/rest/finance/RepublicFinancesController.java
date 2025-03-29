package com.rephelper.interfaces.rest.finance;

import com.rephelper.application.dto.response.RepublicFinancesResponse;
import com.rephelper.application.mapper.RepublicFinancesDtoMapper;
import com.rephelper.domain.exception.ValidationException;
import com.rephelper.domain.model.RepublicFinances;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.RepublicFinancesServicePort;
import com.rephelper.domain.port.in.UserServicePort;
import com.rephelper.infrastructure.adapter.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/finances")
@RequiredArgsConstructor
@Tag(name = "Republic Finances", description = "Republic finances management endpoints")
public class RepublicFinancesController {

    private final RepublicFinancesServicePort republicFinancesService;
    private final UserServicePort userService;
    private final RepublicFinancesDtoMapper republicFinancesDtoMapper;

    @GetMapping("/{republicId}")
    @Operation(summary = "Get republic finances", description = "Retrieves financial information for a republic")
    public ResponseEntity<RepublicFinancesResponse> getRepublicFinances(
            @PathVariable UUID republicId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get user to validate access
        User user = userService.getUserById(currentUser.getUserId());

        if (user.getCurrentRepublic() == null) {
            throw new ValidationException("User is not associated with any republic");
        }

        // Verify user belongs to the specified republic (unless admin)
        if (!republicId.equals(user.getCurrentRepublic().getId()) &&
                !currentUser.getRole().equals("admin")) {
            throw new ValidationException("You can only view finances for your own republic");
        }

        // Get or create republic finances
        RepublicFinances finances = republicFinancesService.getOrCreateRepublicFinances(republicId);

        return ResponseEntity.ok(republicFinancesDtoMapper.toRepublicFinancesResponse(finances));
    }
}