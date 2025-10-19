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
        if (reservationID == null) {
            return Optional.empty();
        }
        
        if (paymentRepository == null) {
            return Optional.empty();
        }
        
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
    
    // Calculate payment adjustment for booking time changes
    public PaymentAdjustment calculatePaymentAdjustment(Long reservationID, BigDecimal newAmount) {
        // Validate inputs
        if (reservationID == null) {
            throw new IllegalArgumentException("Reservation ID cannot be null");
        }
        if (newAmount == null) {
            throw new IllegalArgumentException("New amount cannot be null");
        }
        if (paymentRepository == null) {
            throw new IllegalStateException("PaymentRepository is not initialized");
        }
        
        try {
            Optional<Payment> paymentOpt = getPaymentByReservationId(reservationID);
            
            // Handle case where no payment exists - create a default adjustment
            BigDecimal currentAmount = BigDecimal.ZERO;
            Payment currentPayment = null;
            
            if (paymentOpt.isPresent()) {
                currentPayment = paymentOpt.get();
                currentAmount = currentPayment.getAmount();
            } else {
                // Create a dummy payment for calculation purposes
                currentPayment = new Payment();
                currentPayment.setReservationID(reservationID);
                currentPayment.setAmount(BigDecimal.ZERO);
                currentPayment.setStatus("PENDING");
            }
            
            BigDecimal difference = newAmount.subtract(currentAmount);
            
            PaymentAdjustment adjustment = new PaymentAdjustment();
            adjustment.setReservationID(reservationID);
            adjustment.setOriginalAmount(currentAmount);
            adjustment.setNewAmount(newAmount);
            adjustment.setAdjustmentAmount(difference);
            adjustment.setCurrentPaymentId(currentPayment != null ? currentPayment.getPaymentID() : null);
            
            if (difference.compareTo(BigDecimal.ZERO) > 0) {
                adjustment.setAdjustmentType("ADDITIONAL_PAYMENT");
                adjustment.setRequiresPayment(true);
            } else if (difference.compareTo(BigDecimal.ZERO) < 0) {
                adjustment.setAdjustmentType("REFUND");
                adjustment.setRequiresPayment(false);
            } else {
                adjustment.setAdjustmentType("NO_CHANGE");
                adjustment.setRequiresPayment(false);
            }
            
            System.out.println("Payment adjustment calculated:");
            System.out.println("- Reservation ID: " + reservationID);
            System.out.println("- Original: $" + currentAmount);
            System.out.println("- New: $" + newAmount);
            System.out.println("- Difference: $" + difference);
            System.out.println("- Type: " + adjustment.getAdjustmentType());
            
            return adjustment;
            
        } catch (Exception e) {
            System.err.println("Error calculating payment adjustment: " + e.getMessage());
            e.printStackTrace();
            
            // Return a safe default adjustment to prevent 500 errors
            PaymentAdjustment safeAdjustment = new PaymentAdjustment();
            safeAdjustment.setReservationID(reservationID);
            safeAdjustment.setOriginalAmount(BigDecimal.ZERO);
            safeAdjustment.setNewAmount(newAmount);
            safeAdjustment.setAdjustmentAmount(newAmount);
            safeAdjustment.setAdjustmentType("ADDITIONAL_PAYMENT");
            safeAdjustment.setRequiresPayment(true);
            
            return safeAdjustment;
        }
    }
    
    // Create additional payment for booking modifications
    public Payment createAdditionalPayment(Long reservationID, BigDecimal additionalAmount, String reason) {
        try {
            // Check if payment already exists for this reservation
            Optional<Payment> existingPaymentOpt = getPaymentByReservationId(reservationID);
            
            if (existingPaymentOpt.isPresent()) {
                // Update existing payment by adding the additional amount
                Payment existingPayment = existingPaymentOpt.get();
                BigDecimal newTotalAmount = existingPayment.getAmount().add(additionalAmount);
                
                existingPayment.setAmount(newTotalAmount);
                existingPayment.setUpdatedAt(LocalDateTime.now());
                // Keep status as is, will be updated by completePayment method
                
                Payment savedPayment = paymentRepository.save(existingPayment);
                System.out.println("Payment updated with additional amount: " + savedPayment.getPaymentID() + 
                                 " - New total: $" + newTotalAmount + " (Added: $" + additionalAmount + ")");
                System.out.println("Reason: " + reason);
                
                return savedPayment;
            } else {
                // Create new payment if none exists
                Payment newPayment = new Payment();
                newPayment.setReservationID(reservationID);
                newPayment.setAmount(additionalAmount);
                newPayment.setMethod("ONLINE");
                newPayment.setStatus("PENDING"); // Will be updated by completePayment method
                newPayment.setDate(LocalDateTime.now());
                newPayment.setCreatedAt(LocalDateTime.now());
                
                Payment savedPayment = paymentRepository.save(newPayment);
                System.out.println("New payment created: " + savedPayment.getPaymentID() + " for $" + additionalAmount);
                System.out.println("Reason: " + reason);
                
                return savedPayment;
            }
            
        } catch (Exception e) {
            System.err.println("Error creating additional payment: " + e.getMessage());
            throw new RuntimeException("Failed to create additional payment", e);
        }
    }
    
    // Process refund for booking modifications
    public boolean processRefund(Long reservationID, BigDecimal refundAmount, String reason) {
        try {
            Optional<Payment> paymentOpt = getPaymentByReservationId(reservationID);
            if (!paymentOpt.isPresent()) {
                System.err.println("No payment found for refund processing");
                return false;
            }
            
            Payment payment = paymentOpt.get();
            
            // Update existing payment by subtracting the refund amount
            BigDecimal newAmount = payment.getAmount().subtract(refundAmount);
            
            // Ensure the amount doesn't go below zero
            if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
                newAmount = BigDecimal.ZERO;
            }
            
            payment.setAmount(newAmount);
            payment.setUpdatedAt(LocalDateTime.now());
            payment.setStatus("REFUNDED");
            
            Payment savedPayment = paymentRepository.save(payment);
            System.out.println("Refund processed: Payment " + savedPayment.getPaymentID() + 
                             " - New amount: $" + newAmount + " (Refunded: $" + refundAmount + ")");
            System.out.println("Reason: " + reason);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error processing refund: " + e.getMessage());
            return false;
        }
    }
    
    // Update original payment amount after adjustment
    public boolean updatePaymentAmount(Long reservationID, BigDecimal newAmount) {
        try {
            Optional<Payment> paymentOpt = getPaymentByReservationId(reservationID);
            if (!paymentOpt.isPresent()) {
                return false;
            }
            
            Payment payment = paymentOpt.get();
            payment.setAmount(newAmount);
            payment.setUpdatedAt(LocalDateTime.now());
            
            paymentRepository.save(payment);
            System.out.println("Payment amount updated to: $" + newAmount);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Error updating payment amount: " + e.getMessage());
            return false;
        }
    }
    
    // Inner class for payment adjustment details
    public static class PaymentAdjustment implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private Long reservationID;
        private BigDecimal originalAmount;
        private BigDecimal newAmount;
        private BigDecimal adjustmentAmount;
        private String adjustmentType;
        private boolean requiresPayment;
        // Store payment ID instead of the entire Payment object to avoid serialization issues
        private Long currentPaymentId;
        
        // Getters and setters
        public Long getReservationID() { return reservationID; }
        public void setReservationID(Long reservationID) { this.reservationID = reservationID; }
        
        public BigDecimal getOriginalAmount() { return originalAmount; }
        public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
        
        public BigDecimal getNewAmount() { return newAmount; }
        public void setNewAmount(BigDecimal newAmount) { this.newAmount = newAmount; }
        
        public BigDecimal getAdjustmentAmount() { return adjustmentAmount; }
        public void setAdjustmentAmount(BigDecimal adjustmentAmount) { this.adjustmentAmount = adjustmentAmount; }
        
        public String getAdjustmentType() { return adjustmentType; }
        public void setAdjustmentType(String adjustmentType) { this.adjustmentType = adjustmentType; }
        
        public boolean isRequiresPayment() { return requiresPayment; }
        public void setRequiresPayment(boolean requiresPayment) { this.requiresPayment = requiresPayment; }
        
        public Long getCurrentPaymentId() { return currentPaymentId; }
        public void setCurrentPaymentId(Long currentPaymentId) { this.currentPaymentId = currentPaymentId; }
        
        public boolean isAdditionalPaymentRequired() {
            return "ADDITIONAL_PAYMENT".equals(adjustmentType) && requiresPayment;
        }
        
        public boolean isRefundDue() {
            return "REFUND".equals(adjustmentType);
        }
        
        public BigDecimal getAbsoluteAdjustmentAmount() {
            return adjustmentAmount != null ? adjustmentAmount.abs() : BigDecimal.ZERO;
        }
    }
}
