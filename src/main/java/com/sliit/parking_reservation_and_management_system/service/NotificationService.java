package com.sliit.parking_reservation_and_management_system.service;
import com.sliit.parking_reservation_and_management_system.entity.Notification;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private SMSService smsService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PaymentService paymentService;
    
    // Create a general notification
    public Notification createNotification(Long customerId, String type, String message) {
        Notification notification = new Notification(customerId, type, message);
        return notificationRepository.save(notification);
    }
    
    // Get notification by ID
    public Optional<Notification> getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId);
    }
    
    // Get all notifications for a customer
    public List<Notification> getNotificationsByUserId(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByDateSentDesc(customerId);
    }
    
    // Get recent notifications with limit
    public List<Notification> getRecentNotifications(Long customerId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return notificationRepository.findRecentByCustomerId(customerId, pageRequest);
    }
    
    // Get notifications by type
    public List<Notification> getNotificationsByType(Long customerId, String type) {
        return notificationRepository.findByCustomerIdAndType(customerId, type);
    }
    
    // Count all notifications for a customer (since read status is not in schema)
    public long getUnreadCount(Long customerId) {
        return notificationRepository.countByCustomerId(customerId);
    }
    
    // Get unread notifications (return all since read status is not tracked)
    public List<Notification> getUnreadNotifications(Long customerId) {
        return getRecentNotifications(customerId, 10);
    }
    
    // Mark as read - no-op since read status is not in database schema
    public boolean markAsRead(Long notificationId) {
        // Since read status is not in the database schema, we'll just return true
        return true;
    }
    
    // Mark all as read - no-op since read status is not in database schema
    public boolean markAllAsRead(Long customerId) {
        // Since read status is not in the database schema, we'll just return true
        return true;
    }
    
    // Delete notification
    public boolean deleteNotification(Long notificationId) {
        try {
            notificationRepository.deleteById(notificationId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Delete all notifications for a customer
    public boolean deleteAllNotifications(Long customerId) {
        try {
            List<Notification> notifications = getNotificationsByUserId(customerId);
            notificationRepository.deleteAll(notifications);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Cleanup old notifications (older than 30 days)
    public int cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        return notificationRepository.deleteOldNotifications(cutoffDate);
    }
    
    // Utility methods for common notification types
    
    public Notification createBookingConfirmedNotification(Long customerId, String slotNumber, LocalDateTime startTime) {
        String message = String.format("Booking Confirmed: Your parking reservation for slot %s has been confirmed for %s", 
                                     slotNumber, startTime.toString());
        return createNotification(customerId, "BOOKING", message);
    }
    
    public Notification createPaymentPendingNotification(Long customerId, double amount) {
        String message = String.format("Payment Required: Please complete your payment of $%.2f", amount);
        return createNotification(customerId, "PAYMENT", message);
    }
    
    public Notification createPaymentCompletedNotification(Long customerId, double amount) {
        String message = String.format("Payment Successful: Your payment of $%.2f has been processed successfully", amount);
        return createNotification(customerId, "SUCCESS", message);
    }
    
    public Notification createBookingReminderNotification(Long customerId, String slotNumber, LocalDateTime startTime) {
        String message = String.format("Booking Reminder: Your parking reservation for slot %s starts in 30 minutes", slotNumber);
        return createNotification(customerId, "INFO", message);
    }
    
    public Notification createBookingCancelledNotification(Long customerId, String slotNumber) {
        String message = String.format("Booking Cancelled: Your parking reservation for slot %s has been cancelled", slotNumber);
        return createNotification(customerId, "WARNING", message);
    }
    
    // ========== ENHANCED NOTIFICATION METHODS WITH EMAIL/SMS ==========
    
    // Enhanced notification with email/SMS delivery
    public Notification createNotificationWithDelivery(Long customerId, String type, String message, 
                                                     boolean sendEmail, boolean sendSMS) {
        // Create database notification
        Notification notification = createNotification(customerId, type, message);
        
        // Get user details for email/SMS
        try {
            User user = getUserById(customerId);
            if (user != null) {
                String customerName = user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "");
                
                // Send email if requested and email exists
                if (sendEmail && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                    String subject = getEmailSubject(type);
                    emailService.sendSimpleEmail(user.getEmail(), subject, message);
                }
                
                // Send SMS if requested and phone exists
                if (sendSMS && user.getPhoneNumber() != null && smsService.isSMSEnabled(user.getPhoneNumber())) {
                    String formattedPhone = smsService.formatPhoneNumber(user.getPhoneNumber());
                    if (formattedPhone != null) {
                        smsService.sendSMS(formattedPhone, message);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending notification delivery: " + e.getMessage());
        }
        
        return notification;
    }
    
    // Booking-related notifications with email/SMS
    public void sendBookingConfirmation(Reservation reservation) {
        try {
            User user = getUserById(reservation.getUserId());
            if (user == null) return;
            
            String customerName = user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "");
            String bookingId = reservation.getBookingId();
            
            // Create database notification
            String message = String.format("Your booking %s has been confirmed for slot %d starting at %s", 
                bookingId, reservation.getSlotId(), reservation.getStartTime().toString());
            createNotification(user.getUserID(), "BOOKING", message);
            
            // Send email
            if (user.getEmail() != null) {
                // Get payment amount from Payment table
                Optional<Payment> paymentOpt = paymentService.getPaymentByReservationId(reservation.getId());
                Double paymentAmount = paymentOpt.map(p -> p.getAmount().doubleValue()).orElse(0.0);
                
                emailService.sendBookingConfirmation(
                    user.getEmail(), customerName, bookingId, 
                    "Slot " + reservation.getSlotId(), 
                    reservation.getStartTime(), reservation.getEndTime(), 
                    paymentAmount
                );
            }
            
            // Send SMS
            if (smsService.isSMSEnabled(user.getPhoneNumber())) {
                String formattedPhone = smsService.formatPhoneNumber(user.getPhoneNumber());
                if (formattedPhone != null) {
                    smsService.sendBookingConfirmationSMS(
                        formattedPhone, customerName, bookingId, 
                        "Slot " + reservation.getSlotId(), reservation.getStartTime()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending booking confirmation: " + e.getMessage());
        }
    }
    
    public void sendBookingCancellation(Reservation reservation, String reason, Double refundAmount) {
        try {
            User user = getUserById(reservation.getUserId());
            if (user == null) return;
            
            String customerName = user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "");
            String bookingId = reservation.getBookingId();
            
            // Create database notification
            String message = String.format("Your booking %s has been cancelled. Reason: %s", bookingId, reason);
            createNotification(user.getUserID(), "BOOKING", message);
            
            // Send email
            if (user.getEmail() != null) {
                emailService.sendBookingCancellation(user.getEmail(), customerName, bookingId, reason, refundAmount);
            }
            
            // Send SMS
            if (smsService.isSMSEnabled(user.getPhoneNumber())) {
                String formattedPhone = smsService.formatPhoneNumber(user.getPhoneNumber());
                if (formattedPhone != null) {
                    smsService.sendBookingCancellationSMS(formattedPhone, customerName, bookingId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending booking cancellation: " + e.getMessage());
        }
    }
    
    public void sendBookingReminder(Reservation reservation, int minutesBefore) {
        try {
            User user = getUserById(reservation.getUserId());
            if (user == null) return;
            
            String customerName = user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "");
            String bookingId = reservation.getBookingId();
            
            // Create database notification
            String message = String.format("Reminder: Your parking at slot %d starts in %d minutes", 
                reservation.getSlotId(), minutesBefore);
            createNotification(user.getUserID(), "REMINDER", message);
            
            // Send email
            if (user.getEmail() != null) {
                emailService.sendBookingReminder(
                    user.getEmail(), customerName, bookingId, 
                    "Slot " + reservation.getSlotId(), 
                    reservation.getStartTime(), minutesBefore
                );
            }
            
            // Send SMS (always for reminders)
            if (smsService.isSMSEnabled(user.getPhoneNumber())) {
                String formattedPhone = smsService.formatPhoneNumber(user.getPhoneNumber());
                if (formattedPhone != null) {
                    smsService.sendBookingReminderSMS(
                        formattedPhone, customerName, bookingId, 
                        "Slot " + reservation.getSlotId(), minutesBefore
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending booking reminder: " + e.getMessage());
        }
    }
    
    public void sendBookingExpiryWarning(Reservation reservation, int minutesLeft) {
        try {
            User user = getUserById(reservation.getUserId());
            if (user == null) return;
            
            String customerName = user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "");
            String bookingId = reservation.getBookingId();
            
            // Create database notification
            String message = String.format("WARNING: Your parking expires in %d minutes! Move your vehicle to avoid charges.", minutesLeft);
            createNotification(user.getUserID(), "WARNING", message);
            
            // Send email
            if (user.getEmail() != null) {
                emailService.sendBookingExpiryWarning(user.getEmail(), customerName, bookingId, reservation.getEndTime(), minutesLeft);
            }
            
            // Send SMS (always for warnings)
            if (smsService.isSMSEnabled(user.getPhoneNumber())) {
                String formattedPhone = smsService.formatPhoneNumber(user.getPhoneNumber());
                if (formattedPhone != null) {
                    smsService.sendBookingExpirySMS(formattedPhone, customerName, bookingId, minutesLeft);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending booking expiry warning: " + e.getMessage());
        }
    }
    
    // Payment-related notifications
    public void sendPaymentReminder(Long customerId, String bookingId, LocalDateTime dueDate, Double amount) {
        try {
            User user = getUserById(customerId);
            if (user == null) return;
            
            String customerName = user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "");
            
            // Create database notification
            String message = String.format("Payment reminder: $%.2f due %s for booking %s", amount, dueDate.toString(), bookingId);
            createNotification(customerId, "PAYMENT", message);
            
            // Send email
            if (user.getEmail() != null) {
                emailService.sendPaymentReminder(user.getEmail(), customerName, bookingId, dueDate, amount);
            }
            
            // Send SMS
            if (smsService.isSMSEnabled(user.getPhoneNumber())) {
                String formattedPhone = smsService.formatPhoneNumber(user.getPhoneNumber());
                if (formattedPhone != null) {
                    smsService.sendPaymentReminderSMS(formattedPhone, customerName, bookingId, dueDate, amount);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending payment reminder: " + e.getMessage());
        }
    }
    
    public void sendPaymentDeadlineWarning(Long customerId, String bookingId, LocalDateTime dueDate, Double amount, int hoursLeft) {
        try {
            User user = getUserById(customerId);
            if (user == null) return;
            
            String customerName = user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "");
            
            // Create database notification
            String message = String.format("URGENT: Payment of $%.2f due in %d hours for booking %s!", amount, hoursLeft, bookingId);
            createNotification(customerId, "URGENT", message);
            
            // Send email
            if (user.getEmail() != null) {
                emailService.sendPaymentDeadlineWarning(user.getEmail(), customerName, bookingId, dueDate, amount, hoursLeft);
            }
            
            // Send SMS (always for urgent notifications)
            if (smsService.isSMSEnabled(user.getPhoneNumber())) {
                String formattedPhone = smsService.formatPhoneNumber(user.getPhoneNumber());
                if (formattedPhone != null) {
                    smsService.sendPaymentDeadlineSMS(formattedPhone, customerName, bookingId, hoursLeft, amount);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending payment deadline warning: " + e.getMessage());
        }
    }
    
    // Security-related notifications
    public void sendSecurityAlert(Long customerId, String alertType, String description) {
        try {
            User user = getUserById(customerId);
            if (user == null) return;
            
            String customerName = user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "");
            
            // Create database notification
            String message = String.format("Security Alert: %s - %s", alertType, description);
            createNotification(customerId, "SECURITY", message);
            
            // Send email
            if (user.getEmail() != null) {
                emailService.sendSecurityAlert(user.getEmail(), customerName, alertType, description, LocalDateTime.now());
            }
            
            // Send SMS (always for security alerts)
            if (smsService.isSMSEnabled(user.getPhoneNumber())) {
                String formattedPhone = smsService.formatPhoneNumber(user.getPhoneNumber());
                if (formattedPhone != null) {
                    smsService.sendSecurityAlertSMS(formattedPhone, customerName, alertType);
                }
            }
        } catch (Exception e) {
            System.err.println("Error sending security alert: " + e.getMessage());
        }
    }
    
    // Helper methods
    private User getUserById(Long userId) {
        return userService.getAllUsers().stream()
            .filter(u -> u.getUserID().equals(userId))
            .findFirst()
            .orElse(null);
    }
    
    private String getEmailSubject(String type) {
        switch (type.toUpperCase()) {
            case "BOOKING": return "Booking Update";
            case "PAYMENT": return "Payment Notification";
            case "SECURITY": return "Security Alert";
            case "REMINDER": return "Parking Reminder";
            case "WARNING": return "Important Warning";
            case "URGENT": return "URGENT: Immediate Action Required";
            default: return "Smart Parking Notification";
        }
    }
}
