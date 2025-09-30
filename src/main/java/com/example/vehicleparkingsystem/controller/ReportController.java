package com.example.vehicleparkingsystem.controller;

import com.example.vehicleparkingsystem.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
public class ReportController {

    private final ReportingService reportingService;

    @Autowired
    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    /**
     * Displays the main reporting page.
     *
     * @return The name of the index template.
     */
    @GetMapping("/")
    public String showReportForm() {
        return "index";
    }

    /**
     * Handles the generation of the income report based on user-submitted dates.
     *
     * @param startDate The start date for the report.
     * @param endDate   The end date for the report.
     * @param model     The Spring Model to pass data to the view.
     * @return The name of the income report template or an error page.
     */
    @PostMapping("/reports/income")
    public String generateIncomeReport(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        if (startDate.isAfter(endDate)) {
            model.addAttribute("error", "Start date cannot be after end date.");
            return "index"; // Return to form with error message
        }

        try {
            BigDecimal totalIncome = reportingService.calculateTotalIncome(startDate, endDate);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            model.addAttribute("totalIncome", totalIncome);
            return "income-report";
        } catch (Exception e) {
            // Log the exception in a real application
            model.addAttribute("errorMessage", "An unexpected error occurred while generating the report.");
            return "error";
        }
    }
}
