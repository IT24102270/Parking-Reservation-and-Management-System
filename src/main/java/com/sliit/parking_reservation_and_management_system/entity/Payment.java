package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    private Long paymentID;
    
    @Column(name = "ReservationID", nullable = false)
    private Long reservationID;
    
    @Column(name = "Amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "Method", length = 50)
    private String method;
    
    @Column(name = "Date", nullable = false)
    private LocalDateTime date;
    
    @Column(name = "Status", length = 20, nullable = false)
    private String status;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public Payment() {
        this.date = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    public Payment(Long reservationID, BigDecimal amount, String method) {
        this();
        this.reservationID = reservationID;
        this.amount = amount;
        this.method = method;
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
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    public void prePersist() {
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "PENDING";
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Payment{" +
                "paymentID=" + paymentID +
                ", reservationID=" + reservationID +
                ", amount=" + amount +
                ", method='" + method + '\'' +
                ", date=" + date +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
