package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.SuspiciousReport;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.service.SuspiciousReportService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import com.sliit.parking_reservation_and_management_system.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/security")
public class SecurityOfficerController {

    @Autowired
    private SuspiciousReportService suspiciousReportService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReservationService reservationService;

    // Security Officer Dashboard
    @GetMapping("/dashboard")
    public String securityDashboard(Model model) {
        System.out.println("=== SECURITY OFFICER DASHBOARD ENDPOINT HIT ===");
        
        try {
            // Get current authenticated user
            User currentUser = getCurrentUser();
            System.out.println("Current user from getCurrentUser(): " + (currentUser != null ? currentUser.getEmail() + " (ID: " + currentUser.getUserID() + ")" : "null"));
            
            model.addAttribute("user", currentUser);
            
            // Get dashboard statistics
            if (currentUser != null && currentUser.getUserID() != null) {
                Long officerID = currentUser.getUserID();
                System.out.println("Processing dashboard for officer ID: " + officerID);
                
                // Get all reports by this officer first to debug
                List<SuspiciousReport> allOfficerReports = suspiciousReportService.getReportsBySecurityOfficer(officerID);
                System.out.println("Total reports found for officer " + officerID + ": " + allOfficerReports.size());
                
                if (!allOfficerReports.isEmpty()) {
                    System.out.println("Sample reports for officer " + officerID + ":");
                    for (int i = 0; i < Math.min(3, allOfficerReports.size()); i++) {
                        SuspiciousReport report = allOfficerReports.get(i);
                        System.out.println("  - Report #" + report.getSuspiciousID() + " - " + report.getStatus() + " - " + report.getDate());
                    }
                }
                
                // Get dashboard statistics
                SuspiciousReportService.DashboardStats stats = suspiciousReportService.getDashboardStats(officerID);
                model.addAttribute("totalReports", stats.getTotalReports());
                model.addAttribute("pendingReports", stats.getPendingReports());
                model.addAttribute("recentReports", stats.getRecentReports());
                
                // Get recent suspicious reports for this security officer (last 5 reports)
                List<SuspiciousReport> recentReportsList = suspiciousReportService.getLatestReportsBySecurityOfficer(officerID, 5);
                model.addAttribute("recentReportsList", recentReportsList);
                System.out.println("Recent reports list size: " + recentReportsList.size());
                
                // Get recent reports from last 7 days for this officer
                List<SuspiciousReport> weeklyReports = suspiciousReportService.getRecentReportsBySecurityOfficer(officerID);
                model.addAttribute("weeklyReportsCount", weeklyReports.size());
                System.out.println("Weekly reports count: " + weeklyReports.size());
                
                // Get active reservations for verification
                try {
                    List<Reservation> activeReservations = reservationService.getCurrentActiveReservations();
                    model.addAttribute("activeReservations", activeReservations.size() > 10 ? activeReservations.subList(0, 10) : activeReservations);
                    model.addAttribute("totalActiveReservations", activeReservations.size());
                    System.out.println("Active reservations: " + activeReservations.size());
                } catch (Exception e) {
                    System.err.println("Error getting active reservations: " + e.getMessage());
                    model.addAttribute("activeReservations", List.of());
                    model.addAttribute("totalActiveReservations", 0);
                }
                
                System.out.println("Dashboard data summary for officer " + officerID + ":");
                System.out.println("- Total reports: " + stats.getTotalReports());
                System.out.println("- Pending reports: " + stats.getPendingReports());
                System.out.println("- Recent reports list size: " + recentReportsList.size());
                System.out.println("- Weekly reports: " + weeklyReports.size());
            } else {
                System.out.println("No current user found or user ID is null, using default values");
                // Default values for testing
                model.addAttribute("totalReports", 0L);
                model.addAttribute("pendingReports", 0L);
                model.addAttribute("recentReports", 0L);
                model.addAttribute("recentReportsList", List.of());
                model.addAttribute("weeklyReportsCount", 0);
                model.addAttribute("activeReservations", List.of());
                model.addAttribute("totalActiveReservations", 0);
            }
            
            System.out.println("Security Officer Dashboard loaded successfully");
            return "security-dashboard";
            
        } catch (Exception e) {
            System.err.println("Error loading security dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback data
            User fallbackUser = new User();
            fallbackUser.setFirstName("Security");
            fallbackUser.setLastName("Officer");
            fallbackUser.setEmail("security@example.com");
            fallbackUser.setUserID(1L);
            
            model.addAttribute("user", fallbackUser);
            model.addAttribute("totalReports", 0L);
            model.addAttribute("pendingReports", 0L);
            model.addAttribute("recentReports", 0L);
            model.addAttribute("recentReportsList", List.of());
            model.addAttribute("weeklyReportsCount", 0);
            model.addAttribute("activeReservations", List.of());
            model.addAttribute("totalActiveReservations", 0);
            
            return "security-dashboard";
        }
    }

    // Test endpoint
    @GetMapping("/test-report")
    @ResponseBody
    public String testReport() {
        return "<h1>Security Officer Test Report</h1><p>This endpoint is working!</p><p><a href='/security/dashboard'>Go to Dashboard</a></p>";
    }
    
    // Debug dashboard endpoint
    @GetMapping("/debug-dashboard")
    @ResponseBody
    public String debugDashboard() {
        StringBuilder html = new StringBuilder();
        html.append("<h1>Security Officer Dashboard Debug</h1>");
        
        try {
            User currentUser = getCurrentUser();
            html.append("<h2>Current User</h2>");
            if (currentUser != null) {
                html.append("<p><strong>User ID:</strong> ").append(currentUser.getUserID()).append("</p>");
                html.append("<p><strong>Name:</strong> ").append(currentUser.getFirstName()).append(" ").append(currentUser.getLastName()).append("</p>");
                html.append("<p><strong>Email:</strong> ").append(currentUser.getEmail()).append("</p>");
                html.append("<p><strong>Role:</strong> ").append(currentUser.getRole()).append("</p>");
                
                Long officerID = currentUser.getUserID();
                
                // Test all report queries
                html.append("<h2>Reports for Officer ID: ").append(officerID).append("</h2>");
                
                List<SuspiciousReport> allReports = suspiciousReportService.getReportsBySecurityOfficer(officerID);
                html.append("<p><strong>Total Reports:</strong> ").append(allReports.size()).append("</p>");
                
                List<SuspiciousReport> recentReports = suspiciousReportService.getRecentReportsBySecurityOfficer(officerID);
                html.append("<p><strong>Recent Reports (7 days):</strong> ").append(recentReports.size()).append("</p>");
                
                List<SuspiciousReport> latestReports = suspiciousReportService.getLatestReportsBySecurityOfficer(officerID, 5);
                html.append("<p><strong>Latest 5 Reports:</strong> ").append(latestReports.size()).append("</p>");
                
                if (!latestReports.isEmpty()) {
                    html.append("<h3>Latest Reports Details:</h3>");
                    html.append("<ul>");
                    for (SuspiciousReport report : latestReports) {
                        html.append("<li>Report #").append(report.getSuspiciousID())
                            .append(" - ").append(report.getStatus())
                            .append(" - ").append(report.getDate())
                            .append(" - ").append(report.getDescription().substring(0, Math.min(50, report.getDescription().length())))
                            .append("...</li>");
                    }
                    html.append("</ul>");
                }
                
                // Test dashboard stats
                SuspiciousReportService.DashboardStats stats = suspiciousReportService.getDashboardStats(officerID);
                html.append("<h3>Dashboard Statistics:</h3>");
                html.append("<p><strong>Total Reports:</strong> ").append(stats.getTotalReports()).append("</p>");
                html.append("<p><strong>Pending Reports:</strong> ").append(stats.getPendingReports()).append("</p>");
                html.append("<p><strong>Recent Reports Count:</strong> ").append(stats.getRecentReports()).append("</p>");
                
            } else {
                html.append("<p style='color: red;'>No current user found!</p>");
            }
            
        } catch (Exception e) {
            html.append("<p style='color: red;'>Error: ").append(e.getMessage()).append("</p>");
            html.append("<pre>").append(e.toString()).append("</pre>");
        }
        
        html.append("<h2>Quick Actions</h2>");
        html.append("<p><a href='/security/dashboard' style='background: #28a745; color: white; padding: 10px; text-decoration: none; border-radius: 5px; margin-right: 10px;'>Go to Dashboard</a>");
        html.append("<a href='/public/check-reports' style='background: #007bff; color: white; padding: 10px; text-decoration: none; border-radius: 5px;'>Check All Reports</a></p>");
        
        return html.toString();
    }
    
    // Test template endpoint
    @GetMapping("/test-template")
    public String testTemplate(Model model) {
        System.out.println("=== TEST TEMPLATE ENDPOINT HIT ===");
        
        // Create a simple test user
        User testUser = new User();
        testUser.setUserID(999L);
        testUser.setFirstName("Test");
        testUser.setLastName("Security");
        testUser.setEmail("test@security.com");
        testUser.setRole("SECURITY_OFFICER");
        
        model.addAttribute("user", testUser);
        model.addAttribute("suspiciousReport", new SuspiciousReport());
        
        return "security-report-new-simple";
    }
    
    // Show new suspicious report form
    @GetMapping("/report/new")
    public String showNewReportForm(Model model) {
        System.out.println("=== SUSPICIOUS REPORT NEW FORM ENDPOINT HIT ===");
        try {
            User currentUser = getCurrentUser();
            System.out.println("Current user: " + (currentUser != null ? currentUser.getEmail() : "null"));
            
            model.addAttribute("user", currentUser);
            model.addAttribute("suspiciousReport", new SuspiciousReport());
            
            System.out.println("Returning security-report-new-simple template");
            return "security-report-new-simple";
        } catch (Exception e) {
            System.err.println("Error in showNewReportForm: " + e.getMessage());
            e.printStackTrace();
            // Return a fallback template to avoid error page
            model.addAttribute("error", "Failed to load form: " + e.getMessage());
            return "security-dashboard";
        }
    }

    // Create new suspicious report
    @PostMapping("/report/create")
    public String createSuspiciousReport(
            @ModelAttribute("suspiciousReport") SuspiciousReport suspiciousReport,
            @RequestParam(value = "incidentType", required = false) String incidentType,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "vehicleNumber", required = false) String vehicleNumber,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "witnessInfo", required = false) String witnessInfo,
            @RequestParam(value = "actionTaken", required = false) String actionTaken,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("=== CREATE SUSPICIOUS REPORT ENDPOINT HIT ===");
        try {
            User currentUser = getCurrentUser();
            System.out.println("Current user: " + (currentUser != null ? currentUser.getEmail() : "null"));
            
            if (currentUser == null || currentUser.getUserID() == null) {
                System.err.println("No valid user found - cannot create report without valid SecurityOfficerID");
                redirectAttributes.addFlashAttribute("error", "Unable to identify security officer. Please ensure you are logged in with a valid account.");
                return "redirect:/security/report/new";
            }
            
            // Verify the user ID exists in database to prevent foreign key constraint errors
            try {
                List<User> allUsers = userService.getAllUsers();
                boolean userExists = allUsers.stream()
                    .anyMatch(user -> user.getUserID().equals(currentUser.getUserID()));
                
                if (!userExists) {
                    System.err.println("User ID " + currentUser.getUserID() + " does not exist in database");
                    redirectAttributes.addFlashAttribute("error", "Invalid user account. Please contact administrator.");
                    return "redirect:/security/report/new";
                }
                
                System.out.println("Verified user ID " + currentUser.getUserID() + " exists in database");
            } catch (Exception e) {
                System.err.println("Error verifying user existence: " + e.getMessage());
                redirectAttributes.addFlashAttribute("error", "Database error. Please try again.");
                return "redirect:/security/report/new";
            }

            // Validate required fields
            if (suspiciousReport.getDescription() == null || suspiciousReport.getDescription().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Description is required.");
                return "redirect:/security/report/new";
            }

            if (suspiciousReport.getDate() == null) {
                redirectAttributes.addFlashAttribute("error", "Date and time are required.");
                return "redirect:/security/report/new";
            }

            // Set security officer ID
            suspiciousReport.setSecurityOfficerID(currentUser.getUserID());
            
            // Set default status if not provided
            if (suspiciousReport.getStatus() == null || suspiciousReport.getStatus().trim().isEmpty()) {
                suspiciousReport.setStatus("PENDING");
            }

            // Log the report details
            System.out.println("Creating report with details:");
            System.out.println("- Officer ID: " + suspiciousReport.getSecurityOfficerID());
            System.out.println("- Description: " + suspiciousReport.getDescription());
            System.out.println("- Date: " + suspiciousReport.getDate());
            System.out.println("- Status: " + suspiciousReport.getStatus());
            System.out.println("- Incident Type: " + incidentType);
            System.out.println("- Location: " + location);
            System.out.println("- Vehicle Number: " + vehicleNumber);
            System.out.println("- Priority: " + priority);

            SuspiciousReport savedReport = suspiciousReportService.saveReport(suspiciousReport);
            
            if (savedReport != null) {
                redirectAttributes.addFlashAttribute("success", 
                    "Suspicious report created successfully! Report ID: " + savedReport.getSuspiciousID() + 
                    (incidentType != null ? " | Type: " + incidentType : "") +
                    (location != null ? " | Location: " + location : "") +
                    (vehicleNumber != null ? " | Vehicle: " + vehicleNumber : ""));
                System.out.println("Suspicious report created successfully: ID=" + savedReport.getSuspiciousID());
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to create suspicious report. Please try again.");
            }

        } catch (Exception e) {
            System.err.println("Error creating suspicious report: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while creating the report: " + e.getMessage());
        }

