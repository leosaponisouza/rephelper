package com.rephelper.interfaces.rest.notification;

import com.rephelper.application.dto.response.ApiResponse;
import com.rephelper.application.dto.response.NotificationResponse;
import com.rephelper.application.mapper.NotificationDtoMapper;
import com.rephelper.domain.model.Notification;
import com.rephelper.domain.port.in.NotificationServicePort;
import com.rephelper.infrastructure.adapter.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationServicePort notificationService;
    private final NotificationDtoMapper notificationDtoMapper;

    @GetMapping
    @Operation(summary = "Get all notifications", description = "Retrieves all notifications for the current user")
    public ResponseEntity<List<NotificationResponse>> getAllNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<Notification> notifications = notificationService.getNotificationsByUser(currentUser.getUserId());
        return ResponseEntity.ok(notificationDtoMapper.toNotificationResponseList(notifications));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieves unread notifications for the current user")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        List<Notification> notifications = notificationService.getUnreadNotificationsByUser(currentUser.getUserId());
        return ResponseEntity.ok(notificationDtoMapper.toNotificationResponseList(notifications));
    }

    @GetMapping("/count")
    @Operation(summary = "Count unread notifications", description = "Counts unread notifications for the current user")
    public ResponseEntity<Map<String, Object>> countUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        int count = notificationService.countUnreadNotificationsByUser(currentUser.getUserId());
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Retrieves a specific notification")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Notification notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notificationDtoMapper.toNotificationResponse(notification));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        Notification notification = notificationService.markNotificationAsRead(id);
        return ResponseEntity.ok(notificationDtoMapper.toNotificationResponse(notification));
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications as read for the current user")
    public ResponseEntity<ApiResponse> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        notificationService.markAllNotificationsAsRead(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("All notifications marked as read")
                .build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification", description = "Deletes a notification")
    public ResponseEntity<ApiResponse> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        notificationService.deleteNotification(id, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("Notification deleted successfully")
                .build());
    }

    @DeleteMapping("/clear-old")
    @Operation(summary = "Clear old notifications", description = "Deletes notifications older than the specified number of days")
    public ResponseEntity<ApiResponse> clearOldNotifications(
            @RequestParam(defaultValue = "30") int days,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        notificationService.deleteOldNotifications(currentUser.getUserId(), days);
        return ResponseEntity.ok(ApiResponse.builder()
                .status("success")
                .message("Old notifications cleared successfully")
                .build());
    }
}