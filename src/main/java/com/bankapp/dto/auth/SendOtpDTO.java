package com.bankapp.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class SendOtpDTO {
    @NotBlank private String email;

    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }
}
