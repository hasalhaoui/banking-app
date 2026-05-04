package com.example.banking.identity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IdentityPageController {

    @GetMapping("/")
    public String index() {
        return "identity-dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
