package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.entity.Slot;
import com.sliit.parking_reservation_and_management_system.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VipSlotStrategy implements SlotValidationStrategy {

    private final SlotRepository slotRepository;

    @Autowired
    public VipSlotStrategy(SlotRepository slotRepository) {
        this.slotRepository = slotRepository;
    }

    @Override
    public Slot validateAndSave(Slot slot) throws IllegalArgumentException {
        // VIP-specific validation: Higher minimum hourly rate for VIP slots
        if (slot.getHourlyRate() < 10.0) {
            throw new IllegalArgumentException("VIP slots require a minimum hourly rate of $10.00");
        }

        // Generic validation (shared logic, but can be overridden or extended)
        if (slot.getHourlyRate() <= 0) {
            throw new IllegalArgumentException("Hourly rate must be positive");
        }
        if (!List.of("available", "occupied", "maintenance").contains(slot.getStatus())) {
            throw new IllegalArgumentException("Invalid status");
        }

        // Check slot limit for the location (generic, but applied here)
        if (slot.getId() == null) {
            long currentCount = slotRepository.findAll().stream()
                    .filter(s -> slot.getLocation().equals(s.getLocation()))
                    .count();
            if (currentCount >= 20) { // Assuming MAX_SLOTS_PER_LOCATION = 20
                throw new IllegalArgumentException("Maximum slots (20) reached for location: " + slot.getLocation());
            }
        }

        // VIP-specific enhancement: e.g., auto-set a priority flag if needed (optional extension)
        // slot.setPriority(true); // Assuming Slot has a priority field

        // Save the slot
        Slot savedSlot = slotRepository.save(slot);

        // Flush to ensure immediate DB write
        slotRepository.flush();

        return savedSlot;
    }
}
