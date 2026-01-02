package com.bankapp.dto.resetpassword;

public class VerifyResetPasswordResponseDTO {
    private final String resetPasswordToken;

    public VerifyResetPasswordResponseDTO(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public String getResetPasswordToken() {
        return resetPasswordToken;
    }
}