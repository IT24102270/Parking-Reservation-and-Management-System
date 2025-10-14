package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.entity.ParkingSlot;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.NotificationService;
import com.sliit.parking_reservation_and_management_system.service.PaymentService;
import com.sliit.parking_reservation_and_management_system.service.ParkingSlotService;
import com.sliit.parking_reservation_and_management_system.service.ReservationService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import com.parking.observer.booking.NotificationManager;
import com.parking.observer.booking.EmailNotifier;
import com.parking.observer.booking.SMSNotifier;
import com.parking.observer.booking.InAppNotifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
@RequestMapping("/customer")
public class BookingController {

    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private ParkingSlotService parkingSlotService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Observer Pattern Implementation for Booking Notifications
     * 
     * NotificationManager acts as the Subject that maintains a list of observers.
     * When booking events occur (create/cancel), it notifies all registered observers.
     * This decouples the booking logic from notification logic, allowing multiple
     * notification channels to operate independently.
     */
    private final NotificationManager notificationManager;
    
    /**
     * Constructor - Initialize Observer Pattern components
     * Sets up the NotificationManager (Subject) and registers all observers
     */
    public BookingController() {
        // Initialize the Subject (NotificationManager)
        this.notificationManager = new NotificationManager();
        
        // Register concrete observers for different notification channels
        this.notificationManager.addObserver(new EmailNotifier());
        this.notificationManager.addObserver(new SMSNotifier());
        this.notificationManager.addObserver(new InAppNotifier());
        
        System.out.println("BookingController initialized with Observer Pattern");
        System.out.println("Registered observers: " + notificationManager.getObserverNames());
    }

    @GetMapping("/booking/new")
    public String newBookingForm(Model model) {
        System.out.println("=== New Booking Form Request Started ===");
        
        try {
            // Get current user
            User currentUser = getCurrentUser();
            System.out.println("Current user: " + (currentUser != null ? currentUser.getEmail() : "null"));
            
            if (currentUser == null) {
                System.out.println("No authenticated user found, creating default user for testing");
                // Create a default user for testing
                currentUser = new User();
                currentUser.setUserID(1L);
                currentUser.setFirstName("Test");
                currentUser.setLastName("User");
                currentUser.setEmail("test@example.com");
            }
            
            // Get available parking slots
            System.out.println("Fetching available parking slots...");
            List<ParkingSlot> availableSlots = null;
            try {
                availableSlots = parkingSlotService.getAvailableSlots();
                System.out.println("Available slots found: " + (availableSlots != null ? availableSlots.size() : 0));
            } catch (Exception e) {
                System.err.println("Error fetching parking slots: " + e.getMessage());
                availableSlots = new ArrayList<>();
            }
            
            // If no slots available, create some default test slots
            if (availableSlots == null || availableSlots.isEmpty()) {
                System.out.println("No parking slots found in database, creating default test slots");
                availableSlots = createDefaultTestSlots();
            }
            
            // Create new reservation object
            Reservation reservation = new Reservation();
            
            // Set default times (current time + 5 minutes for start, +1 hour for end)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime defaultStart = now.plusMinutes(5);
            LocalDateTime defaultEnd = defaultStart.plusHours(1);
            
            reservation.setStartTime(defaultStart);
            reservation.setEndTime(defaultEnd);
            
            model.addAttribute("reservation", reservation);
            model.addAttribute("availableSlots", availableSlots);
            model.addAttribute("user", currentUser);
            
            System.out.println("New booking form loaded successfully");
            System.out.println("- User: " + currentUser.getEmail());
            System.out.println("- Available slots: " + availableSlots.size());
            System.out.println("- Default start time: " + defaultStart);
            System.out.println("- Default end time: " + defaultEnd);
            
        } catch (Exception e) {
            System.err.println("=== CRITICAL ERROR in newBookingForm ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getSimpleName());
            e.printStackTrace();
            System.err.println("=== END CRITICAL ERROR ===");
            
            // Set fallback data to prevent complete failure
            User fallbackUser = new User();
            fallbackUser.setUserID(1L);
            fallbackUser.setFirstName("Test");
            fallbackUser.setLastName("User");
            fallbackUser.setEmail("test@example.com");
            
            Reservation fallbackReservation = new Reservation();
            LocalDateTime now = LocalDateTime.now();
            fallbackReservation.setStartTime(now.plusMinutes(5));
            fallbackReservation.setEndTime(now.plusHours(1));
            
            model.addAttribute("reservation", fallbackReservation);
            model.addAttribute("availableSlots", createDefaultTestSlots());
            model.addAttribute("user", fallbackUser);
            model.addAttribute("error", "Some features may be limited due to database connectivity issues");
        }
        
        System.out.println("=== Returning customer-booking-new template ===");
        return "customer-booking-new";
    }

    @PostMapping("/booking/create")
    public String createBooking(@ModelAttribute Reservation reservation,
                              @RequestParam Long slotId,
                              @RequestParam String vehicleType,
                              @RequestParam String vehicleNumber,
                              @RequestParam String startTimeStr,
                              @RequestParam String endTimeStr,
                              @RequestParam(defaultValue = "ONLINE") String paymentType,
                              RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "User not authenticated");
                return "redirect:/login";
            }
            
