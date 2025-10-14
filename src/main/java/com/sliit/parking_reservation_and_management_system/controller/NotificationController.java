package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.Notification;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.NotificationService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    // Get current authenticated user
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                return userService.getAllUsers().stream()
                    .filter(user -> user.getEmail().equals(username))
                    .findFirst()
                    .orElse(null);
            }
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
        }
        return null;
    }
    
    // View all notifications page
    @GetMapping("/notifications")
    public String viewNotifications(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            List<Notification> notifications = notificationService.getNotificationsByUserId(currentUser.getUserID());
            long unreadCount = notificationService.getUnreadCount(currentUser.getUserID());
            
            model.addAttribute("notifications", notifications);
            model.addAttribute("unreadCount", unreadCount);
            model.addAttribute("user", currentUser);
            
        } catch (Exception e) {
            System.err.println("Error loading notifications: " + e.getMessage());
            model.addAttribute("error", "Error loading notifications");
        }
        
        return "customer-notifications";
    }
    
    // API endpoint to get unread count
    @GetMapping("/api/notifications/unread-count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            long unreadCount = notificationService.getUnreadCount(currentUser.getUserID());
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", unreadCount);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error getting unread count");
            response.put("success", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // API endpoint to mark notification as read
    @PostMapping("/api/notifications/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Verify notification belongs to current user
            Optional<Notification> notification = notificationService.getNotificationById(id);
            if (notification.isEmpty() || !notification.get().getUserId().equals(currentUser.getUserID())) {
                return ResponseEntity.notFound().build();
            }
            
            boolean success = notificationService.markAsRead(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            
            if (success) {
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to mark notification as read");
                return ResponseEntity.internalServerError().body(response);
            }
            
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error marking notification as read");
            response.put("success", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // API endpoint to mark all notifications as read
    @PostMapping("/api/notifications/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            boolean success = notificationService.markAllAsRead(currentUser.getUserID());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            
            if (success) {
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to mark all notifications as read");
                return ResponseEntity.internalServerError().body(response);
            }
            
        } catch (Exception e) {
            System.err.println("Error marking all notifications as read: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error marking all notifications as read");
            response.put("success", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // API endpoint to get recent notifications
    @GetMapping("/api/notifications/recent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentNotifications(@RequestParam(defaultValue = "10") int limit) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<Notification> notifications = notificationService.getRecentNotifications(currentUser.getUserID(), limit);
            long unreadCount = notificationService.getUnreadCount(currentUser.getUserID());
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notifications);
            response.put("unreadCount", unreadCount);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error getting recent notifications: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error getting recent notifications");
            response.put("success", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // Delete notification
    @DeleteMapping("/api/notifications/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // Verify notification belongs to current user
            Optional<Notification> notification = notificationService.getNotificationById(id);
            if (notification.isEmpty() || !notification.get().getUserId().equals(currentUser.getUserID())) {
                return ResponseEntity.notFound().build();
            }
            
            boolean success = notificationService.deleteNotification(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            
            if (success) {
                return ResponseEntity.ok(response);
            } else {
                response.put("error", "Failed to delete notification");
                return ResponseEntity.internalServerError().body(response);
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Error deleting notification");
            response.put("success", false);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
