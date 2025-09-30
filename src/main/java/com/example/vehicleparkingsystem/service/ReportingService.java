package com.example.vehicleparkingsystem.service;

import com.example.vehicleparkingsystem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class ReportingService {

    private final PaymentRepository paymentRepository;

    @Autowired
    public ReportingService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Calculates the total income from 'PAID' payments within a specified date range.
     *
     * @param startDate The starting date of the reporting period.
     * @param endDate   The ending date of the reporting period.
     * @return A BigDecimal representing the total income.
     */
    public BigDecimal calculateTotalIncome(LocalDate startDate, LocalDate endDate) {
        // We convert LocalDate to LocalDateTime to include the entire day in the query.
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return paymentRepository.sumPaidAmountBetweenDates(startDateTime, endDateTime);
    }
}
