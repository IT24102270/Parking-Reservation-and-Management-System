package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.Slot;
import com.sliit.parking_reservation_and_management_system.repository.SlotRepository;
import com.sliit.parking_reservation_and_management_system.strategy.SlotStrategyFactory;
import com.sliit.parking_reservation_and_management_system.strategy.SlotValidationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SlotService {

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private SlotStrategyFactory slotStrategyFactory;

    public List<Slot> getSlotsByLocation(String location) {
        return slotRepository.findAll().stream()
                .filter(slot -> location.equals(slot.getLocation()))
                .collect(Collectors.toList());
    }

    public Optional<Slot> getSlotById(Integer id) {
        return slotRepository.findById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Slot saveSlot(Slot slot) throws IllegalArgumentException {
        System.out.println("🔧 SlotService.saveSlot() called with slot: " + slot);

        // Use Strategy Pattern: Select and delegate to the appropriate validation and save strategy
        SlotValidationStrategy strategy = slotStrategyFactory.getStrategy(slot);
        System.out.println("📋 Selected strategy: " + strategy.getClass().getSimpleName());

        Slot savedSlot = strategy.validateAndSave(slot);

        System.out.println("✅ Slot saved successfully with ID: " + savedSlot.getId() + " using Strategy Pattern");

        return savedSlot;
    }
}