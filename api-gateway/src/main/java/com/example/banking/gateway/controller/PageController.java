package com.example.banking.gateway.controller;

import com.example.banking.gateway.config.GatewayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final GatewayProperties properties;

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("services", properties.services());
        return "dashboard";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("identityUrl", properties.services().identityUrl());
        return "login";
    }
}
