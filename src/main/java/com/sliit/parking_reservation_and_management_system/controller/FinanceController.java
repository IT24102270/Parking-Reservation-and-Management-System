package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.dto.ReportDetailsDTO;
import com.sliit.parking_reservation_and_management_system.entity.IncomeReport;
import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.entity.Pricing;
import com.sliit.parking_reservation_and_management_system.entity.Report;
import com.sliit.parking_reservation_and_management_system.service.FinancialReportService;
import com.sliit.parking_reservation_and_management_system.service.IncomeReportService;
import com.sliit.parking_reservation_and_management_system.service.PaymentService;
import com.sliit.parking_reservation_and_management_system.service.PricingService;
import com.sliit.parking_reservation_and_management_system.service.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    private final ReportService reportService;
    private final PricingService pricingService;
    private final PaymentService paymentService;
    private final IncomeReportService incomeReportService;
    private final FinancialReportService financialReportService;

    public FinanceController(ReportService reportService, PricingService pricingService, 
                           PaymentService paymentService, IncomeReportService incomeReportService,
                           FinancialReportService financialReportService) {
        this.reportService = reportService;
        this.pricingService = pricingService;
        this.paymentService = paymentService;
        this.incomeReportService = incomeReportService;
        this.financialReportService = financialReportService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("reports", reportService.findAllReports());
        // model.addAttribute("pricingRules", pricingService.getAllPricingRules()); // Pricing table doesn't exist
        // model.addAttribute("newPricingRule", new Pricing()); // Pricing table doesn't exist
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

    // ✅ ADD THIS NEW METHOD FOR VIEWING PAYMENTS
    @GetMapping("/payments")
    public String viewPayments(Model model) {
        List<Payment> payments = paymentService.getAllPayments();
        model.addAttribute("payments", payments);
        
        // Calculate payment status counts
        long completedCount = payments.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
        long pendingCount = payments.stream().filter(p -> "PENDING".equals(p.getStatus())).count();
        long failedCount = payments.stream().filter(p -> "FAILED".equals(p.getStatus())).count();
        long refundedCount = payments.stream().filter(p -> "REFUNDED".equals(p.getStatus())).count();
        
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("failedCount", failedCount);
        model.addAttribute("refundedCount", refundedCount);
        
        return "finance-payments";
    }

    // ✅ INCOME REPORT EXPORT ENDPOINTS
    @GetMapping("/reports/export")
    public String showExportReportForm(Model model) {
        model.addAttribute("incomeReports", incomeReportService.getAllIncomeReports());
        return "finance-export-report";
    }
    
    @PostMapping("/reports/export/current-month")
    public String exportCurrentMonthReport(RedirectAttributes redirectAttributes) {
        try {
            IncomeReport report = incomeReportService.generateCurrentMonthReport();
            redirectAttributes.addFlashAttribute("success", 
                "Current month income report generated successfully! Report ID: " + report.getReportID() + 
                ", Total Income: $" + report.getTotalIncome());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to generate current month report: " + e.getMessage());
        }
        return "redirect:/finance/reports/export";
    }
    
    @PostMapping("/reports/export/last-month")
    public String exportLastMonthReport(RedirectAttributes redirectAttributes) {
        try {
            IncomeReport report = incomeReportService.generateLastMonthReport();
            redirectAttributes.addFlashAttribute("success", 
                "Last month income report generated successfully! Report ID: " + report.getReportID() + 
                ", Total Income: $" + report.getTotalIncome());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to generate last month report: " + e.getMessage());
        }
        return "redirect:/finance/reports/export";
    }
    
    @PostMapping("/reports/export/custom")
    public String exportCustomRangeReport(@RequestParam("startDate") LocalDate startDate,
                                        @RequestParam("endDate") LocalDate endDate,
                                        RedirectAttributes redirectAttributes) {
        try {
            IncomeReport report = incomeReportService.generateCustomRangeReport(startDate, endDate);
            redirectAttributes.addFlashAttribute("success", 
                "Custom range income report generated successfully! Report ID: " + report.getReportID() + 
                ", Total Income: $" + report.getTotalIncome() + 
                " (From " + startDate + " to " + endDate + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to generate custom range report: " + e.getMessage());
        }
        return "redirect:/finance/reports/export";
    }
    
    @GetMapping("/reports/income/{id}")
    public String viewIncomeReport(@PathVariable("id") Integer id, Model model) {
        try {
            IncomeReport report = incomeReportService.getIncomeReportById(id);
            if (report == null) {
                model.addAttribute("error", "Income report not found with ID: " + id);
                return "redirect:/finance/reports/export";
            }
            
            // Get payment statistics for the report period
            IncomeReportService.PaymentStatistics stats = 
                incomeReportService.calculatePaymentStatistics(report.getStartDate(), report.getEndDate());
            
            model.addAttribute("report", report);
            model.addAttribute("statistics", stats);
            return "finance-income-report-details";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading income report: " + e.getMessage());
            return "redirect:/finance/reports/export";
        }
    }

    // DISABLED: Pricing table doesn't exist in database
    // @PostMapping("/pricing/add")
    // public String addPricingRule(@ModelAttribute("newPricingRule") Pricing newPricingRule, RedirectAttributes redirectAttributes) {
    //     if (newPricingRule.getStartTime().isAfter(newPricingRule.getEndTime())) {
    //         redirectAttributes.addFlashAttribute("pricingError", "Start time must be before end time.");
    //         return "redirect:/finance/dashboard";
    //     }
    //     pricingService.savePricingRule(newPricingRule);
    //     redirectAttributes.addFlashAttribute("pricingSuccess", "New pricing rule added successfully!");
    //     return "redirect:/finance/dashboard";
    // }

    // ✅ REFUND MANAGEMENT ENDPOINTS
    @GetMapping("/refunds")
    public String showRefundManagement(Model model) {
        List<Payment> completedPayments = paymentService.getAllPayments().stream()
            .filter(p -> "COMPLETED".equals(p.getStatus()))
            .filter(p -> p.getAmount().compareTo(BigDecimal.ZERO) > 0) // Only positive amounts (not already refunded)
            .toList();
        
        List<Payment> refundedPayments = paymentService.getAllPayments().stream()
            .filter(p -> "REFUNDED".equals(p.getStatus()) || 
                        (p.getAmount().compareTo(BigDecimal.ZERO) < 0 && "COMPLETED".equals(p.getStatus())))
            .toList();
        
        model.addAttribute("completedPayments", completedPayments);
        model.addAttribute("refundedPayments", refundedPayments);
        
        // Calculate refund statistics
        BigDecimal totalRefunded = refundedPayments.stream()
            .map(Payment::getAmount)
            .map(BigDecimal::abs)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("totalRefunded", totalRefunded);
        model.addAttribute("refundCount", refundedPayments.size());
        
        return "finance-refund-management";
    }
    
    @GetMapping("/refunds/process/{id}")
    public String showRefundForm(@PathVariable("id") Long id, Model model) {
        Optional<Payment> paymentOpt = paymentService.getPaymentById(id);
        if (!paymentOpt.isPresent()) {
            model.addAttribute("error", "Payment not found with ID: " + id);
            return "redirect:/finance/refunds";
        }
        
        Payment payment = paymentOpt.get();
        if (!"COMPLETED".equals(payment.getStatus()) || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            model.addAttribute("error", "Payment is not eligible for refund");
            return "redirect:/finance/refunds";
        }
        
        model.addAttribute("payment", payment);
        return "finance-refund-form";
    }
    
    @PostMapping("/refunds/process/{id}")
    public String processRefund(@PathVariable("id") Long id,
                              @RequestParam("refundAmount") BigDecimal refundAmount,
                              @RequestParam("reason") String reason,
                              RedirectAttributes redirectAttributes) {
        try {
            Optional<Payment> paymentOpt = paymentService.getPaymentById(id);
            if (!paymentOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Payment not found with ID: " + id);
                return "redirect:/finance/refunds";
            }

            Payment payment = paymentOpt.get();
            
            // Validate refund amount
            if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error", "Refund amount must be greater than zero");
                return "redirect:/finance/refunds/process/" + id;
            }
            
            if (refundAmount.compareTo(payment.getAmount()) > 0) {
                redirectAttributes.addFlashAttribute("error", "Refund amount cannot exceed original payment amount");
                return "redirect:/finance/refunds/process/" + id;
            }

            // Process refund
            boolean refundProcessed = paymentService.processRefund(
                payment.getReservationID(),
                refundAmount,
                reason
            );

            if (refundProcessed) {
                redirectAttributes.addFlashAttribute("success", 
                    "Refund of $" + refundAmount + " for Payment ID #" + id + " processed successfully.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to process refund for Payment ID #" + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error processing refund: " + e.getMessage());
        }
        return "redirect:/finance/refunds";
    }
    
    @PostMapping("/payments/refund/{id}")
    public String refundPayment(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            // Convert Integer to Long for payment ID
            Long paymentId = id.longValue();

            // Get the payment details to extract reservation ID and amount
            Optional<Payment> paymentOpt = paymentService.getPaymentById(paymentId);
            if (!paymentOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("paymentError", "Payment not found with ID: " + id);
                return "redirect:/finance/payments";
            }

            Payment payment = paymentOpt.get();

            // Process full refund of the payment amount
            boolean refundProcessed = paymentService.processRefund(
                payment.getReservationID(),
                payment.getAmount(),
                "Finance department initiated full refund"
            );

            if (refundProcessed) {
                redirectAttributes.addFlashAttribute("paymentSuccess", "Full refund for Payment ID #" + id + " processed successfully.");
            } else {
                redirectAttributes.addFlashAttribute("paymentError", "Failed to process refund for Payment ID #" + id);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("paymentError", "Error processing refund: " + e.getMessage());
        }
        return "redirect:/finance/payments";
    }

    // ✅ FINANCIAL REPORTS MANAGEMENT ENDPOINTS

    @GetMapping("/reports/financial")
    public String showFinancialReports(Model model) {
        List<Report> financialReports = financialReportService.getAllFinancialReports();
        List<Report> recentReports = financialReportService.getRecentFinancialReports();
        
        // Calculate report counts by type
        long dailyReports = financialReports.stream().filter(r -> "DAILY".equals(r.getReportType())).count();
        long weeklyReports = financialReports.stream().filter(r -> "WEEKLY".equals(r.getReportType())).count();
        long monthlyReports = financialReports.stream().filter(r -> "MONTHLY".equals(r.getReportType())).count();
        
        model.addAttribute("financialReports", financialReports);
        model.addAttribute("recentReports", recentReports);
        model.addAttribute("totalReports", financialReports.size());
        model.addAttribute("dailyReports", dailyReports);
        model.addAttribute("weeklyReports", weeklyReports);
        model.addAttribute("monthlyReports", monthlyReports);
        
        return "finance-financial-reports";
    }

    @GetMapping("/reports/financial/generate")
    public String showGenerateReportForm(Model model) {
        return "finance-generate-report";
    }

    @PostMapping("/reports/financial/generate/daily")
    public String generateDailyReport(@RequestParam("reportDate") LocalDate reportDate,
                                    RedirectAttributes redirectAttributes) {
        try {
            Report report = financialReportService.generateDailyReport(reportDate);
            FinancialReportService.FinancialData financialData = 
                financialReportService.calculateFinancialData(reportDate, reportDate);
            
            redirectAttributes.addFlashAttribute("success", 
                "Daily financial report generated successfully for " + reportDate + 
                ". Total Revenue: $" + financialData.getTotalRevenue() + 
                ", Net Income: $" + financialData.getNetIncome());
            redirectAttributes.addFlashAttribute("generatedReportId", report.getReportID());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error generating daily report: " + e.getMessage());
        }
        return "redirect:/finance/reports/financial";
    }

    @PostMapping("/reports/financial/generate/weekly")
    public String generateWeeklyReport(@RequestParam("weekStartDate") LocalDate weekStartDate,
                                     RedirectAttributes redirectAttributes) {
        try {
            Report report = financialReportService.generateWeeklyReport(weekStartDate);
            LocalDate endDate = weekStartDate.plusDays(6);
            FinancialReportService.FinancialData financialData = 
                financialReportService.calculateFinancialData(weekStartDate, endDate);
            
            redirectAttributes.addFlashAttribute("success", 
                "Weekly financial report generated successfully for week starting " + weekStartDate + 
                ". Total Revenue: $" + financialData.getTotalRevenue() + 
                ", Net Income: $" + financialData.getNetIncome());
            redirectAttributes.addFlashAttribute("generatedReportId", report.getReportID());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error generating weekly report: " + e.getMessage());
        }
        return "redirect:/finance/reports/financial";
    }

    @PostMapping("/reports/financial/generate/monthly")
    public String generateMonthlyReport(@RequestParam("monthDate") LocalDate monthDate,
                                      RedirectAttributes redirectAttributes) {
        try {
            Report report = financialReportService.generateMonthlyReport(monthDate);
            LocalDate startDate = monthDate.withDayOfMonth(1);
            LocalDate endDate = monthDate.withDayOfMonth(monthDate.lengthOfMonth());
            FinancialReportService.FinancialData financialData = 
                financialReportService.calculateFinancialData(startDate, endDate);
            
            redirectAttributes.addFlashAttribute("success", 
                "Monthly financial report generated successfully for " + monthDate.getMonth() + " " + monthDate.getYear() + 
                ". Total Revenue: $" + financialData.getTotalRevenue() + 
                ", Net Income: $" + financialData.getNetIncome());
            redirectAttributes.addFlashAttribute("generatedReportId", report.getReportID());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error generating monthly report: " + e.getMessage());
        }
        return "redirect:/finance/reports/financial";
    }

    @PostMapping("/reports/financial/generate/custom")
    public String generateCustomReport(@RequestParam("startDate") LocalDate startDate,
                                     @RequestParam("endDate") LocalDate endDate,
                                     @RequestParam(value = "description", required = false) String description,
                                     RedirectAttributes redirectAttributes) {
        try {
            if (startDate.isAfter(endDate)) {
                redirectAttributes.addFlashAttribute("error", "Start date must be before or equal to end date");
                return "redirect:/finance/reports/financial/generate";
            }
            
            Report report = financialReportService.generateCustomRangeReport(startDate, endDate, description);
            FinancialReportService.FinancialData financialData = 
                financialReportService.calculateFinancialData(startDate, endDate);
            
            redirectAttributes.addFlashAttribute("success", 
                "Custom financial report generated successfully for " + startDate + " to " + endDate + 
                ". Total Revenue: $" + financialData.getTotalRevenue() + 
                ", Net Income: $" + financialData.getNetIncome());
            redirectAttributes.addFlashAttribute("generatedReportId", report.getReportID());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error generating custom report: " + e.getMessage());
        }
        return "redirect:/finance/reports/financial";
    }

    @GetMapping("/reports/financial/{id}")
    public String viewFinancialReport(@PathVariable("id") Integer reportId, Model model) {
        try {
            Optional<Report> reportOpt = financialReportService.getFinancialReportById(reportId);
            if (!reportOpt.isPresent()) {
                model.addAttribute("error", "Financial report not found with ID: " + reportId);
                return "redirect:/finance/reports/financial";
            }
            
            Report report = reportOpt.get();
            
            // Calculate date range based on report type
            LocalDate startDate, endDate;
            if ("DAILY".equals(report.getReportType())) {
                startDate = endDate = report.getCreatedAt().toLocalDate();
            } else if ("WEEKLY".equals(report.getReportType())) {
                startDate = report.getCreatedAt().toLocalDate().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                endDate = startDate.plusDays(6);
            } else if ("MONTHLY".equals(report.getReportType())) {
                startDate = report.getCreatedAt().toLocalDate().with(java.time.temporal.TemporalAdjusters.firstDayOfMonth());
                endDate = report.getCreatedAt().toLocalDate().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
            } else {
                // For custom reports, use a reasonable default or get from report data
                startDate = report.getCreatedAt().toLocalDate().minusDays(30);
                endDate = report.getCreatedAt().toLocalDate();
            }
            
            FinancialReportService.FinancialData financialData = 
                financialReportService.getFinancialDataForReport(report, startDate, endDate);
            
            model.addAttribute("report", report);
            model.addAttribute("financialData", financialData);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
            
            return "finance-financial-report-details";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading financial report: " + e.getMessage());
            return "redirect:/finance/reports/financial";
        }
    }

    @PostMapping("/reports/financial/{id}/update")
    public String updateFinancialReport(@PathVariable("id") Integer reportId,
                                      @RequestParam("reportType") String reportType,
                                      @RequestParam(value = "description", required = false) String description,
                                      RedirectAttributes redirectAttributes) {
        try {
            Report updatedReport = financialReportService.updateFinancialReport(reportId, reportType, description);
            redirectAttributes.addFlashAttribute("success", 
                "Financial report #" + reportId + " updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating financial report: " + e.getMessage());
        }
        return "redirect:/finance/reports/financial/" + reportId;
    }

    @PostMapping("/reports/financial/{id}/delete")
    public String deleteFinancialReport(@PathVariable("id") Integer reportId,
                                      RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = financialReportService.deleteFinancialReport(reportId);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", 
                    "Financial report #" + reportId + " deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Financial report #" + reportId + " not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting financial report: " + e.getMessage());
        }
        return "redirect:/finance/reports/financial";
    }

    // ✅ QUICK REPORT GENERATION ENDPOINTS

    @PostMapping("/reports/financial/generate/today")
    public String generateTodayReport(RedirectAttributes redirectAttributes) {
        try {
            Report report = financialReportService.generateTodayReport();
            FinancialReportService.FinancialData financialData = 
                financialReportService.calculateFinancialData(LocalDate.now(), LocalDate.now());
            
            redirectAttributes.addFlashAttribute("success", 
                "Today's financial report generated successfully. " +
                "Total Revenue: $" + financialData.getTotalRevenue() + 
                ", Net Income: $" + financialData.getNetIncome());
            redirectAttributes.addFlashAttribute("generatedReportId", report.getReportID());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error generating today's report: " + e.getMessage());
        }
        return "redirect:/finance/reports/financial";
    }

    @PostMapping("/reports/financial/generate/this-week")
    public String generateThisWeekReport(RedirectAttributes redirectAttributes) {
        try {
            Report report = financialReportService.generateThisWeekReport();
            LocalDate startDate = LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            LocalDate endDate = startDate.plusDays(6);
            FinancialReportService.FinancialData financialData = 
                financialReportService.calculateFinancialData(startDate, endDate);
            
            redirectAttributes.addFlashAttribute("success", 
                "This week's financial report generated successfully. " +
                "Total Revenue: $" + financialData.getTotalRevenue() + 
                ", Net Income: $" + financialData.getNetIncome());
            redirectAttributes.addFlashAttribute("generatedReportId", report.getReportID());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error generating this week's report: " + e.getMessage());
        }
        return "redirect:/finance/reports/financial";
    }

    @PostMapping("/reports/financial/generate/this-month")
    public String generateThisMonthReport(RedirectAttributes redirectAttributes) {
        try {
            Report report = financialReportService.generateThisMonthReport();
            LocalDate startDate = LocalDate.now().with(java.time.temporal.TemporalAdjusters.firstDayOfMonth());
            LocalDate endDate = LocalDate.now().with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
            FinancialReportService.FinancialData financialData = 
                financialReportService.calculateFinancialData(startDate, endDate);
            
            redirectAttributes.addFlashAttribute("success", 
                "This month's financial report generated successfully. " +
                "Total Revenue: $" + financialData.getTotalRevenue() + 
                ", Net Income: $" + financialData.getNetIncome());
            redirectAttributes.addFlashAttribute("generatedReportId", report.getReportID());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error generating this month's report: " + e.getMessage());
        }
        return "redirect:/finance/reports/financial";
    }
}