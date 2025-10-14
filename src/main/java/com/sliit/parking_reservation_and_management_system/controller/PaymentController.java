package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.PaymentService;
import com.sliit.parking_reservation_and_management_system.service.ReservationService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import com.sliit.parking_reservation_and_management_system.service.NotificationService;
import com.sliit.parking_reservation_and_management_system.service.ParkingSlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private ReservationService reservationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private ParkingSlotService parkingSlotService;

    @GetMapping("/payments")
    public String viewPayments(Model model) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get user's reservations and their payments
            List<Reservation> userReservations = reservationService.getReservationsByUserId(currentUser.getUserID());
            
            // Get payments for user's reservations
            List<Payment> userPayments = userReservations.stream()
                .map(reservation -> paymentService.getPaymentByReservationId(reservation.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
            
            model.addAttribute("user", currentUser);
            model.addAttribute("payments", userPayments);
            model.addAttribute("reservations", userReservations);
            
            System.out.println("Payments loaded for user: " + currentUser.getEmail());
            System.out.println("Total payments: " + userPayments.size());
            
        } catch (Exception e) {
            System.err.println("Error loading payments: " + e.getMessage());
            model.addAttribute("error", "Error loading payments");
        }
        
        return "customer-payments";
    }
    
    @GetMapping("/payment/{id}")
    public String viewPayment(@PathVariable Long id, Model model) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get payment details
            Payment payment = paymentService.getPaymentById(id).orElse(null);
            if (payment == null) {
                model.addAttribute("error", "Payment not found");
                return "redirect:/customer/payments";
            }
            
            // Get associated reservation
            Reservation reservation = reservationService.getReservationById(payment.getReservationID()).orElse(null);
            if (reservation == null || !reservation.getUserId().equals(currentUser.getUserID())) {
                model.addAttribute("error", "Access denied");
                return "redirect:/customer/payments";
            }
            
            model.addAttribute("user", currentUser);
            model.addAttribute("payment", payment);
            model.addAttribute("reservation", reservation);
            
            System.out.println("Payment details loaded: " + payment.getPaymentID());
            
        } catch (Exception e) {
            System.err.println("Error loading payment details: " + e.getMessage());
            model.addAttribute("error", "Error loading payment details");
            return "redirect:/customer/payments";
        }
        
        return "customer-payment-details";
    }
    
    @PostMapping("/payment/{id}/cancel")
    public String cancelPayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get payment details
            Payment payment = paymentService.getPaymentById(id).orElse(null);
            if (payment == null) {
                redirectAttributes.addFlashAttribute("error", "Payment not found");
                return "redirect:/customer/payments";
            }
            
            // Verify user ownership
            Reservation reservation = reservationService.getReservationById(payment.getReservationID()).orElse(null);
            if (reservation == null || !reservation.getUserId().equals(currentUser.getUserID())) {
                redirectAttributes.addFlashAttribute("error", "Access denied");
                return "redirect:/customer/payments";
            }
            
            // Cancel the payment
            boolean cancelled = paymentService.cancelPayment(id);
            
            if (cancelled) {
                redirectAttributes.addFlashAttribute("success", "Payment cancelled successfully");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to cancel payment");
            }
            
        } catch (Exception e) {
            System.err.println("Error cancelling payment: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error cancelling payment");
        }
        
        return "redirect:/customer/payments";
    }
    
    @GetMapping("/payment/{id}/pay")
    public String showPaymentPage(@PathVariable Long id, Model model) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            Payment payment = paymentService.getPaymentById(id).orElse(null);
            if (payment == null) {
                model.addAttribute("error", "Payment not found");
                return "redirect:/customer/bookings";
            }
            
            Reservation reservation = reservationService.getReservationById(payment.getReservationID()).orElse(null);
            if (reservation == null || !reservation.getUserId().equals(currentUser.getUserID())) {
                model.addAttribute("error", "Reservation not found or access denied");
                return "redirect:/customer/bookings";
            }
            
            model.addAttribute("payment", payment);
            model.addAttribute("reservation", reservation);
            model.addAttribute("user", currentUser);
            
            return "customer-payment-form";
            
        } catch (Exception e) {
            System.err.println("Error showing payment page: " + e.getMessage());
            return "redirect:/customer/bookings";
        }
    }
    
    @PostMapping("/payment/{id}/complete")
    public String completePayment(@PathVariable Long id, 
                                 @RequestParam String paymentMethod,
                                 RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            Payment payment = paymentService.getPaymentById(id).orElse(null);
            if (payment == null) {
                redirectAttributes.addFlashAttribute("error", "Payment not found");
                return "redirect:/customer/bookings";
            }
            
            Reservation reservation = reservationService.getReservationById(payment.getReservationID()).orElse(null);
            if (reservation == null || !reservation.getUserId().equals(currentUser.getUserID())) {
                redirectAttributes.addFlashAttribute("error", "Reservation not found or access denied");
                return "redirect:/customer/bookings";
            }
            
            // Complete the payment
            boolean paymentCompleted = paymentService.completePayment(id, paymentMethod);
            
            if (paymentCompleted) {
                // Update reservation status to CONFIRMED
                reservationService.updateReservationStatus(reservation.getId(), "CONFIRMED");
                
                // Update parking slot status if booking starts soon
                if (reservation.getStartTime().isBefore(java.time.LocalDateTime.now().plusMinutes(10))) {
                    parkingSlotService.updateSlotStatus(reservation.getSlotId(), "OCCUPIED");
                }
                
                // Send booking confirmation notifications
                try {
                    notificationService.sendBookingConfirmation(reservation);
                    System.out.println("Booking confirmation notifications sent after payment completion");
                } catch (Exception notificationError) {
                    System.err.println("Warning: Failed to send notifications: " + notificationError.getMessage());
                }
                
                redirectAttributes.addFlashAttribute("success", 
                    "Payment completed successfully! Your booking is now confirmed.");
                
                System.out.println("Payment completed and booking confirmed:");
                System.out.println("- Payment ID: " + payment.getPaymentID());
                System.out.println("- Reservation ID: " + reservation.getId());
                System.out.println("- Amount: $" + payment.getAmount());
                System.out.println("- Payment Method: " + paymentMethod);
                
            } else {
                redirectAttributes.addFlashAttribute("error", "Payment processing failed. Please try again.");
            }
            
            return "redirect:/customer/bookings";
            
        } catch (Exception e) {
            System.err.println("Error completing payment: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error processing payment: " + e.getMessage());
            return "redirect:/customer/bookings";
        }
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
        
        // Return default user for testing if no authentication
        User defaultUser = new User();
        defaultUser.setUserID(1L);
        defaultUser.setFirstName("Test");
        defaultUser.setLastName("User");
        defaultUser.setEmail("test@example.com");
        return defaultUser;
    }
}
