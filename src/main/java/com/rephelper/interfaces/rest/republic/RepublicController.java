package com.rephelper.interfaces.rest.republic;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rephelper.application.dto.request.CreateRepublicRequest;
import com.rephelper.application.dto.request.JoinRepublicRequest;
import com.rephelper.application.dto.request.RegenerateCodeRequest;
import com.rephelper.application.dto.request.UpdateRepublicRequest;
import com.rephelper.application.dto.response.ApiResponse;
import com.rephelper.application.dto.response.AuthResponse;
import com.rephelper.application.dto.response.RepublicResponse;
import com.rephelper.application.dto.response.UserResponse;
import com.rephelper.application.dto.response.RepublicCodeResponse;
import com.rephelper.application.mapper.RepublicDtoMapper;
import com.rephelper.application.mapper.UserDtoMapper;
import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.model.Address;
import com.rephelper.domain.model.Republic;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.RepublicServicePort;
import com.rephelper.domain.port.in.UserServicePort;
import com.rephelper.infrastructure.adapter.security.CustomUserDetails;
import com.rephelper.infrastructure.adapter.security.JwtTokenProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/republics")
@RequiredArgsConstructor
@Tag(name = "Republics", description = "Republic management endpoints")
public class RepublicController {

    private final RepublicServicePort republicService;
    private final UserServicePort userService;
    private final RepublicDtoMapper republicDtoMapper;
    private final UserDtoMapper userDtoMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    @Operation(summary = "Create a new republic", description = "Creates a new republic with the current user as owner")
    public ResponseEntity<AuthResponse> createRepublic(
            @Valid @RequestBody CreateRepublicRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get current user
        User owner = userService.getUserById(currentUser.getUserId());

        // Create republic from request
        Republic republic = Republic.builder()
                .name(request.getName())
                .address(Address.builder()
                        .street(request.getStreet())
                        .number(request.getNumber())
                        .complement(request.getComplement())
                        .neighborhood(request.getNeighborhood())
                        .city(request.getCity())
                        .state(request.getState())
                        .zipCode(request.getZipCode())
                        .build())
                .owner(owner)
                .build();

        // Create republic
        Republic createdRepublic = republicService.createRepublic(republic);

        // Get updated user (with new republic association)
        User updatedUser = userService.getUserById(currentUser.getUserId());

        // Generate new token with updated user info
        String token = jwtTokenProvider.generateToken(updatedUser.getId());

        // Create response with token and user
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .user(userDtoMapper.toUserResponse(updatedUser))
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all republics", description = "Retrieves all republics")
    public ResponseEntity<List<RepublicResponse>> getAllRepublics() {
        List<Republic> republics = republicService.getAllRepublics();
        return ResponseEntity.ok(republicDtoMapper.toRepublicResponseList(republics));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get republic by ID", description = "Retrieves republic details by ID")
    public ResponseEntity<RepublicResponse> getRepublicById(@PathVariable UUID id) {
        Republic republic = republicService.getRepublicById(id);
        return ResponseEntity.ok(republicDtoMapper.toRepublicResponse(republic));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get republic by code", description = "Retrieves republic details by invitation code")
    public ResponseEntity<RepublicResponse> getRepublicByCode(@PathVariable String code) {
        Republic republic = republicService.getRepublicByCode(code);
        return ResponseEntity.ok(republicDtoMapper.toRepublicResponse(republic));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update republic", description = "Updates republic details")
    public ResponseEntity<RepublicResponse> updateRepublic(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRepublicRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get current republic
        Republic republic = republicService.getRepublicById(id);

        // Check if user is owner or admin
        User user = userService.getUserById(currentUser.getUserId());
        boolean isSystemAdmin = "admin".equals(currentUser.getRole());
        boolean isOwner = republic.getOwner().getId().equals(currentUser.getUserId());

        if (!isSystemAdmin && !isOwner) {
            throw new ForbiddenException("You do not have permission to update this republic");
        }

        // Convert request to address
        Address address = republicDtoMapper.toAddress(request);

        // Update republic
        Republic updatedRepublic = republicService.updateRepublic(id, request.getName(), address);

        return ResponseEntity.ok(republicDtoMapper.toRepublicResponse(updatedRepublic));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete republic", description = "Deletes a republic")
    public ResponseEntity<ApiResponse> deleteRepublic(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get current republic
        Republic republic = republicService.getRepublicById(id);

        // Check if user is owner or admin
        boolean isSystemAdmin = "admin".equals(currentUser.getRole());
        boolean isOwner = republic.getOwner().getId().equals(currentUser.getUserId());

        if (!isSystemAdmin && !isOwner) {
            throw new ForbiddenException("You do not have permission to delete this republic");
        }

        // Delete republic
        republicService.deleteRepublic(id);

        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("Republic deleted successfully")
                .build());
    }

    @PostMapping("/join")
    @Operation(summary = "Join republic", description = "Join a republic using the invitation code")
    public ResponseEntity<AuthResponse> joinRepublic(
            @Valid @RequestBody JoinRepublicRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Join republic
        User updatedUser = republicService.joinRepublicByCode(currentUser.getUserId(), request.getCode());

        // Generate new token with updated user info
        String token = jwtTokenProvider.generateToken(updatedUser.getId());

        // Create response with token and user
        AuthResponse response = AuthResponse.builder()
                .token(token)
                .user(userDtoMapper.toUserResponse(updatedUser))
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{republicId}/members/{userId}/admin")
    @Operation(summary = "Add admin", description = "Add a user as admin of a republic")
    public ResponseEntity<RepublicResponse> addRepublicAdmin(
            @PathVariable UUID republicId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get current republic
        Republic republic = republicService.getRepublicById(republicId);

        // Check if user is owner or admin
        boolean isSystemAdmin = "admin".equals(currentUser.getRole());
        boolean isOwner = republic.getOwner().getId().equals(currentUser.getUserId());

        if (!isSystemAdmin && !isOwner) {
            throw new ForbiddenException("You do not have permission to add admins to this republic");
        }

        // Add admin
        Republic updatedRepublic = republicService.addRepublicAdmin(republicId, userId);

        return ResponseEntity.ok(republicDtoMapper.toRepublicResponse(updatedRepublic));
    }

    @DeleteMapping("/{republicId}/members/{userId}/admin")
    @Operation(summary = "Remove admin", description = "Remove admin status from a user in a republic")
    public ResponseEntity<RepublicResponse> removeRepublicAdmin(
            @PathVariable UUID republicId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get current republic
        Republic republic = republicService.getRepublicById(republicId);

        // Check if user is owner or admin
        boolean isSystemAdmin = "admin".equals(currentUser.getRole());
        boolean isOwner = republic.getOwner().getId().equals(currentUser.getUserId());

        if (!isSystemAdmin && !isOwner) {
            throw new ForbiddenException("You do not have permission to remove admins from this republic");
        }

        // Remove admin
        Republic updatedRepublic = republicService.removeRepublicAdmin(republicId, userId);

        return ResponseEntity.ok(republicDtoMapper.toRepublicResponse(updatedRepublic));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get members", description = "Get all members of a republic")
    public ResponseEntity<List<UserResponse>> getRepublicMembers(@PathVariable UUID id) {
        List<User> members = republicService.getRepublicMembers(id);
        return ResponseEntity.ok(userDtoMapper.toUserResponseList(members));
    }

    @PostMapping("/{id}/regenerate-code")
    @Operation(summary = "Regenerate code", description = "Regenerate the invitation code for a republic")
    public ResponseEntity<RepublicCodeResponse> regenerateRepublicCode(
            @PathVariable UUID id,
            @RequestBody(required = false) RegenerateCodeRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get current republic
        Republic republic = republicService.getRepublicById(id);

        // Check if user is owner or admin
        boolean isSystemAdmin = "admin".equals(currentUser.getRole());
        boolean isOwner = republic.getOwner().getId().equals(currentUser.getUserId());
        boolean isRepublicAdmin = republic.isAdmin(userService.getUserById(currentUser.getUserId()));

        if (!isSystemAdmin && !isOwner && !isRepublicAdmin) {
            throw new ForbiddenException("Você não tem permissão para regenerar o código desta república");
        }

        Republic updatedRepublic;
        
        // Se um código personalizado foi fornecido, use-o
        if (request != null && request.getCustomCode() != null && !request.getCustomCode().isBlank()) {
            updatedRepublic = republicService.regenerateCodeWithCustomCode(id, request.getCustomCode());
        } else {
            // Caso contrário, gere um código aleatório
            updatedRepublic = republicService.regenerateCode(id);
        }

        // Return only the code
        RepublicCodeResponse response = RepublicCodeResponse.builder()
                .code(updatedRepublic.getCode())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/code-length")
    @Operation(summary = "Get code length", description = "Get the required length for invitation codes")
    public ResponseEntity<Map<String, Integer>> getCodeLength() {
        int codeLength = republicService.getCodeLength();
        Map<String, Integer> response = new HashMap<>();
        response.put("codeLength", codeLength);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{republicId}/members/{memberId}")
    @Operation(summary = "Remove member", description = "Remove a user from a republic by clearing their current_republic_id")
    public ResponseEntity<ApiResponse> removeMember(
            @PathVariable UUID republicId,
            @PathVariable UUID memberId,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Get current republic
        Republic republic = republicService.getRepublicById(republicId);

        // Check if user is owner, admin or the user themselves
        boolean isSystemAdmin = "admin".equals(currentUser.getRole());
        boolean isOwner = republic.getOwner().getId().equals(currentUser.getUserId());
        boolean isRepublicAdmin = republic.isAdmin(userService.getUserById(currentUser.getUserId()));
        boolean isSelfRemoval = currentUser.getUserId().equals(memberId);

        if (!isSystemAdmin && !isOwner && !isRepublicAdmin && !isSelfRemoval) {
            throw new ForbiddenException("You do not have permission to remove members from this republic");
        }

        // Remove user from republic (clear current_republic_id)
        userService.removeFromRepublic(memberId);

        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("User removed from republic successfully")
                .build());
    }
}