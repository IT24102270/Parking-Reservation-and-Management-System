package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.entity.Slot;
import com.sliit.parking_reservation_and_management_system.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GeneralSlotStrategy implements SlotValidationStrategy {

    private final SlotRepository slotRepository;

    @Autowired
    public GeneralSlotStrategy(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public Slot validateAndSave(Slot slot) throws IllegalArgumentException {
        // General validation: Standard hourly rate check for general slots
        if (slot.getHourlyRate() <= 0) {
            throw new IllegalArgumentException("Hourly rate must be positive");
        }
        if (!List.of("available", "occupied", "maintenance").contains(slot.getStatus())) {
            throw new IllegalArgumentException("Invalid status");
        }

        // Check slot limit for the location (generic)
        if (slot.getId() == null) {
            long currentCount = slotRepository.findAll().stream()
                    .filter(s -> slot.getLocation().equals(s.getLocation()))
                    .count();
            if (currentCount >= 20) { // Assuming MAX_SLOTS_PER_LOCATION = 20
                throw new IllegalArgumentException("Maximum slots (20) reached for location: " + slot.getLocation());
            }
        }

        // General-specific enhancement: e.g., no additional fees or flags (basic save)

        // Save the slot
        Slot savedSlot = slotRepository.save(slot);

        // Flush to ensure immediate DB write
        slotRepository.flush();

        return savedSlot;
    }
}
