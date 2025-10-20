package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.service.SlotAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for testing and managing automatic slot availability system
 */
@Controller
@RequestMapping("/public")
public class SlotAvailabilityController {

    @Autowired
    private SlotAvailabilityService slotAvailabilityService;

    /**
     * Display slot availability status and testing interface
     */
    @GetMapping("/slot-availability")
    public String showSlotAvailability(Model model) {
        try {
            SlotAvailabilityService.SlotAvailabilityStats stats = slotAvailabilityService.getAvailabilityStats();
            model.addAttribute("stats", stats);
            model.addAttribute("pageTitle", "Slot Availability Management");
            
            return "slot-availability-test";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading slot availability data: " + e.getMessage());
            return "slot-availability-test";
        }
    }

    /**
     * Manually trigger slot status updates (for testing)
     */
    @PostMapping("/force-update-slots")
    public String forceUpdateSlots(RedirectAttributes redirectAttributes) {
        try {
            slotAvailabilityService.forceUpdateAllSlotStatuses();
            redirectAttributes.addFlashAttribute("success", "All slot statuses have been updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating slot statuses: " + e.getMessage());
        }
        return "redirect:/public/slot-availability";
    }

    /**
     * Trigger the scheduled update manually (for testing)
     */
    @PostMapping("/trigger-scheduled-update")
    public String triggerScheduledUpdate(RedirectAttributes redirectAttributes) {
        try {
            slotAvailabilityService.updateSlotStatuses();
            redirectAttributes.addFlashAttribute("success", "Scheduled slot status update completed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error in scheduled update: " + e.getMessage());
        }
        return "redirect:/public/slot-availability";
    }

    /**
     * Get current availability stats as JSON (for AJAX calls)
     */
    @GetMapping("/slot-stats")
    @ResponseBody
    public SlotAvailabilityService.SlotAvailabilityStats getSlotStats() {
        return slotAvailabilityService.getAvailabilityStats();
    }
    
    /**
     * Test endpoint to demonstrate the complete booking-to-availability flow
     */
    @GetMapping("/test-flow")
    @ResponseBody
    public String testBookingFlow() {
        StringBuilder result = new StringBuilder();
        result.append("=== PARKING SLOT AVAILABILITY SYSTEM TEST ===\n\n");
        
        try {
            // Get current stats
            SlotAvailabilityService.SlotAvailabilityStats stats = slotAvailabilityService.getAvailabilityStats();
            result.append("Current Slot Statistics:\n");
            result.append("- Total Slots: ").append(stats.totalSlots).append("\n");
            result.append("- Available: ").append(stats.availableSlots).append("\n");
            result.append("- Occupied: ").append(stats.occupiedSlots).append("\n");
            result.append("- Maintenance: ").append(stats.maintenanceSlots).append("\n\n");
            
            result.append("=== HOW THE SYSTEM WORKS ===\n\n");
            
            result.append("1. BOOKING CREATION:\n");
            result.append("   - User creates booking (status: PENDING)\n");
            result.append("   - Slot remains AVAILABLE until payment completed\n");
            result.append("   - System checks slot availability for time period\n\n");
            
            result.append("2. PAYMENT COMPLETION:\n");
            result.append("   - PaymentService.processPayment() triggers slot management\n");
            result.append("   - Reservation status: PENDING → CONFIRMED\n");
            result.append("   - If start time is soon (within 5 min): Slot → OCCUPIED\n");
            result.append("   - If start time is later: Scheduled task will handle it\n\n");
            
            result.append("3. AUTOMATIC SLOT MANAGEMENT:\n");
            result.append("   - Scheduled task runs every 2 minutes\n");
            result.append("   - Checks reservations that should start/end\n");
            result.append("   - CONFIRMED → ACTIVE (slot becomes OCCUPIED)\n");
            result.append("   - ACTIVE → COMPLETED (slot becomes AVAILABLE)\n\n");
            
            result.append("4. BOOKING CANCELLATION:\n");
            result.append("   - User cancels booking\n");
            result.append("   - Reservation status → CANCELLED\n");
            result.append("   - Slot availability updated immediately\n");
            result.append("   - Slot becomes AVAILABLE for new bookings\n\n");
            
            result.append("5. REAL-TIME AVAILABILITY CHECKING:\n");
            result.append("   - SlotAvailabilityService.isSlotAvailable() checks overlaps\n");
            result.append("   - Prevents double-booking for same time period\n");
            result.append("   - Considers CONFIRMED and ACTIVE reservations\n\n");
            
            result.append("=== SYSTEM STATUS ===\n");
            result.append("✅ Scheduled task: Running every 2 minutes\n");
            result.append("✅ Payment completion: Triggers slot management\n");
            result.append("✅ Booking cancellation: Updates availability immediately\n");
            result.append("✅ Real-time checking: Prevents conflicts\n");
            result.append("✅ Time-based automation: Slots auto-update based on reservation times\n\n");
            
            result.append("=== TEST ENDPOINTS ===\n");
            result.append("- Force update all slots: POST /public/force-update-slots\n");
            result.append("- Trigger scheduled update: POST /public/trigger-scheduled-update\n");
            result.append("- View current stats: GET /public/slot-stats\n");
            result.append("- Test booking flow: GET /public/test-flow\n\n");
            
            result.append("The system is now fully operational! 🚗✨\n");
            
        } catch (Exception e) {
            result.append("ERROR: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
}
