package com.bankapp.dto.signup;

import jakarta.validation.constraints.NotBlank;

public class CreateCredentialsDTO {
    @NotBlank private String username;
    @NotBlank private String password;

    public CreateCredentialsDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
