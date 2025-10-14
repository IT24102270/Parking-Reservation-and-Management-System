package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.ParkingSlot;
import com.sliit.parking_reservation_and_management_system.repository.ParkingSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParkingSlotService {
    
    @Autowired
    private ParkingSlotRepository parkingSlotRepository;
    
    public List<ParkingSlot> getAllParkingSlots() {
        return parkingSlotRepository.findAll();
    }
    
    public Optional<ParkingSlot> getParkingSlotById(Long id) {
        return parkingSlotRepository.findById(id);
    }
    
    public ParkingSlot saveParkingSlot(ParkingSlot parkingSlot) {
        return parkingSlotRepository.save(parkingSlot);
    }
    
    public void deleteParkingSlot(Long id) {
        parkingSlotRepository.deleteById(id);
    }
    
    public List<ParkingSlot> getParkingSlotsByStatus(String status) {
        return parkingSlotRepository.findByStatus(status);
    }
    
    public Long countAvailableSlots() {
        return parkingSlotRepository.countAvailableSlots();
    }
    
    public Long countOccupiedSlots() {
        return parkingSlotRepository.countOccupiedSlots();
    }
    
    public List<ParkingSlot> getAvailableSlots() {
        return parkingSlotRepository.findAvailableSlots();
    }
    
    
    public ParkingSlot updateSlotStatus(Long id, String status) {
        Optional<ParkingSlot> slotOpt = parkingSlotRepository.findById(id);
        if (slotOpt.isPresent()) {
            ParkingSlot slot = slotOpt.get();
            slot.setStatus(status);
            return parkingSlotRepository.save(slot);
        }
        return null;
    }
    
    public boolean isSlotAvailable(Long slotId) {
        Optional<ParkingSlot> slotOpt = parkingSlotRepository.findById(slotId);
        return slotOpt.isPresent() && "AVAILABLE".equals(slotOpt.get().getStatus());
    }
}
