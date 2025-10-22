package com.sliit.parking_reservation_and_management_system.dto;

import java.time.LocalDateTime;

/**
 * DTO for payment response data
 */
public class PaymentResponse {
    
    private boolean success;
    private String transactionId;
    private String message;
    private String paymentMethod;
    private double amount;
    private double processingFee;
    private String currency;
    private LocalDateTime timestamp;
    private String errorCode;
    private String errorMessage;
    
    // Constructors
    public PaymentResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    public PaymentResponse(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }
    
    public PaymentResponse(boolean success, String transactionId, String message, String paymentMethod, double amount) {
        this(success, message);
        this.transactionId = transactionId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }
    
    // Static factory methods for common responses
    public static PaymentResponse success(String transactionId, String message, String paymentMethod, double amount) {
        return new PaymentResponse(true, transactionId, message, paymentMethod, amount);
    }
    
    public static PaymentResponse failure(String message) {
        return new PaymentResponse(false, message);
    }
    
    public static PaymentResponse failure(String errorCode, String errorMessage) {
        PaymentResponse response = new PaymentResponse(false, errorMessage);
        response.setErrorCode(errorCode);
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public double getProcessingFee() {
        return processingFee;
    }
    
    public void setProcessingFee(double processingFee) {
        this.processingFee = processingFee;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    @Override
    public String toString() {
        return "PaymentResponse{" +
                "success=" + success +
                ", transactionId='" + transactionId + '\'' +
                ", message='" + message + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", amount=" + amount +
                ", processingFee=" + processingFee +
                ", currency='" + currency + '\'' +
                ", timestamp=" + timestamp +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}
