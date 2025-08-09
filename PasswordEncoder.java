import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncoder {
    
    public static void main(String[] args) {
        String password = "Password@123";
        String encodedPassword = encode(password);
        System.out.println("Original password: " + password);
        System.out.println("Encoded password: " + encodedPassword);
        System.out.println();
        System.out.println("INSERT statement:");
        System.out.println("INSERT INTO users (");
        System.out.println("    id,");
        System.out.println("    company_id,");
        System.out.println("    email,");
        System.out.println("    password_hash,");
        System.out.println("    first_name,");
        System.out.println("    last_name,");
        System.out.println("    phone,");
        System.out.println("    role,");
        System.out.println("    active,");
        System.out.println("    email_verified,");
        System.out.println("    last_login_at,");
        System.out.println("    created_at,");
        System.out.println("    updated_at");
        System.out.println(") VALUES (");
        System.out.println("    uuid_generate_v4(),");
        System.out.println("    NULL,  -- Super users don't belong to any company");
        System.out.println("    'superuser@projectmaster.com',");
        System.out.println("    '" + encodedPassword + "',");
        System.out.println("    'Super',");
        System.out.println("    'User',");
        System.out.println("    '+1234567890',");
        System.out.println("    'SUPER_USER',");
        System.out.println("    true,");
        System.out.println("    false,");
        System.out.println("    NULL,");
        System.out.println("    CURRENT_TIMESTAMP,");
        System.out.println("    CURRENT_TIMESTAMP");
        System.out.println(");");
    }
    
    public static String encode(String rawPassword) {
        try {
            // Generate salt - using a fixed salt for reproducible results
            // In production, this would be random, but for this demo we need consistency
            byte[] salt = "ProjectMasterSalt".getBytes(); // 16 bytes
            if (salt.length != 16) {
                // Ensure exactly 16 bytes
                byte[] fixedSalt = new byte[16];
                System.arraycopy(salt, 0, fixedSalt, 0, Math.min(salt.length, 16));
                salt = fixedSalt;
            }
            
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
}