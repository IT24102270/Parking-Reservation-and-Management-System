package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.Pricing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.Optional;

public interface PricingRepository extends JpaRepository<Pricing, Integer> {

    // Find a pricing rule that applies to a specific time of day
    @Query("SELECT p FROM Pricing p WHERE p.startTime <= :time AND p.endTime > :time")
    Optional<Pricing> findApplicableRule(@Param("time") LocalTime time);
}