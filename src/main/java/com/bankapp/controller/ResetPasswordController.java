package com.bankapp.controller;

import com.bankapp.dto.resetpassword.ResetPasswordDTO;
import com.bankapp.dto.resetpassword.VerifyResetPasswordRequestDTO;
import com.bankapp.dto.resetpassword.VerifyResetPasswordResponseDTO;
import com.bankapp.service.ResetPasswordService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reset-password")
public class ResetPasswordController {
    private final ResetPasswordService resetPasswordService;

    @Autowired
    public ResetPasswordController(
            ResetPasswordService resetPasswordService) {
        this.resetPasswordService = resetPasswordService;
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyResetPasswordResponseDTO> verify(
            @Valid @RequestBody VerifyResetPasswordRequestDTO req) {

        return ResponseEntity.ok(resetPasswordService.verifyResetPassword(req));
    }

    @PostMapping("/create-credentials")
    public ResponseEntity<Void> createCredentials(
            @Valid @RequestBody ResetPasswordDTO req,
            Authentication auth) {

        Integer customerId = (Integer) auth.getPrincipal();
        resetPasswordService.resetPassword(customerId, req);
        return ResponseEntity.noContent().build();
    }
}