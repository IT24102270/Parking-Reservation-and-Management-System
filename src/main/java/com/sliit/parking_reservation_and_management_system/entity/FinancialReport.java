package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FINANCIAL")
public class FinancialReport extends Report {
    // Fields are now inherited from Report base class
}