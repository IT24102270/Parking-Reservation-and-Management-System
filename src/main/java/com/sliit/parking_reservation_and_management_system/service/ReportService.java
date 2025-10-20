package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.dto.ReportDetailsDTO;
import com.sliit.parking_reservation_and_management_system.entity.*;
import com.sliit.parking_reservation_and_management_system.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ReportService {

    private final ReservationRepository reservationRepository;
    private final FinancialReportRepository financialReportRepository;
    private final StatisticReportRepository statisticReportRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final PaymentRepository paymentRepository;
    private final IncomeReportRepository incomeReportRepository; // ✅ RE-ADD THIS REPOSITORY

    // ✅ UPDATE THE CONSTRUCTOR TO INCLUDE IncomeReportRepository
    public ReportService(ReservationRepository reservationRepository,
                         FinancialReportRepository financialReportRepository,
                         StatisticReportRepository statisticReportRepository,
                         UserRepository userRepository,
                         ReportRepository reportRepository,
                         PaymentRepository paymentRepository,
                         IncomeReportRepository incomeReportRepository) {
        this.reservationRepository = reservationRepository;
        this.financialReportRepository = financialReportRepository;
        this.statisticReportRepository = statisticReportRepository;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
        this.paymentRepository = paymentRepository;
        this.incomeReportRepository = incomeReportRepository; // ✅ ASSIGN IT
    }

    @Transactional
    public void generateFinancialReport(LocalDate startDate, LocalDate endDate, String userEmail) {
        // --- Logic to calculate revenue (remains the same) ---
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        Integer totalReservations = reservationRepository.countByReservationDateBetween(startDateTime, endDateTime);
        BigDecimal totalRevenue = paymentRepository.sumCompletedPaymentsByDateRange(startDateTime, endDateTime).orElse(BigDecimal.ZERO);
        User currentUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));

        // --- Save the Financial Report (remains the same) ---
        FinancialReport report = new FinancialReport();
        report.setReportType("FINANCIAL");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setUser(currentUser);
        report.setGeneratedBy(currentUser.getUserID().intValue());
        report.setTotalReservations(totalReservations);
        report.setTotalRevenue(totalRevenue);
        FinancialReport savedReport = financialReportRepository.save(report);

        // START: RE-ADD THE LOGIC TO SAVE THE INCOME REPORT
        IncomeReport incomeReport = new IncomeReport();
        incomeReport.setReportID(savedReport.getReportID());
        incomeReport.setTotalIncome(savedReport.getTotalRevenue());
        incomeReport.setDate(LocalDate.now());
        incomeReport.setStartDate(savedReport.getStartDate());
        incomeReport.setEndDate(savedReport.getEndDate());
        incomeReportRepository.save(incomeReport);
        // ✅ END: LOGIC RE-ADDED
    }

    // ... (The rest of the service methods remain the same) ...

    public void generateStatisticReport(LocalDate startDate, LocalDate endDate, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("User not found"));
        StatisticReport report = new StatisticReport();
        report.setReportType("STATISTIC");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setUser(currentUser);
        report.setGeneratedBy(currentUser.getUserID().intValue());
        report.setCustomerActivityRate(new BigDecimal("85.50"));
        report.setSlotUsageRate(new BigDecimal("70.25"));
        statisticReportRepository.save(report);
    }

    public List<Report> findAllReports() {
        return reportRepository.findAll();
    }

    public ReportDetailsDTO getReportDetailsById(Integer reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new RuntimeException("Report not found with ID: " + reportId));
        LocalDateTime startDateTime = report.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = report.getEndDate().atTime(LocalTime.MAX);
        List<Payment> transactions = paymentRepository.findPaymentsBetweenDates(startDateTime, endDateTime);
        return new ReportDetailsDTO(report, transactions);
    }
    
    /**
     * Create a base report entry and return the generated ReportID
     */
    public Integer createBaseReport() {
        Report baseReport = new Report();
        baseReport.setReportType("INCOME");
        baseReport.setGeneratedBy(1); // TODO: Get actual user ID from security context
        baseReport.setCreatedAt(LocalDateTime.now());
        baseReport.setUpdatedAt(LocalDateTime.now());
        
        Report savedReport = reportRepository.save(baseReport);
        return savedReport.getReportID();
    }
}