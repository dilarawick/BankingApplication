package com.bankapp.dto.signup;

public class VerifySignupResponseDTO {
    private final String signupToken;

    public VerifySignupResponseDTO(String signupToken) {
        this.signupToken = signupToken;
    }

    public String getSignupToken() {
        return signupToken;
    }
}
