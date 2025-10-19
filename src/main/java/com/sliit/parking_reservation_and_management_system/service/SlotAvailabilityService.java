package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.ParkingSlot;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.repository.ParkingSlotRepository;
import com.sliit.parking_reservation_and_management_system.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service to manage automatic slot availability based on reservation times
 * Handles making slots unavailable during bookings and available again after expiry
 */
@Service
@Transactional
public class SlotAvailabilityService {

    @Autowired
    private ParkingSlotRepository parkingSlotRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    /**
     * Check if a slot is available for a specific time period
     * @param slotId The slot to check
     * @param startTime Desired start time
     * @param endTime Desired end time
     * @return true if slot is available, false if occupied
     */
    public boolean isSlotAvailable(Long slotId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // Check if slot exists and is not in maintenance
            Optional<ParkingSlot> slotOpt = parkingSlotRepository.findById(slotId);
            if (!slotOpt.isPresent()) {
                System.err.println("Slot not found: " + slotId);
                return false;
            }
            
            ParkingSlot slot = slotOpt.get();
            if ("MAINTENANCE".equalsIgnoreCase(slot.getStatus())) {
                System.out.println("Slot " + slotId + " is under maintenance");
                return false;
            }
            
            // Check for overlapping reservations
            List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
                slotId, startTime, endTime
            );
            
            // Filter out cancelled reservations
            long activeOverlappingCount = overlappingReservations.stream()
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .count();
            
            boolean isAvailable = activeOverlappingCount == 0;
            
            System.out.println("Slot " + slotId + " availability check:");
            System.out.println("- Time period: " + startTime + " to " + endTime);
            System.out.println("- Overlapping reservations: " + activeOverlappingCount);
            System.out.println("- Available: " + isAvailable);
            
