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
}