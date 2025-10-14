package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customer")
public class ProfileController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String viewProfile(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("editMode", false);
                System.out.println("Profile view for user: " + currentUser.getEmail());
            } else {
                model.addAttribute("error", "User not found");
                return "redirect:/customer/dashboard";
            }
        } catch (Exception e) {
            System.err.println("Error loading profile: " + e.getMessage());
            model.addAttribute("error", "Error loading profile");
            return "redirect:/customer/dashboard";
        }
        
        return "customer-profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("user", currentUser);
                model.addAttribute("editMode", true);
                System.out.println("Profile edit for user: " + currentUser.getEmail());
            } else {
                model.addAttribute("error", "User not found");
                return "redirect:/customer/dashboard";
            }
        } catch (Exception e) {
            System.err.println("Error loading profile for edit: " + e.getMessage());
            model.addAttribute("error", "Error loading profile");
            return "redirect:/customer/dashboard";
        }
        
        return "customer-profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser, 
                              RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/customer/profile";
            }

            // Update only allowed fields (don't update password, email, role, etc.)
            currentUser.setFirstName(updatedUser.getFirstName());
            currentUser.setLastName(updatedUser.getLastName());
            currentUser.setPhoneNumber(updatedUser.getPhoneNumber());
            
            // Save updated user
            userService.saveUser(currentUser);
            
            System.out.println("Profile updated for user: " + currentUser.getEmail());
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            
        } catch (Exception e) {
            System.err.println("Error updating profile: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
        }
        
        return "redirect:/customer/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "User not found");
                return "redirect:/customer/profile";
            }

            // Validate new password
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "New passwords do not match");
                return "redirect:/customer/profile";
            }

            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long");
                return "redirect:/customer/profile";
            }

            // Update password (UserService will handle encoding)
            currentUser.setPasswordHash(newPassword);
            userService.saveUser(currentUser);
            
            System.out.println("Password changed for user: " + currentUser.getEmail());
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error changing password: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error changing password: " + e.getMessage());
        }
        
        return "redirect:/customer/profile";
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                
                // Find user by email
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
}
