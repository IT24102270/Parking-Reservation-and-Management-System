package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    
    List<ParkingSlot> findByStatus(String status);
    
    @Query("SELECT COUNT(p) FROM ParkingSlot p WHERE p.status = 'AVAILABLE'")
    Long countAvailableSlots();
    
    @Query("SELECT COUNT(p) FROM ParkingSlot p WHERE p.status = 'OCCUPIED'")
    Long countOccupiedSlots();
    
    @Query("SELECT p FROM ParkingSlot p WHERE p.status = 'AVAILABLE' ORDER BY p.id")
    List<ParkingSlot> findAvailableSlots();
}
