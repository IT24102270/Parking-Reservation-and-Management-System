package com.sliit.parking_reservation_and_management_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username:noreply@smartparking.lk}")
    private String fromEmail;
    
    @Value("${app.name:Smart Parking System}")
    private String appName;
    
    // Send simple text email
    public void sendSimpleEmail(String to, String subject, String text) {
        if (mailSender == null) {
            System.out.println("=== EMAIL (Mail sender not configured) ===");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Message: " + text);
            System.out.println("==========================================");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            System.out.println("Simple email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send simple email to " + to + ": " + e.getMessage());
        }
    }
    
    // Send HTML email
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (mailSender == null) {
            System.out.println("=== HTML EMAIL (Mail sender not configured) ===");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("HTML Content: " + htmlContent.substring(0, Math.min(200, htmlContent.length())) + "...");
            System.out.println("===============================================");
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("HTML email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email to " + to + ": " + e.getMessage());
        }
    }
    
    // Booking confirmation email
    public void sendBookingConfirmation(String to, String customerName, String bookingId, 
                                      String slotNumber, LocalDateTime startTime, LocalDateTime endTime, 
                                      Double amount) {
        String subject = "Booking Confirmed - " + bookingId;
        String htmlContent = createBookingConfirmationTemplate(customerName, bookingId, slotNumber, 
                                                             startTime, endTime, amount);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    // Booking cancellation email
    public void sendBookingCancellation(String to, String customerName, String bookingId, 
                                      String reason, Double refundAmount) {
        String subject = "Booking Cancelled - " + bookingId;
        String htmlContent = createBookingCancellationTemplate(customerName, bookingId, reason, refundAmount);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    // Payment reminder email
    public void sendPaymentReminder(String to, String customerName, String bookingId, 
                                  LocalDateTime dueDate, Double amount) {
        String subject = "Payment Reminder - " + bookingId;
        String htmlContent = createPaymentReminderTemplate(customerName, bookingId, dueDate, amount);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    // Payment deadline warning
    public void sendPaymentDeadlineWarning(String to, String customerName, String bookingId, 
                                         LocalDateTime dueDate, Double amount, int hoursLeft) {
        String subject = "URGENT: Payment Due Soon - " + bookingId;
        String htmlContent = createPaymentDeadlineTemplate(customerName, bookingId, dueDate, amount, hoursLeft);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    // Security alert email
    public void sendSecurityAlert(String to, String customerName, String alertType, 
                                String description, LocalDateTime timestamp) {
        String subject = "Security Alert - " + alertType;
        String htmlContent = createSecurityAlertTemplate(customerName, alertType, description, timestamp);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    // Booking reminder (before start time)
    public void sendBookingReminder(String to, String customerName, String bookingId, 
                                  String slotNumber, LocalDateTime startTime, int minutesBefore) {
        String subject = "Parking Reminder - " + bookingId;
        String htmlContent = createBookingReminderTemplate(customerName, bookingId, slotNumber, 
                                                         startTime, minutesBefore);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    // Booking expiry warning
    public void sendBookingExpiryWarning(String to, String customerName, String bookingId, 
                                       LocalDateTime endTime, int minutesLeft) {
        String subject = "Parking Expires Soon - " + bookingId;
        String htmlContent = createBookingExpiryTemplate(customerName, bookingId, endTime, minutesLeft);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    // Email Templates
    private String createBookingConfirmationTemplate(String customerName, String bookingId, 
                                                   String slotNumber, LocalDateTime startTime, 
                                                   LocalDateTime endTime, Double amount) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #8b5cf6, #7c3aed); color: white; padding: 20px; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .booking-details { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .detail-row { display: flex; justify-content: space-between; margin: 10px 0; }
                    .label { font-weight: bold; color: #6b7280; }
                    .value { color: #1f2937; }
                    .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                    .success { color: #10b981; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Booking Confirmed!</h1>
                        <p>Your parking reservation has been successfully confirmed.</p>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>Great news! Your parking reservation has been <span class="success">confirmed</span>.</p>
                        
                        <div class="booking-details">
                            <h3>Booking Details</h3>
                            <div class="detail-row">
                                <span class="label">Booking ID:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Parking Slot:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Start Time:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">End Time:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="label">Amount:</span>
                                <span class="value">$%.2f</span>
                            </div>
                        </div>
                        
                        <p><strong>Important:</strong> Please arrive on time and ensure your vehicle is parked within the designated slot.</p>
                        
                        <div class="footer">
                            <p>Thank you for choosing %s!</p>
                            <p>Need help? Contact us at support@smartparking.lk</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, bookingId, slotNumber, 
                startTime.format(formatter), endTime.format(formatter), 
                amount, appName);
    }
    
    private String createBookingCancellationTemplate(String customerName, String bookingId, 
                                                   String reason, Double refundAmount) {
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; padding: 20px; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .refund-info { background: #fef3c7; padding: 15px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #f59e0b; }
                    .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>❌ Booking Cancelled</h1>
                        <p>Your parking reservation has been cancelled.</p>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>Your booking <strong>%s</strong> has been cancelled.</p>
                        <p><strong>Reason:</strong> %s</p>
                        
                        %s
                        
                        <p>We apologize for any inconvenience caused. You can make a new reservation anytime through our platform.</p>
                        
                        <div class="footer">
                            <p>Thank you for choosing %s!</p>
                            <p>Need help? Contact us at support@smartparking.lk</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, bookingId, reason, 
                refundAmount != null && refundAmount > 0 ? 
                    String.format("<div class=\"refund-info\"><strong>Refund Information:</strong> $%.2f will be refunded to your account within 3-5 business days.</div>", refundAmount) : "",
                appName);
    }
    
    private String createPaymentReminderTemplate(String customerName, String bookingId, 
                                               LocalDateTime dueDate, Double amount) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; padding: 20px; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .payment-info { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #f59e0b; }
                    .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                    .amount { font-size: 24px; font-weight: bold; color: #f59e0b; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>💳 Payment Reminder</h1>
                        <p>Your payment is due soon.</p>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>This is a friendly reminder that your payment for booking <strong>%s</strong> is due.</p>
                        
                        <div class="payment-info">
                            <h3>Payment Details</h3>
                            <p><strong>Amount Due:</strong> <span class="amount">$%.2f</span></p>
                            <p><strong>Due Date:</strong> %s</p>
                            <p><strong>Booking ID:</strong> %s</p>
                        </div>
                        
                        <p>Please complete your payment before the due date to avoid cancellation of your booking.</p>
                        
                        <div class="footer">
                            <p>Thank you for choosing %s!</p>
                            <p>Need help? Contact us at support@smartparking.lk</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, bookingId, amount, dueDate.format(formatter), bookingId, appName);
    }
    
    private String createPaymentDeadlineTemplate(String customerName, String bookingId, 
                                               LocalDateTime dueDate, Double amount, int hoursLeft) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; padding: 20px; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .urgent-info { background: #fef2f2; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #ef4444; }
                    .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                    .amount { font-size: 24px; font-weight: bold; color: #ef4444; }
                    .urgent { color: #ef4444; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚨 URGENT: Payment Due Soon</h1>
                        <p>Your booking will be cancelled if payment is not received.</p>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p class="urgent">URGENT: Your payment is due in %d hours!</p>
                        
                        <div class="urgent-info">
                            <h3>⚠️ Immediate Action Required</h3>
                            <p><strong>Amount Due:</strong> <span class="amount">$%.2f</span></p>
                            <p><strong>Due Date:</strong> %s</p>
                            <p><strong>Time Left:</strong> <span class="urgent">%d hours</span></p>
                            <p><strong>Booking ID:</strong> %s</p>
                        </div>
                        
                        <p><strong>Warning:</strong> If payment is not received by the due date, your booking will be automatically cancelled.</p>
                        
                        <div class="footer">
                            <p>Thank you for choosing %s!</p>
                            <p>Need help? Contact us at support@smartparking.lk</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, hoursLeft, amount, dueDate.format(formatter), hoursLeft, bookingId, appName);
    }
    
    private String createSecurityAlertTemplate(String customerName, String alertType, 
                                             String description, LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #dc2626, #991b1b); color: white; padding: 20px; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .alert-info { background: #fef2f2; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #dc2626; }
                    .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔒 Security Alert</h1>
                        <p>Important security notification for your account.</p>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>We detected a security event related to your account.</p>
                        
                        <div class="alert-info">
                            <h3>Security Event Details</h3>
                            <p><strong>Alert Type:</strong> %s</p>
                            <p><strong>Description:</strong> %s</p>
                            <p><strong>Time:</strong> %s</p>
                        </div>
                        
                        <p>If this was not you, please contact our security team immediately.</p>
                        
                        <div class="footer">
                            <p>Thank you for choosing %s!</p>
                            <p>Security concerns? Contact us at security@smartparking.lk</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, alertType, description, timestamp.format(formatter), appName);
    }
    
    private String createBookingReminderTemplate(String customerName, String bookingId, 
                                               String slotNumber, LocalDateTime startTime, 
                                               int minutesBefore) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #3b82f6, #2563eb); color: white; padding: 20px; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .reminder-info { background: white; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #3b82f6; }
                    .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⏰ Parking Reminder</h1>
                        <p>Your parking reservation starts soon.</p>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p>This is a reminder that your parking reservation starts in %d minutes.</p>
                        
                        <div class="reminder-info">
                            <h3>Booking Details</h3>
                            <p><strong>Booking ID:</strong> %s</p>
                            <p><strong>Parking Slot:</strong> %s</p>
                            <p><strong>Start Time:</strong> %s</p>
                        </div>
                        
                        <p>Please make sure to arrive on time to secure your parking spot.</p>
                        
                        <div class="footer">
                            <p>Thank you for choosing %s!</p>
                            <p>Need help? Contact us at support@smartparking.lk</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, minutesBefore, bookingId, slotNumber, startTime.format(formatter), appName);
    }
    
    private String createBookingExpiryTemplate(String customerName, String bookingId, 
                                             LocalDateTime endTime, int minutesLeft) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm");
        
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; padding: 20px; border-radius: 10px 10px 0 0; }
                    .content { background: #f9fafb; padding: 30px; border-radius: 0 0 10px 10px; }
                    .expiry-info { background: #fef3c7; padding: 20px; border-radius: 8px; margin: 20px 0; border-left: 4px solid #f59e0b; }
                    .footer { text-align: center; margin-top: 30px; color: #6b7280; font-size: 14px; }
                    .warning { color: #f59e0b; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Parking Expires Soon</h1>
                        <p>Your parking reservation is about to expire.</p>
                    </div>
                    <div class="content">
                        <p>Dear %s,</p>
                        <p class="warning">Your parking reservation expires in %d minutes!</p>
                        
                        <div class="expiry-info">
                            <h3>Expiry Details</h3>
                            <p><strong>Booking ID:</strong> %s</p>
                            <p><strong>End Time:</strong> %s</p>
                            <p><strong>Time Left:</strong> <span class="warning">%d minutes</span></p>
                        </div>
                        
                        <p>Please move your vehicle before the expiry time to avoid additional charges.</p>
                        
                        <div class="footer">
                            <p>Thank you for choosing %s!</p>
                            <p>Need help? Contact us at support@smartparking.lk</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, minutesLeft, bookingId, endTime.format(formatter), minutesLeft, appName);
    }
}