            return isAvailable;
            
        } catch (Exception e) {
            System.err.println("Error checking slot availability: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Make a slot occupied when a reservation starts
     * @param slotId The slot to occupy
     * @param reservationId The reservation causing the occupation
     */
    public void occupySlot(Long slotId, Long reservationId) {
        try {
            Optional<ParkingSlot> slotOpt = parkingSlotRepository.findById(slotId);
            if (slotOpt.isPresent()) {
                ParkingSlot slot = slotOpt.get();
                slot.setStatus("OCCUPIED");
                parkingSlotRepository.save(slot);
                
                System.out.println("Slot " + slotId + " marked as OCCUPIED for reservation " + reservationId);
            }
        } catch (Exception e) {
            System.err.println("Error occupying slot " + slotId + ": " + e.getMessage());
        }
    }
    
    /**
     * Make a slot available when a reservation ends
     * @param slotId The slot to free up
     * @param reservationId The reservation that ended
     */
    public void freeSlot(Long slotId, Long reservationId) {
        try {
            Optional<ParkingSlot> slotOpt = parkingSlotRepository.findById(slotId);
            if (slotOpt.isPresent()) {
                ParkingSlot slot = slotOpt.get();
                
                // Only free the slot if no other active reservations are using it
                LocalDateTime now = LocalDateTime.now();
                List<Reservation> activeReservations = reservationRepository.findActiveReservationsForSlot(slotId, now);
                
                if (activeReservations.isEmpty()) {
                    slot.setStatus("AVAILABLE");
                    parkingSlotRepository.save(slot);
                    System.out.println("Slot " + slotId + " marked as AVAILABLE after reservation " + reservationId + " ended");
                } else {
                    System.out.println("Slot " + slotId + " remains OCCUPIED - " + activeReservations.size() + " active reservations");
                }
            }
        } catch (Exception e) {
            System.err.println("Error freeing slot " + slotId + ": " + e.getMessage());
        }
    }
    
    /**
     * Scheduled task to automatically update slot statuses based on reservation times
     * Runs every 5 minutes to check for reservations that should start or end
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    public void updateSlotStatuses() {
        try {
            LocalDateTime now = LocalDateTime.now();
            System.out.println("=== Running automatic slot status update at " + now + " ===");
            
            // Find reservations that should start now (within last 5 minutes)
            LocalDateTime fiveMinutesAgo = now.minusMinutes(5);
            List<Reservation> startingReservations = reservationRepository.findReservationsStartingBetween(
                fiveMinutesAgo, now
            );
            
            for (Reservation reservation : startingReservations) {
                if ("CONFIRMED".equalsIgnoreCase(reservation.getStatus())) {
                    // Mark slot as occupied and reservation as active
                    occupySlot(reservation.getSlotId(), reservation.getId());
                    
                    // Update reservation status to ACTIVE
                    reservation.setStatus("ACTIVE");
                    reservation.setUpdatedAt(now);
                    reservationRepository.save(reservation);
                    
                    System.out.println("Started reservation " + reservation.getId() + " - Slot " + reservation.getSlotId() + " now OCCUPIED");
                }
            }
            
            // Find reservations that should end now (within last 5 minutes)
            List<Reservation> endingReservations = reservationRepository.findReservationsEndingBetween(
                fiveMinutesAgo, now
            );
            
            for (Reservation reservation : endingReservations) {
                if ("ACTIVE".equalsIgnoreCase(reservation.getStatus())) {
                    // Mark reservation as completed
                    reservation.setStatus("COMPLETED");
                    reservation.setUpdatedAt(now);
                    reservationRepository.save(reservation);
                    
                    // Free up the slot if no other reservations are active
                    freeSlot(reservation.getSlotId(), reservation.getId());
                    
                    System.out.println("Completed reservation " + reservation.getId() + " - Slot " + reservation.getSlotId() + " status updated");
                }
            }
            
            System.out.println("Processed " + startingReservations.size() + " starting and " + 
                             endingReservations.size() + " ending reservations");
            
        } catch (Exception e) {
            System.err.println("Error in automatic slot status update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Manual trigger to update all slot statuses immediately
     * Useful for testing or manual corrections
     */
    public void forceUpdateAllSlotStatuses() {
        try {
            LocalDateTime now = LocalDateTime.now();
            System.out.println("=== Force updating all slot statuses at " + now + " ===");
            
            List<ParkingSlot> allSlots = parkingSlotRepository.findAll();
            
            for (ParkingSlot slot : allSlots) {
                if (!"MAINTENANCE".equalsIgnoreCase(slot.getStatus())) {
                    // Check if slot should be occupied
                    List<Reservation> activeReservations = reservationRepository.findActiveReservationsForSlot(slot.getId(), now);
                    
                    if (!activeReservations.isEmpty()) {
                        if (!"OCCUPIED".equalsIgnoreCase(slot.getStatus())) {
                            slot.setStatus("OCCUPIED");
                            parkingSlotRepository.save(slot);
                            System.out.println("Corrected slot " + slot.getId() + " to OCCUPIED");
                        }
                    } else {
                        if (!"AVAILABLE".equalsIgnoreCase(slot.getStatus())) {
                            slot.setStatus("AVAILABLE");
                            parkingSlotRepository.save(slot);
                            System.out.println("Corrected slot " + slot.getId() + " to AVAILABLE");
                        }
                    }
                }
            }
            
            System.out.println("=== Completed force update of all slot statuses ===");
            
        } catch (Exception e) {
            System.err.println("Error in force slot status update: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get current slot availability statistics
     */
    public SlotAvailabilityStats getAvailabilityStats() {
        try {
            List<ParkingSlot> allSlots = parkingSlotRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            
            long totalSlots = allSlots.size();
            long availableSlots = 0;
            long occupiedSlots = 0;
            long maintenanceSlots = 0;
            
            for (ParkingSlot slot : allSlots) {
                switch (slot.getStatus().toUpperCase()) {
                    case "AVAILABLE":
                        availableSlots++;
                        break;
                    case "OCCUPIED":
                        occupiedSlots++;
                        break;
                    case "MAINTENANCE":
                        maintenanceSlots++;
                        break;
                }
            }
            
            // Count active reservations for verification
            long activeReservations = reservationRepository.countActiveReservations(now);
            
            return new SlotAvailabilityStats(totalSlots, availableSlots, occupiedSlots, maintenanceSlots, activeReservations);
            
        } catch (Exception e) {
            System.err.println("Error getting availability stats: " + e.getMessage());
            return new SlotAvailabilityStats(0, 0, 0, 0, 0);
        }
    }
    
    /**
     * Inner class for availability statistics
     */
    public static class SlotAvailabilityStats {
        public final long totalSlots;
        public final long availableSlots;
        public final long occupiedSlots;
        public final long maintenanceSlots;
        public final long activeReservations;
        
        public SlotAvailabilityStats(long totalSlots, long availableSlots, long occupiedSlots, 
                                   long maintenanceSlots, long activeReservations) {
            this.totalSlots = totalSlots;
            this.availableSlots = availableSlots;
            this.occupiedSlots = occupiedSlots;
            this.maintenanceSlots = maintenanceSlots;
            this.activeReservations = activeReservations;
        }
        
        @Override
        public String toString() {
            return String.format("SlotStats[Total: %d, Available: %d, Occupied: %d, Maintenance: %d, Active Reservations: %d]",
                totalSlots, availableSlots, occupiedSlots, maintenanceSlots, activeReservations);
        }
    }
}
