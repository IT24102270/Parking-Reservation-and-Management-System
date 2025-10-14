package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
    
    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }
    
    public Reservation saveReservation(Reservation reservation) {
        if (reservation.getCreatedAt() == null) {
            reservation.setCreatedAt(LocalDateTime.now());
        }
        return reservationRepository.save(reservation);
    }
    
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }
    
    public List<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserId(userId);
    }
    
    public Long countByUserId(Long userId) {
        return reservationRepository.countByUserId(userId);
    }
    
    public Long countActiveByUserId(Long userId) {
        return reservationRepository.countActiveByUserId(userId, LocalDateTime.now());
    }
    
    public List<Reservation> getActiveReservationsByUserId(Long userId) {
        return reservationRepository.findActiveByUserId(userId, LocalDateTime.now());
    }
    
    public List<Reservation> getReservationsByUserIdOrderByDate(Long userId) {
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Reservation> getActiveReservationsBySlotId(Long slotId) {
        return reservationRepository.findActiveBySlotId(slotId, LocalDateTime.now());
    }
    
    public Reservation updateReservationStatus(Long id, String status) {
        Optional<Reservation> reservationOpt = reservationRepository.findById(id);
        if (reservationOpt.isPresent()) {
            Reservation reservation = reservationOpt.get();
            reservation.setStatus(status);
            return reservationRepository.save(reservation);
        }
        return null;
    }
    
    public boolean isSlotAvailable(Long slotId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Reservation> activeReservations = getActiveReservationsBySlotId(slotId);
        
        for (Reservation reservation : activeReservations) {
            // Check for time overlap
            if (startTime.isBefore(reservation.getEndTime()) && endTime.isAfter(reservation.getStartTime())) {
                return false;
            }
        }
        return true;
    }
    
    public Double getTotalSpentByUserId(Long userId) {
        try {
            List<Reservation> userReservations = getReservationsByUserId(userId);
            return userReservations.stream()
                .mapToDouble(reservation -> {
                    Optional<Payment> payment = paymentService.getPaymentByReservationId(reservation.getId());
                    return payment.map(p -> p.getAmount().doubleValue()).orElse(0.0);
                })
                .sum();
        } catch (Exception e) {
            System.err.println("Error calculating total spent for user " + userId + ": " + e.getMessage());
            return 0.0;
        }
    }
    
    public List<Reservation> getRecentReservationsByUserId(Long userId, int limit) {
        try {
            List<Reservation> userReservations = getReservationsByUserId(userId);
            return userReservations.stream()
                .sorted((r1, r2) -> {
                    // Sort by created_at descending (most recent first)
                    if (r1.getCreatedAt() == null && r2.getCreatedAt() == null) return 0;
                    if (r1.getCreatedAt() == null) return 1;
                    if (r2.getCreatedAt() == null) return -1;
                    return r2.getCreatedAt().compareTo(r1.getCreatedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting recent reservations for user " + userId + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    public boolean canBeCancelled(Reservation reservation) {
        try {
            // Check if reservation exists and has valid status
            if (reservation == null) {
                System.out.println("DEBUG: Reservation is null");
                return false;
            }
            
            // Only CONFIRMED and PENDING bookings can be cancelled
            String status = reservation.getStatus();
            System.out.println("DEBUG: Reservation status: " + status);
            if (!"CONFIRMED".equals(status) && !"PENDING".equals(status)) {
                System.out.println("DEBUG: Status not eligible for cancellation");
                return false;
            }
            
            // Check if booking was created within the last hour
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime createdAt = reservation.getCreatedAt();
            
            System.out.println("DEBUG: Current time: " + now);
            System.out.println("DEBUG: Created at: " + createdAt);
            
            if (createdAt == null) {
                System.out.println("DEBUG: CreatedAt is null");
                return false;
            }
            
            // Calculate time since booking creation in minutes
            long minutesSinceCreation = ChronoUnit.MINUTES.between(createdAt, now);
            System.out.println("DEBUG: Minutes since creation: " + minutesSinceCreation);
            
            // Allow cancellation if booking was made within the last 60 minutes (1 hour)
            boolean canCancel = minutesSinceCreation <= 60;
            System.out.println("DEBUG: Can cancel: " + canCancel);
            return canCancel;
            
        } catch (Exception e) {
            System.err.println("Error checking if reservation can be cancelled: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public String getCancellationMessage(Reservation reservation) {
        try {
            if (reservation == null) {
                return "Reservation not found";
            }
            
            String status = reservation.getStatus();
            if ("CANCELLED".equals(status)) {
                return "This booking is already cancelled";
            }
            
            if ("COMPLETED".equals(status)) {
                return "This booking is already completed";
            }
            
            if ("ACTIVE".equals(status)) {
                return "Cannot cancel an active booking";
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime createdAt = reservation.getCreatedAt();
            
            if (createdAt == null) {
                return "Invalid booking creation time";
            }
            
            // Calculate time since booking creation
            long minutesSinceCreation = ChronoUnit.MINUTES.between(createdAt, now);
            
            if (minutesSinceCreation > 60) {
                long hoursElapsed = minutesSinceCreation / 60;
                long minsElapsed = minutesSinceCreation % 60;
                return String.format("Cannot cancel - booking was made %d hours %d minutes ago (cancellation allowed only within 1 hour of booking)", 
                                   hoursElapsed, minsElapsed);
            }
            
            // Calculate remaining time for cancellation
            long minutesLeft = 60 - minutesSinceCreation;
            return String.format("Booking can be cancelled (you have %d minutes left to cancel)", minutesLeft);
            
        } catch (Exception e) {
            System.err.println("Error getting cancellation message: " + e.getMessage());
            return "Error checking cancellation status";
        }
    }
}
