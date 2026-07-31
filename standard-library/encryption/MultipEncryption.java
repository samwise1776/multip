package standardlibrary.encryption;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * Multip Standard Library — Encryption Module
 * Provides hashing and encryption functions.
 */
public class MultipEncryption {
    public static String md5(String input) { return hash("MD5", input); }
    public static String sha1(String input) { return hash("SHA-1", input); }
    public static String sha256(String input) { return hash("SHA-256", input); }
    public static String sha512(String input) { return hash("SHA-512", input); }

    private static String hash(String algorithm, String input) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }

    public static String base64Encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
    public static String base64Decode(String encoded) {
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }

    public static String aesEncrypt(String input, String key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes(StandardCharsets.UTF_8)));
    }
    public static String aesDecrypt(String encrypted, String key) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypted)), StandardCharsets.UTF_8);
    }

    public static String hmac(String algorithm, String input, String key) throws Exception {
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm));
        byte[] hash = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static String uuid() {
        return java.util.UUID.randomUUID().toString();
    }
}
