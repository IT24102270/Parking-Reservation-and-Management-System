package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.SuspiciousReport;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.SuspiciousReportService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/public")
public class PublicTestController {

    @Autowired
    private SuspiciousReportService suspiciousReportService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/report-form")
    public String showReportForm(Model model) {
        System.out.println("=== PUBLIC REPORT FORM ACCESSED ===");
        
        // Create a test user
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

    @PostMapping("/report-create")
    public String createReport(
            @ModelAttribute("suspiciousReport") SuspiciousReport suspiciousReport,
            @RequestParam(value = "incidentType", required = false) String incidentType,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "vehicleNumber", required = false) String vehicleNumber,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "witnessInfo", required = false) String witnessInfo,
            @RequestParam(value = "actionTaken", required = false) String actionTaken,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("=== PUBLIC REPORT CREATE ACCESSED ===");
        
        try {
            // Get a Security Officer User ID from the User table (foreign key constraint)
            Long securityOfficerUserID = getSecurityOfficerUserID();
            
            if (securityOfficerUserID == null) {
                System.err.println("No Security Officer User ID available from User table");
                redirectAttributes.addFlashAttribute("error", "No Security Officer account found. Please contact administrator.");
                return "redirect:/public/report-form";
            }
            
            System.out.println("Using Security Officer User ID from User table: " + securityOfficerUserID);
            
            // Set the SecurityOfficerID to reference an existing Security Officer User.UserID
            suspiciousReport.setSecurityOfficerID(securityOfficerUserID);
            
            // Set default values if missing
            if (suspiciousReport.getDate() == null) {
                System.out.println("Date is null, setting to current time");
                suspiciousReport.setDate(LocalDateTime.now());
            } else {
                System.out.println("Date from form: " + suspiciousReport.getDate());
            }
            
            if (suspiciousReport.getStatus() == null || suspiciousReport.getStatus().trim().isEmpty()) {
                suspiciousReport.setStatus("PENDING");
            }
            
            // Validate description
            if (suspiciousReport.getDescription() == null || suspiciousReport.getDescription().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Description is required");
                return "redirect:/public/report-form";
            }
            
            System.out.println("Creating report:");
            System.out.println("- Description: " + suspiciousReport.getDescription());
            System.out.println("- Date: " + suspiciousReport.getDate());
            System.out.println("- Status: " + suspiciousReport.getStatus());
            System.out.println("- Officer ID: " + suspiciousReport.getSecurityOfficerID());
            
            SuspiciousReport savedReport = suspiciousReportService.saveReport(suspiciousReport);
            
            if (savedReport != null) {
                redirectAttributes.addFlashAttribute("success", 
                    "Report created successfully! ID: " + savedReport.getSuspiciousID());
                System.out.println("Report saved with ID: " + savedReport.getSuspiciousID());
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to save report");
            }
            
        } catch (Exception e) {
            System.err.println("Error creating report: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        
        return "redirect:/public/report-form";
    }
    
    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "<h1>Public Test Controller Working!</h1><p><a href='/public/report-form'>Test Report Form</a></p><p><a href='/public/test-links'>All Test Links</a></p>";
    }
    
    @GetMapping("/check-users")
    @ResponseBody
    public String checkUsers() {
        try {
            List<User> allUsers = userService.getAllUsers();
            StringBuilder html = new StringBuilder();
            html.append("<h1>Database Users Check</h1>");
            html.append("<p><strong>Total Users:</strong> ").append(allUsers.size()).append("</p>");
            
            if (allUsers.isEmpty()) {
                html.append("<p style='color: red;'>No users found in database!</p>");
            } else {
                html.append("<table border='1' style='border-collapse: collapse;'>");
                html.append("<tr><th>UserID</th><th>Name</th><th>Email</th><th>Role</th></tr>");
                
                for (User user : allUsers) {
                    html.append("<tr>");
                    html.append("<td>").append(user.getUserID()).append("</td>");
                    html.append("<td>").append(user.getFirstName()).append(" ").append(user.getLastName()).append("</td>");
                    html.append("<td>").append(user.getEmail()).append("</td>");
                    html.append("<td>").append(user.getRole()).append("</td>");
                    html.append("</tr>");
                }
                html.append("</table>");
            }
            
            html.append("<p><a href='/public/test-links'>← Back to Test Links</a></p>");
            return html.toString();
            
        } catch (Exception e) {
            return "<h1>Error checking users</h1><p>" + e.getMessage() + "</p><p><a href='/public/test-links'>← Back to Test Links</a></p>";
        }
    }
    
