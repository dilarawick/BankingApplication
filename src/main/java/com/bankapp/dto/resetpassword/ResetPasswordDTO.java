package com.bankapp.dto.resetpassword;

import jakarta.validation.constraints.NotBlank;

public class ResetPasswordDTO {
    @NotBlank private String password;

    public ResetPasswordDTO(String password) {
        this.password = password;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
