package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.entity.Slot;
import com.sliit.parking_reservation_and_management_system.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StaffSlotStrategy implements SlotValidationStrategy {

    private final SlotRepository slotRepository;

    @Autowired
    public StaffSlotStrategy(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public Slot validateAndSave(Slot slot) throws IllegalArgumentException {
        // Staff-specific validation: Lower minimum hourly rate for staff slots (e.g., discounted)
        if (slot.getHourlyRate() < 2.0) {
            throw new IllegalArgumentException("Staff slots require a minimum hourly rate of $2.00");
        }

        // Generic validation (shared logic)
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

        // Staff-specific enhancement: e.g., apply automatic discount or set staff-only flag (optional extension)
        // slot.setDiscountedRate(slot.getHourlyRate() * 0.8); // Assuming Slot has a discountedRate field

        // Save the slot
        Slot savedSlot = slotRepository.save(slot);

        // Flush to ensure immediate DB write
        slotRepository.flush();

        return savedSlot;
    }
}
