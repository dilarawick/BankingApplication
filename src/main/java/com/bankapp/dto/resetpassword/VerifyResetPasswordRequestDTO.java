package com.bankapp.dto.resetpassword;

import jakarta.validation.constraints.NotBlank;

public class VerifyResetPasswordRequestDTO {
    @NotBlank private String name;
    @NotBlank private String nic;
    @NotBlank private String email;
    @NotBlank private String otp;
    public VerifyResetPasswordRequestDTO() {}

    public VerifyResetPasswordRequestDTO(String name, String nic, String email, String otp) {
        this.name = name;
        this.nic = nic;
        this.email = email;
        this.otp = otp;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNic() { return nic; }
    public void setNic(String nic) { this.nic = nic; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}
