package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // Admin Dashboard: list all users
    @GetMapping("/dashboard")
    public String viewDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-dashboard";
    }

    // ---------------------------
    // Staff Registration
    // ---------------------------

    // Show staff registration form
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "staff-register";
    }

    // Handle staff registration
    @PostMapping("/register")
    public String registerStaff(@ModelAttribute("user") User user) {
        // Password hashing, role normalization, and default status
        // are handled inside userService.saveUser(...)
        userService.saveUser(user);
        return "redirect:/admin/dashboard";
    }

    // ---------------------------
    // User Management
    // ---------------------------

    // Edit user form
    @GetMapping("/edit/{id}")
    public String editUser(@PathVariable("id") int id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
        model.addAttribute("user", user);
        return "edit-user";
    }

    // Update user
    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable("id") int id, @ModelAttribute("user") User updatedUser) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));

        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setPhoneNumber(updatedUser.getPhoneNumber());
        user.setRole(updatedUser.getRole());
        user.setStatus(updatedUser.getStatus());

        // Do NOT overwrite password here (no password field in the form).
        // If you later add a password reset field, call user.setPasswordHash(...)
        // and userService.saveUser(...) will hash it.

        userService.saveUser(user);
        return "redirect:/admin/dashboard";
    }

    // Delete user
    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") int id) {
        userService.deleteUser(id);
        return "redirect:/admin/dashboard";
    }

    // Deactivate user
    @GetMapping("/deactivate/{id}")
    public String deactivateUser(@PathVariable("id") int id) {
        userService.deactivateUser(id);
        return "redirect:/admin/dashboard";
    }

    // Activate user
    @GetMapping("/activate/{id}")
    public String activateUser(@PathVariable("id") int id) {
        userService.activateUser(id);
        return "redirect:/admin/dashboard";
    }
}
