package com.sliit.parking_reservation_and_management_system.service;

import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationSchedulerService {
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private PaymentService paymentService;
    
    // Check for booking reminders every 5 minutes
    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 milliseconds
    public void sendBookingReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderTime = now.plusMinutes(30); // 30 minutes before start
            
            // Find reservations starting in 30 minutes
            List<Reservation> upcomingReservations = reservationRepository.findUpcomingReservations(
                now.plusMinutes(25), now.plusMinutes(35)
            );
            
            for (Reservation reservation : upcomingReservations) {
                if ("CONFIRMED".equals(reservation.getStatus())) {
                    long minutesUntilStart = ChronoUnit.MINUTES.between(now, reservation.getStartTime());
                    if (minutesUntilStart > 0 && minutesUntilStart <= 30) {
                        notificationService.sendBookingReminder(reservation, (int) minutesUntilStart);
                        System.out.println("Sent booking reminder for reservation: " + reservation.getBookingId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in sendBookingReminders: " + e.getMessage());
        }
    }
    
    // Check for booking expiry warnings every 5 minutes
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void sendExpiryWarnings() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Find reservations expiring in the next 15 minutes
            List<Reservation> expiringReservations = reservationRepository.findExpiringReservations(
                now, now.plusMinutes(15)
            );
            
            for (Reservation reservation : expiringReservations) {
                if ("ACTIVE".equals(reservation.getStatus())) {
                    long minutesLeft = ChronoUnit.MINUTES.between(now, reservation.getEndTime());
                    if (minutesLeft > 0 && minutesLeft <= 15) {
                        notificationService.sendBookingExpiryWarning(reservation, (int) minutesLeft);
                        System.out.println("Sent expiry warning for reservation: " + reservation.getBookingId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in sendExpiryWarnings: " + e.getMessage());
        }
    }
    
    // Check for payment reminders every hour
    @Scheduled(fixedRate = 3600000) // 1 hour = 3,600,000 milliseconds
    public void sendPaymentReminders() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime reminderTime = now.plusDays(1); // 24 hours before due
            
            // Find reservations with payments due in 24 hours
            List<Reservation> pendingPayments = reservationRepository.findPendingPayments(
                now.plusHours(23), now.plusHours(25)
            );
            
            for (Reservation reservation : pendingPayments) {
                if ("PENDING".equals(reservation.getStatus())) {
                    // Get payment amount from Payment table
                    Optional<Payment> paymentOpt = paymentService.getPaymentByReservationId(reservation.getId());
                    if (paymentOpt.isPresent()) {
                        Payment payment = paymentOpt.get();
                        // Calculate payment due date (assuming 24 hours after booking creation)
                        LocalDateTime dueDate = reservation.getCreatedAt().plusDays(1);
                        
                        notificationService.sendPaymentReminder(
                            reservation.getUserId(), 
                            reservation.getBookingId(), 
                            dueDate, 
                            payment.getAmount().doubleValue()
                        );
                        System.out.println("Sent payment reminder for reservation: " + reservation.getBookingId());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in sendPaymentReminders: " + e.getMessage());
        }
    }
    
    // Check for urgent payment deadlines every 30 minutes
    @Scheduled(fixedRate = 1800000) // 30 minutes = 1,800,000 milliseconds
    public void sendPaymentDeadlineWarnings() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Find reservations with payments due in the next 6 hours
            List<Reservation> urgentPayments = reservationRepository.findUrgentPayments(
                now, now.plusHours(6)
            );
            
            for (Reservation reservation : urgentPayments) {
                if ("PENDING".equals(reservation.getStatus())) {
                    // Get payment amount from Payment table
                    Optional<Payment> paymentOpt = paymentService.getPaymentByReservationId(reservation.getId());
                    if (paymentOpt.isPresent()) {
                        Payment payment = paymentOpt.get();
                        // Calculate payment due date
                        LocalDateTime dueDate = reservation.getCreatedAt().plusDays(1);
                        long hoursLeft = ChronoUnit.HOURS.between(now, dueDate);
                        
                        if (hoursLeft > 0 && hoursLeft <= 6) {
                            notificationService.sendPaymentDeadlineWarning(
                                reservation.getUserId(), 
                                reservation.getBookingId(), 
                                dueDate, 
                                payment.getAmount().doubleValue(), 
                                (int) hoursLeft
                            );
                            System.out.println("Sent urgent payment warning for reservation: " + reservation.getBookingId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error in sendPaymentDeadlineWarnings: " + e.getMessage());
        }
    }
    
    // Update reservation statuses every 10 minutes
    @Scheduled(fixedRate = 600000) // 10 minutes = 600,000 milliseconds
    public void updateReservationStatuses() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Update CONFIRMED reservations to ACTIVE when start time is reached
            List<Reservation> startingReservations = reservationRepository.findStartingReservations(now);
            for (Reservation reservation : startingReservations) {
                if ("CONFIRMED".equals(reservation.getStatus())) {
                    reservation.setStatus("ACTIVE");
                    reservationRepository.save(reservation);
                    System.out.println("Updated reservation to ACTIVE: " + reservation.getBookingId());
                }
            }
            
            // Update ACTIVE reservations to COMPLETED when end time is reached
            List<Reservation> completingReservations = reservationRepository.findCompletingReservations(now);
            for (Reservation reservation : completingReservations) {
                if ("ACTIVE".equals(reservation.getStatus())) {
                    reservation.setStatus("COMPLETED");
                    reservationRepository.save(reservation);
                    System.out.println("Updated reservation to COMPLETED: " + reservation.getBookingId());
                }
            }
            
            // Cancel PENDING reservations with overdue payments
            LocalDateTime paymentDeadline = now.minusDays(1); // 24 hours overdue
            List<Reservation> overduePayments = reservationRepository.findOverduePayments(paymentDeadline);
            for (Reservation reservation : overduePayments) {
                if ("PENDING".equals(reservation.getStatus())) {
                    reservation.setStatus("CANCELLED");
                    reservationRepository.save(reservation);
                    
                    // Send cancellation notification
                    notificationService.sendBookingCancellation(
                        reservation, 
                        "Payment overdue", 
                        0.0 // No refund for overdue payments
                    );
                    System.out.println("Cancelled overdue reservation: " + reservation.getBookingId());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error in updateReservationStatuses: " + e.getMessage());
        }
    }
    
    // Send daily summary notifications at 8 AM
    @Scheduled(cron = "0 0 8 * * *") // Every day at 8:00 AM
    public void sendDailySummary() {
        try {
            LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime tomorrow = today.plusDays(1);
            
            // Find all users with reservations today
            List<Reservation> todayReservations = reservationRepository.findReservationsByDateRange(today, tomorrow);
            
            // Group by user and send summary
            todayReservations.stream()
                .collect(java.util.stream.Collectors.groupingBy(Reservation::getUserId))
                .forEach((userId, reservations) -> {
                    try {
                        String summary = createDailySummary(reservations);
                        notificationService.createNotificationWithDelivery(
                            userId, 
                            "SUMMARY", 
                            summary, 
                            true, // Send email
                            false // Don't send SMS for summaries
                        );
                    } catch (Exception e) {
                        System.err.println("Error sending daily summary to user " + userId + ": " + e.getMessage());
                    }
                });
            
            System.out.println("Sent daily summaries for " + todayReservations.size() + " reservations");
            
        } catch (Exception e) {
            System.err.println("Error in sendDailySummary: " + e.getMessage());
        }
    }
    
    // Clean up old notifications every week
    @Scheduled(cron = "0 0 2 * * SUN") // Every Sunday at 2:00 AM
    public void cleanupOldNotifications() {
        try {
            int deletedCount = notificationService.cleanupOldNotifications();
            System.out.println("Cleaned up " + deletedCount + " old notifications");
        } catch (Exception e) {
            System.err.println("Error in cleanupOldNotifications: " + e.getMessage());
        }
    }
    
    // Security monitoring - check for suspicious activities every 15 minutes
    @Scheduled(fixedRate = 900000) // 15 minutes = 900,000 milliseconds
    public void monitorSecurityEvents() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime checkTime = now.minusMinutes(15);
            
            // Check for multiple failed login attempts (this would require login attempt tracking)
            // Check for unusual booking patterns
            // Check for payment anomalies
            
            // For now, this is a placeholder for security monitoring
            // In a real implementation, you would check various security metrics
            
            System.out.println("Security monitoring check completed at: " + now);
            
        } catch (Exception e) {
            System.err.println("Error in monitorSecurityEvents: " + e.getMessage());
        }
    }
    
    // Helper method to create daily summary
    private String createDailySummary(List<Reservation> reservations) {
        StringBuilder summary = new StringBuilder();
        summary.append("Daily Parking Summary:\n\n");
        
        for (Reservation reservation : reservations) {
            summary.append(String.format("• %s - Slot %d (%s to %s) - %s\n",
                reservation.getBookingId(),
                reservation.getSlotId(),
                reservation.getStartTime().toLocalTime(),
                reservation.getEndTime().toLocalTime(),
                reservation.getStatus()
            ));
        }
        
        summary.append("\nHave a great day!");
        return summary.toString();
    }
    
    // Manual trigger methods for testing
    public void triggerBookingReminders() {
        sendBookingReminders();
    }
    
    public void triggerExpiryWarnings() {
        sendExpiryWarnings();
    }
    
    public void triggerPaymentReminders() {
        sendPaymentReminders();
    }
    
    public void triggerPaymentDeadlineWarnings() {
        sendPaymentDeadlineWarnings();
    }
    
    public void triggerStatusUpdates() {
        updateReservationStatuses();
    }
}
