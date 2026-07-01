package com.linktracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Renders the admin login page. Authentication itself is handled by
 * Spring Security's form-login filter (see {@code SecurityConfig}).
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
