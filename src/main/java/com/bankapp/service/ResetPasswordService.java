package com.bankapp.service;

import com.bankapp.dto.resetpassword.ResetPasswordDTO;
import com.bankapp.dto.resetpassword.VerifyResetPasswordRequestDTO;
import com.bankapp.dto.resetpassword.VerifyResetPasswordResponseDTO;
import com.bankapp.model.Customer;
import com.bankapp.repository.CustomerRepository;
import com.bankapp.security.JwtUtil;
import com.bankapp.util.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ResetPasswordService {
    private final CustomerRepository customerRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    @Autowired
    public ResetPasswordService(
            CustomerRepository customerRepo,
            BCryptPasswordEncoder passwordEncoder,
            OtpService otpService,
            JwtUtil jwtUtil) {
        this.customerRepo = customerRepo;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
    }

    public VerifyResetPasswordResponseDTO verifyResetPassword(VerifyResetPasswordRequestDTO dto) {
        if(!otpService.verifyOtp(dto.getEmail(), dto.getOtp())){
            throw new RuntimeException("Invalid OTP");
        }

        Customer customer = customerRepo.findByNic(dto.getNic())
                .orElseThrow(() -> new RuntimeException("Details do match!"));

        if (!customer.getEmail().equals(dto.getEmail()) ||
            !customer.getName().equals(dto.getName())) {
            throw new RuntimeException("Details do not match");
        }

        otpService.consumeOtp(dto.getEmail());

        String token = jwtUtil.generateSignupToken(
                customer.getCustomerID(),
                customer.getEmail());
        return new VerifyResetPasswordResponseDTO(token);
    }

    public void resetPassword(Integer customerId, ResetPasswordDTO req) {

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Invalid signup session"));

        PasswordValidator.validate(req.getPassword());

        customer.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        customerRepo.save(customer);
    }
}
