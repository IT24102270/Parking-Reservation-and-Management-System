package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "Pricing")
public class Pricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pricingID;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal rateMultiplier;

    // Getters and Setters
    public Integer getPricingID() {
        return pricingID;
    }

    public void setPricingID(Integer pricingID) {
        this.pricingID = pricingID;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getRateMultiplier() {
        return rateMultiplier;
    }

    public void setRateMultiplier(BigDecimal rateMultiplier) {
        this.rateMultiplier = rateMultiplier;
    }
}