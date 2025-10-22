package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.dto.PaymentRequest;
import com.sliit.parking_reservation_and_management_system.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context class that uses the Strategy pattern for payment processing
 * This class maintains a reference to a PaymentStrategy and delegates the work to it
 */
@Component
public class PaymentContext {
    
    private PaymentStrategy paymentStrategy;
    private final Map<String, PaymentStrategy> strategyMap;
    
    @Autowired
    public PaymentContext(List<PaymentStrategy> strategies) {
        this.strategyMap = new HashMap<>();
        
        // Initialize strategy map
        for (PaymentStrategy strategy : strategies) {
            strategyMap.put(strategy.getPaymentMethodType(), strategy);
        }
        
        // Set default strategy to Credit Card
        this.paymentStrategy = strategyMap.get("CREDIT_CARD");
    }
    
    /**
     * Set the payment strategy at runtime
     * @param paymentMethodType The type of payment method (CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER, DIGITAL_WALLET)
     */
    public void setPaymentStrategy(String paymentMethodType) {
        PaymentStrategy strategy = strategyMap.get(paymentMethodType);
        if (strategy != null) {
            this.paymentStrategy = strategy;
            System.out.println("🔄 Payment strategy changed to: " + paymentMethodType);
        } else {
            throw new IllegalArgumentException("Unsupported payment method: " + paymentMethodType);
        }
    }
    
    /**
     * Set the payment strategy directly
     * @param paymentStrategy The payment strategy to use
     */
    public void setPaymentStrategy(PaymentStrategy paymentStrategy) {
        if (paymentStrategy != null) {
            this.paymentStrategy = paymentStrategy;
            System.out.println("🔄 Payment strategy changed to: " + paymentStrategy.getPaymentMethodType());
        } else {
            throw new IllegalArgumentException("Payment strategy cannot be null");
        }
    }
    
    /**
     * Process payment using the current strategy
     * @param paymentRequest Payment details
     * @return PaymentResponse with result
     */
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        if (paymentStrategy == null) {
            return PaymentResponse.failure("NO_STRATEGY", "No payment strategy is set");
        }
        
        if (paymentRequest == null) {
            return PaymentResponse.failure("INVALID_REQUEST", "Payment request cannot be null");
        }
        
        // Set the payment method type in the request
        paymentRequest.setPaymentMethod(paymentStrategy.getPaymentMethodType());
        
        System.out.println("💳 Processing payment using " + paymentStrategy.getPaymentMethodType() + " strategy");
        System.out.println("   Amount: $" + paymentRequest.getAmount());
        
        return paymentStrategy.processPayment(paymentRequest);
    }
    
    /**
     * Process payment with automatic strategy selection based on payment method
     * @param paymentRequest Payment details (must include paymentMethod)
     * @return PaymentResponse with result
     */
    public PaymentResponse processPaymentWithAutoStrategy(PaymentRequest paymentRequest) {
        if (paymentRequest == null || paymentRequest.getPaymentMethod() == null) {
            return PaymentResponse.failure("INVALID_REQUEST", "Payment request and method must be specified");
        }
        
        String paymentMethod = paymentRequest.getPaymentMethod().toUpperCase();
        PaymentStrategy strategy = strategyMap.get(paymentMethod);
        
        if (strategy == null) {
            return PaymentResponse.failure("UNSUPPORTED_METHOD", "Unsupported payment method: " + paymentMethod);
        }
        
        // Temporarily set the strategy
        PaymentStrategy originalStrategy = this.paymentStrategy;
        this.paymentStrategy = strategy;
        
        try {
            return processPayment(paymentRequest);
        } finally {
            // Restore original strategy
            this.paymentStrategy = originalStrategy;
        }
    }
    
    /**
     * Get the current payment strategy
     * @return Current PaymentStrategy
     */
    public PaymentStrategy getCurrentStrategy() {
        return paymentStrategy;
    }
    
    /**
     * Get the current payment method type
     * @return Current payment method type
     */
    public String getCurrentPaymentMethodType() {
        return paymentStrategy != null ? paymentStrategy.getPaymentMethodType() : "NONE";
    }
    
    /**
     * Get all available payment methods
     * @return Array of available payment method types
     */
    public String[] getAvailablePaymentMethods() {
        return strategyMap.keySet().toArray(new String[0]);
    }
    
    /**
     * Check if a payment method is supported
     * @param paymentMethodType Payment method type to check
     * @return true if supported, false otherwise
     */
    public boolean isPaymentMethodSupported(String paymentMethodType) {
        return strategyMap.containsKey(paymentMethodType);
    }
    
    /**
     * Get processing fee for a specific payment method
     * @param paymentMethodType Payment method type
     * @param amount Payment amount
     * @return Processing fee
     */
    public double getProcessingFee(String paymentMethodType, double amount) {
        PaymentStrategy strategy = strategyMap.get(paymentMethodType);
        if (strategy != null) {
            return strategy.getProcessingFee(amount);
        }
        return 0.0;
    }
    
    /**
     * Get processing time for a specific payment method
     * @param paymentMethodType Payment method type
     * @return Processing time in minutes
     */
    public int getProcessingTime(String paymentMethodType) {
        PaymentStrategy strategy = strategyMap.get(paymentMethodType);
        if (strategy != null) {
            return strategy.getProcessingTimeMinutes();
        }
        return 0;
    }
    
    /**
     * Validate payment request for a specific payment method
     * @param paymentMethodType Payment method type
     * @param paymentRequest Payment request to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePaymentRequest(String paymentMethodType, PaymentRequest paymentRequest) {
        PaymentStrategy strategy = strategyMap.get(paymentMethodType);
        if (strategy != null) {
            return strategy.validatePaymentRequest(paymentRequest);
        }
        return false;
    }
    
    /**
     * Get strategy information
     * @return Map with strategy information
     */
    public Map<String, Object> getStrategyInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("currentStrategy", getCurrentPaymentMethodType());
        info.put("availableMethods", getAvailablePaymentMethods());
        
        Map<String, Object> methodDetails = new HashMap<>();
        for (Map.Entry<String, PaymentStrategy> entry : strategyMap.entrySet()) {
            Map<String, Object> details = new HashMap<>();
            details.put("processingTime", "Instant");
            details.put("processingFeeRate", "0%");
            methodDetails.put(entry.getKey(), details);
        }
        info.put("methodDetails", methodDetails);
        
        return info;
    }
}
