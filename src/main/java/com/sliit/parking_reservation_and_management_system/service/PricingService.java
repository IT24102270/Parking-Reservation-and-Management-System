package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.Pricing;
import com.sliit.parking_reservation_and_management_system.repository.PricingRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Service
public class PricingService {

    private final PricingRepository pricingRepository;

    public PricingService(PricingRepository pricingRepository) {
        this.pricingRepository = pricingRepository;
    }

    /**
     * Retrieves all defined pricing rules.
     */
    public List<Pricing> getAllPricingRules() {
        return pricingRepository.findAll();
    }

    /**
     * Saves a new pricing rule to the database.
     */
    public Pricing savePricingRule(Pricing pricing) {
        return pricingRepository.save(pricing);
    }

    /**
     * Calculates the dynamic hourly rate for a given slot's base rate
     * at the current time.
     * @param baseRate The base hourly rate of the parking slot.
     * @return The dynamically adjusted hourly rate.
     */
    public BigDecimal getCurrentDynamicRate(BigDecimal baseRate) {
        LocalTime now = LocalTime.now();

        // Find if any rule applies to the current time
        return pricingRepository.findApplicableRule(now)
                .map(rule -> baseRate.multiply(rule.getRateMultiplier())) // If a rule is found, apply its multiplier
                .orElse(baseRate); // Otherwise, return the base rate (off-peak)
    }
}