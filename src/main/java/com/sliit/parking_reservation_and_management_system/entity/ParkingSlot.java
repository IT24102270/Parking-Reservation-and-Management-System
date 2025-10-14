package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ParkingSlot")
public class ParkingSlot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SlotID")
    private Long id;
    
    @Column(name = "Location")
    private String location;
    
    @Column(name = "Status", nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE, OCCUPIED, MAINTENANCE
    
    // Constructors
    public ParkingSlot() {
    }
    
    public ParkingSlot(String location) {
        this.location = location;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "ParkingSlot{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
