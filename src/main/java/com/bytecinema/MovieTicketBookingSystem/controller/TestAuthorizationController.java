package com.bytecinema.MovieTicketBookingSystem.controller;

import com.bytecinema.MovieTicketBookingSystem.util.annatiation.ApiMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestAuthorizationController {
    @GetMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiMessage("TEST")
    public String test() {
        return "Quyền của ADMIN";
    }
}
