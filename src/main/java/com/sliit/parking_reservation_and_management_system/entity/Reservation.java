package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reservation")
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReservationID")
    private Long id;
    
    @Column(name = "UserID", nullable = false)
    private Long userId;
    
    @Column(name = "SlotID", nullable = false)
    private Long slotId;
    
    @Column(name = "StartTime", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "EndTime", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "Status", nullable = false)
    private String status = "PENDING"; // PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "VehicleNumber")
    private String vehicleNumber;
    
    // Constructors
    public Reservation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public Reservation(Long userId, Long slotId, LocalDateTime startTime, LocalDateTime endTime) {
        this();
        this.userId = userId;
        this.slotId = slotId;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getSlotId() {
        return slotId;
    }
    
    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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
    
    public String getVehicleNumber() {
        return vehicleNumber;
    }
    
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
    
    
    // Helper method to generate booking ID
    public String getBookingId() {
        if (id != null && createdAt != null) {
            return String.format("BK-%s-%d", 
                createdAt.toLocalDate().toString().replace("-", ""), 
                id);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", userId=" + userId +
                ", slotId=" + slotId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", status='" + status + '\'' +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                '}';
    }
}
