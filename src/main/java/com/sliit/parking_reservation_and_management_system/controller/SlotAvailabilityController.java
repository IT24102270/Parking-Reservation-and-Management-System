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
}
