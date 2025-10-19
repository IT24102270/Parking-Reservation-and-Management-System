package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.repository.PaymentRepository;
import org.springframework.data.domain.Sort; // 👈 1. IMPORT SORT
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Retrieves all payments, sorted by the most recent first for effective monitoring.
     */
    public List<Payment> getAllPayments() {
        // 🟢 2. ADD SORTING LOGIC
        return paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "paymentDate"));
    }

    /**
     * Processes a refund for a specific payment.
     * @param paymentId The ID of the payment to be refunded.
     */
    @Transactional
    public void processRefund(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment ID: " + paymentId));

        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new IllegalStateException("Only completed payments can be refunded.");
        }

        payment.setStatus("REFUNDED");
        payment.setRefundStatus("PROCESSED");
        payment.setRefundDate(LocalDateTime.now());

        paymentRepository.save(payment);
    }
}