    @GetMapping("/check-reports")
    @ResponseBody
    public String checkReports() {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<h1>Comprehensive Reports & Security Officer Debug</h1>");

            // Check users first
            List<User> allUsers = userService.getAllUsers();
            html.append("<h2>Users in Database</h2>");
            html.append("<p><strong>Total Users:</strong> ").append(allUsers.size()).append("</p>");

            if (!allUsers.isEmpty()) {
                html.append("<table border='1' style='border-collapse: collapse; width: 100%; margin-bottom: 20px;'>");
                html.append("<tr><th>UserID</th><th>Name</th><th>Email</th><th>Role</th></tr>");

                for (User user : allUsers) {
                    html.append("<tr>");
                    html.append("<td>").append(user.getUserID()).append("</td>");
                    html.append("<td>").append(user.getFirstName()).append(" ").append(user.getLastName()).append("</td>");
                    html.append("<td>").append(user.getEmail()).append("</td>");
                    html.append("<td>").append(user.getRole()).append("</td>");
                    html.append("</tr>");
                }
                html.append("</table>");
            }

            // Get all reports
            List<SuspiciousReport> allReports = suspiciousReportService.getAllReports();
            html.append("<h2>Reports in Database</h2>");
            html.append("<p><strong>Total Reports:</strong> ").append(allReports.size()).append("</p>");

            if (allReports.isEmpty()) {
                html.append("<p style='color: red;'>No reports found in database!</p>");
                html.append("<p><a href='/public/report-db-only' style='background: #007bff; color: white; padding: 10px; text-decoration: none; border-radius: 5px;'>Create Test Report</a></p>");
            } else {
                html.append("<table border='1' style='border-collapse: collapse; width: 100%; margin-bottom: 20px;'>");
                html.append("<tr><th>ID</th><th>Officer ID</th><th>Description</th><th>Date</th><th>Status</th></tr>");

                for (SuspiciousReport report : allReports) {
                    html.append("<tr>");
                    html.append("<td>").append(report.getSuspiciousID()).append("</td>");
                    html.append("<td>").append(report.getSecurityOfficerID()).append("</td>");
                    html.append("<td>").append(report.getDescription().length() > 50 ?
                            report.getDescription().substring(0, 50) + "..." :
                            report.getDescription()).append("</td>");
                    html.append("<td>").append(report.getDate()).append("</td>");
                    html.append("<td>").append(report.getStatus()).append("</td>");
                    html.append("</tr>");
                }
                html.append("</table>");

                // Test recent reports functionality for each user
                html.append("<h2>Security Officer Reports Analysis</h2>");
                for (User user : allUsers) {
                    Long officerID = user.getUserID();
                    List<SuspiciousReport> officerReports = suspiciousReportService.getReportsBySecurityOfficer(officerID);
                    List<SuspiciousReport> recentReports = suspiciousReportService.getRecentReportsBySecurityOfficer(officerID);
                    List<SuspiciousReport> latestReports = suspiciousReportService.getLatestReportsBySecurityOfficer(officerID, 5);

                    html.append("<div style='border: 1px solid #ccc; padding: 10px; margin: 10px 0;'>");
                    html.append("<h3>Officer: ").append(user.getFirstName()).append(" ").append(user.getLastName()).append(" (ID: ").append(officerID).append(")</h3>");
                    html.append("<p><strong>Total reports:</strong> ").append(officerReports.size()).append("</p>");
                    html.append("<p><strong>Recent reports (7 days):</strong> ").append(recentReports.size()).append("</p>");
                    html.append("<p><strong>Latest 5 reports:</strong> ").append(latestReports.size()).append("</p>");

                    if (!latestReports.isEmpty()) {
                        html.append("<p><strong>Latest Reports:</strong></p>");
                        html.append("<ul>");
                        for (SuspiciousReport report : latestReports) {
                            html.append("<li>Report #").append(report.getSuspiciousID())
                                    .append(" - ").append(report.getStatus())
                                    .append(" - ").append(report.getDate())
                                    .append("</li>");
                        }
                        html.append("</ul>");
                    }
                    html.append("</div>");
                }
            }

            html.append("<h2>Quick Actions</h2>");
            html.append("<p><a href='/security/dashboard' style='background: #28a745; color: white; padding: 10px; text-decoration: none; border-radius: 5px; margin-right: 10px;'>Go to Security Dashboard</a>");
            html.append("<a href='/public/report-db-only' style='background: #007bff; color: white; padding: 10px; text-decoration: none; border-radius: 5px; margin-right: 10px;'>Create New Report</a>");
            html.append("<a href='/public/create-test-reports' style='background: #ffc107; color: black; padding: 10px; text-decoration: none; border-radius: 5px; margin-right: 10px;'>Create Test Reports</a>");
            html.append("<a href='/public/validate-foreign-keys' style='background: #dc3545; color: white; padding: 10px; text-decoration: none; border-radius: 5px;'>Validate Foreign Keys</a></p>");
            html.append("<p><a href='/public/test-links'>← Back to Test Links</a></p>");
            return html.toString();

        } catch(Exception e){
            return "<h1>Error checking reports</h1><p>" + e.getMessage() + "</p><p><a href='/public/test-links'>← Back to Test Links</a></p>";
        }
    }
    @GetMapping("/create-test-reports")
    @ResponseBody
    public String createTestReports() {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<h1>Creating Test Reports</h1>");
            
            // Get a Security Officer User ID from the User table (foreign key constraint)
            Long securityOfficerUserID = getSecurityOfficerUserID();
            
            if (securityOfficerUserID == null) {
                html.append("<p style='color: red;'>Error: Could not get Security Officer User ID from User table!</p>");
                return html.toString();
            }
            
            html.append("<p>Using Security Officer User ID from User table: ").append(securityOfficerUserID).append("</p>");
            
            // Create 5 test reports
            String[] descriptions = {
                "Suspicious individual loitering near parking entrance for extended period",
                "Unauthorized vehicle parked in reserved spot without permit",
                "Possible break-in attempt observed at vehicle in section B",
                "Unusual activity reported by customer - investigating further",
                "Security patrol found damaged barrier gate requiring maintenance"
            };
            
            String[] statuses = {"PENDING", "INVESTIGATING", "RESOLVED", "PENDING", "INVESTIGATING"};
            
            html.append("<h2>Creating Test Reports:</h2>");
            html.append("<ul>");
            
            for (int i = 0; i < descriptions.length; i++) {
                SuspiciousReport report = new SuspiciousReport();
                report.setSecurityOfficerID(securityOfficerUserID); // Use Security Officer User ID
                report.setDescription(descriptions[i]);
                report.setDate(LocalDateTime.now().minusHours(i * 2)); // Spread reports over time
                report.setStatus(statuses[i]);
                
                SuspiciousReport savedReport = suspiciousReportService.saveReport(report);
                html.append("<li>Created Report #").append(savedReport.getSuspiciousID())
                    .append(" - ").append(savedReport.getStatus())
                    .append(" - ").append(savedReport.getDate())
                    .append(" (Security Officer ID: ").append(securityOfficerUserID).append(")")
                    .append("</li>");
            }
            
            html.append("</ul>");
            html.append("<p style='color: green;'><strong>Successfully created 5 test reports!</strong></p>");
            
            html.append("<h2>Quick Actions</h2>");
            html.append("<p><a href='/security/dashboard' style='background: #28a745; color: white; padding: 10px; text-decoration: none; border-radius: 5px; margin-right: 10px;'>Go to Security Dashboard</a>");
            html.append("<a href='/security/debug-dashboard' style='background: #17a2b8; color: white; padding: 10px; text-decoration: none; border-radius: 5px; margin-right: 10px;'>Debug Dashboard</a>");
            html.append("<a href='/public/check-reports' style='background: #007bff; color: white; padding: 10px; text-decoration: none; border-radius: 5px;'>Check All Reports</a></p>");
            html.append("<p><a href='/public/test-links'>← Back to Test Links</a></p>");
            
            return html.toString();
            
        } catch (Exception e) {
            return "<h1>Error creating test reports</h1><p>" + e.getMessage() + "</p><pre>" + e.toString() + "</pre><p><a href='/public/test-links'>← Back to Test Links</a></p>";
        }
    }
    
    @GetMapping("/validate-foreign-keys")
    @ResponseBody
    public String validateForeignKeys() {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<h1>Foreign Key Validation: SecurityOfficerID → User.UserID</h1>");
            
            // Get all users and reports
            List<User> allUsers = userService.getAllUsers();
            List<SuspiciousReport> allReports = suspiciousReportService.getAllReports();
            
            html.append("<h2>User Table (Valid UserIDs)</h2>");
            html.append("<p><strong>Total Users:</strong> ").append(allUsers.size()).append("</p>");
            
            // Count Security Officers
            long securityOfficerCount = allUsers.stream()
                .filter(user -> "SECURITY_OFFICER".equals(user.getRole()))
                .count();
            html.append("<p><strong>Security Officers:</strong> ").append(securityOfficerCount).append("</p>");
            
            if (!allUsers.isEmpty()) {
                html.append("<p><strong>All User IDs:</strong> ");
                for (int i = 0; i < allUsers.size(); i++) {
                    if (i > 0) html.append(", ");
                    User user = allUsers.get(i);
                    html.append(user.getUserID());
                    if ("SECURITY_OFFICER".equals(user.getRole())) {
                        html.append(" (Security Officer)");
                    }
                }
                html.append("</p>");
            }
            
            html.append("<h2>SuspiciousReport Table Foreign Key Validation</h2>");
            html.append("<p><strong>Total Reports:</strong> ").append(allReports.size()).append("</p>");
            
            if (allReports.isEmpty()) {
                html.append("<p style='color: orange;'>No reports to validate.</p>");
            } else {
                int validReports = 0;
                int invalidReports = 0;
                int securityOfficerReports = 0;
                int nonSecurityOfficerReports = 0;
                
                html.append("<table border='1' style='border-collapse: collapse; width: 100%;'>");
                html.append("<tr><th>Report ID</th><th>SecurityOfficerID</th><th>Valid User?</th><th>Security Officer?</th><th>Status</th></tr>");
                
                for (SuspiciousReport report : allReports) {
                    Long securityOfficerID = report.getSecurityOfficerID();
                    
                    // Check if the ID exists in User table
                    Optional<User> userOpt = allUsers.stream()
                        .filter(user -> user.getUserID().equals(securityOfficerID))
                        .findFirst();
                    
                    boolean isValidUser = userOpt.isPresent();
                    boolean isSecurityOfficer = userOpt.isPresent() && "SECURITY_OFFICER".equals(userOpt.get().getRole());
                    
                    html.append("<tr>");
                    html.append("<td>").append(report.getSuspiciousID()).append("</td>");
                    html.append("<td>").append(securityOfficerID).append("</td>");
                    
                    // Valid User column
                    if (isValidUser) {
                        html.append("<td style='color: green; font-weight: bold;'>✓ VALID</td>");
                        validReports++;
                    } else {
                        html.append("<td style='color: red; font-weight: bold;'>✗ INVALID</td>");
                        invalidReports++;
                    }
                    
                    // Security Officer column
                    if (isSecurityOfficer) {
                        html.append("<td style='color: blue; font-weight: bold;'>✓ SECURITY OFFICER</td>");
                        securityOfficerReports++;
                    } else if (isValidUser) {
                        html.append("<td style='color: orange; font-weight: bold;'>⚠ OTHER ROLE</td>");
                        nonSecurityOfficerReports++;
                    } else {
                        html.append("<td style='color: red; font-weight: bold;'>✗ N/A</td>");
                    }
                    
                    html.append("<td>").append(report.getStatus()).append("</td>");
                    html.append("</tr>");
                }
                
                html.append("</table>");
                
                html.append("<h3>Validation Summary</h3>");
                html.append("<p><strong>Valid User References:</strong> <span style='color: green;'>").append(validReports).append("</span></p>");
                html.append("<p><strong>Invalid User References:</strong> <span style='color: red;'>").append(invalidReports).append("</span></p>");
                html.append("<p><strong>Security Officer Reports:</strong> <span style='color: blue;'>").append(securityOfficerReports).append("</span></p>");
                html.append("<p><strong>Non-Security Officer Reports:</strong> <span style='color: orange;'>").append(nonSecurityOfficerReports).append("</span></p>");
                
                if (invalidReports > 0) {
                    html.append("<div style='background: #f8d7da; color: #721c24; padding: 15px; border-radius: 5px; margin: 10px 0;'>");
                    html.append("<h4>⚠️ Foreign Key Constraint Violations Found!</h4>");
                    html.append("<p>There are ").append(invalidReports).append(" reports with SecurityOfficerID values that don't exist in the User table.</p>");
                    html.append("<p>This will cause foreign key constraint errors when trying to save new reports.</p>");
                    html.append("</div>");
                }
                
                if (nonSecurityOfficerReports > 0) {
                    html.append("<div style='background: #fff3cd; color: #856404; padding: 15px; border-radius: 5px; margin: 10px 0;'>");
                    html.append("<h4>⚠️ Non-Security Officer Reports Found!</h4>");
                    html.append("<p>There are ").append(nonSecurityOfficerReports).append(" reports created by users who are not Security Officers.</p>");
                    html.append("<p>Reports should only be created by users with SECURITY_OFFICER role.</p>");
                    html.append("</div>");
                }
                
                if (invalidReports == 0 && securityOfficerReports == allReports.size()) {
                    html.append("<div style='background: #d4edda; color: #155724; padding: 15px; border-radius: 5px; margin: 10px 0;'>");
                    html.append("<h4>✅ Perfect! All Reports Valid!</h4>");
                    html.append("<p>All SecurityOfficerID values properly reference existing Security Officer User.UserID values.</p>");
                    html.append("</div>");
                } else if (invalidReports == 0) {
                    html.append("<div style='background: #d4edda; color: #155724; padding: 15px; border-radius: 5px; margin: 10px 0;'>");
                    html.append("<h4>✅ All Foreign Keys Valid!</h4>");
                    html.append("<p>All SecurityOfficerID values reference existing User.UserID values.</p>");
                    html.append("</div>");
                }
            }
            
            html.append("<h2>Quick Actions</h2>");
            html.append("<p><a href='/public/create-test-reports' style='background: #ffc107; color: black; padding: 10px; text-decoration: none; border-radius: 5px; margin-right: 10px;'>Create Valid Test Reports</a>");
            html.append("<a href='/security/dashboard' style='background: #28a745; color: white; padding: 10px; text-decoration: none; border-radius: 5px;'>Go to Security Dashboard</a></p>");
            html.append("<p><a href='/public/test-links'>← Back to Test Links</a></p>");
            
            return html.toString();
            
        } catch (Exception e) {
            return "<h1>Error validating foreign keys</h1><p>" + e.getMessage() + "</p><pre>" + e.toString() + "</pre><p><a href='/public/test-links'>← Back to Test Links</a></p>";
        }
    }
    
    @GetMapping("/test-links")
    public String testLinks() {
        return "test-links";
    }
    
    @GetMapping("/report-basic")
    public String showBasicReportForm(Model model) {
        System.out.println("=== BASIC REPORT FORM ACCESSED ===");
        
        // Create a test user
        User testUser = new User();
        testUser.setUserID(999L);
        testUser.setFirstName("Test");
        testUser.setLastName("Security");
        testUser.setEmail("test@security.com");
        testUser.setRole("SECURITY_OFFICER");
        
        model.addAttribute("user", testUser);
        
        return "security-report-basic";
    }
    
    @GetMapping("/report-db-only")
    public String showDatabaseOnlyForm(Model model) {
        System.out.println("=== DATABASE ONLY REPORT FORM ACCESSED ===");
        
        try {
            // Find a valid user ID from the database
            List<User> allUsers = userService.getAllUsers();
            Long validUserID = null;
            
            if (!allUsers.isEmpty()) {
                // Use the first user's ID
                validUserID = allUsers.get(0).getUserID();
                System.out.println("Found valid user ID: " + validUserID);
            } else {
                // Create a test user if no users exist
                System.out.println("No users found, creating test user");
                User testUser = new User();
                testUser.setFirstName("Test");
                testUser.setLastName("Security");
                testUser.setEmail("test.security@parking.com");
                testUser.setRole("SECURITY_OFFICER");
                testUser.setPasswordHash("password123"); // Use correct method name
                testUser.setPhoneNumber("1234567890");
                
                User savedUser = userService.saveUser(testUser);
                validUserID = savedUser.getUserID();
                System.out.println("Created test user with ID: " + validUserID);
            }
            
            model.addAttribute("validUserID", validUserID);
            
        } catch (Exception e) {
            System.err.println("Error getting valid user ID: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("validUserID", 1L); // Fallback
            model.addAttribute("error", "Could not find valid user ID: " + e.getMessage());
        }
        
        return "suspicious-report-form";
    }
    
    // Helper method to get a valid Security Officer User ID from the User table
    private Long getSecurityOfficerUserID() {
        try {
            List<User> allUsers = userService.getAllUsers();
            
            // First, try to find an existing Security Officer
            Optional<User> securityOfficer = allUsers.stream()
                .filter(user -> "SECURITY_OFFICER".equals(user.getRole()))
                .findFirst();
            
            if (securityOfficer.isPresent()) {
                Long securityOfficerID = securityOfficer.get().getUserID();
                System.out.println("Found existing Security Officer with User ID: " + securityOfficerID + 
                                 " (" + securityOfficer.get().getFirstName() + " " + securityOfficer.get().getLastName() + ")");
                return securityOfficerID;
            }
            
            // If no Security Officer exists, create one
            System.out.println("No Security Officer found in User table, creating one");
            User newSecurityOfficer = new User();
            newSecurityOfficer.setFirstName("Security");
            newSecurityOfficer.setLastName("Officer");
            newSecurityOfficer.setEmail("security.officer@parking.com");
            newSecurityOfficer.setRole("SECURITY_OFFICER");
            newSecurityOfficer.setPasswordHash("securitypass123");
            newSecurityOfficer.setPhoneNumber("1234567890");
            
            User savedUser = userService.saveUser(newSecurityOfficer);
            System.out.println("Created new Security Officer in User table with ID: " + savedUser.getUserID());
            return savedUser.getUserID();
            
        } catch (Exception e) {
            System.err.println("Error getting Security Officer User ID from User table: " + e.getMessage());
            return null;
        }
    }
    
    @PostMapping("/save-report")
    public String saveReport(
            @RequestParam("securityOfficerID") Long securityOfficerID,
            @RequestParam("description") String description,
            @RequestParam("date") String dateStr,
            @RequestParam("status") String status,
            RedirectAttributes redirectAttributes) {
        
        System.out.println("=== SAVE REPORT ACCESSED ===");
        
        try {
            // Instead of using the provided SecurityOfficerID, get a Security Officer User ID from User table
            Long securityOfficerUserID = getSecurityOfficerUserID();
            
            if (securityOfficerUserID == null) {
                System.err.println("No Security Officer User ID available from User table");
                redirectAttributes.addFlashAttribute("error", "No Security Officer account found. Please contact administrator.");
                return "redirect:/public/report-db-only";
            }
            
            System.out.println("Using Security Officer User ID from User table: " + securityOfficerUserID + " instead of provided ID: " + securityOfficerID);
            
            // Parse the date
            LocalDateTime date = LocalDateTime.parse(dateStr);
            
            // Create the suspicious report with Security Officer User ID from User table
            SuspiciousReport report = new SuspiciousReport();
            report.setSecurityOfficerID(securityOfficerUserID); // Use Security Officer User ID as foreign key
            report.setDescription(description);
            report.setDate(date);
            report.setStatus(status);
            
            System.out.println("Saving report:");
            System.out.println("- SecurityOfficerID (Security Officer User ID): " + securityOfficerUserID);
            System.out.println("- Description: " + description);
            System.out.println("- Date: " + date);
            System.out.println("- Status: " + status);
            
            // Save to database
            SuspiciousReport savedReport = suspiciousReportService.saveReport(report);
            
            if (savedReport != null) {
                redirectAttributes.addFlashAttribute("success", 
                    "Suspicious report created successfully! Report ID: " + savedReport.getSuspiciousID());
                System.out.println("Report saved successfully with ID: " + savedReport.getSuspiciousID());
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to save report");
            }
            
        } catch (Exception e) {
            System.err.println("Error saving report: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error saving report: " + e.getMessage());
        }
        
        return "redirect:/public/report-db-only";
    }
}
