package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.entity.ParkingSlot;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.NotificationService;
import com.sliit.parking_reservation_and_management_system.service.PaymentService;
import com.sliit.parking_reservation_and_management_system.service.ParkingSlotService;
import com.sliit.parking_reservation_and_management_system.service.ReservationService;
import com.sliit.parking_reservation_and_management_system.service.SlotAvailabilityService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
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
    
    @Autowired
    private SlotAvailabilityService slotAvailabilityService;

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
            
            // Check if slot is available using the new SlotAvailabilityService
            if (!slotAvailabilityService.isSlotAvailable(slotId, startTime, endTime)) {
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
            
            // CRITICAL: Update slot availability immediately when booking is cancelled
            // This uses the new SlotAvailabilityService for proper time-based slot management
            System.out.println("=== UPDATING SLOT AVAILABILITY AFTER CANCELLATION ===");
            System.out.println("Slot ID: " + reservation.getSlotId());
            System.out.println("Original Status: " + reservation.getStatus());
            
            slotAvailabilityService.updateSlotAvailabilityNow(reservation.getSlotId());
            
            System.out.println("Slot availability updated for slot " + reservation.getSlotId());
            
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
    
    // Update booking with payment adjustment
    @PostMapping("/booking/{id}/update")
    public String updateBooking(@PathVariable Long id,
                               @RequestParam String startTimeStr,
                               @RequestParam String endTimeStr,
                               RedirectAttributes redirectAttributes) {
        // Update booking with payment adjustment logic
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get reservation details
            Reservation reservation = reservationService.getReservationById(id).orElse(null);
            if (reservation == null) {
                redirectAttributes.addFlashAttribute("error", "Booking not found");
                return "redirect:/customer/bookings";
            }
            System.out.println("Found reservation: " + reservation.getId());
            
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
            LocalDateTime startTime;
            LocalDateTime endTime;
            
            try {
                startTime = LocalDateTime.parse(startTimeStr);
                endTime = LocalDateTime.parse(endTimeStr);
            } catch (Exception parseError) {
                redirectAttributes.addFlashAttribute("error", "Invalid date/time format. Please use the date picker.");
                return "redirect:/customer/booking/" + id + "/edit";
            }
            
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
            
            // Calculate payment adjustment
            PaymentService.PaymentAdjustment adjustment;
            try {
                adjustment = paymentService.calculatePaymentAdjustment(
                    reservation.getId(), 
                    java.math.BigDecimal.valueOf(totalAmount)
                );
                
                if (adjustment == null) {
                    redirectAttributes.addFlashAttribute("error", "Failed to calculate payment adjustment. Please try again.");
                    return "redirect:/customer/booking/" + id + "/edit";
                }
            } catch (Exception adjustmentError) {
                redirectAttributes.addFlashAttribute("error", "Error calculating payment adjustment: " + adjustmentError.getMessage());
                return "redirect:/customer/booking/" + id + "/edit";
            }
            
            // Handle different adjustment scenarios
            if (adjustment.isAdditionalPaymentRequired()) {
                // Additional payment required - redirect to payment confirmation
                redirectAttributes.addFlashAttribute("paymentAdjustment", adjustment);
                redirectAttributes.addFlashAttribute("reservationId", id);
                redirectAttributes.addFlashAttribute("newStartTime", startTimeStr);
                redirectAttributes.addFlashAttribute("newEndTime", endTimeStr);
                redirectAttributes.addFlashAttribute("info", 
                    String.format("Additional payment of $%.2f is required for the time change. Please confirm to proceed.", 
                    adjustment.getAbsoluteAdjustmentAmount()));
                
                return "redirect:/customer/booking/" + id + "/payment-adjustment";
                
            } else if (adjustment.isRefundDue()) {
                // Process refund automatically
                boolean refundProcessed = paymentService.processRefund(
                    reservation.getId(), 
                    adjustment.getAbsoluteAdjustmentAmount(), 
                    "Booking time change - shorter duration"
                );
                
                if (refundProcessed) {
                    try {
                        // Update reservation and payment
                        updateReservationAndPayment(reservation, startTime, endTime, totalAmount);
                        
                        redirectAttributes.addFlashAttribute("success", 
                            String.format("Booking updated successfully! A refund of $%.2f has been processed.", 
                            adjustment.getAbsoluteAdjustmentAmount()));
                    } catch (Exception updateError) {
                        System.err.println("Error updating reservation after refund: " + updateError.getMessage());
                        redirectAttributes.addFlashAttribute("error", "Refund processed but failed to update booking. Please contact support.");
                        return "redirect:/customer/booking/" + id + "/edit";
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", "Failed to process refund. Please contact support.");
                    return "redirect:/customer/booking/" + id + "/edit";
                }
                
            } else {
                try {
                    // No payment change - update directly
                    updateReservationAndPayment(reservation, startTime, endTime, totalAmount);
                    redirectAttributes.addFlashAttribute("success", "Booking updated successfully!");
                } catch (Exception updateError) {
                    System.err.println("Error updating reservation: " + updateError.getMessage());
                    redirectAttributes.addFlashAttribute("error", "Failed to update booking: " + updateError.getMessage());
                    return "redirect:/customer/booking/" + id + "/edit";
                }
            }
            
            System.out.println("Booking updated successfully:");
            System.out.println("- Reservation ID: " + reservation.getId());
            System.out.println("- New Start Time: " + startTime);
            System.out.println("- New End Time: " + endTime);
            System.out.println("- New Amount: $" + totalAmount);
            System.out.println("- Adjustment Type: " + adjustment.getAdjustmentType());
            
            return "redirect:/customer/booking/" + id + "/view";
            
        } catch (Exception e) {
            System.err.println("Error updating booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error updating booking: " + e.getMessage());
            return "redirect:/customer/booking/" + id + "/edit";
        }
    }
    
    // Helper method to update reservation and payment
    private void updateReservationAndPayment(Reservation reservation, LocalDateTime startTime, 
                                           LocalDateTime endTime, double totalAmount) {
        try {
            // Update reservation
            reservation.setStartTime(startTime);
            reservation.setEndTime(endTime);
            reservation.setUpdatedAt(LocalDateTime.now());
            reservationService.saveReservation(reservation);
            
            // Update payment amount - create payment if it doesn't exist
            boolean paymentUpdated = paymentService.updatePaymentAmount(reservation.getId(), java.math.BigDecimal.valueOf(totalAmount));
            
            if (!paymentUpdated) {
                // Create new payment if none exists
                System.out.println("No existing payment found. Creating new payment for reservation: " + reservation.getId());
                paymentService.createPayment(
                    reservation.getId(), 
                    java.math.BigDecimal.valueOf(totalAmount), 
                    "ONLINE"
                );
            }
            
            System.out.println("Reservation and payment updated successfully");
            
        } catch (Exception e) {
            System.err.println("Error updating reservation and payment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update reservation and payment", e);
        }
    }
    
    // Payment adjustment confirmation page
    @GetMapping("/booking/{id}/payment-adjustment")
    public String showPaymentAdjustment(@PathVariable Long id, Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            Reservation reservation = reservationService.getReservationById(id).orElse(null);
            if (reservation == null || !reservation.getUserId().equals(currentUser.getUserID())) {
                model.addAttribute("error", "Booking not found or access denied");
                return "redirect:/customer/bookings";
            }
            
            model.addAttribute("reservation", reservation);
            model.addAttribute("user", currentUser);
            
            return "customer-booking-payment-adjustment";
            
        } catch (Exception e) {
            System.err.println("Error showing payment adjustment: " + e.getMessage());
            return "redirect:/customer/bookings";
        }
    }
    
    // Process payment adjustment
    @PostMapping("/booking/{id}/confirm-payment-adjustment")
    public String confirmPaymentAdjustment(@PathVariable Long id,
                                         @RequestParam String startTimeStr,
                                         @RequestParam String endTimeStr,
                                         @RequestParam String paymentMethod,
                                         RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            Reservation reservation = reservationService.getReservationById(id).orElse(null);
            if (reservation == null || !reservation.getUserId().equals(currentUser.getUserID())) {
                redirectAttributes.addFlashAttribute("error", "Booking not found or access denied");
                return "redirect:/customer/bookings";
            }
            
            // Parse new times
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            
            // Calculate new cost
            long durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
            double durationHours = Math.ceil(durationMinutes / 60.0);
            double totalAmount = durationHours * 5.0;
            
            // Calculate payment adjustment
            PaymentService.PaymentAdjustment adjustment = paymentService.calculatePaymentAdjustment(
                reservation.getId(), 
                java.math.BigDecimal.valueOf(totalAmount)
            );
            
            if (adjustment.isAdditionalPaymentRequired()) {
                try {
                    // Create additional payment
                    Payment additionalPayment = paymentService.createAdditionalPayment(
                        reservation.getId(),
                        adjustment.getAbsoluteAdjustmentAmount(),
                        "Booking time change - extended duration"
                    );
                    
                    // Complete the additional payment immediately (in real app, this would go through payment gateway)
                    boolean paymentCompleted = paymentService.completePayment(additionalPayment.getPaymentID(), paymentMethod);
                    
                    if (paymentCompleted) {
                        // Update only the reservation (payment already updated by createAdditionalPayment)
                        reservation.setStartTime(startTime);
                        reservation.setEndTime(endTime);
                        reservation.setUpdatedAt(LocalDateTime.now());
                        reservationService.saveReservation(reservation);
                        
                        redirectAttributes.addFlashAttribute("success", 
                            String.format("Booking updated successfully! Additional payment of $%.2f has been processed.", 
                            adjustment.getAbsoluteAdjustmentAmount()));
                    } else {
                        redirectAttributes.addFlashAttribute("error", "Payment processing failed. Please try again.");
                        return "redirect:/customer/booking/" + id + "/payment-adjustment";
                    }
                } catch (Exception paymentError) {
                    System.err.println("Error processing additional payment: " + paymentError.getMessage());
                    paymentError.printStackTrace();
                    redirectAttributes.addFlashAttribute("error", "Error processing payment adjustment: " + paymentError.getMessage());
                    return "redirect:/customer/booking/" + id + "/payment-adjustment";
                }
            }
            
            return "redirect:/customer/booking/" + id + "/view";
            
        } catch (Exception e) {
            System.err.println("Error confirming payment adjustment: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error processing payment adjustment: " + e.getMessage());
            return "redirect:/customer/booking/" + id + "/payment-adjustment";
        }
    }
}
