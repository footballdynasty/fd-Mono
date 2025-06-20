package com.footballdynasty.controller;

import com.footballdynasty.entity.Notification;
import com.footballdynasty.entity.User;
import com.footballdynasty.entity.Role;
import com.footballdynasty.service.NotificationService;
import com.footballdynasty.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.sentry.Sentry;

import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "User notification management APIs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final NotificationService notificationService;
    private final PermissionService permissionService;

    @Autowired
    public NotificationController(NotificationService notificationService, PermissionService permissionService) {
        this.notificationService = notificationService;
        this.permissionService = permissionService;
    }

    @GetMapping
    @Operation(summary = "Get user notifications", description = "Retrieve notifications for the authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUserNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int page) {
        
        try {
            Optional<User> currentUserOpt = permissionService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = currentUserOpt.get();
            List<Notification> notifications = notificationService.getUserNotifications(
                currentUser.getId(), unreadOnly, limit);
            
            Map<String, Object> stats = notificationService.getUserNotificationStats(currentUser.getId());

            // Convert notifications to DTOs
            List<Map<String, Object>> notificationDTOs = new ArrayList<>();
            for (Notification notification : notifications) {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", notification.getId().toString());
                dto.put("type", notification.getType().name());
                dto.put("title", notification.getTitle());
                dto.put("message", notification.getMessage());
                dto.put("isRead", notification.isRead());
                dto.put("createdAt", notification.getCreatedAt().format(ISO_FORMATTER));
                dto.put("data", notification.getData() != null ? notification.getData() : new HashMap<>());
                notificationDTOs.add(dto);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationDTOs);
            response.put("stats", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error retrieving notifications: {}", e.getMessage(), e);
            Sentry.captureException(e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve notifications"));
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get notification statistics", description = "Get notification counts and statistics for the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getNotificationStats() {
        try {
            Optional<User> currentUserOpt = permissionService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = currentUserOpt.get();

            Map<String, Object> stats = notificationService.getUserNotificationStats(currentUser.getId());
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("Error retrieving notification statistics: {}", e.getMessage(), e);
            Sentry.captureException(e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve notification statistics"));
        }
    }

    @GetMapping("/inbox-count")
    @Operation(summary = "Get inbox count", description = "Get total unread notifications and admin requests for navbar badge")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inbox count retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getInboxCount() {
        try {
            Optional<User> currentUserOpt = permissionService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = currentUserOpt.get();

            boolean isAdmin = currentUser.getRoles().contains(Role.COMMISSIONER);
            Map<String, Object> count = notificationService.getInboxCount(currentUser.getId(), isAdmin);
            
            return ResponseEntity.ok(count);

        } catch (Exception e) {
            logger.error("Error retrieving inbox count: {}", e.getMessage(), e);
            Sentry.captureException(e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve inbox count"));
        }
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification marked as read"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> markAsRead(@PathVariable String notificationId) {
        try {
            Optional<User> currentUserOpt = permissionService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = currentUserOpt.get();

            UUID notificationUUID = UUID.fromString(notificationId);
            notificationService.markAsRead(notificationUUID, currentUser.getId());

            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid notification ID"));
        } catch (Exception e) {
            logger.error("Error marking notification as read: {}", e.getMessage(), e);
            Sentry.captureException(e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to mark notification as read"));
        }
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all unread notifications as read for the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All notifications marked as read"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> markAllAsRead() {
        try {
            Optional<User> currentUserOpt = permissionService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = currentUserOpt.get();

            int updated = notificationService.markAllAsRead(currentUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "All notifications marked as read");
            response.put("count", updated);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error marking all notifications as read: {}", e.getMessage(), e);
            Sentry.captureException(e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to mark all notifications as read"));
        }
    }

    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deleteNotification(@PathVariable String notificationId) {
        try {
            Optional<User> currentUserOpt = permissionService.getCurrentUser();
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.status(401).body(Map.of("error", "User not authenticated"));
            }
            
            User currentUser = currentUserOpt.get();

            UUID notificationUUID = UUID.fromString(notificationId);
            notificationService.deleteNotification(notificationUUID, currentUser.getId());

            return ResponseEntity.ok(Map.of("message", "Notification deleted"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid notification ID"));
        } catch (Exception e) {
            logger.error("Error deleting notification: {}", e.getMessage(), e);
            Sentry.captureException(e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to delete notification"));
        }
    }
}