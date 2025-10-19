package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReservationID")
    private Integer reservationID;

    // ✅ THIS IS THE KEY CHANGE
    @ManyToOne(fetch = FetchType.EAGER) // Change from LAZY (default) to EAGER
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @Column(name = "TotalCost", nullable = false)
    private BigDecimal totalCost;

    @Column(name = "ReservationDate", nullable = false)
    private LocalDateTime reservationDate;

    // --- Getters and Setters ---

    public Integer getReservationID() {
        return reservationID;
    }

    public void setReservationID(Integer reservationID) {
        this.reservationID = reservationID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public LocalDateTime getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(LocalDateTime reservationDate) {
        this.reservationDate = reservationDate;
    }
}