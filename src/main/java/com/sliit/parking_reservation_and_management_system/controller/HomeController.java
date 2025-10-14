package com.sliit.parking_reservation_and_management_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Map root URL (/) to index.html or redirect to dashboard if logged in
    @GetMapping("/")
    public String index() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // If user is authenticated and not anonymous, redirect to dashboard
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            return "redirect:/customer/dashboard";
        }
        
        return "index"; // looks for src/main/resources/templates/index.html
    }
    
    // Handle login success redirect
    @GetMapping("/login-success")
    public String loginSuccess() {
        return "redirect:/customer/dashboard";
    }
    
    // Handle dashboard redirect
    @GetMapping("/dashboard")
    public String dashboard() {
        return "redirect:/customer/dashboard";
    }
}
