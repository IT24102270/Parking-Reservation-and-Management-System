package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.dto.PaymentRequest;
import com.sliit.parking_reservation_and_management_system.dto.PaymentResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Concrete strategy for Bank Transfer payment processing
 */
@Component
public class BankTransferPaymentStrategy implements PaymentStrategy {
    
    private static final String PAYMENT_METHOD_TYPE = "BANK_TRANSFER";
    
    
    @Override
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        try {
            // Validate payment request
            if (!validatePaymentRequest(paymentRequest)) {
                return PaymentResponse.failure("INVALID_BANK_DETAILS", "Invalid bank account information provided");
            }
            
            // Simulate bank transfer processing
            System.out.println("🏦 Processing Bank Transfer Payment:");
            System.out.println("   Account Number: " + maskAccountNumber(paymentRequest.getBankAccountNumber()));
            System.out.println("   Routing Number: " + maskRoutingNumber(paymentRequest.getBankRoutingNumber()));
            System.out.println("   Amount: $" + paymentRequest.getAmount());
            
            // Simulate processing delay (bank transfers take longer)
            Thread.sleep(3000);
            
            // Simulate bank verification
            boolean isBankVerified = simulateBankVerification(
                paymentRequest.getBankAccountNumber(), 
                paymentRequest.getBankRoutingNumber()
            );
            
            if (!isBankVerified) {
                System.out.println("❌ Bank Transfer Failed - Invalid bank account");
                return PaymentResponse.failure("INVALID_ACCOUNT", "Bank account verification failed");
            }
            
            // Simulate success/failure (95% success rate for demo)
            boolean isSuccessful = Math.random() > 0.05;
            
            if (isSuccessful) {
                String transactionId = "BT_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                
                PaymentResponse response = PaymentResponse.success(
                    transactionId,
                    "Bank transfer payment processed successfully",
                    PAYMENT_METHOD_TYPE,
                    paymentRequest.getAmount()
                );
                response.setCurrency(paymentRequest.getCurrency());
                
                System.out.println("✅ Bank Transfer Payment Successful - Transaction ID: " + transactionId);
                return response;
            } else {
                System.out.println("❌ Bank Transfer Failed - Insufficient funds");
                return PaymentResponse.failure("INSUFFICIENT_FUNDS", "Insufficient funds in bank account");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Bank Transfer Payment Error: " + e.getMessage());
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
     * Simulate bank account verification
     * @param accountNumber Bank account number
     * @param routingNumber Bank routing number
     * @return true if verification successful, false otherwise
     */
    private boolean simulateBankVerification(String accountNumber, String routingNumber) {
        try {
            // Simulate verification delay
            Thread.sleep(1000);
            
            // Simulate verification success (90% success rate for demo)
            return Math.random() > 0.1;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    
    /**
     * Mask bank account number for security
     * @param accountNumber Original account number
     * @return Masked account number
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        
        String cleaned = accountNumber.replaceAll("\\s", "");
        if (cleaned.length() < 8) {
            return "****" + cleaned.substring(cleaned.length() - 4);
        }
        
        return "****" + cleaned.substring(cleaned.length() - 4);
    }
    
    /**
     * Mask routing number for security
     * @param routingNumber Original routing number
     * @return Masked routing number
     */
    private String maskRoutingNumber(String routingNumber) {
        if (routingNumber == null || routingNumber.length() < 4) {
            return "****";
        }
        
        String cleaned = routingNumber.replaceAll("\\s", "");
        if (cleaned.length() < 6) {
            return "****" + cleaned.substring(cleaned.length() - 2);
        }
        
        return "****" + cleaned.substring(cleaned.length() - 2);
    }
}
