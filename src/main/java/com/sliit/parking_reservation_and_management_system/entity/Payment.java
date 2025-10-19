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
    private Integer paymentID;

    @OneToOne
    @JoinColumn(name = "ReservationID", nullable = false)
    private Reservation reservation;

    @Column(name = "Amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "Status", nullable = false)
    private String status; // e.g., COMPLETED, PENDING, FAILED, REFUNDED

    @Column(name = "PaymentDate", nullable = false)
    private LocalDateTime paymentDate;

    // 🆕 START: Add new fields for refunds
    @Column(name = "RefundStatus")
    private String refundStatus; // e.g., NONE, PROCESSED

    @Column(name = "RefundDate")
    private LocalDateTime refundDate;
    // 🆕 END: Add new fields for refunds


    // Getters and Setters for all fields
    public Integer getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(Integer paymentID) {
        this.paymentID = paymentID;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    // 🆕 START: Add getters and setters for new fields
    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public LocalDateTime getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(LocalDateTime refundDate) {
        this.refundDate = refundDate;
    }
    // 🆕 END: Add getters and setters for new fields
}