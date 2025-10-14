package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    // Create a new payment (or return existing one)
    public Payment createPayment(Long reservationID, BigDecimal amount, String method) {
        try {
            // Check if payment already exists for this reservation
            Optional<Payment> existingPayment = getPaymentByReservationId(reservationID);
            if (existingPayment.isPresent()) {
                Payment payment = existingPayment.get();
                System.out.println("Payment already exists for reservation " + reservationID + ": " + payment.getPaymentID());
                
                // Update the existing payment if needed (amount or method changed)
                boolean updated = false;
                if (!payment.getAmount().equals(amount)) {
                    payment.setAmount(amount);
                    updated = true;
                }
                String newMethod = method != null ? method : "ONLINE";
                if (!newMethod.equals(payment.getMethod())) {
                    payment.setMethod(newMethod);
                    updated = true;
                }
                
                if (updated) {
                    payment.setUpdatedAt(LocalDateTime.now());
                    Payment updatedPayment = paymentRepository.save(payment);
                    System.out.println("Payment updated successfully: " + updatedPayment);
                    return updatedPayment;
                }
                
                return payment;
            }
            
            // Create new payment if none exists
            Payment payment = new Payment();
            payment.setReservationID(reservationID);
            payment.setAmount(amount);
            payment.setMethod(method != null ? method : "ONLINE");
            payment.setStatus("PENDING");
            payment.setDate(LocalDateTime.now());
            payment.setCreatedAt(LocalDateTime.now());
            
            Payment savedPayment = paymentRepository.save(payment);
            System.out.println("Payment created successfully: " + savedPayment);
            return savedPayment;
            
        } catch (Exception e) {
            System.err.println("Error creating payment: " + e.getMessage());
            throw new RuntimeException("Failed to create payment", e);
        }
    }
    
    // Save payment
    public Payment savePayment(Payment payment) {
        try {
            return paymentRepository.save(payment);
        } catch (Exception e) {
            System.err.println("Error saving payment: " + e.getMessage());
            throw new RuntimeException("Failed to save payment", e);
        }
    }
    
    // Get payment by ID
    public Optional<Payment> getPaymentById(Long paymentID) {
        try {
            return paymentRepository.findById(paymentID);
        } catch (Exception e) {
            System.err.println("Error retrieving payment by ID " + paymentID + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    
    // Get payment by reservation ID
    public Optional<Payment> getPaymentByReservationId(Long reservationID) {
        try {
            return paymentRepository.findByReservationID(reservationID);
        } catch (Exception e) {
            System.err.println("Error retrieving payment for reservation " + reservationID + ": " + e.getMessage());
            return Optional.empty();
        }
    }
    
    // Get all payments for a reservation
    public List<Payment> getAllPaymentsByReservationId(Long reservationID) {
        try {
            return paymentRepository.findAllByReservationID(reservationID);
        } catch (Exception e) {
            System.err.println("Error retrieving payments for reservation " + reservationID + ": " + e.getMessage());
            return List.of();
        }
    }
    
    // Update payment status
    public boolean updatePaymentStatus(Long paymentID, String status) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentID);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus(status);
                payment.setUpdatedAt(LocalDateTime.now());
                
                if ("COMPLETED".equals(status) || "PAID".equals(status)) {
                    payment.setDate(LocalDateTime.now()); // Update payment date when completed
                }
                
                paymentRepository.save(payment);
                System.out.println("Payment status updated: ID=" + paymentID + ", Status=" + status);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;
        }
    }
    
    // Process payment (mark as completed)
    public boolean processPayment(Long paymentID, String paymentMethod) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentID);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                
                if (!"PENDING".equals(payment.getStatus())) {
                    System.out.println("Payment already processed: " + payment.getStatus());
                    return false;
                }
                
                payment.setStatus("COMPLETED");
                payment.setMethod(paymentMethod);
                payment.setDate(LocalDateTime.now());
                payment.setUpdatedAt(LocalDateTime.now());
                
                paymentRepository.save(payment);
                System.out.println("Payment processed successfully: " + payment);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            return false;
        }
    }
    
    // Get payments by status
    public List<Payment> getPaymentsByStatus(String status) {
        try {
            return paymentRepository.findByStatus(status);
        } catch (Exception e) {
            System.err.println("Error retrieving payments by status " + status + ": " + e.getMessage());
            return List.of();
        }
    }
    
    // Get all payments
    public List<Payment> getAllPayments() {
        try {
            return paymentRepository.findAll();
        } catch (Exception e) {
            System.err.println("Error retrieving all payments: " + e.getMessage());
            return List.of();
        }
    }
    
    // Get recent payments
    public List<Payment> getRecentPayments() {
        try {
            return paymentRepository.findRecentPayments();
        } catch (Exception e) {
            System.err.println("Error retrieving recent payments: " + e.getMessage());
            return List.of();
        }
    }
    
    // Check if payment exists for reservation
    public boolean paymentExistsForReservation(Long reservationID) {
        try {
            return paymentRepository.existsByReservationID(reservationID);
        } catch (Exception e) {
            System.err.println("Error checking payment existence for reservation " + reservationID + ": " + e.getMessage());
            return false;
        }
    }
    
    // Get total amount by status
    public BigDecimal getTotalAmountByStatus(String status) {
        try {
            BigDecimal total = paymentRepository.getTotalAmountByStatus(status);
            return total != null ? total : BigDecimal.ZERO;
        } catch (Exception e) {
            System.err.println("Error calculating total amount by status " + status + ": " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    // Count payments by status
    public Long countPaymentsByStatus(String status) {
        try {
            return paymentRepository.countByStatus(status);
        } catch (Exception e) {
            System.err.println("Error counting payments by status " + status + ": " + e.getMessage());
            return 0L;
        }
    }
    
    // Complete payment (update status to COMPLETED)
    public boolean completePayment(Long paymentID, String paymentMethod) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentID);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                
                if ("COMPLETED".equals(payment.getStatus())) {
                    System.out.println("Payment already completed");
                    return true;
                }
                
                if ("CANCELLED".equals(payment.getStatus())) {
                    System.out.println("Cannot complete cancelled payment");
                    return false;
                }
                
                payment.setStatus("COMPLETED");
                payment.setMethod(paymentMethod != null ? paymentMethod : payment.getMethod());
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                
                System.out.println("Payment completed: " + paymentID);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error completing payment: " + e.getMessage());
            return false;
        }
    }
    
    // Delete payment
    public boolean deletePayment(Long paymentID) {
        try {
            if (paymentRepository.existsById(paymentID)) {
                paymentRepository.deleteById(paymentID);
                System.out.println("Payment deleted: " + paymentID);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting payment: " + e.getMessage());
            return false;
        }
    }
    
    // Check if payment exists for reservation (helper method)
    public boolean hasPayment(Long reservationID) {
        try {
            return getPaymentByReservationId(reservationID).isPresent();
        } catch (Exception e) {
            System.err.println("Error checking payment existence: " + e.getMessage());
            return false;
        }
    }
    
    // Cancel payment (update status to CANCELLED)
    public boolean cancelPayment(Long paymentID) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentID);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setStatus("CANCELLED");
                payment.setUpdatedAt(LocalDateTime.now());
                paymentRepository.save(payment);
                
                System.out.println("Payment cancelled successfully: " + payment);
                return true;
            } else {
                System.err.println("Payment not found with ID: " + paymentID);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error cancelling payment: " + e.getMessage());
            return false;
        }
    }
}
