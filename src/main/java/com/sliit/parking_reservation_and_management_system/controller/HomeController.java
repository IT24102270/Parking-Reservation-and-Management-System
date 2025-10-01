package com.sliit.parking_reservation_and_management_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // Map root URL (/) to index.html
    @GetMapping("/")
    public String index() {
        return "index"; // looks for src/main/resources/templates/index.html
    }
}
