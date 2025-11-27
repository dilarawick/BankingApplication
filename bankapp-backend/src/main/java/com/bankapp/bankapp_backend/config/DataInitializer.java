package com.bankapp.bankapp_backend.config;

import com.bankapp.bankapp_backend.entity.User;
import com.bankapp.bankapp_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Check if users already exist
        if (userRepository.count() == 0) {
            // Create test user 1
            User user1 = new User();
            user1.setUsername("john_doe");
            user1.setPassword(passwordEncoder.encode("password123"));
            user1.setAccountNumber("1234567890");
            user1.setAccountName("John Doe");
            user1.setEmail("john.doe@example.com");
            user1.setBalance(new BigDecimal("50000.00"));
            userRepository.save(user1);

            // Create test user 2
            User user2 = new User();
            user2.setUsername("jane_smith");
            user2.setPassword(passwordEncoder.encode("password123"));
            user2.setAccountNumber("9876543210");
            user2.setAccountName("Jane Smith");
            user2.setEmail("jane.smith@example.com");
            user2.setBalance(new BigDecimal("25000.00"));
            userRepository.save(user2);

            System.out.println("Test users created successfully!");
            System.out.println("User 1 - Username: john_doe, Password: password123, Balance: 50000.00");
            System.out.println("User 2 - Username: jane_smith, Password: password123, Balance: 25000.00");
        }
    }
}
