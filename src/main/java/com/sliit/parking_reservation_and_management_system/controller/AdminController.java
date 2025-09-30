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
    public String registerUser(
            @ModelAttribute("user") User user,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
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
    public String updateUser(@PathVariable("id") int id,
                             @ModelAttribute("user") User updatedUser,
                             Model model) {
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
