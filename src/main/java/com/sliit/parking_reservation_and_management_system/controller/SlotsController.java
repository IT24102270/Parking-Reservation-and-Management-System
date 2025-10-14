package com.sliit.parking_reservation_and_management_system.controller;

import com.sliit.parking_reservation_and_management_system.entity.ParkingSlot;
import com.sliit.parking_reservation_and_management_system.entity.User;
import com.sliit.parking_reservation_and_management_system.service.ParkingSlotService;
import com.sliit.parking_reservation_and_management_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer")
public class SlotsController {

    @Autowired
    private ParkingSlotService parkingSlotService;
    
    @Autowired
    private UserService userService;

    @GetMapping("/slots")
    public String viewSlots(@RequestParam(required = false) String search,
                           @RequestParam(required = false) String status,
                           Model model) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get all parking slots
            List<ParkingSlot> allSlots = parkingSlotService.getAllParkingSlots();
            List<ParkingSlot> filteredSlots = allSlots;
            
            // Apply search filter
            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = search.trim().toLowerCase();
                filteredSlots = filteredSlots.stream()
                    .filter(slot -> 
                        (slot.getLocation() != null && slot.getLocation().toLowerCase().contains(searchTerm)) ||
                        slot.getId().toString().contains(searchTerm) ||
                        (slot.getStatus() != null && slot.getStatus().toLowerCase().contains(searchTerm))
                    )
                    .collect(Collectors.toList());
            }
            
            // Apply status filter
            if (status != null && !status.trim().isEmpty() && !status.equals("ALL")) {
                filteredSlots = filteredSlots.stream()
                    .filter(slot -> status.equalsIgnoreCase(slot.getStatus()))
                    .collect(Collectors.toList());
            }
            
            // Calculate statistics
            long totalSlots = allSlots.size();
            long availableSlots = allSlots.stream().filter(slot -> "AVAILABLE".equals(slot.getStatus())).count();
            long occupiedSlots = allSlots.stream().filter(slot -> "OCCUPIED".equals(slot.getStatus())).count();
            long maintenanceSlots = allSlots.stream().filter(slot -> "MAINTENANCE".equals(slot.getStatus())).count();
            
            // Add attributes to model
            model.addAttribute("slots", filteredSlots);
            model.addAttribute("user", currentUser);
            model.addAttribute("search", search);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("totalSlots", totalSlots);
            model.addAttribute("availableSlots", availableSlots);
            model.addAttribute("occupiedSlots", occupiedSlots);
            model.addAttribute("maintenanceSlots", maintenanceSlots);
            
            System.out.println("Slots page loaded:");
            System.out.println("- Total slots: " + totalSlots);
            System.out.println("- Filtered slots: " + filteredSlots.size());
            System.out.println("- Search term: " + search);
            System.out.println("- Status filter: " + status);
            
        } catch (Exception e) {
            System.err.println("Error loading slots page: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error loading parking slots");
        }
        
        return "customer-slots";
    }
    
    @GetMapping("/slots/{id}")
    public String viewSlotDetails(@PathVariable Long id, Model model) {
        try {
            // Get current user
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login";
            }
            
            // Get slot details
            ParkingSlot slot = parkingSlotService.getParkingSlotById(id).orElse(null);
            if (slot == null) {
                model.addAttribute("error", "Parking slot not found");
                return "redirect:/customer/slots";
            }
            
            model.addAttribute("slot", slot);
            model.addAttribute("user", currentUser);
            
        } catch (Exception e) {
            System.err.println("Error loading slot details: " + e.getMessage());
            model.addAttribute("error", "Error loading slot details");
            return "redirect:/customer/slots";
        }
        
        return "customer-slot-details";
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
}
