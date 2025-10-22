package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.dto.PaymentRequest;
import com.sliit.parking_reservation_and_management_system.dto.PaymentResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Concrete strategy for PayPal payment processing
 */
@Component
public class PayPalPaymentStrategy implements PaymentStrategy {
    
    private static final String PAYMENT_METHOD_TYPE = "PAYPAL";
    
    
    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        try {
            // Validate payment request
            if (!validatePaymentRequest(paymentRequest)) {
                return PaymentResponse.failure("INVALID_PAYPAL_DETAILS", "Invalid PayPal information provided");
            }
            
            // Simulate PayPal processing
            System.out.println("🅿️ Processing PayPal Payment:");
            System.out.println("   PayPal Email: " + paymentRequest.getPaypalEmail());
            System.out.println("   Amount: $" + paymentRequest.getAmount());
            
            // Simulate processing delay
            Thread.sleep(2000);
            
            // Simulate PayPal authentication and payment
            boolean isAuthenticated = simulatePayPalAuthentication(paymentRequest.getPaypalEmail());
            if (!isAuthenticated) {
                System.out.println("❌ PayPal Payment Failed - Authentication failed");
                return PaymentResponse.failure("AUTHENTICATION_FAILED", "PayPal authentication failed");
            }
            
            // Simulate success/failure (85% success rate for demo)
            boolean isSuccessful = Math.random() > 0.15;
            
            if (isSuccessful) {
                String transactionId = "PP_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                
                PaymentResponse response = PaymentResponse.success(
                    transactionId,
                    "PayPal payment processed successfully",
                    PAYMENT_METHOD_TYPE,
                    paymentRequest.getAmount()
                );
                response.setCurrency(paymentRequest.getCurrency());
                
                System.out.println("✅ PayPal Payment Successful - Transaction ID: " + transactionId);
                return response;
            } else {
                System.out.println("❌ PayPal Payment Failed - Insufficient funds or payment declined");
                return PaymentResponse.failure("PAYMENT_DECLINED", "PayPal payment was declined");
            }
            
        } catch (Exception e) {
            System.err.println("❌ PayPal Payment Error: " + e.getMessage());
            return PaymentResponse.failure("PROCESSING_ERROR", "An error occurred while processing the payment");
        }
    }
    
    @Override
    public String getPaymentMethodType() {
        return PAYMENT_METHOD_TYPE;
    }
    
    @Override
    public boolean validatePaymentRequest(PaymentRequest paymentRequest) {
        if (paymentRequest == null) {
            return false;
        }
        
        // Only check if amount is positive
        if (paymentRequest.getAmount() <= 0) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public double getProcessingFee(double amount) {
        return 0.0; // No processing fee
    }
    
    @Override
    public int getProcessingTimeMinutes() {
        return 0; // Instant processing
    }
    
    /**
     * Simulate PayPal authentication process
     * @param email PayPal email address
     * @return true if authentication successful, false otherwise
     */
    private boolean simulatePayPalAuthentication(String email) {
        try {
            // Simulate authentication delay
            Thread.sleep(500);
            
            // Simulate authentication success (90% success rate for demo)
            return Math.random() > 0.1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
}
