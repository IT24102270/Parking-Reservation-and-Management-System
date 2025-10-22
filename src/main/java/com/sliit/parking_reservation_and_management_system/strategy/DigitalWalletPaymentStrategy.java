package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.dto.PaymentRequest;
import com.sliit.parking_reservation_and_management_system.dto.PaymentResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Concrete strategy for Digital Wallet payment processing
 */
@Component
public class DigitalWalletPaymentStrategy implements PaymentStrategy {
    
    private static final String PAYMENT_METHOD_TYPE = "DIGITAL_WALLET";
    
    
    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        try {
            // Validate payment request
            if (!validatePaymentRequest(paymentRequest)) {
                return PaymentResponse.failure("INVALID_WALLET_DETAILS", "Invalid digital wallet information provided");
            }
            
            // Simulate digital wallet processing
            System.out.println("📱 Processing Digital Wallet Payment:");
            System.out.println("   Amount: $" + paymentRequest.getAmount());
            
            // Simulate processing delay
            Thread.sleep(1500);
            
            // Simulate success/failure (88% success rate for demo)
            boolean isSuccessful = Math.random() > 0.12;
            
            if (isSuccessful) {
                String transactionId = "DW_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                
                PaymentResponse response = PaymentResponse.success(
                    transactionId,
                    "Digital wallet payment processed successfully",
                    PAYMENT_METHOD_TYPE,
                    paymentRequest.getAmount()
                );
                response.setCurrency(paymentRequest.getCurrency());
                
                System.out.println("✅ Digital Wallet Payment Successful - Transaction ID: " + transactionId);
                return response;
            } else {
                System.out.println("❌ Digital Wallet Payment Failed - Insufficient funds or wallet declined");
                return PaymentResponse.failure("PAYMENT_DECLINED", "Digital wallet payment was declined");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Digital Wallet Payment Error: " + e.getMessage());
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
}
