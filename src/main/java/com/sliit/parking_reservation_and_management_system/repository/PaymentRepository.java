package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // ✅ ADD THIS METHOD TO FIND TRANSACTIONS FOR A REPORT
    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}