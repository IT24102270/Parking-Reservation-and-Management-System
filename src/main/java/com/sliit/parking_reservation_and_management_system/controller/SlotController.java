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
    public String showDashboard(@RequestParam(name = "location", required = false) String locationParam, Model model) {
        System.out.println("=== SLOT MANAGER DASHBOARD ACCESSED ===");
        System.out.println("URL: /slotmanager/dashboard");
        System.out.println("Location parameter: " + locationParam);
        
        String location = (locationParam == null || locationParam.isBlank()) ? "FourWheeler" : locationParam;
        
        try {
            List<Slot> slotsForLocation = slotService.getSlotsByLocation(location);
            model.addAttribute("slots", slotsForLocation);
            model.addAttribute("currentLocation", location);
            
            System.out.println("Successfully loaded " + slotsForLocation.size() + " slots for location: " + location);
            System.out.println("Returning template: slotmanager-dashboard.html");
            
        } catch (Exception e) {
            System.err.println("Error loading slot data: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("slots", List.of());
            model.addAttribute("currentLocation", location);
            model.addAttribute("error", "Error loading slot data: " + e.getMessage());
        }
        
        return "slotmanager-dashboard";
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
}