package com.bankapp.controller;

import com.bankapp.dto.signup.CreateCredentialsDTO;
import com.bankapp.dto.signup.VerifySignupRequestDTO;
import com.bankapp.dto.signup.VerifySignupResponseDTO;
import com.bankapp.service.SignupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signup")
public class SignupController {
    private final SignupService signupService;

    @Autowired
    public SignupController(
            SignupService signupService) {
        this.signupService = signupService;
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifySignupResponseDTO> verify(
            @Valid @RequestBody VerifySignupRequestDTO req) {

        return ResponseEntity.ok(signupService.verifySignup(req));
    }

    @PostMapping("/create-credentials")
    public ResponseEntity<Void> createCredentials(
            @Valid @RequestBody CreateCredentialsDTO req,
            Authentication auth) {

        Integer customerId = (Integer) auth.getPrincipal();
        signupService.createCredentials(customerId, req);
        return ResponseEntity.noContent().build();
    }
}