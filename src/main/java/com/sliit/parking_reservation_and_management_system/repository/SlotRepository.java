package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlotRepository extends JpaRepository<Slot, Integer> {
}