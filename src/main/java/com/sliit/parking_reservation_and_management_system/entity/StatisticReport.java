package com.sliit.parking_reservation_and_management_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "StatisticReport")
@PrimaryKeyJoinColumn(name = "ReportID")
public class StatisticReport extends Report {
    @Column(name = "CustomerActivityRate")
    private BigDecimal customerActivityRate;

    @Column(name = "SlotUsageRate")
    private BigDecimal slotUsageRate;

    @Override // 👈 Add this
    public BigDecimal getCustomerActivityRate() {
        return customerActivityRate;
    }

    public void setCustomerActivityRate(BigDecimal customerActivityRate) {
        this.customerActivityRate = customerActivityRate;
    }

    @Override // 👈 Add this
    public BigDecimal getSlotUsageRate() {
        return slotUsageRate;
    }

    public void setSlotUsageRate(BigDecimal slotUsageRate) {
        this.slotUsageRate = slotUsageRate;
    }
}