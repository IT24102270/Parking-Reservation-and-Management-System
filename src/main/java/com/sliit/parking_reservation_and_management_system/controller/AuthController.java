package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Show login page
    @GetMapping("/login")
    public String login() {
        return "login"; // maps to login.html (Thymeleaf)
    }

    // Show registration page
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register"; // maps to register.html
    }

    // Handle registration form submission
    @PostMapping("/register")
    public String processRegister(@ModelAttribute("user") User user) {
        // Encrypt password before saving
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        // Default role is CUSTOMER for self-registration
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("CUSTOMER");
        }

        userRepository.save(user);

        return "redirect:/login?success"; // redirect to login page
    }
}
