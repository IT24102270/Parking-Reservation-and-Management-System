package com.sliit.parking_reservation_and_management_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == 404) {
                model.addAttribute("errorMessage", "Page not found");
                model.addAttribute("errorCode", "404");
            } else if (statusCode == 500) {
                model.addAttribute("errorMessage", "Internal server error - redirecting to dashboard");
                model.addAttribute("errorCode", "500");
                // For 500 errors, redirect to a working dashboard
                return "redirect:/customer/dashboard";
            } else {
                model.addAttribute("errorMessage", "An error occurred");
                model.addAttribute("errorCode", statusCode.toString());
            }
        }
        
        return "error";
    }
}
