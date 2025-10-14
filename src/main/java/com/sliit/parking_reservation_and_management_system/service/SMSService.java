package com.sliit.parking_reservation_and_management_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class SMSService {
    
    @Value("${sms.api.url:https://api.twilio.com/2010-04-01/Accounts}")
    private String smsApiUrl;
    
    @Value("${sms.api.key:your-api-key}")
    private String apiKey;
    
    @Value("${sms.api.secret:your-api-secret}")
    private String apiSecret;
    
    @Value("${sms.from.number:+1234567890}")
    private String fromNumber;
    
    @Value("${app.name:Smart Parking}")
    private String appName;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // Send simple SMS
    public void sendSMS(String to, String message) {
        try {
            // For demo purposes, we'll log the SMS instead of actually sending
            // In production, integrate with actual SMS provider (Twilio, AWS SNS, etc.)
            System.out.println("=== SMS NOTIFICATION ===");
            System.out.println("To: " + to);
            System.out.println("Message: " + message);
            System.out.println("Timestamp: " + LocalDateTime.now());
            System.out.println("========================");
            
            // Uncomment below for actual SMS sending with Twilio
            // sendViaTwilio(to, message);
            
        } catch (Exception e) {
            System.err.println("Failed to send SMS to " + to + ": " + e.getMessage());
        }
    }
    
    // Booking confirmation SMS
    public void sendBookingConfirmationSMS(String to, String customerName, String bookingId, 
                                         String slotNumber, LocalDateTime startTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
        String message = String.format(
            "%s: Booking confirmed! ID: %s, Slot: %s, Time: %s. See you soon!",
            appName, bookingId, slotNumber, startTime.format(formatter)
        );
        sendSMS(to, message);
    }
    
    // Booking cancellation SMS
    public void sendBookingCancellationSMS(String to, String customerName, String bookingId) {
        String message = String.format(
            "%s: Your booking %s has been cancelled. Check email for details.",
            appName, bookingId
        );
        sendSMS(to, message);
    }
    
    // Payment reminder SMS
    public void sendPaymentReminderSMS(String to, String customerName, String bookingId, 
                                     LocalDateTime dueDate, Double amount) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm");
        String message = String.format(
            "%s: Payment reminder - $%.2f due %s for booking %s. Pay now to avoid cancellation.",
            appName, amount, dueDate.format(formatter), bookingId
        );
        sendSMS(to, message);
    }
    
    // Urgent payment deadline SMS
    public void sendPaymentDeadlineSMS(String to, String customerName, String bookingId, 
                                     int hoursLeft, Double amount) {
        String message = String.format(
            "URGENT - %s: Payment of $%.2f due in %d hours for booking %s. Pay now or booking will be cancelled!",
            appName, amount, hoursLeft, bookingId
        );
        sendSMS(to, message);
    }
    
    // Security alert SMS
    public void sendSecurityAlertSMS(String to, String customerName, String alertType) {
        String message = String.format(
            "%s SECURITY ALERT: %s detected on your account. Check email for details or call support immediately.",
            appName, alertType
        );
        sendSMS(to, message);
    }
    
    // Booking reminder SMS (before start time)
    public void sendBookingReminderSMS(String to, String customerName, String bookingId, 
                                     String slotNumber, int minutesBefore) {
        String message = String.format(
            "%s: Reminder - Your parking at slot %s starts in %d minutes. Booking: %s",
            appName, slotNumber, minutesBefore, bookingId
        );
        sendSMS(to, message);
    }
    
    // Booking expiry warning SMS
    public void sendBookingExpirySMS(String to, String customerName, String bookingId, 
                                   int minutesLeft) {
        String message = String.format(
            "%s: WARNING - Your parking expires in %d minutes! Booking: %s. Move vehicle to avoid charges.",
            appName, minutesLeft, bookingId
        );
        sendSMS(to, message);
    }
    
    // Parking violation SMS
    public void sendParkingViolationSMS(String to, String customerName, String violationType, 
                                      String slotNumber) {
        String message = String.format(
            "%s: Parking violation detected - %s at slot %s. Contact security immediately.",
            appName, violationType, slotNumber
        );
        sendSMS(to, message);
    }
    
    // Account suspension SMS
    public void sendAccountSuspensionSMS(String to, String customerName, String reason) {
        String message = String.format(
            "%s: Your account has been suspended due to %s. Contact support for assistance.",
            appName, reason
        );
        sendSMS(to, message);
    }
    
    // Emergency notification SMS
    public void sendEmergencyNotificationSMS(String to, String customerName, String emergencyType, 
                                           String location) {
        String message = String.format(
            "%s EMERGENCY: %s reported at %s. Please evacuate immediately if in the area.",
            appName, emergencyType, location
        );
        sendSMS(to, message);
    }
    
    // Private method for actual Twilio integration (commented for demo)
    /*
    private void sendViaTwilio(String to, String message) {
        try {
            String accountSid = apiKey;
            String authToken = apiSecret;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(accountSid, authToken);
            
            Map<String, String> params = new HashMap<>();
            params.put("From", fromNumber);
            params.put("To", to);
            params.put("Body", message);
            
            String requestBody = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
            
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            String url = smsApiUrl + "/" + accountSid + "/Messages.json";
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            
            System.out.println("SMS sent successfully via Twilio to: " + to);
            
        } catch (Exception e) {
            System.err.println("Failed to send SMS via Twilio: " + e.getMessage());
            throw e;
        }
    }
    */
    
    // Validate phone number format
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove all non-digit characters
        String cleanNumber = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Check if it's a valid format (basic validation)
        return cleanNumber.matches("^\\+?[1-9]\\d{1,14}$");
    }
    
    // Format phone number for SMS
    public String formatPhoneNumber(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            return null;
        }
        
        String cleanNumber = phoneNumber.replaceAll("[^0-9+]", "");
        
        // Add country code if not present (assuming Sri Lanka +94)
        if (!cleanNumber.startsWith("+")) {
            if (cleanNumber.startsWith("0")) {
                cleanNumber = "+94" + cleanNumber.substring(1);
            } else if (cleanNumber.length() == 9) {
                cleanNumber = "+94" + cleanNumber;
            } else {
                cleanNumber = "+" + cleanNumber;
            }
        }
        
        return cleanNumber;
    }
    
    // Check if SMS notifications are enabled for user
    public boolean isSMSEnabled(String phoneNumber) {
        // In a real implementation, check user preferences from database
        return phoneNumber != null && !phoneNumber.trim().isEmpty() && isValidPhoneNumber(phoneNumber);
    }
}
