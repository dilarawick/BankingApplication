package com.bankapp.bankapp_backend.service;

import com.bankapp.bankapp_backend.model.Customer;
import com.bankapp.bankapp_backend.model.CustomerAccount;
import com.bankapp.bankapp_backend.model.Account;
import com.bankapp.bankapp_backend.repository.CustomerRepository;
import com.bankapp.bankapp_backend.repository.AccountRepository;
import com.bankapp.bankapp_backend.repository.CustomerAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;

@Service
public class AuthService {
    @Autowired
    private CustomerRepository customerRepo;
    @Autowired
    private AccountRepository accountRepo;
    @Autowired
    private CustomerAccountRepository customerAccountRepo;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private OtpService otpService;

    public Optional<Customer> authenticate(String username, String rawPassword) {
        Optional<Customer> c = customerRepo.findByUsername(username);
        if (c.isPresent() && c.get().getPasswordHash() != null) {
            if (passwordEncoder.matches(rawPassword, c.get().getPasswordHash()))
                return c;
        }
        return Optional.empty();
    }

    public boolean sendOtpEmail(String email) {
        String otp = otpService.generateOtpFor(email);
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setTo(email);
            m.setFrom("novabank.noreply@gmail.com");
            m.setSubject("NovaBank OTP");
            m.setText("Your NovaBank verification code: " + otp + " (expires in 5 minutes)");
            mailSender.send(m);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean verifyOtp(String email, String otp) {
        return otpService.verifyOtp(email, otp);
    }

    public Optional<Customer> findCustomerByNameNic(String name, String nic) {
        return customerRepo.findByNameAndNic(name, nic);
    }

    public Optional<Account> findAccountByAccountNo(String accountNo) {
        return accountRepo.findByAccountNo(accountNo);
    }

    public boolean createCredentialsForCustomer(Integer customerId, String username, String rawPassword) {
        if (customerId == null)
            return false;
        Optional<Customer> oc = customerRepo.findById(customerId);
        if (!oc.isPresent())
            return false;
        Customer c = oc.get();
        if (c.getUsername() != null)
            return false; // already has username
        c.setUsername(username);
        c.setPasswordHash(passwordEncoder.encode(rawPassword));
        customerRepo.save(c);
        return true;
    }

    public boolean createCredentialsForCustomerWithPassword(Integer customerId, String rawPassword) {
        if (customerId == null)
            return false;
        Optional<Customer> oc = customerRepo.findById(customerId);
        if (!oc.isPresent())
            return false;
        Customer c = oc.get();
        if (c.getUsername() != null)
            return false; // already has username
        // Use email as username
        c.setUsername(c.getEmail());
        c.setPasswordHash(passwordEncoder.encode(rawPassword));
        customerRepo.save(c);
        return true;
    }

    public boolean addCustomerAccount(Integer customerId, String accountNo, boolean isPrimary) {
        if (customerId == null)
            return false;
        Optional<Account> acc = accountRepo.findByAccountNo(accountNo);
        if (!acc.isPresent())
            return false;
        CustomerAccount ca = new CustomerAccount();
        ca.setCustomerID(customerId);
        ca.setAccountNo(accountNo);
        ca.setIsPrimary(isPrimary);
        if (isPrimary) {
            // unset other primaries for this customer
            List<CustomerAccount> list = customerAccountRepo.findByCustomerID(customerId);
            for (CustomerAccount other : list) {
                if (other.getIsPrimary() != null && other.getIsPrimary()) {
                    other.setIsPrimary(false);
                    customerAccountRepo.save(other);
                }
            }
        }
        customerAccountRepo.save(ca);
        return true;
    }

    public boolean changePasswordByCustomer(Integer customerId, String newRawPassword) {
        if (customerId == null)
            return false;
        Optional<Customer> oc = customerRepo.findById(customerId);
        if (!oc.isPresent())
            return false;
        Customer c = oc.get();
        c.setPasswordHash(passwordEncoder.encode(newRawPassword));
        customerRepo.save(c);
        return true;
    }

    // fetch dashboard info
    public Customer getCustomer(Integer customerId) {
        if (customerId == null)
            return null;
        return customerRepo.findById(customerId).orElse(null);
    }
}
