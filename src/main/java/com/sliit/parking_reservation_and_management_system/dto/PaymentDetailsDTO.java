package com.sliit.parking_reservation_and_management_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDetailsDTO {
    private Long paymentID;
    private Long reservationID;
    private BigDecimal amount;
    private String method;
    private LocalDateTime date;
    private String status;
    private String customerName;
    private String customerEmail;
    
    // Constructors
    public PaymentDetailsDTO() {}
    
    public PaymentDetailsDTO(Long paymentID, Long reservationID, BigDecimal amount, 
                           String method, LocalDateTime date, String status, 
                           String customerName, String customerEmail) {
        this.paymentID = paymentID;
        this.reservationID = reservationID;
        this.amount = amount;
        this.method = method;
        this.date = date;
        this.status = status;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
    }
    
    // Getters and Setters
    public Long getPaymentID() {
        return paymentID;
    }
    
    public void setPaymentID(Long paymentID) {
        this.paymentID = paymentID;
    }
    
    public Long getReservationID() {
        return reservationID;
    }
    
    public void setReservationID(Long reservationID) {
        this.reservationID = reservationID;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getMethod() {
        return method;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getCustomerEmail() {
        return customerEmail;
    }
    
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }
}
