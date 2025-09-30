package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Password strength regex (8–12 chars, upper, lower, digit, special char)
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,12}$");

    // Show login page
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Show registration page
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Handle registration form submission
    @PostMapping("/register")
    public String processRegister(
            @ModelAttribute("user") User user,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model
    ) {
        // 1. Check duplicate email
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Email already exists. Please use another one.");
            return "register";
        }

        // 2. Validate password strength
        if (!PASSWORD_PATTERN.matcher(user.getPasswordHash()).matches()) {
            model.addAttribute("user", user);
            model.addAttribute("error",
                    "Password must be 8-12 characters, include uppercase, lowercase, number, and a special character.");
            return "register";
        }

        // 3. Confirm password match
        if (!user.getPasswordHash().equals(confirmPassword)) {
            model.addAttribute("user", user);
            model.addAttribute("error", "Passwords do not match.");
            return "register";
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

        // 4. Encrypt password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        // 5. Default role = CUSTOMER
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("CUSTOMER");
        }

        userRepository.save(user);
        return "redirect:/login?success";
    }
}
