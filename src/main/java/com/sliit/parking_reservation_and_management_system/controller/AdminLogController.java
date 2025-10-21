package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.logging.AdminActionLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Controller for viewing admin action logs
 * Only accessible by ADMIN users
 */
@Controller
@RequestMapping("/admin/logs")
public class AdminLogController {
    
    @Autowired
    private AdminActionLogger adminActionLogger;
    
    /**
     * View admin action logs from file
     * @param model Model for view
     * @return Log view template
     */
    @GetMapping("/database")
    public String viewFileLogs(Model model) {
        try {
            // Get current admin
            String adminEmail = getCurrentAdminEmail();
            
            // Read log file
            List<String> logLines = readLogFile();
            
            model.addAttribute("logLines", logLines);
            model.addAttribute("logFilePath", adminActionLogger.getLogFilePath());
            model.addAttribute("currentAdmin", adminEmail);
            model.addAttribute("logType", "file");
            
            return "admin-logs-file";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error reading log file: " + e.getMessage());
            return "admin-logs-file";
        }
    }
    
    /**
     * View admin action logs from text file (alternative endpoint)
     * @param model Model for view
     * @return Log view template
     */
    @GetMapping("/file")
    public String viewFileLogsAlternative(Model model) {
        try {
            // Get current admin
            String adminEmail = getCurrentAdminEmail();
            
            // Read log file
            List<String> logLines = readLogFile();
            
            model.addAttribute("logLines", logLines);
            model.addAttribute("logFilePath", adminActionLogger.getLogFilePath());
            model.addAttribute("currentAdmin", adminEmail);
            model.addAttribute("logType", "file");
            
            return "admin-logs-file";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error reading log file: " + e.getMessage());
            return "admin-logs-file";
        }
    }
    
    /**
     * Clear all logs (admin only)
     * @param redirectAttributes Redirect attributes
     * @return Redirect to logs view
     */
    @PostMapping("/clear")
    public String clearAllLogs(RedirectAttributes redirectAttributes) {
        try {
            // Clear the log file by overwriting with new header
            adminActionLogger.clearLogFile();
            
            redirectAttributes.addFlashAttribute("success", "Log file cleared successfully!");
            return "redirect:/admin/logs/database";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error clearing log file: " + e.getMessage());
            return "redirect:/admin/logs/database";
        }
    }
    
    /**
     * Read log file content
     * @return List of log lines
     */
    private List<String> readLogFile() throws IOException {
        List<String> lines = new java.util.ArrayList<>();
        String logFilePath = adminActionLogger.getLogFilePath();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        
        // Return last 100 lines (most recent)
        int startIndex = Math.max(0, lines.size() - 100);
        return lines.subList(startIndex, lines.size());
    }
    
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
}
