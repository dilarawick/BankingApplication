package com.bankapp.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class OtpService {
    private static class OtpEntry {
        String otp;
        Instant expiresAt;
        OtpEntry(String otp, Instant expiresAt) { this.otp = otp; this.expiresAt = expiresAt; }
    }

    private final Map<String, OtpEntry> store = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOtpFor(String email) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        Instant expires = Instant.now().plusSeconds(5 * 60); // 5 minutes
        store.put(email, new OtpEntry(otp, expires));
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpEntry e = store.get(email);
        if (e == null) return false;
        if (Instant.now().isAfter(e.expiresAt)) {
            store.remove(email);
            return false;
        }
        return e.otp.equals(otp);
    }

    public void consumeOtp(String email) {
        store.remove(email);
    }
}
