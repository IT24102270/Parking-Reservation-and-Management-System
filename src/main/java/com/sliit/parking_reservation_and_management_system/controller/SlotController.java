// Updated SlotController.java
package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.Slot;
import com.sliit.parking_reservation_and_management_system.service.SlotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/slotmanager")
public class SlotController {

    @Autowired
    private SlotService slotService;

    @GetMapping("/dashboard")
    public String showDashboard(@RequestParam(name = "location", required = false) String locationParam, 
                               @RequestParam(name = "status", required = false) String statusFilter,
                               @RequestParam(name = "search", required = false) String searchTerm,
                               Model model) {
        System.out.println("=== COMPREHENSIVE SLOT MANAGER DASHBOARD ACCESSED ===");
        System.out.println("URL: /slotmanager/dashboard");
        System.out.println("Location parameter: " + locationParam);
        System.out.println("Status filter: " + statusFilter);
        System.out.println("Search term: " + searchTerm);
        
        try {
            // Get all slots for comprehensive management
            List<Slot> allSlots = slotService.getAllSlots();
            
            // Apply filters
            List<Slot> filteredSlots = allSlots;
            
            if (locationParam != null && !locationParam.isBlank() && !"ALL".equals(locationParam)) {
                filteredSlots = filteredSlots.stream()
                    .filter(slot -> locationParam.equals(slot.getLocation()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equals(statusFilter)) {
                filteredSlots = filteredSlots.stream()
                    .filter(slot -> statusFilter.equalsIgnoreCase(slot.getStatus()))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            if (searchTerm != null && !searchTerm.isBlank()) {
                filteredSlots = filteredSlots.stream()
                    .filter(slot -> String.valueOf(slot.getId()).contains(searchTerm) ||
                                   (slot.getType() != null && slot.getType().toLowerCase().contains(searchTerm.toLowerCase())))
                    .collect(java.util.stream.Collectors.toList());
            }
            
            // Calculate statistics
            long totalSlots = allSlots.size();
            long availableSlots = allSlots.stream().mapToLong(slot -> "AVAILABLE".equalsIgnoreCase(slot.getStatus()) ? 1 : 0).sum();
            long occupiedSlots = allSlots.stream().mapToLong(slot -> "OCCUPIED".equalsIgnoreCase(slot.getStatus()) ? 1 : 0).sum();
            long maintenanceSlots = allSlots.stream().mapToLong(slot -> "MAINTENANCE".equalsIgnoreCase(slot.getStatus()) ? 1 : 0).sum();
            
            // Group slots by location for statistics
            java.util.Map<String, Long> slotsByLocation = allSlots.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    Slot::getLocation, 
                    java.util.stream.Collectors.counting()
                ));
            
            // Add data to model
            model.addAttribute("slots", filteredSlots);
            model.addAttribute("allSlots", allSlots);
            model.addAttribute("currentLocation", locationParam != null ? locationParam : "ALL");
            model.addAttribute("currentStatus", statusFilter != null ? statusFilter : "ALL");
            model.addAttribute("searchTerm", searchTerm != null ? searchTerm : "");
            
            // Statistics
            model.addAttribute("totalSlots", totalSlots);
            model.addAttribute("availableSlots", availableSlots);
            model.addAttribute("occupiedSlots", occupiedSlots);
            model.addAttribute("maintenanceSlots", maintenanceSlots);
            model.addAttribute("slotsByLocation", slotsByLocation);
            
            // Filter options
            model.addAttribute("locationOptions", List.of("ALL", "TwoWheeler", "ThreeWheeler", "FourWheeler", "HeavyVehicle"));
            model.addAttribute("statusOptions", List.of("ALL", "AVAILABLE", "OCCUPIED", "MAINTENANCE"));
            
            System.out.println("Successfully loaded " + filteredSlots.size() + " filtered slots out of " + totalSlots + " total slots");
            System.out.println("Statistics - Available: " + availableSlots + ", Occupied: " + occupiedSlots + ", Maintenance: " + maintenanceSlots);
            
        } catch (Exception e) {
            System.err.println("Error loading slot data: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("slots", List.of());
            model.addAttribute("error", "Error loading slot data: " + e.getMessage());
            
            // Add default values for error case
            model.addAttribute("currentLocation", "ALL");
            model.addAttribute("currentStatus", "ALL");
            model.addAttribute("searchTerm", "");
            model.addAttribute("totalSlots", 0L);
            model.addAttribute("availableSlots", 0L);
            model.addAttribute("occupiedSlots", 0L);
            model.addAttribute("maintenanceSlots", 0L);
        }
        
        return "slot-management-dashboard";
    }

    @GetMapping("/updateslot")
    public String showUpdateForm(@RequestParam(required = false) Integer id,
                                 @RequestParam(name = "location", required = false) String locationParam,
                                 Model model) {
        Slot slot;
        if (id != null) {
            slot = slotService.getSlotById(id).orElse(new Slot());
        } else {
            slot = new Slot();
            if (locationParam != null && !locationParam.isBlank()) {
                slot.setLocation(locationParam);
            }
        }
        model.addAttribute("slot", slot);
        model.addAttribute("isUpdate", id != null);
        return "updateslot";
    }

    @PostMapping("/update-slot")
    public String updateSlot(@Valid @ModelAttribute Slot slot, BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isUpdate", slot.getId() != null);
            return "updateslot";
        }
        try {
            slotService.saveSlot(slot);
            redirectAttributes.addFlashAttribute("successMessage", "Slot " + (slot.getId() != null ? "updated" : "added") + " successfully!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isUpdate", slot.getId() != null);
            return "updateslot";
        }
        return "redirect:/slotmanager/dashboard?location=" + slot.getLocation();
    }

    // Vehicle type specific page mappings
    @GetMapping("/twowheel")
    public String twoWheelPage(Model model) {
        List<Slot> slots = slotService.getSlotsByLocation("TwoWheeler");
        model.addAttribute("slots", slots);
        return "twowheel";
    }

    @GetMapping("/threewheel")
    public String threeWheelPage(Model model) {
        List<Slot> slots = slotService.getSlotsByLocation("ThreeWheeler");
        model.addAttribute("slots", slots);
        return "threewheel";
    }

    @GetMapping("/fourwheel")
    public String fourWheelPage(Model model) {
        List<Slot> slots = slotService.getSlotsByLocation("FourWheeler");
        model.addAttribute("slots", slots);
        return "fourwheel";
    }

    @GetMapping("/heavyvehicle")
    public String heavyVehiclePage(Model model) {
        List<Slot> slots = slotService.getSlotsByLocation("HeavyVehicle");
        model.addAttribute("slots", slots);
        return "heavyvehicle";
    }
    
    // Bulk operations for slot management
    @PostMapping("/bulk-update-status")
    public String bulkUpdateStatus(@RequestParam("slotIds") List<Integer> slotIds,
                                  @RequestParam("newStatus") String newStatus,
                                  RedirectAttributes redirectAttributes) {
        try {
            int updatedCount = 0;
            for (Integer slotId : slotIds) {
                Optional<Slot> slotOpt = slotService.getSlotById(slotId);
                if (slotOpt.isPresent()) {
                    Slot slot = slotOpt.get();
                    slot.setStatus(newStatus.toUpperCase());
                    slotService.saveSlot(slot);
                    updatedCount++;
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", 
                "Successfully updated status for " + updatedCount + " slots to " + newStatus);
        } catch (Exception e) {
            System.err.println("Error in bulk status update: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating slot status: " + e.getMessage());
        }
        return "redirect:/slotmanager/dashboard";
    }
    
    @PostMapping("/bulk-delete")
    public String bulkDeleteSlots(@RequestParam("slotIds") List<Integer> slotIds,
                                 RedirectAttributes redirectAttributes) {
        try {
            int deletedCount = slotService.deleteSlots(slotIds);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Successfully deleted " + deletedCount + " slots");
        } catch (Exception e) {
            System.err.println("Error in bulk delete: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting slots: " + e.getMessage());
        }
        return "redirect:/slotmanager/dashboard";
    }
    
    @PostMapping("/bulk-add")
    public String bulkAddSlots(@RequestParam("location") String location,
                              @RequestParam("type") String type,
                              @RequestParam("count") int count,
                              @RequestParam("hourlyRate") double hourlyRate,
                              RedirectAttributes redirectAttributes) {
        try {
            int addedCount = slotService.bulkAddSlots(location, type, count, hourlyRate);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Successfully added " + addedCount + " new " + location + " slots");
        } catch (Exception e) {
            System.err.println("Error in bulk add: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error adding slots: " + e.getMessage());
        }
        return "redirect:/slotmanager/dashboard";
    }
    
    // Quick status change for individual slots
    @PostMapping("/quick-status-change")
    public String quickStatusChange(@RequestParam("slotId") Integer slotId,
                                   @RequestParam("newStatus") String newStatus,
                                   RedirectAttributes redirectAttributes) {
        try {
            Optional<Slot> slotOpt = slotService.getSlotById(slotId);
            if (slotOpt.isPresent()) {
                Slot slot = slotOpt.get();
                String oldStatus = slot.getStatus();
                slot.setStatus(newStatus.toUpperCase());
                slotService.saveSlot(slot);
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Slot " + slotId + " status changed from " + oldStatus + " to " + newStatus);
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Slot not found");
            }
        } catch (Exception e) {
            System.err.println("Error changing slot status: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error changing slot status: " + e.getMessage());
        }
        return "redirect:/slotmanager/dashboard";
    }
    
    // Delete individual slot
    @PostMapping("/delete-slot")
    public String deleteSlot(@RequestParam("slotId") Integer slotId,
                            RedirectAttributes redirectAttributes) {
        try {
            if (slotService.deleteSlot(slotId)) {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Slot " + slotId + " deleted successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Slot not found");
            }
        } catch (Exception e) {
            System.err.println("Error deleting slot: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error deleting slot: " + e.getMessage());
        }
        return "redirect:/slotmanager/dashboard";
    }
}