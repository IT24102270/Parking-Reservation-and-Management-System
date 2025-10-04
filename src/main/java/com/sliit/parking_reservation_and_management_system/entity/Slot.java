package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
// Removed timestamp imports as they're no longer needed

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

    // Removed timestamp columns as they don't exist in the database

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

    // Removed getter and setter methods for timestamp columns
}