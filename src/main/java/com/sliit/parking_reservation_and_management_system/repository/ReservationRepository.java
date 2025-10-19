package com.sliit.parking_reservation_and_management_system.repository;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.reservationDate BETWEEN :start AND :end")
    Integer countByReservationDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(r.totalCost) FROM Reservation r WHERE r.reservationDate BETWEEN :start AND :end")
    Optional<BigDecimal> sumTotalCostByReservationDateBetween(LocalDateTime start, LocalDateTime end);
}