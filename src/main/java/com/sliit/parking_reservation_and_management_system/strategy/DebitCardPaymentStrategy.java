package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.dto.PaymentRequest;
import com.sliit.parking_reservation_and_management_system.dto.PaymentResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Concrete strategy for Debit Card payment processing
 */
@Component
public class DebitCardPaymentStrategy implements PaymentStrategy {
    
    private static final String PAYMENT_METHOD_TYPE = "DEBIT_CARD";
    
    
    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        try {
            // Validate payment request
            if (!validatePaymentRequest(paymentRequest)) {
                return PaymentResponse.failure("INVALID_CARD_DETAILS", "Invalid debit card information provided");
            }
            
            // Simulate debit card processing
            System.out.println("💳 Processing Debit Card Payment:");
            System.out.println("   Card Number: " + maskCardNumber(paymentRequest.getCardNumber()));
            System.out.println("   Card Holder: " + paymentRequest.getCardHolderName());
            System.out.println("   Amount: $" + paymentRequest.getAmount());
            
            // Simulate processing delay
            Thread.sleep(1000);
            
            // Simulate success/failure (90% success rate for demo)
            boolean isSuccessful = Math.random() > 0.1;
            
            if (isSuccessful) {
                String transactionId = "DC_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                
                PaymentResponse response = PaymentResponse.success(
                    transactionId,
                    "Debit card payment processed successfully",
                    PAYMENT_METHOD_TYPE,
                    paymentRequest.getAmount()
                );
                response.setCurrency(paymentRequest.getCurrency());
                
                System.out.println("✅ Debit Card Payment Successful - Transaction ID: " + transactionId);
                return response;
            } else {
                System.out.println("❌ Debit Card Payment Failed - Insufficient funds or card declined");
                return PaymentResponse.failure("PAYMENT_DECLINED", "Debit card payment was declined");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Debit Card Payment Error: " + e.getMessage());
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
     * Mask debit card number for security
     * @param cardNumber Original card number
     * @return Masked card number
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        
        String cleaned = cardNumber.replaceAll("\\s", "");
        if (cleaned.length() < 8) {
            return "****" + cleaned.substring(cleaned.length() - 4);
        }
        
        return "****-****-****-" + cleaned.substring(cleaned.length() - 4);
    }
}
