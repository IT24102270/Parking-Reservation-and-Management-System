package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.strategy.PaymentContext;
import com.sliit.parking_reservation_and_management_system.dto.PaymentRequest;
import com.sliit.parking_reservation_and_management_system.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Test controller to demonstrate Strategy Pattern implementation
 * This controller provides endpoints to test different payment strategies
 */
@RestController
@RequestMapping("/test/payment-strategy")
public class PaymentStrategyTestController {
    
    @Autowired
    private PaymentContext paymentContext;
    
    /**
     * Test all payment strategies with sample data
     */
    @GetMapping("/test-all")
    public String testAllStrategies() {
        StringBuilder result = new StringBuilder();
        result.append("<h1>🧪 Strategy Pattern Test - Payment Processing</h1>");
        result.append("<p>Testing all available payment strategies...</p>");
        
        // Test Credit Card
        result.append(testPaymentStrategy("CREDIT_CARD", createCreditCardRequest()));
        
        // Test Debit Card
        result.append(testPaymentStrategy("DEBIT_CARD", createDebitCardRequest()));
        
        // Test PayPal
        result.append(testPaymentStrategy("PAYPAL", createPayPalRequest()));
        
        // Test Bank Transfer
        result.append(testPaymentStrategy("BANK_TRANSFER", createBankTransferRequest()));
        
        // Test Digital Wallet
        result.append(testPaymentStrategy("DIGITAL_WALLET", createDigitalWalletRequest()));
        
        result.append("<hr>");
        result.append("<h2>📊 Strategy Information</h2>");
        result.append(getStrategyInfo());
        
        return result.toString();
    }
    
    /**
     * Test specific payment strategy
     */
    @GetMapping("/test/{method}")
    public String testSpecificStrategy(@PathVariable String method) {
        PaymentRequest request = createRequestForMethod(method);
        return testPaymentStrategy(method.toUpperCase(), request);
    }
    
    /**
     * Get strategy information
     */
    @GetMapping("/info")
    public String getStrategyInfo() {
        try {
            Map<String, Object> strategyInfo = paymentContext.getStrategyInfo();
            
            StringBuilder info = new StringBuilder();
            info.append("<h3>Current Strategy: ").append(strategyInfo.get("currentStrategy")).append("</h3>");
            info.append("<h3>Available Methods:</h3><ul>");
            
            String[] methods = (String[]) strategyInfo.get("availableMethods");
            for (String method : methods) {
                info.append("<li>").append(method).append("</li>");
            }
            info.append("</ul>");
            
            return info.toString();
        } catch (Exception e) {
            return "<p>Error getting strategy info: " + e.getMessage() + "</p>";
        }
    }
    
    /**
     * Test a specific payment strategy
     */
    private String testPaymentStrategy(String method, PaymentRequest request) {
        StringBuilder result = new StringBuilder();
        result.append("<div style='border: 1px solid #ccc; margin: 10px; padding: 10px;'>");
        result.append("<h3>🔄 Testing ").append(method).append(" Strategy</h3>");
        
        try {
            // Set the strategy
            paymentContext.setPaymentStrategy(method);
            
            // Process payment
            PaymentResponse response = paymentContext.processPayment(request);
            
            if (response.isSuccess()) {
                result.append("<p style='color: green;'>✅ <strong>SUCCESS</strong></p>");
                result.append("<p><strong>Transaction ID:</strong> ").append(response.getTransactionId()).append("</p>");
                result.append("<p><strong>Amount:</strong> $").append(response.getAmount()).append("</p>");
                result.append("<p><strong>Processing Fee:</strong> $").append(response.getProcessingFee()).append("</p>");
                result.append("<p><strong>Message:</strong> ").append(response.getMessage()).append("</p>");
            } else {
                result.append("<p style='color: red;'>❌ <strong>FAILED</strong></p>");
                result.append("<p><strong>Error Code:</strong> ").append(response.getErrorCode()).append("</p>");
                result.append("<p><strong>Error Message:</strong> ").append(response.getMessage()).append("</p>");
            }
            
        } catch (Exception e) {
            result.append("<p style='color: red;'>❌ <strong>ERROR:</strong> ").append(e.getMessage()).append("</p>");
        }
        
        result.append("</div>");
        return result.toString();
    }
    
    /**
     * Create credit card payment request
     */
    private PaymentRequest createCreditCardRequest() {
        PaymentRequest request = new PaymentRequest("CREDIT_CARD", 100.0, "USD");
        request.setCardNumber("4111111111111111");
        request.setCardHolderName("John Doe");
        request.setExpiryDate("12/25");
        request.setCvv("123");
        request.setDescription("Test credit card payment");
        return request;
    }
    
    /**
     * Create debit card payment request
     */
    private PaymentRequest createDebitCardRequest() {
        PaymentRequest request = new PaymentRequest("DEBIT_CARD", 100.0, "USD");
        request.setCardNumber("5555555555554444");
        request.setCardHolderName("Jane Smith");
        request.setExpiryDate("06/26");
        request.setCvv("456");
        request.setDescription("Test debit card payment");
        return request;
    }
    
    /**
     * Create PayPal payment request
     */
    private PaymentRequest createPayPalRequest() {
        PaymentRequest request = new PaymentRequest("PAYPAL", 100.0, "USD");
        request.setPaypalEmail("test@paypal.com");
        request.setDescription("Test PayPal payment");
        return request;
    }
    
    /**
     * Create bank transfer payment request
     */
    private PaymentRequest createBankTransferRequest() {
        PaymentRequest request = new PaymentRequest("BANK_TRANSFER", 100.0, "USD");
        request.setBankAccountNumber("1234567890");
        request.setBankRoutingNumber("123456789");
        request.setDescription("Test bank transfer payment");
        return request;
    }
    
    /**
     * Create digital wallet payment request
     */
    private PaymentRequest createDigitalWalletRequest() {
        PaymentRequest request = new PaymentRequest("DIGITAL_WALLET", 100.0, "USD");
        request.setDescription("Test digital wallet payment");
        return request;
    }
    
    /**
     * Create request for specific method
     */
    private PaymentRequest createRequestForMethod(String method) {
        switch (method.toUpperCase()) {
            case "CREDIT_CARD":
                return createCreditCardRequest();
            case "DEBIT_CARD":
                return createDebitCardRequest();
            case "PAYPAL":
                return createPayPalRequest();
            case "BANK_TRANSFER":
                return createBankTransferRequest();
            case "DIGITAL_WALLET":
                return createDigitalWalletRequest();
            default:
                return new PaymentRequest(method.toUpperCase(), 100.0, "USD");
        }
    }
}
