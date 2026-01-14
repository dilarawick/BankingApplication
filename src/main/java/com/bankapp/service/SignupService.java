package com.bankapp.service;

import com.bankapp.dto.signup.CreateCredentialsDTO;
import com.bankapp.dto.signup.VerifySignupRequestDTO;
import com.bankapp.dto.signup.VerifySignupResponseDTO;
import com.bankapp.model.Customer;
import com.bankapp.repository.CustomerRepository;
import com.bankapp.security.JwtUtil;
import com.bankapp.util.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SignupService {
    private final CustomerRepository customerRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    @Autowired
    public SignupService(
            CustomerRepository customerRepo,
            BCryptPasswordEncoder passwordEncoder,
            OtpService otpService,
            JwtUtil jwtUtil) {
        this.customerRepo = customerRepo;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
    }

    public VerifySignupResponseDTO verifySignup(VerifySignupRequestDTO dto) {
        if(!otpService.verifyOtp(dto.getEmail(), dto.getOtp())){
            throw new RuntimeException("Invalid OTP");
        }

        Customer customer = customerRepo.findByNic(dto.getNic())
                .orElseThrow(() -> new RuntimeException("Details do match!"));

        if (!customer.getEmail().equals(dto.getEmail()) ||
            !customer.getName().equals(dto.getName()) ||
            !customer.getPhoneNumber().equals(dto.getPhone())) {
            throw new RuntimeException("Details do not match");
        }

        if (customer.getUsername() != null) {
            throw new RuntimeException("Account already exists");
        }

        otpService.consumeOtp(dto.getEmail());

        String token = jwtUtil.generateSignupToken(
                customer.getCustomerID(),
                customer.getEmail());
        return new VerifySignupResponseDTO(token);
    }

    public void createCredentials(Integer customerId, CreateCredentialsDTO req) {

        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Invalid signup session"));

        if (customer.getUsername() != null) {
            throw new RuntimeException("Account already exists");
        }

        PasswordValidator.validate(req.getPassword());

        customer.setUsername(req.getUsername());
        customer.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        customerRepo.save(customer);
    }
}
