package com.sliit.parking_reservation_and_management_system.controller;
import com.sliit.parking_reservation_and_management_system.entity.Notification;
import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.NotificationService;
import com.sliit.parking_reservation_and_management_system.service.ParkingSlotService;
import com.sliit.parking_reservation_and_management_system.service.PaymentService;
import com.sliit.parking_reservation_and_management_system.service.ReservationService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class DashboardController {
    
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private ParkingSlotService parkingSlotService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/dashboard")
    public String customerDashboard(Model model) {
        System.out.println("=== DASHBOARD ENDPOINT HIT: /customer/dashboard ===");
        return loadDashboard(model);
    }
    
    // Add a simple test endpoint to verify routing
    @GetMapping("/test")
    public String testEndpoint() {
        System.out.println("=== TEST ENDPOINT HIT: /customer/test ===");
        return "customer-dashboard"; // Return same template for testing
    }
    
    
    private String loadDashboard(Model model) {
        System.out.println("=== Customer Dashboard Request Started ===");
        
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = null;
            
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                System.out.println("Authenticated user: " + username);
                
                // Try to get user from database by email/username
                try {
                    // Assuming username is email, find user by email
                    currentUser = userService.getAllUsers().stream()
                        .filter(user -> user.getEmail().equals(username))
                        .findFirst()
                        .orElse(null);
                    
                    if (currentUser != null) {
                        System.out.println("Found user in database: " + currentUser.getEmail());
                    } else {
                        System.out.println("User not found in database, creating default user");
                        // Create a default user for testing
                        currentUser = new User();
                        currentUser.setUserID(1L); // Default user ID for testing
                        currentUser.setFirstName("Dulanjana");
                        currentUser.setLastName("Ayeshmantha");
                        currentUser.setEmail(username);
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching user from database: " + e.getMessage());
                    // Create default user
                    currentUser = new User();
                    currentUser.setUserID(1L);
                    currentUser.setFirstName("Customer");
                    currentUser.setLastName("User");
                    currentUser.setEmail("customer@example.com");
                }
            } else {
                System.out.println("No authentication found, using default user");
                currentUser = new User();
                currentUser.setUserID(1L);
                currentUser.setFirstName("Dulanjana");
                currentUser.setLastName("Ayeshmantha");
                currentUser.setEmail("customer@example.com");
            }
            
            // Add user to model
            model.addAttribute("user", currentUser);
            System.out.println("User added to model: " + currentUser.getFirstName() + " " + currentUser.getLastName());
            
            // Fetch real dashboard statistics from database
            Long totalBookings = 0L;
            Long activeBookings = 0L;
            Long availableSlots = 0L;
            Double totalSpent = 0.0;
            
            // Get reservation statistics for the user
            if (currentUser.getUserID() != null) {
                try {
                    System.out.println("Fetching reservation data for user ID: " + currentUser.getUserID());
                    totalBookings = reservationService.countByUserId(currentUser.getUserID());
                    activeBookings = reservationService.countActiveByUserId(currentUser.getUserID());
                    
                    // Calculate real total spent from database
                    totalSpent = reservationService.getTotalSpentByUserId(currentUser.getUserID());
                    
                    System.out.println("Successfully fetched reservation data:");
                    System.out.println("- Total Bookings: " + totalBookings);
                    System.out.println("- Active Bookings: " + activeBookings);
                    System.out.println("- Total Spent: $" + totalSpent);
                    
                } catch (Exception e) {
                    System.err.println("Error fetching reservation data: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Get parking slot statistics
            try {
                System.out.println("Fetching parking slot data");
                availableSlots = parkingSlotService.countAvailableSlots();
                System.out.println("Available slots: " + availableSlots);
            } catch (Exception e) {
                e.printStackTrace();
                // If no slots in database, set a default
                availableSlots = 0L;
            }
            
            // Get recent activity (last 5 reservations)
            List<Reservation> recentReservations = reservationService.getRecentReservationsByUserId(currentUser.getUserID(), 5);
            
            // Get notification data with error handling
            List<Notification> recentNotifications = new ArrayList<>();
            List<Notification> unreadNotifications = new ArrayList<>();
            long unreadCount = 0;
            
            try {
                System.out.println("Fetching notification data for user ID: " + currentUser.getUserID());
                
                // Create a test notification if none exist
                long existingCount = notificationService.getUnreadCount(currentUser.getUserID());
                if (existingCount == 0) {
                    System.out.println("No notifications found, creating test notification");
                    notificationService.createNotification(
                        currentUser.getUserID(), 
                        "INFO", 
                        "Welcome to the Parking Management System! Your dashboard is ready to use."
                    );
                }
                
                recentNotifications = notificationService.getRecentNotifications(currentUser.getUserID(), 10);
                unreadNotifications = notificationService.getUnreadNotifications(currentUser.getUserID());
                unreadCount = notificationService.getUnreadCount(currentUser.getUserID());
                
                System.out.println("Successfully fetched notification data:");
                System.out.println("- Recent Notifications: " + recentNotifications.size());
                System.out.println("- Unread Count: " + unreadCount);
                
            } catch (Exception e) {
                System.err.println("Error fetching notification data: " + e.getMessage());
                e.printStackTrace();
                // Continue with empty notification lists
            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("totalBookings", totalBookings);
            model.addAttribute("activeBookings", activeBookings);
            model.addAttribute("availableSlots", availableSlots);
            model.addAttribute("totalSpent", totalSpent);
            model.addAttribute("recentReservations", recentReservations);
            model.addAttribute("recentNotifications", recentNotifications);
            model.addAttribute("unreadNotifications", unreadNotifications);
            model.addAttribute("unreadCount", unreadCount);
            
            System.out.println("Dashboard loaded for user: " + currentUser.getEmail());
            System.out.println("- Total bookings: " + totalBookings);
            System.out.println("- Active bookings: " + activeBookings);
            System.out.println("- Available slots: " + availableSlots);
            System.out.println("- Recent reservations: " + recentReservations.size());
        } catch (Exception e) {
            System.err.println("=== CRITICAL ERROR in customerDashboard ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getSimpleName());
            e.printStackTrace();
            System.err.println("=== END CRITICAL ERROR ===");
            
            // Fallback to prevent complete failure
            User fallbackUser = new User();
            fallbackUser.setFirstName("Customer");
            fallbackUser.setLastName("User");
            fallbackUser.setEmail("customer@example.com");
            fallbackUser.setUserID(1L);
            
            model.addAttribute("user", fallbackUser);
            model.addAttribute("totalBookings", 0L);
            model.addAttribute("activeBookings", 0L);
            model.addAttribute("availableSlots", 0L);
            model.addAttribute("totalSpent", "$0.00");
            model.addAttribute("recentReservations", new ArrayList<>());
            model.addAttribute("recentNotifications", new ArrayList<>());
            model.addAttribute("unreadNotifications", new ArrayList<>());
            model.addAttribute("unreadCount", 0L);
        }
        
        System.out.println("=== Returning customer-dashboard template ===");
        System.out.println("Template path: src/main/resources/templates/customer-dashboard.html");
        
        try {
            return "customer-dashboard";
        } catch (Exception templateError) {
            System.err.println("Template resolution error: " + templateError.getMessage());
            templateError.printStackTrace();
            throw templateError;
        }
    }

}
