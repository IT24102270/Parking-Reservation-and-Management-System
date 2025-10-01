package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Entity
@Table(name = "ParkingSlot")
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slotId")
    private Integer id;

    @Column(name = "location")
    private String location; // Mapped to vehicleType in form (e.g., "FourWheeler")

    @Column(name = "type")
    private String type; // Mapped to slotType in form (e.g., "VIP")

    @Column(name = "status")
    private String status;

    @Column(name = "hourlyRate")
    private double hourlyRate;

    @CreationTimestamp
    @Column(name = "createdAt", nullable = true)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updatedAt", nullable = true)
    private Date updatedAt;

    // Default constructor
    public Slot() {}

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}