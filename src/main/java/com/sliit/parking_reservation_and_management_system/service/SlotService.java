// SlotService.java
package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.Slot;
import com.sliit.parking_reservation_and_management_system.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SlotService {

    @Autowired
    private SlotRepository slotRepository;

    private static final int MAX_SLOTS_PER_LOCATION = 20;

    public List<Slot> getSlotsByLocation(String location) {
        return slotRepository.findAll().stream()
                .filter(slot -> location.equals(slot.getLocation()))
                .collect(Collectors.toList());
    }
    
    public List<Slot> getAllSlots() {
        return slotRepository.findAll();
    }

    public Optional<Slot> getSlotById(Integer id) {
        return slotRepository.findById(id);
    }

    public Slot saveSlot(Slot slot) throws IllegalArgumentException {
        // Check if adding a new slot would exceed the limit
        if (slot.getId() == null) {
            long currentCount = slotRepository.findAll().stream()
                    .filter(s -> slot.getLocation().equals(s.getLocation()))
                    .count();
            if (currentCount >= MAX_SLOTS_PER_LOCATION) {
                throw new IllegalArgumentException("Maximum slots (" + MAX_SLOTS_PER_LOCATION + ") reached for location: " + slot.getLocation());
            }
        }
        // Additional validation
        if (slot.getHourlyRate() <= 0) {
            throw new IllegalArgumentException("Hourly rate must be positive");
        }
        if (!List.of("available", "occupied", "maintenance").contains(slot.getStatus())) {
            throw new IllegalArgumentException("Invalid status");
        }
        return slotRepository.save(slot);
    }
    
    // Bulk operations for comprehensive slot management
    public int deleteSlots(List<Integer> slotIds) {
        int deletedCount = 0;
        for (Integer slotId : slotIds) {
            try {
                if (slotRepository.existsById(slotId)) {
                    slotRepository.deleteById(slotId);
                    deletedCount++;
                }
            } catch (Exception e) {
                System.err.println("Error deleting slot " + slotId + ": " + e.getMessage());
            }
        }
        return deletedCount;
    }
    
    public boolean deleteSlot(Integer slotId) {
        try {
            if (slotRepository.existsById(slotId)) {
                slotRepository.deleteById(slotId);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting slot " + slotId + ": " + e.getMessage());
            return false;
        }
    }
    
    public int bulkAddSlots(String location, String type, int count, double hourlyRate) {
        int addedCount = 0;
        
        // Check current count to respect the limit
        long currentCount = slotRepository.findAll().stream()
                .filter(s -> location.equals(s.getLocation()))
                .count();
        
        int maxToAdd = (int) Math.min(count, MAX_SLOTS_PER_LOCATION - currentCount);
        
        for (int i = 0; i < maxToAdd; i++) {
            try {
                Slot newSlot = new Slot();
                newSlot.setLocation(location);
                newSlot.setType(type);
                newSlot.setStatus("AVAILABLE");
                newSlot.setHourlyRate(hourlyRate);
                
                slotRepository.save(newSlot);
                addedCount++;
            } catch (Exception e) {
                System.err.println("Error adding slot " + (i + 1) + ": " + e.getMessage());
                break;
            }
        }
        
        if (addedCount < count) {
            System.out.println("Added " + addedCount + " out of " + count + " requested slots due to limits or errors");
        }
        
        return addedCount;
    }
    
    // Additional utility methods for comprehensive management
    public long countSlotsByStatus(String status) {
        return slotRepository.findAll().stream()
                .filter(slot -> status.equalsIgnoreCase(slot.getStatus()))
                .count();
    }
    
    public long countSlotsByLocation(String location) {
        return slotRepository.findAll().stream()
                .filter(slot -> location.equals(slot.getLocation()))
                .count();
    }
    
    public List<Slot> getSlotsByStatus(String status) {
        return slotRepository.findAll().stream()
                .filter(slot -> status.equalsIgnoreCase(slot.getStatus()))
                .collect(Collectors.toList());
    }
    
    public boolean updateSlotStatus(Integer slotId, String newStatus) {
        try {
            Optional<Slot> slotOpt = slotRepository.findById(slotId);
            if (slotOpt.isPresent()) {
                Slot slot = slotOpt.get();
                slot.setStatus(newStatus.toUpperCase());
                slotRepository.save(slot);
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error updating slot status: " + e.getMessage());
            return false;
        }
    }
}