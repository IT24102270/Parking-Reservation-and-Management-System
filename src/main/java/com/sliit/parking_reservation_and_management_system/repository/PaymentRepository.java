package com.sliit.parking_reservation_and_management_system.repository;
import com.sliit.parking_reservation_and_management_system.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Integer> { }