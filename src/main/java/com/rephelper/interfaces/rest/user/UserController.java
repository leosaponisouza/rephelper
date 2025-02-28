package com.rephelper.interfaces.rest.user;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rephelper.application.dto.request.CreateUserRequest;
import com.rephelper.application.dto.request.UpdateUserRequest;
import com.rephelper.application.dto.response.ApiResponse;
import com.rephelper.application.dto.response.UserResponse;
import com.rephelper.application.mapper.UserDtoMapper;
import com.rephelper.domain.exception.ForbiddenException;
import com.rephelper.domain.model.User;
import com.rephelper.domain.port.in.UserServicePort;
import com.rephelper.infrastructure.adapter.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
public class UserController {

    private final UserServicePort userService;
    private final UserDtoMapper userDtoMapper;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with the given details")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User newUser = userService.createUser(userDtoMapper.toUser(request));
        return new ResponseEntity<>(userDtoMapper.toUserResponse(newUser), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Retrieves all users (admin only)")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(userDtoMapper.toUserResponseList(users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves user details by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userDtoMapper.toUserResponse(user));
    }

    @GetMapping("/firebase/{firebaseUid}")
    @Operation(summary = "Get user by Firebase UID", description = "Retrieves user details by Firebase UID")
    public ResponseEntity<UserResponse> getUserByFirebaseUid(@PathVariable String firebaseUid) {
        User user = userService.getUserByFirebaseUid(firebaseUid);
        return ResponseEntity.ok(userDtoMapper.toUserResponse(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates a user's details")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Check if user is updating their own profile or is an admin
        if (!id.equals(currentUser.getUserId()) && !currentUser.getRole().equals("ADMIN")) {
            throw new ForbiddenException("You do not have permission to update this user");
        }

        User userToUpdate = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .profilePictureUrl(request.getProfilePictureUrl())
                .build();

        User updatedUser = userService.updateUser(id, userToUpdate);
        return ResponseEntity.ok(userDtoMapper.toUserResponse(updatedUser));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user")
    public ResponseEntity<ApiResponse> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        // Check if user is deleting their own account or is an admin
        if (!id.equals(currentUser.getUserId()) && !currentUser.getRole().equals("ADMIN")) {
            throw new ForbiddenException("You do not have permission to delete this user");
        }

        userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("User deleted successfully")
                .build());
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieves the current authenticated user's details")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails currentUser) {
        User user = userService.getUserById(currentUser.getUserId());
        return ResponseEntity.ok(userDtoMapper.toUserResponse(user));
    }
}