package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.dto.ReportDetailsDTO;
import com.sliit.parking_reservation_and_management_system.entity.Pricing;
import com.sliit.parking_reservation_and_management_system.service.PaymentService;
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
    private final PaymentService paymentService;

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
        model.addAttribute("payments", paymentService.getAllPayments());
        return "finance-dashboard";
    }

    @PostMapping("/generate-report")
    public String generateReport(@RequestParam String reportType,
                                 @RequestParam LocalDate startDate,
                                 @RequestParam LocalDate endDate,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        if (startDate.isAfter(endDate)) {
            redirectAttributes.addFlashAttribute("error", "Start date cannot be after end date.");
            return "redirect:/finance/dashboard";
        }
        String userEmail = userDetails.getUsername();
        if ("FINANCIAL".equals(reportType)) {
            reportService.generateFinancialReport(startDate, endDate, userEmail);
        } else if ("STATISTIC".equals(reportType)) {
            reportService.generateStatisticReport(startDate, endDate, userEmail);
        }
        redirectAttributes.addFlashAttribute("success", "Report generated successfully!");
        return "redirect:/finance/dashboard";
    }

    // ✅ ADD THIS NEW METHOD FOR THE DETAILS PAGE
    @GetMapping("/report/{id}")
    public String getReportDetails(@PathVariable("id") Integer id, Model model) {
        ReportDetailsDTO reportDetails = reportService.getReportDetailsById(id);
        model.addAttribute("reportDetails", reportDetails);
        return "report-details";
    }

    @PostMapping("/pricing/add")
    public String addPricingRule(@ModelAttribute("newPricingRule") Pricing newPricingRule, RedirectAttributes redirectAttributes) {
        if (newPricingRule.getStartTime().isAfter(newPricingRule.getEndTime())) {
            redirectAttributes.addFlashAttribute("pricingError", "Start time must be before end time.");
            return "redirect:/finance/dashboard";
        }
        pricingService.savePricingRule(newPricingRule);
        redirectAttributes.addFlashAttribute("pricingSuccess", "New pricing rule added successfully!");
        return "redirect:/finance/dashboard";
    }

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
}