        return "redirect:/security/dashboard";
    }

    // View all reports by current security officer
    @GetMapping("/reports")
    public String viewReports(Model model) {
        try {
            User currentUser = getCurrentUser();
            model.addAttribute("user", currentUser);
            
            if (currentUser != null && currentUser.getUserID() != null) {
                Long officerID = currentUser.getUserID();
                
                // Get all reports by this officer
                List<SuspiciousReport> reports = suspiciousReportService.getReportsBySecurityOfficer(officerID);
                model.addAttribute("reports", reports);
                model.addAttribute("totalReports", reports.size());
                
                // Get recent reports (last 7 days)
                List<SuspiciousReport> recentReports = suspiciousReportService.getRecentReportsBySecurityOfficer(officerID);
                model.addAttribute("recentReports", recentReports);
                model.addAttribute("recentReportsCount", recentReports.size());
                
                // Get latest 3 reports for quick view
                List<SuspiciousReport> latestReports = suspiciousReportService.getLatestReportsBySecurityOfficer(officerID, 3);
                model.addAttribute("latestReports", latestReports);
                
                // Get reports by status for statistics
                List<SuspiciousReport> pendingReports = suspiciousReportService.getReportsBySecurityOfficerAndStatus(officerID, "PENDING");
                List<SuspiciousReport> investigatingReports = suspiciousReportService.getReportsBySecurityOfficerAndStatus(officerID, "INVESTIGATING");
                List<SuspiciousReport> resolvedReports = suspiciousReportService.getReportsBySecurityOfficerAndStatus(officerID, "RESOLVED");
                
                model.addAttribute("pendingCount", pendingReports.size());
                model.addAttribute("investigatingCount", investigatingReports.size());
                model.addAttribute("resolvedCount", resolvedReports.size());
                
                System.out.println("Reports page loaded for officer " + officerID + ":");
                System.out.println("- Total reports: " + reports.size());
                System.out.println("- Recent reports (7 days): " + recentReports.size());
                System.out.println("- Pending: " + pendingReports.size());
                System.out.println("- Investigating: " + investigatingReports.size());
                System.out.println("- Resolved: " + resolvedReports.size());
            } else {
                model.addAttribute("reports", List.of());
                model.addAttribute("totalReports", 0);
                model.addAttribute("recentReports", List.of());
                model.addAttribute("recentReportsCount", 0);
                model.addAttribute("latestReports", List.of());
                model.addAttribute("pendingCount", 0);
                model.addAttribute("investigatingCount", 0);
                model.addAttribute("resolvedCount", 0);
            }
            
            return "security-reports";
            
        } catch (Exception e) {
            System.err.println("Error loading reports: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("reports", List.of());
            model.addAttribute("totalReports", 0);
            model.addAttribute("error", "Failed to load reports: " + e.getMessage());
            return "security-reports";
        }
    }

    // View specific report details
    @GetMapping("/report/{id}")
    public String viewReportDetails(@PathVariable Long id, Model model) {
        try {
            User currentUser = getCurrentUser();
            model.addAttribute("user", currentUser);
            
            Optional<SuspiciousReport> reportOpt = suspiciousReportService.getReportById(id);
            if (reportOpt.isPresent()) {
                SuspiciousReport report = reportOpt.get();
                
                // Check if current user is the owner of this report or is admin
                if (currentUser != null && 
                    (currentUser.getUserID().equals(report.getSecurityOfficerID()) || 
                     "ADMIN".equals(currentUser.getRole()))) {
                    
                    model.addAttribute("report", report);
                    return "security-report-details";
                } else {
                    model.addAttribute("error", "You don't have permission to view this report.");
                    return "redirect:/security/reports";
                }
            } else {
                model.addAttribute("error", "Report not found.");
                return "redirect:/security/reports";
            }
            
        } catch (Exception e) {
            System.err.println("Error loading report details: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load report details: " + e.getMessage());
            return "redirect:/security/reports";
        }
    }

    // Update report status
    @PostMapping("/report/{id}/status")
    public String updateReportStatus(@PathVariable Long id, @RequestParam String status, RedirectAttributes redirectAttributes) {
        try {
            SuspiciousReport updatedReport = suspiciousReportService.updateReportStatus(id, status);
            if (updatedReport != null) {
                redirectAttributes.addFlashAttribute("success", "Report status updated to: " + status);
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to update report status.");
            }
        } catch (Exception e) {
            System.err.println("Error updating report status: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error updating report status: " + e.getMessage());
        }
        
        return "redirect:/security/report/" + id;
    }
    
    // Edit report form
    @GetMapping("/report/{id}/edit")
    public String editReportForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            model.addAttribute("user", currentUser);
            
            Optional<SuspiciousReport> reportOpt = suspiciousReportService.getReportById(id);
            if (reportOpt.isPresent()) {
                SuspiciousReport report = reportOpt.get();
                
                // Check if current user is the owner of this report or is admin
                if (currentUser != null && 
                    (currentUser.getUserID().equals(report.getSecurityOfficerID()) || 
                     "ADMIN".equals(currentUser.getRole()))) {
                    
                    model.addAttribute("suspiciousReport", report);
                    model.addAttribute("isEdit", true);
                    return "security-report-edit";
                } else {
                    redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this report.");
                    return "redirect:/security/reports";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Report not found.");
                return "redirect:/security/reports";
            }
            
        } catch (Exception e) {
            System.err.println("Error loading report for edit: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to load report for editing: " + e.getMessage());
            return "redirect:/security/reports";
        }
    }
    
    // Update report
    @PostMapping("/report/{id}/update")
    public String updateReport(@PathVariable Long id, 
                              @ModelAttribute("suspiciousReport") SuspiciousReport suspiciousReport,
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            
            if (currentUser == null || currentUser.getUserID() == null) {
                redirectAttributes.addFlashAttribute("error", "User authentication failed. Please login again.");
                return "redirect:/security/reports";
            }
            
            Optional<SuspiciousReport> existingReportOpt = suspiciousReportService.getReportById(id);
            if (existingReportOpt.isPresent()) {
                SuspiciousReport existingReport = existingReportOpt.get();
                
                // Check if current user is the owner of this report or is admin
                if (currentUser.getUserID().equals(existingReport.getSecurityOfficerID()) || 
                    "ADMIN".equals(currentUser.getRole())) {
                    
                    // Validate description
                    if (suspiciousReport.getDescription() == null || suspiciousReport.getDescription().trim().isEmpty()) {
                        redirectAttributes.addFlashAttribute("error", "Description is required.");
                        return "redirect:/security/report/" + id + "/edit";
                    }
                    
                    // Update only allowed fields
                    existingReport.setDescription(suspiciousReport.getDescription());
                    existingReport.setStatus(suspiciousReport.getStatus());
                    
                    SuspiciousReport updatedReport = suspiciousReportService.saveReport(existingReport);
                    
                    if (updatedReport != null) {
                        redirectAttributes.addFlashAttribute("success", "Report updated successfully!");
                        return "redirect:/security/report/" + id;
                    } else {
                        redirectAttributes.addFlashAttribute("error", "Failed to update report. Please try again.");
                        return "redirect:/security/report/" + id + "/edit";
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", "You don't have permission to edit this report.");
                    return "redirect:/security/reports";
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Report not found.");
                return "redirect:/security/reports";
            }
            
        } catch (Exception e) {
            System.err.println("Error updating report: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while updating the report: " + e.getMessage());
            return "redirect:/security/report/" + id + "/edit";
        }
    }
    
    // Delete report
    @PostMapping("/report/{id}/delete")
    public String deleteReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            
            if (currentUser == null || currentUser.getUserID() == null) {
                redirectAttributes.addFlashAttribute("error", "User authentication failed. Please login again.");
                return "redirect:/security/reports";
            }
            
            Optional<SuspiciousReport> reportOpt = suspiciousReportService.getReportById(id);
            if (reportOpt.isPresent()) {
                SuspiciousReport report = reportOpt.get();
                
                // Check if current user is the owner of this report or is admin
                if (currentUser.getUserID().equals(report.getSecurityOfficerID()) || 
                    "ADMIN".equals(currentUser.getRole())) {
                    
                    boolean deleted = suspiciousReportService.deleteReport(id);
                    
                    if (deleted) {
                        redirectAttributes.addFlashAttribute("success", "Report deleted successfully!");
                    } else {
                        redirectAttributes.addFlashAttribute("error", "Failed to delete report. Please try again.");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", "You don't have permission to delete this report.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Report not found.");
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting report: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the report: " + e.getMessage());
        }
        
        return "redirect:/security/reports";
    }

    // QR Code Scanner page (placeholder for future implementation)
    @GetMapping("/scanner")
    public String qrScanner(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("user", currentUser);
        return "security-qr-scanner";
    }

    // Vehicle verification page
    @GetMapping("/verify")
    public String vehicleVerification(Model model) {
        try {
            User currentUser = getCurrentUser();
            model.addAttribute("user", currentUser);
            
            // Get active reservations for verification
            List<Reservation> activeReservations = reservationService.getCurrentActiveReservations();
            model.addAttribute("activeReservations", activeReservations);
            
            return "security-verify";
            
        } catch (Exception e) {
            System.err.println("Error loading vehicle verification: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("activeReservations", List.of());
            return "security-verify";
        }
    }
    
    // API endpoint for vehicle verification
    @PostMapping("/verify/vehicle")
    @ResponseBody
    public Map<String, Object> verifyVehicle(@RequestParam String vehicleNumber) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Clean and format vehicle number
            String cleanVehicleNumber = vehicleNumber.trim().toUpperCase();
            
            // Check if vehicle is authorized
            boolean isAuthorized = reservationService.isVehicleAuthorized(cleanVehicleNumber);
            
            if (isAuthorized) {
                Optional<Reservation> reservationOpt = reservationService.getActiveReservationForVehicle(cleanVehicleNumber);
                
                if (reservationOpt.isPresent()) {
                    Reservation reservation = reservationOpt.get();
                    response.put("status", "success");
                    response.put("authorized", true);
                    response.put("message", "Vehicle authorized - Valid reservation found");
                    response.put("reservationId", reservation.getId());
                    response.put("vehicleNumber", reservation.getVehicleNumber());
                    response.put("slotId", reservation.getSlotId());
                    response.put("startTime", reservation.getStartTime().toString());
                    response.put("endTime", reservation.getEndTime().toString());
                    response.put("reservationStatus", reservation.getStatus());
                    
                    // Add user information if available
                    if (reservation.getUser() != null) {
                        response.put("customerName", reservation.getUser().getFirstName() + " " + reservation.getUser().getLastName());
                        response.put("customerEmail", reservation.getUser().getEmail());
                    }
                    
                    // Add parking slot information if available
                    if (reservation.getParkingSlot() != null) {
                        response.put("slotId", reservation.getParkingSlot().getId());
                        response.put("slotLocation", reservation.getParkingSlot().getLocation());
                    }
                }
            } else {
                response.put("status", "error");
                response.put("authorized", false);
                response.put("message", "Vehicle not authorized - No valid reservation found");
                response.put("vehicleNumber", cleanVehicleNumber);
            }
            
        } catch (Exception e) {
            System.err.println("Error verifying vehicle: " + e.getMessage());
            e.printStackTrace();
            response.put("status", "error");
            response.put("authorized", false);
            response.put("message", "Error occurred during verification: " + e.getMessage());
        }
        
        return response;
    }

    // Helper method to get current authenticated user (Security Officer)
    private User getCurrentUser() {
        System.out.println("=== getCurrentUser() called ===");
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Authentication object: " + authentication);
            System.out.println("Is authenticated: " + (authentication != null ? authentication.isAuthenticated() : "null"));
            
            // First, let's get all users to see what we have
            List<User> allUsers = userService.getAllUsers();
            System.out.println("Total users in database: " + allUsers.size());
            
            if (!allUsers.isEmpty()) {
                System.out.println("Available users:");
                for (User user : allUsers) {
                    System.out.println("  - ID: " + user.getUserID() + ", Email: " + user.getEmail() + ", Role: " + user.getRole());
                }
            }
            
            if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
                String username = authentication.getName();
                System.out.println("Authenticated user: " + username);
                
                // Find user by email
                Optional<User> userOpt = allUsers.stream()
                    .filter(user -> user.getEmail().equals(username))
                    .findFirst();
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    System.out.println("Found authenticated user in database: " + user.getEmail() + " (ID: " + user.getUserID() + ")");
                    return user;
                }
                
                System.out.println("Authenticated user not found in database, will use Security Officer fallback");
            } else {
                System.out.println("No valid authentication found, using Security Officer fallback");
            }
            
            // Fallback: Use existing Security Officer or create one
            Optional<User> securityOfficer = allUsers.stream()
                .filter(user -> "SECURITY_OFFICER".equals(user.getRole()))
                .findFirst();
            
            if (securityOfficer.isPresent()) {
                User officer = securityOfficer.get();
                System.out.println("Using existing Security Officer: " + officer.getEmail() + " (ID: " + officer.getUserID() + ")");
                return officer;
            } else {
                // Create a new Security Officer if none exist
                System.out.println("No Security Officer exists, creating new one");
                User newSecurityOfficer = new User();
                newSecurityOfficer.setFirstName("Security");
                newSecurityOfficer.setLastName("Officer");
                newSecurityOfficer.setEmail("security.officer@parking.com");
                newSecurityOfficer.setRole("SECURITY_OFFICER");
                newSecurityOfficer.setPasswordHash("securitypass123");
                newSecurityOfficer.setPhoneNumber("1234567890");
                
                try {
                    User savedUser = userService.saveUser(newSecurityOfficer);
                    System.out.println("Created new Security Officer with ID: " + savedUser.getUserID());
                    return savedUser;
                } catch (Exception saveError) {
                    System.err.println("Failed to save new Security Officer: " + saveError.getMessage());
                    saveError.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error in getCurrentUser: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("getCurrentUser returning null - this should not happen");
        return null;
    }
    
    // Helper method to get a valid User ID that exists in the User table
    private Long getValidUserID() {
        try {
            List<User> allUsers = userService.getAllUsers();
            if (!allUsers.isEmpty()) {
                Long validID = allUsers.get(0).getUserID();
                System.out.println("getValidUserID() returning: " + validID);
                return validID;
            } else {
                // Create a user if none exist
                User newUser = new User();
                newUser.setFirstName("Security");
                newUser.setLastName("Officer");
                newUser.setEmail("security@parking.com");
                newUser.setRole("SECURITY_OFFICER");
                newUser.setPasswordHash("securitypass123");
                newUser.setPhoneNumber("1234567890");
                
                User savedUser = userService.saveUser(newUser);
                System.out.println("Created new user with valid ID: " + savedUser.getUserID());
                return savedUser.getUserID();
            }
        } catch (Exception e) {
            System.err.println("Error getting valid user ID: " + e.getMessage());
            return null;
        }
    }
    
    // Helper method to validate that a User ID exists in the database
    private boolean validateUserExists(Long userID) {
        try {
            List<User> allUsers = userService.getAllUsers();
            return allUsers.stream().anyMatch(user -> user.getUserID().equals(userID));
        } catch (Exception e) {
            System.err.println("Error validating user existence: " + e.getMessage());
            return false;
        }
    }
}
