package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import com.sliit.parking_reservation_and_management_system.logging.AdminActionLogger;
import com.parking.observer.booking.NotificationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    
    @Autowired
    private NotificationManager notificationManager;
    
    @Autowired
    private AdminActionLogger adminActionLogger;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // static role list for dropdown
    private static final List<String> ROLE_OPTIONS = List.of(
            "ADMIN",
            "CUSTOMER",
            "PARKING_SLOT_MANAGER",
            "FINANCE_EXECUTIVE",
            "SECURITY_OFFICER",
            "CUSTOMER_SUPPORT_OFFICER"
    );
    
    /**
     * Get current authenticated admin user
     * @return Admin user email or "Unknown" if not authenticated
     */
    private String getCurrentAdminEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            System.err.println("Error getting current admin: " + e.getMessage());
        }
        return "Unknown";
    }
    
    
    /**
     * Log admin action to file only
     * @param action Action performed
     * @param details Additional details
     * @param targetEntity Target entity (optional)
     * @param targetId Target entity ID (optional)
     * @param request HttpServletRequest for IP address
     */
    private void logAdminAction(String action, String details, String targetEntity, String targetId, HttpServletRequest request) {
        String adminEmail = getCurrentAdminEmail();
        
        // Log to file (singleton pattern)
        if (targetEntity != null && targetId != null) {
            adminActionLogger.logAction(adminEmail, action, details, targetEntity, targetId);
        } else {
            adminActionLogger.logAction(adminEmail, action, details);
        }
    }
    
    /**
     * Log admin action without target entity
     * @param action Action performed
     * @param details Additional details
     * @param request HttpServletRequest for IP address
     */
    private void logAdminAction(String action, String details, HttpServletRequest request) {
        logAdminAction(action, details, null, null, request);
    }
    // ---------------------------
    // Dashboard with pagination + filters
    // ---------------------------
    @GetMapping("/dashboard")
    public String viewDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String email,
            Model model,
            HttpServletRequest request
    ) {
        int pageSize = 15;

        Page<User> userPage = userService.searchUsers(role, status, email, page, pageSize);

        model.addAttribute("userPage", userPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());

        // keep current filter values
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("email", email);

        // provide dropdown role options
        model.addAttribute("roleOptions", ROLE_OPTIONS);
        
        // Log admin dashboard access
        String filterDetails = String.format("Page: %d, Role: %s, Status: %s, Email: %s", 
            page, role != null ? role : "All", status != null ? status : "All", email != null ? email : "All");
        logAdminAction("DASHBOARD_ACCESS", "Accessed admin dashboard with filters: " + filterDetails, request);

        return "admin-dashboard";
    }

    // ---------------------------
    // Staff Registration
    // ---------------------------

    // Show staff registration form
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "user-register";
    }

    // Handle staff registration
    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("user") User user,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model , RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        // 1. Check duplicate email
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Email already exists. Please use another one.");
            return "user-register";
        }

        // 2. Validate password strength
        String rawPassword = user.getPasswordHash();
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,12}$";
        if (!rawPassword.matches(regex)) {
            model.addAttribute("user", user);
            model.addAttribute("error",
                    "Password must be 8–12 characters, include uppercase, lowercase, number, and special character.");
            return "user-register";
        }

        // 3. Confirm password match
        if (!rawPassword.equals(confirmPassword)) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Passwords do not match.");
            return "user-register";
        }

        // Email regex: must contain @ and .
        String emailRegex = "^[^@]+@[^@]+\\.[^@]+$";
        if (!user.getEmail().matches(emailRegex)) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Invalid email format. Must contain '@' and '.'");
            return "user-register"; // or "register" for customer
        }

        // Phone regex: must be 10 digits starting with 0
        String phoneRegex = "^0\\d{9}$";
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isBlank() &&
                !user.getPhoneNumber().matches(phoneRegex)) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Phone must be 10 digits and start with 0");
            return "user-register";
        }


        // 4. Hash password
        user.setPasswordHash(userService.encodePassword(rawPassword));

        // 5. Default status = ACTIVE
        user.setStatus("ACTIVE");

        // 6. Save user
        userService.saveUser(user);
        
        // Log user registration
        String userDetails = String.format("Email: %s, Role: %s, Name: %s %s", 
            user.getEmail(), user.getRole(), user.getFirstName(), user.getLastName());
        logAdminAction("USER_REGISTRATION", "Registered new user: " + userDetails, "User", user.getUserID().toString(), request);
        
        redirectAttributes.addFlashAttribute("success", "User registered successfully!");
        return "redirect:/admin/dashboard";
    }


    // ---------------------------
    // User Management
    // ---------------------------

    // Edit user form
    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable("id") int id, Model model, HttpServletRequest request) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        
        // Log user edit access
        logAdminAction("USER_EDIT_ACCESS", "Accessed edit form for user: " + user.getEmail(), "User", String.valueOf(id), request);
        
        return "edit-user";
    }

    // Update user
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable("id") int id,
                             @ModelAttribute("user") User updatedUser,
                             Model model , RedirectAttributes redirectAttributes,
                             HttpServletRequest request) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        // --- Email validation ---
        String emailRegex = "^[^@]+@[^@]+\\.[^@]+$";
        if (!updatedUser.getEmail().matches(emailRegex)) {
            model.addAttribute("user", updatedUser);
            model.addAttribute("error", "Invalid email format. Must contain '@' and '.'");
            return "edit-user";
        }

        // --- Phone validation ---
        String phoneRegex = "^0\\d{9}$";
        if (updatedUser.getPhoneNumber() != null && !updatedUser.getPhoneNumber().isBlank() &&
                !updatedUser.getPhoneNumber().matches(phoneRegex)) {
            model.addAttribute("user", updatedUser);
            model.addAttribute("error", "Phone must be 10 digits and start with 0");
            return "edit-user";
        }

        // --- Apply updates ---
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        user.setRole(updatedUser.getRole());
        user.setStatus(updatedUser.getStatus());

        userService.saveUser(user);
        
        // Log user update
        String updateDetails = String.format("Updated user %s: Name: %s %s, Role: %s, Status: %s", 
            user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole(), user.getStatus());
        logAdminAction("USER_UPDATE", updateDetails, "User", String.valueOf(id), request);

        // Add success message for redirect
        redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        return "redirect:/admin/dashboard";
    }


    // Delete user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") int id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Get user details before deletion for logging
        User userToDelete = userService.getUserById(id).orElse(null);
        String userEmail = userToDelete != null ? userToDelete.getEmail() : "Unknown";
        
        userService.deleteUser(id);
        
        // Log user deletion
        logAdminAction("USER_DELETE", "Deleted user: " + userEmail, "User", String.valueOf(id), request);
        
        redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        return "redirect:/admin/dashboard";
    }

    // Deactivate user
    @GetMapping("/deactivate/{id}")
    public String deactivateUser(@PathVariable("id") int id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Get user details before deactivation for logging
        User userToDeactivate = userService.getUserById(id).orElse(null);
        String userEmail = userToDeactivate != null ? userToDeactivate.getEmail() : "Unknown";
        
        userService.deactivateUser(id);
        
        // Log user deactivation
        logAdminAction("USER_DEACTIVATE", "Deactivated user: " + userEmail, "User", String.valueOf(id), request);
        
        redirectAttributes.addFlashAttribute("success", "User deactivated successfully!");
        return "redirect:/admin/dashboard";
    }

    // Activate user
    @GetMapping("/activate/{id}")
    public String activateUser(@PathVariable("id") int id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Get user details before activation for logging
        User userToActivate = userService.getUserById(id).orElse(null);
        String userEmail = userToActivate != null ? userToActivate.getEmail() : "Unknown";
        
        userService.activateUser(id);
        
        // Log user activation
        logAdminAction("USER_ACTIVATE", "Activated user: " + userEmail, "User", String.valueOf(id), request);
        
        redirectAttributes.addFlashAttribute("success", "User activated successfully!");
        return "redirect:/admin/dashboard";
    }
    
    // ========================================
    // OBSERVER PATTERN TESTING ENDPOINTS
    // ========================================
    
    /**
     * Test endpoint to demonstrate Observer pattern functionality
     * This creates a mock reservation and triggers all notification observers
     * Check the console to see the Observer pattern in action!
     */
    @GetMapping("/test-observer-pattern")
    @ResponseBody
    public String testObserverPattern(HttpServletRequest request) {
        try {
            // Create a mock reservation for testing
            Reservation mockReservation = new Reservation();
            mockReservation.setUserId(1L);
            mockReservation.setSlotId(101L);
            mockReservation.setVehicleNumber("ABC-1234");
            mockReservation.setStatus("CONFIRMED");
            mockReservation.setStartTime(LocalDateTime.now().plusHours(1));
            mockReservation.setEndTime(LocalDateTime.now().plusHours(3));
            mockReservation.setCreatedAt(LocalDateTime.now());
            
            // Trigger the Observer pattern
            System.out.println("\n🚀 ADMIN DASHBOARD: Testing Observer Pattern");
            System.out.println("================================================");
            
            // Test different event types
            notificationManager.notifyObservers(mockReservation, "CREATED");
            Thread.sleep(1000); // Small delay to see console output clearly
            
            notificationManager.notifyObservers(mockReservation, "UPDATED");
            Thread.sleep(1000);
            
            notificationManager.notifyObservers(mockReservation, "CANCELLED");
            
            System.out.println("================================================");
            System.out.println("✅ Observer Pattern test completed successfully!");
            System.out.println("📊 Active observers: " + notificationManager.getObserverCount());
            System.out.println("📋 Observer types: " + notificationManager.getObserverNames());
            
            // Log observer pattern test
            logAdminAction("OBSERVER_PATTERN_TEST", "Tested observer pattern functionality with mock reservation", request);
            
            return "<h1>✅ Observer Pattern Test Completed!</h1>" +
                   "<p><strong>Check your console/logs to see the Observer pattern in action!</strong></p>" +
                   "<ul>" +
                   "<li>📧 Email notifications triggered</li>" +
                   "<li>📱 SMS notifications triggered</li>" +
                   "<li>🔔 In-app notifications triggered</li>" +
                   "<li>🖥️ Console notifications logged</li>" +
                   "</ul>" +
                   "<p>Active observers: " + notificationManager.getObserverCount() + "</p>" +
                   "<p>Observer types: " + notificationManager.getObserverNames() + "</p>" +
                   "<p><a href='/admin/dashboard'>← Back to Admin Dashboard</a></p>";
                   
        } catch (Exception e) {
            System.err.println("❌ Observer Pattern test failed: " + e.getMessage());
            e.printStackTrace();
            return "<h1>❌ Observer Pattern Test Failed</h1>" +
                   "<p>Error: " + e.getMessage() + "</p>" +
                   "<p><a href='/admin/dashboard'>← Back to Admin Dashboard</a></p>";
        }
    }
    
    /**
     * Get Observer pattern statistics
     */
    @GetMapping("/observer-stats")
    @ResponseBody
    public String getObserverStats(HttpServletRequest request) {
        // Log observer stats access
        logAdminAction("OBSERVER_STATS_ACCESS", "Accessed observer pattern statistics", request);
        return "<h2>📊 Observer Pattern Statistics</h2>" +
               "<ul>" +
               "<li><strong>Total Observers:</strong> " + notificationManager.getObserverCount() + "</li>" +
               "<li><strong>Observer Types:</strong> " + notificationManager.getObserverNames() + "</li>" +
               "</ul>" +
               "<p><a href='/admin/test-observer-pattern'>🧪 Test Observer Pattern</a></p>" +
               "<p><a href='/admin/dashboard'>← Back to Admin Dashboard</a></p>";
    }
    
    /**
     * Test endpoint to verify admin logging functionality
     */
    @GetMapping("/test-logging")
    @ResponseBody
    public String testLogging(HttpServletRequest request) {
        // Log test action
        logAdminAction("LOGGING_TEST", "Tested admin logging functionality", request);
        
        return "<h1>✅ Admin Logging Test Completed!</h1>" +
               "<p><strong>Check the following to verify logging is working:</strong></p>" +
               "<ul>" +
               "<li>📄 <a href='/admin/logs/database' target='_blank'>View Database Logs</a></li>" +
               "<li>📁 <a href='/admin/logs/file' target='_blank'>View File Logs</a></li>" +
               "<li>📊 <a href='/admin/logs/admin/" + getCurrentAdminEmail() + "' target='_blank'>View My Actions</a></li>" +
               "</ul>" +
               "<p><strong>Log file location:</strong> " + adminActionLogger.getLogFilePath() + "</p>" +
               "<p><a href='/admin/dashboard'>← Back to Admin Dashboard</a></p>";
    }
}
