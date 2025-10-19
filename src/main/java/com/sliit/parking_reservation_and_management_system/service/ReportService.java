package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.*;
import com.sliit.parking_reservation_and_management_system.repository.*;
import org.springframework.stereotype.Service;
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

    public ReportService(ReservationRepository reservationRepository,
                         FinancialReportRepository financialReportRepository,
                         StatisticReportRepository statisticReportRepository,
                         UserRepository userRepository,
                         ReportRepository reportRepository) {
        this.reservationRepository = reservationRepository;
        this.financialReportRepository = financialReportRepository;
        this.statisticReportRepository = statisticReportRepository;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
    }

    public void generateFinancialReport(LocalDate startDate, LocalDate endDate, String userEmail) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Integer totalReservations = reservationRepository.countByReservationDateBetween(startDateTime, endDateTime);
        BigDecimal totalRevenue = reservationRepository.sumTotalCostByReservationDateBetween(startDateTime, endDateTime)
                .orElse(BigDecimal.ZERO);

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FinancialReport report = new FinancialReport();
        report.setReportType("FINANCIAL");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setUser(currentUser);
        report.setGeneratedBy(currentUser.getUserID());
        report.setTotalReservations(totalReservations);
        report.setTotalRevenue(totalRevenue);

        financialReportRepository.save(report);
    }

    public void generateStatisticReport(LocalDate startDate, LocalDate endDate, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StatisticReport report = new StatisticReport();
        report.setReportType("STATISTIC");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setUser(currentUser);
        report.setGeneratedBy(currentUser.getUserID());
        report.setCustomerActivityRate(new BigDecimal("85.50"));
        report.setSlotUsageRate(new BigDecimal("70.25"));

        statisticReportRepository.save(report);
    }

    public List<Report> findAllReports() {
        return reportRepository.findAll();
    }
}