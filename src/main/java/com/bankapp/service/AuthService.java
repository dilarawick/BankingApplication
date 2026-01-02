package com.bankapp.service;

import com.bankapp.dto.login.LoginRequestDTO;
import com.bankapp.dto.login.LoginResponseDTO;
import com.bankapp.dto.signup.PasswordSetupRequestDTO;
import com.bankapp.exception.EmailSendException;
import com.bankapp.exception.ResourceNotFoundException;
import com.bankapp.model.Customer;
import com.bankapp.security.JwtUtil;
import com.bankapp.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final CustomerRepository customerRepo;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(
            CustomerRepository customerRepo,
            BCryptPasswordEncoder passwordEncoder,
            EmailService emailService,
            OtpService otpService,
            JwtUtil jwtUtil) {
        this.customerRepo = customerRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.otpService = otpService;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        Customer customer = customerRepo.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        Integer customerId = customer.getCustomerID();
        String name = customer.getName();
        String email = customer.getEmail();

        String token = jwtUtil.generateLoginToken(customerId, name, email);
        LoginResponseDTO response = new LoginResponseDTO(customerId, name, email);
        response.setToken(token);

        return response;
    }

    //Remove after developing
    public void setupPassword(PasswordSetupRequestDTO request) {
        Customer customer = customerRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        String hashed = passwordEncoder.encode(request.getNewPassword());
        customer.setPasswordHash(hashed);

        customerRepo.save(customer);
    }

    public void sendOtpEmail(String email) {
        Customer customer = customerRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));
        String name = customer.getName();
        String otp = otpService.generateOtpFor(email);
        try {
            emailService.sendOtp(
                    email,
                    name,
                    otp
            );
        } catch (Exception e) {
            throw new EmailSendException("OTP could not be sent to " + email);
        }
    }
}
