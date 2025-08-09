package com.projectmaster.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class PasswordConfig {

    @Bean
    public SimplePasswordEncoder passwordEncoder() {
        return new SimplePasswordEncoder();
    }

    public static class SimplePasswordEncoder {
        
        private final SecureRandom random = new SecureRandom();
        
        public String encode(String rawPassword) {
            try {
                // Generate salt
                byte[] salt = new byte[16];
                random.nextBytes(salt);
                
                // Hash password with salt
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(salt);
                byte[] hashedPassword = md.digest(rawPassword.getBytes());
                
                // Combine salt and hash
                byte[] combined = new byte[salt.length + hashedPassword.length];
                System.arraycopy(salt, 0, combined, 0, salt.length);
                System.arraycopy(hashedPassword, 0, combined, salt.length, hashedPassword.length);
                
                return Base64.getEncoder().encodeToString(combined);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-256 algorithm not available", e);
            }
        }
        
        public boolean matches(String rawPassword, String encodedPassword) {
            try {
                byte[] combined = Base64.getDecoder().decode(encodedPassword);
                
                // Extract salt (first 16 bytes)
                byte[] salt = new byte[16];
                System.arraycopy(combined, 0, salt, 0, 16);
                
                // Extract hash (remaining bytes)
                byte[] storedHash = new byte[combined.length - 16];
                System.arraycopy(combined, 16, storedHash, 0, storedHash.length);
                
                // Hash the raw password with the extracted salt
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                md.update(salt);
                byte[] hashedPassword = md.digest(rawPassword.getBytes());
                
                // Compare hashes
                return MessageDigest.isEqual(storedHash, hashedPassword);
            } catch (Exception e) {
                return false;
            }
        }
    }
}