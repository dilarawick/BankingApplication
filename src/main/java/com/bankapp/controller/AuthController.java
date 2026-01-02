package com.bankapp.controller;

import com.bankapp.dto.auth.SendOtpDTO;
import com.bankapp.dto.login.LoginRequestDTO;
import com.bankapp.dto.login.LoginResponseDTO;
import com.bankapp.dto.signup.PasswordSetupRequestDTO;
import com.bankapp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(
            AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    // Remove after development
    @PostMapping("/setup-password")
    public ResponseEntity<String> setupPassword(@RequestBody PasswordSetupRequestDTO request) {
        authService.setupPassword(request);
        return ResponseEntity.ok("Password updated successfully");
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOtp(@Valid @RequestBody SendOtpDTO request) {
        authService.sendOtpEmail(request.getEmail());
        return ResponseEntity.noContent().build();
    }
}