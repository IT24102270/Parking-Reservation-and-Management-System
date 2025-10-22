package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.dto.PaymentRequest;
import com.sliit.parking_reservation_and_management_system.dto.PaymentResponse;

/**
 * Strategy interface for different payment processing methods
 * This allows the system to handle various payment types dynamically
 */
public interface PaymentStrategy {
    
    /**
     * Process payment using the specific strategy
     * @param paymentRequest Payment details and amount
     * @return PaymentResponse with success status and transaction details
     */
    PaymentResponse processPayment(PaymentRequest paymentRequest);
    
    /**
     * Get the payment method type this strategy handles
     * @return Payment method type (e.g., "CREDIT_CARD", "PAYPAL", "BANK_TRANSFER")
     */
    String getPaymentMethodType();
    
    /**
     * Validate payment request before processing
     * @param paymentRequest Payment details to validate
     * @return true if valid, false otherwise
     */
    boolean validatePaymentRequest(PaymentRequest paymentRequest);
    
    /**
     * Get processing fee for this payment method
     * @param amount Payment amount
     * @return Processing fee amount
     */
    double getProcessingFee(double amount);
    
    /**
     * Get estimated processing time in minutes
     * @return Processing time in minutes
     */
    int getProcessingTimeMinutes();
}
