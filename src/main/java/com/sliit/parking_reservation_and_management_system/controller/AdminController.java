package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

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
    // ---------------------------
    // Dashboard with pagination + filters
    // ---------------------------
    @GetMapping("/dashboard")
    public String viewDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String email,
            Model model
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
    public String registerUser(@ModelAttribute("user") User user, Model model) {
        // Password hashing, role normalization, and default status
        // are handled inside userService.saveUser(...)
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Email already exists. Please use another one.");
            return "user-register"; // show form again with error
        }
        user.setStatus("ACTIVE");
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
