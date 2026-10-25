package com.patelsamaj.patelsamaj_backend.controller;

import com.patelsamaj.patelsamaj_backend.dto.AuthenticationDTO;
import com.patelsamaj.patelsamaj_backend.dto.ForgotPasswordDTO;
import com.patelsamaj.patelsamaj_backend.service.AuthenticationService;
import com.patelsamaj.patelsamaj_backend.service.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthenticationService service;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> authenticate(@RequestBody AuthenticationDTO request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        SecurityContextHolder.clearContext();

        return ResponseEntity.ok("Logged out successfully.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@NotNull @RequestBody ForgotPasswordDTO email) {
        userService.resetAndEmailPassword(email);

        return ResponseEntity.ok("If that email exists in our system, a new password has been sent.");
    }
}