package com.example.vehicleparkingsystem.repository;

import com.example.vehicleparkingsystem.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    /**
     * Finds all payments within a given date range where the status is 'PAID'.
     *
     * @param startDate The start of the date range (inclusive).
     * @param endDate   The end of the date range (inclusive).
     * @return A list of paid payments within the specified range.
     */
    List<Payment> findByStatusAndDateBetween(String status, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Calculates the sum of amounts for all payments within a given date range
     * where the status is 'PAID'. This is a more efficient way to get the total income.
     *
     * @param startDate The start of the date range (inclusive).
     * @param endDate   The end of the date range (inclusive).
     * @return The total income as a BigDecimal. Returns 0 if no payments are found.
     */
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'PAID' AND p.date BETWEEN :startDate AND :endDate")
    BigDecimal sumPaidAmountBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

}
