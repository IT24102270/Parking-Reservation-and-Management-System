package com.sliit.parking_reservation_and_management_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/debug")
public class DebugController {

    @GetMapping("/test")
    @ResponseBody
    public String test() {
        return "<h1>Debug Controller Working!</h1>";
    }
    
    @GetMapping("/template-test")
    public String templateTest(Model model) {
        model.addAttribute("message", "Template test working!");
        return "debug-template";
    }
}
