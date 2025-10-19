package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.Pricing;
import com.sliit.parking_reservation_and_management_system.service.PaymentService; // 👈 1. Import PaymentService
import com.sliit.parking_reservation_and_management_system.service.PricingService;
import com.sliit.parking_reservation_and_management_system.service.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    private final ReportService reportService;
    private final PricingService pricingService;
    private final PaymentService paymentService; // 👈 2. Inject PaymentService

    // 3. Update constructor
    public FinanceController(ReportService reportService, PricingService pricingService, PaymentService paymentService) {
        this.reportService = reportService;
        this.pricingService = pricingService;
        this.paymentService = paymentService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("reports", reportService.findAllReports());
        model.addAttribute("pricingRules", pricingService.getAllPricingRules());
        model.addAttribute("newPricingRule", new Pricing());
        model.addAttribute("payments", paymentService.getAllPayments()); // 👈 4. Add payments to the model
        return "finance-dashboard";
    }

    // ... (your existing generate-report and pricing methods remain the same)

    // 🆕 START: Add this new method for processing refunds
    @PostMapping("/payments/refund/{id}")
    public String refundPayment(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            paymentService.processRefund(id);
            redirectAttributes.addFlashAttribute("paymentSuccess", "Refund for Payment ID #" + id + " processed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("paymentError", "Error processing refund: " + e.getMessage());
        }
        return "redirect:/finance/dashboard";
    }
    // 🆕 END: Add this new method
}