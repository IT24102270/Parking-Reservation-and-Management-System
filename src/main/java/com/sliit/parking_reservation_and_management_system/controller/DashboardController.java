package com.sliit.parking_reservation_and_management_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/customer/dashboard")
    public String customerDashboard() {
        return "customer-dashboard";
    }

    @GetMapping("/slotmanager/dashboard")
    public String slotManagerDashboard() {
        return "slotmanager-dashboard";
    }

    @GetMapping("/finance/dashboard")
    public String financeDashboard() {
        return "finance-dashboard";
    }

    @GetMapping("/security/dashboard")
    public String securityDashboard() {
        return "security-dashboard";
    }

    @GetMapping("/support/dashboard")
    public String supportDashboard() {
        return "support-dashboard";
    }
}
