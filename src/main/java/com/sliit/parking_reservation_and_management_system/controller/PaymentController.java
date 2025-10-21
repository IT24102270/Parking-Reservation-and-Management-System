package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.Payment;
import com.sliit.parking_reservation_and_management_system.entity.Reservation;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.PaymentService;
import com.sliit.parking_reservation_and_management_system.service.ReservationService;
import com.sliit.parking_reservation_and_management_system.service.SlotAvailabilityService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import com.sliit.parking_reservation_and_management_system.service.NotificationService;
import com.sliit.parking_reservation_and_management_system.service.ParkingSlotService;
import com.sliit.parking_reservation_and_management_system.strategy.PaymentContext;
import com.sliit.parking_reservation_and_management_system.dto.PaymentRequest;
import com.sliit.parking_reservation_and_management_system.dto.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
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
    
    @Autowired
    private SlotAvailabilityService slotAvailabilityService;
    
    @Autowired
    private PaymentContext paymentContext;

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
                                 @RequestParam(required = false) String cardNumber,
                                 @RequestParam(required = false) String cardHolderName,
                                 @RequestParam(required = false) String expiryDate,
                                 @RequestParam(required = false) String cvv,
                                 @RequestParam(required = false) String paypalEmail,
                                 @RequestParam(required = false) String bankAccountNumber,
                                 @RequestParam(required = false) String bankRoutingNumber,
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
            
            // Create payment request for strategy pattern
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setPaymentMethod(paymentMethod.toUpperCase());
            paymentRequest.setAmount(payment.getAmount().doubleValue());
            paymentRequest.setCurrency("USD");
            paymentRequest.setDescription("Parking reservation payment for slot " + reservation.getSlotId());
            paymentRequest.setReservationId(reservation.getId().toString());
            paymentRequest.setUserId(currentUser.getUserID().toString());
            
            // Set payment method specific details
            if ("CREDIT_CARD".equalsIgnoreCase(paymentMethod) || "DEBIT_CARD".equalsIgnoreCase(paymentMethod)) {
                paymentRequest.setCardNumber(cardNumber);
                paymentRequest.setCardHolderName(cardHolderName);
                paymentRequest.setExpiryDate(expiryDate);
                paymentRequest.setCvv(cvv);
            } else if ("PAYPAL".equalsIgnoreCase(paymentMethod)) {
                paymentRequest.setPaypalEmail(paypalEmail);
            } else if ("BANK_TRANSFER".equalsIgnoreCase(paymentMethod)) {
                paymentRequest.setBankAccountNumber(bankAccountNumber);
                paymentRequest.setBankRoutingNumber(bankRoutingNumber);
            } else if ("DIGITAL_WALLET".equalsIgnoreCase(paymentMethod)) {
                // Digital wallet doesn't need additional details
                // The wallet is already authenticated through the UI
            }
            
            // Process payment using strategy pattern
            System.out.println("🔄 Processing payment using Strategy Pattern:");
            System.out.println("   Payment Method: " + paymentMethod);
            System.out.println("   Amount: $" + payment.getAmount());
            
            PaymentResponse paymentResponse = paymentContext.processPaymentWithAutoStrategy(paymentRequest);
            
            if (paymentResponse.isSuccess()) {
                // Complete the payment using existing service
                paymentService.completePayment(id, paymentMethod);
                
                System.out.println("✅ Payment processed successfully:");
                System.out.println("   Transaction ID: " + paymentResponse.getTransactionId());
                System.out.println("   Message: " + paymentResponse.getMessage());
                // Update reservation status to CONFIRMED
                reservationService.updateReservationStatus(reservation.getId(), "CONFIRMED");
                
                // Check if booking should start immediately (within 10 minutes)
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                if (reservation.getStartTime().isBefore(now.plusMinutes(10))) {
                    // Use SlotAvailabilityService to properly manage slot status
                    slotAvailabilityService.occupySlot(reservation.getSlotId(), reservation.getId());
                    
                    // Also update reservation to ACTIVE if it should start now
                    if (reservation.getStartTime().isBefore(now.plusMinutes(2))) {
                        reservationService.updateReservationStatus(reservation.getId(), "ACTIVE");
                        System.out.println("Reservation " + reservation.getId() + " activated immediately after payment");
                    }
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
                System.out.println("❌ Payment processing failed:");
                System.out.println("   Error Code: " + paymentResponse.getErrorCode());
                System.out.println("   Error Message: " + paymentResponse.getMessage());
                
                redirectAttributes.addFlashAttribute("error", 
                    "Payment processing failed: " + paymentResponse.getMessage());
            }
            
            return "redirect:/customer/bookings";
            
        } catch (Exception e) {
            System.err.println("Error completing payment: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error processing payment: " + e.getMessage());
            return "redirect:/customer/bookings";
        }
    }
    
    /**
     * Get available payment methods and their details
     */
    @GetMapping("/payment-methods")
    @ResponseBody
    public String getPaymentMethods() {
        try {
            Map<String, Object> strategyInfo = paymentContext.getStrategyInfo();
            
            StringBuilder response = new StringBuilder();
            response.append("<h2>💳 Available Payment Methods</h2>");
            response.append("<div class='payment-methods'>");
            
            String[] availableMethods = (String[]) strategyInfo.get("availableMethods");
            @SuppressWarnings("unchecked")
            Map<String, Object> methodDetails = (Map<String, Object>) strategyInfo.get("methodDetails");
            
            for (String method : availableMethods) {
                @SuppressWarnings("unchecked")
                Map<String, Object> details = (Map<String, Object>) methodDetails.get(method);
                response.append("<div class='payment-method'>");
                response.append("<h3>").append(method.replace("_", " ")).append("</h3>");
                response.append("<p><strong>Processing Time:</strong> ").append(details.get("processingTime")).append(" minutes</p>");
                response.append("<p><strong>Processing Fee:</strong> ").append(details.get("processingFeeRate")).append("%</p>");
                response.append("</div>");
            }
            
            response.append("</div>");
            response.append("<p><a href='/customer/payments'>← Back to Payments</a></p>");
            
            return response.toString();
            
        } catch (Exception e) {
            return "<h2>❌ Error</h2><p>Failed to load payment methods: " + e.getMessage() + "</p>";
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
