package com.sliit.parking_reservation_and_management_system.strategy;

import com.sliit.parking_reservation_and_management_system.entity.Slot;

public interface SlotValidationStrategy {

    /**
     * Validates the slot based on the specific strategy (e.g., VIP, General, Staff)
     * and saves it to the database. Throws IllegalArgumentException if validation fails.
     *
     * @param slot the Slot to validate and save
     * @return the saved Slot
     * @throws IllegalArgumentException if validation fails
     */
    Slot validateAndSave(Slot slot) throws IllegalArgumentException;
}