            // Parse date times
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, formatter);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, formatter);
            
            // Validate booking times
            if (startTime.isBefore(LocalDateTime.now().plusMinutes(5))) {
                redirectAttributes.addFlashAttribute("error", "Start time must be at least 5 minutes in the future");
                return "redirect:/customer/booking/new";
            }
            
            if (endTime.isBefore(startTime.plusMinutes(30))) {
                redirectAttributes.addFlashAttribute("error", "Minimum booking duration is 30 minutes");
                return "redirect:/customer/booking/new";
            }
            
            // Check if slot is available
            if (!reservationService.isSlotAvailable(slotId, startTime, endTime)) {
                redirectAttributes.addFlashAttribute("error", "Selected slot is not available for the chosen time period");
                return "redirect:/customer/booking/new";
            }
            
            // Get parking slot for pricing
            ParkingSlot slot = parkingSlotService.getParkingSlotById(slotId).orElse(null);
            if (slot == null) {
                redirectAttributes.addFlashAttribute("error", "Selected parking slot not found");
                return "redirect:/customer/booking/new";
            }
            
            // Calculate duration and cost
            long durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
            double durationHours = Math.ceil(durationMinutes / 60.0); // Round up to nearest hour
            double pricePerHour = 5.0; // Fixed price since PricePerHour column is removed
            double totalAmount = durationHours * pricePerHour;
            
            // Create new reservation object
            Reservation newReservation = new Reservation();
            newReservation.setUserId(currentUser.getUserID());
            newReservation.setSlotId(slotId);
            newReservation.setStartTime(startTime);
            newReservation.setEndTime(endTime);
            newReservation.setVehicleNumber(vehicleNumber);
            newReservation.setStatus("PENDING"); // Keep PENDING until payment is completed
            newReservation.setCreatedAt(LocalDateTime.now());
            newReservation.setUpdatedAt(LocalDateTime.now());
            
            // Save reservation with PENDING status
            Reservation savedReservation = reservationService.saveReservation(newReservation);
            
            // Observer Pattern: Notify all observers about reservation creation
            // This triggers notifications across all registered channels (Email, SMS, In-App)
            System.out.println("=== Triggering Observer Pattern for Reservation Creation ===");
            notificationManager.notifyObservers(savedReservation, "CREATED");
            
            // Create payment record (mandatory for booking)
            Payment payment;
            try {
                payment = paymentService.createPayment(
                    savedReservation.getId(), 
                    java.math.BigDecimal.valueOf(totalAmount), 
                    "ONLINE"
                );
                
                System.out.println("Payment record created:");
                System.out.println("- Payment ID: " + payment.getPaymentID());
                System.out.println("- Reservation ID: " + payment.getReservationID());
                System.out.println("- Amount: $" + payment.getAmount());
                System.out.println("- Status: " + payment.getStatus());
                
            } catch (Exception paymentError) {
                System.err.println("Error: Failed to create payment record: " + paymentError.getMessage());
                // Delete the reservation if payment creation fails
                reservationService.deleteReservation(savedReservation.getId());
                redirectAttributes.addFlashAttribute("error", "Failed to create payment record. Please try again.");
                return "redirect:/customer/booking/new";
            }
            
            // Do NOT update slot status or send confirmations yet - wait for payment completion
            
            System.out.println("Booking created successfully (PENDING payment):");
            System.out.println("- Reservation ID: " + savedReservation.getId());
            System.out.println("- User: " + currentUser.getEmail());
            System.out.println("- Slot ID: " + slot.getId());
            System.out.println("- Duration: " + durationHours + " hours");
            System.out.println("- Total Amount: $" + totalAmount);
            System.out.println("- Payment ID: " + payment.getPaymentID());
            
            // Redirect to payment page to complete the booking
            redirectAttributes.addFlashAttribute("success", 
                "Booking created! Please complete payment to confirm your reservation.");
            
            return "redirect:/customer/payment/" + payment.getPaymentID() + "/pay";
            
        } catch (Exception e) {
            System.err.println("Error creating booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error creating booking: " + e.getMessage());
            return "redirect:/customer/booking/new";
        }
    }

    @GetMapping("/bookings")
    public String viewBookings(Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get user's reservations
            List<Reservation> reservations = reservationService.getReservationsByUserIdOrderByDate(currentUser.getUserID());
            
            // Get payment information for each reservation
            Map<Long, Payment> paymentMap = new HashMap<>();
            for (Reservation reservation : reservations) {
                Optional<Payment> payment = paymentService.getPaymentByReservationId(reservation.getId());
                if (payment.isPresent()) {
                    paymentMap.put(reservation.getId(), payment.get());
                }
            }
            
            model.addAttribute("reservations", reservations);
            model.addAttribute("payments", paymentMap);
            model.addAttribute("user", currentUser);
            
            System.out.println("Bookings loaded for user: " + currentUser.getEmail());
            System.out.println("Total bookings: " + reservations.size());
            
        } catch (Exception e) {
            System.err.println("Error loading bookings: " + e.getMessage());
            model.addAttribute("error", "Error loading bookings");
        }
        
        return "customer-bookings";
    }

    @PostMapping("/booking/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "User not authenticated");
                return "redirect:/login";
            }
            
            Reservation reservation = reservationService.getReservationById(id).orElse(null);
            if (reservation == null || !reservation.getUserId().equals(currentUser.getUserID())) {
                redirectAttributes.addFlashAttribute("error", "Booking not found or access denied");
                return "redirect:/customer/bookings";
            }
            
            // Check if booking can be cancelled (time-based validation)
            if (!reservationService.canBeCancelled(reservation)) {
                String message = reservationService.getCancellationMessage(reservation);
                redirectAttributes.addFlashAttribute("error", message);
                return "redirect:/customer/bookings";
            }
            
            // Update reservation status
            reservationService.updateReservationStatus(id, "CANCELLED");
            
            // Observer Pattern: Notify all observers about reservation cancellation
            // This triggers notifications across all registered channels (Email, SMS, In-App)
            System.out.println("=== Triggering Observer Pattern for Reservation Cancellation ===");
            // Update the reservation object status for accurate notification
            reservation.setStatus("CANCELLED");
            notificationManager.notifyObservers(reservation, "CANCELLED");
            
            // Free up the parking slot if it was occupied
            if ("ACTIVE".equals(reservation.getStatus()) || "CONFIRMED".equals(reservation.getStatus())) {
                parkingSlotService.updateSlotStatus(reservation.getSlotId(), "AVAILABLE");
            }
            
            System.out.println("Booking cancelled successfully:");
            System.out.println("- Reservation ID: " + id);
            System.out.println("- User: " + currentUser.getEmail());
            System.out.println("- Original Start Time: " + reservation.getStartTime());
            System.out.println("- Cancellation Time: " + java.time.LocalDateTime.now());
            
            // Send cancellation notifications (email + SMS)
            try {
                // Get payment amount from payment table
                Optional<Payment> paymentOpt = paymentService.getPaymentByReservationId(reservation.getId());
                Double refundAmount = 0.0;
                
                if (paymentOpt.isPresent()) {
                    Payment payment = paymentOpt.get();
                    // Calculate refund amount (full refund if cancelled within 1 hour of booking)
                    LocalDateTime now = LocalDateTime.now();
                    long minutesSinceCreation = ChronoUnit.MINUTES.between(reservation.getCreatedAt(), now);
                    
                    if (minutesSinceCreation <= 60 && "COMPLETED".equals(payment.getStatus())) {
                        refundAmount = payment.getAmount().doubleValue();
                        // Update payment status to indicate refund
                        payment.setStatus("REFUNDED");
                        paymentService.savePayment(payment);
                    }
                }
                
                String reason = "Cancelled by customer within allowed time frame";
                notificationService.sendBookingCancellation(reservation, reason, refundAmount);
                System.out.println("Booking cancellation notifications sent (email + SMS)");
                System.out.println("- Refund amount: $" + refundAmount);
            } catch (Exception notificationError) {
                System.err.println("Warning: Failed to send cancellation notifications: " + notificationError.getMessage());
            }
            
            redirectAttributes.addFlashAttribute("success", "Booking cancelled successfully");
            
        } catch (Exception e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error cancelling booking: " + e.getMessage());
        }
        
        return "redirect:/customer/bookings";
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                
                return userService.getAllUsers().stream()
                    .filter(user -> user.getEmail().equals(username))
                    .findFirst()
                    .orElse(null);
            }
        } catch (Exception e) {
            System.err.println("Error getting current user: " + e.getMessage());
        }
        
        return null;
    }
    
    private List<ParkingSlot> createDefaultTestSlots() {
        List<ParkingSlot> testSlots = new ArrayList<>();
        
        // Create some default test parking slots
        for (int i = 1; i <= 5; i++) {
            ParkingSlot slot = new ParkingSlot();
            slot.setId((long) i);
            slot.setLocation("Ground Floor - Section A" + i);
            slot.setStatus("AVAILABLE");
            testSlots.add(slot);
        }
        
        System.out.println("Created " + testSlots.size() + " default test slots");
        return testSlots;
    }
    
    @GetMapping("/booking/{id}/view")
    public String viewBooking(@PathVariable Long id, Model model) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get reservation details
            Reservation reservation = reservationService.getReservationById(id).orElse(null);
            if (reservation == null) {
                model.addAttribute("error", "Booking not found");
                return "redirect:/customer/bookings";
            }
            
            // Check if reservation belongs to current user
            if (!reservation.getUserId().equals(currentUser.getUserID())) {
                model.addAttribute("error", "Access denied");
                return "redirect:/customer/bookings";
            }
            
            // Get parking slot details
            ParkingSlot slot = parkingSlotService.getParkingSlotById(reservation.getSlotId()).orElse(null);
            
            // Get payment details
            Payment payment = paymentService.getPaymentByReservationId(reservation.getId()).orElse(null);
            
            model.addAttribute("reservation", reservation);
            model.addAttribute("slot", slot);
            model.addAttribute("payment", payment);
            model.addAttribute("user", currentUser);
            
            System.out.println("Viewing booking: " + id + " for user: " + currentUser.getEmail());
            
        } catch (Exception e) {
            System.err.println("Error viewing booking: " + e.getMessage());
            model.addAttribute("error", "Error loading booking details");
            return "redirect:/customer/bookings";
        }
        
        return "customer-booking-view";
    }
    
    @GetMapping("/booking/{id}/edit")
    public String editBookingForm(@PathVariable Long id, Model model) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get reservation details
            Reservation reservation = reservationService.getReservationById(id).orElse(null);
            if (reservation == null) {
                model.addAttribute("error", "Booking not found");
                return "redirect:/customer/bookings";
            }
            
            // Check if reservation belongs to current user
            if (!reservation.getUserId().equals(currentUser.getUserID())) {
                model.addAttribute("error", "Access denied");
                return "redirect:/customer/bookings";
            }
            
            // Check if booking can be edited (only CONFIRMED bookings can be edited)
            if (!"CONFIRMED".equals(reservation.getStatus())) {
                model.addAttribute("error", "Only confirmed bookings can be edited");
                return "redirect:/customer/bookings";
            }
            
            // Get parking slot details
            ParkingSlot slot = parkingSlotService.getParkingSlotById(reservation.getSlotId()).orElse(null);
            
            // Get payment details
            Payment payment = paymentService.getPaymentByReservationId(reservation.getId()).orElse(null);
            
            model.addAttribute("reservation", reservation);
            model.addAttribute("slot", slot);
            model.addAttribute("payment", payment);
            model.addAttribute("user", currentUser);
            
            System.out.println("Editing booking: " + id + " for user: " + currentUser.getEmail());
            
        } catch (Exception e) {
            System.err.println("Error loading booking for edit: " + e.getMessage());
            model.addAttribute("error", "Error loading booking details");
            return "redirect:/customer/bookings";
        }
        
        return "customer-booking-edit";
    }
    
    @PostMapping("/booking/{id}/update")
    public String updateBooking(@PathVariable Long id,
                               @RequestParam String startTimeStr,
                               @RequestParam String endTimeStr,
                               RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get existing reservation
            Reservation reservation = reservationService.getReservationById(id).orElse(null);
            if (reservation == null) {
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/customer/bookings";
            }
            
            // Check if reservation belongs to current user
            if (!reservation.getUserId().equals(currentUser.getUserID())) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/customer/bookings";
            }
            
            // Check if booking can be edited
            if (!"CONFIRMED".equals(reservation.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "Only confirmed bookings can be edited");
                return "redirect:/customer/bookings";
            }
            
            // Parse new times
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            
            // Validate times
            LocalDateTime now = LocalDateTime.now();
            if (startTime.isBefore(now.plusMinutes(5))) {
                redirectAttributes.addFlashAttribute("error", "Start time must be at least 5 minutes from now");
                return "redirect:/customer/booking/" + id + "/edit";
            }
            
            if (endTime.isBefore(startTime.plusMinutes(30))) {
                redirectAttributes.addFlashAttribute("error", "Booking duration must be at least 30 minutes");
                return "redirect:/customer/booking/" + id + "/edit";
            }
            
            // Calculate new duration and cost
            long durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
            double durationHours = Math.ceil(durationMinutes / 60.0);
            double pricePerHour = 5.0; // Fixed price
            double totalAmount = durationHours * pricePerHour;
            
            // Update reservation
            reservation.setStartTime(startTime);
            reservation.setEndTime(endTime);
            reservation.setUpdatedAt(LocalDateTime.now());
            
            // Update payment amount in Payment table
            Optional<Payment> paymentOpt = paymentService.getPaymentByReservationId(reservation.getId());
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setAmount(java.math.BigDecimal.valueOf(totalAmount));
                paymentService.savePayment(payment);
                System.out.println("Payment amount updated: $" + totalAmount);
            }
            
            // Save updated reservation
            Reservation updatedReservation = reservationService.saveReservation(reservation);
            
            System.out.println("Booking updated successfully:");
            System.out.println("- Reservation ID: " + updatedReservation.getId());
            System.out.println("- New Start Time: " + startTime);
            System.out.println("- New End Time: " + endTime);
            System.out.println("- New Amount: $" + totalAmount);
            
            redirectAttributes.addFlashAttribute("success", "Booking updated successfully!");
            return "redirect:/customer/booking/" + id + "/view";
            
        } catch (Exception e) {
            System.err.println("Error updating booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating booking: " + e.getMessage());
            return "redirect:/customer/booking/" + id + "/edit";
        }
    }
}
