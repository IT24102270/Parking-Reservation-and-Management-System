package com.sliit.parking_reservation_and_management_system.repository;

import com.sliit.parking_reservation_and_management_system.entity.IncomeReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncomeReportRepository extends JpaRepository<IncomeReport, Integer> {
}