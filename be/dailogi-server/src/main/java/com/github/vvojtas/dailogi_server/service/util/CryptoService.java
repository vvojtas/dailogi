package com.github.vvojtas.dailogi_server.service.util;

import com.github.vvojtas.dailogi_server.exception.CryptoException;
import com.github.vvojtas.dailogi_server.properties.OpenRouterEncryptionProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service responsible for encryption and decryption of sensitive data.
 */
@Service
@RequiredArgsConstructor
public class CryptoService {
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    
    private final OpenRouterEncryptionProperties encryptionProperties;
    
    /**
     * Encrypts the given plain text using AES-GCM algorithm
     * 
     * @param plainText text to encrypt
     * @return EncryptionResult containing the encrypted text and the nonce used for encryption
     * @throws CryptoException if encryption fails
     * @throws IllegalArgumentException if plainText is null or empty
     */
    public EncryptionResult encrypt(String plainText) {
        if (!StringUtils.hasText(plainText)) {
            throw new IllegalArgumentException("Plain text cannot be null or empty");
        }
        
        try {
            // Generate a random IV/nonce
            byte[] iv = new byte[encryptionProperties.getIvLength()];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            
            // Initialize cipher for encryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(decodeKey(), "AES");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(
                encryptionProperties.getTagLength(), iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
            
            // Encrypt the data
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
            
            return new EncryptionResult(encryptedText, iv);
        } catch (Exception e) {
            throw new CryptoException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypts the given cipher text using the provided nonce
     * 
     * @param cipherText the encrypted text
     * @param nonce the nonce used during encryption
     * @return the decrypted plain text
     * @throws CryptoException if decryption fails
     * @throws IllegalArgumentException if cipherText is null/empty or nonce is invalid
     */
    public String decrypt(String cipherText, byte[] nonce) {
        if (!StringUtils.hasText(cipherText)) {
            throw new IllegalArgumentException("Cipher text cannot be null or empty");
        }
        if (nonce == null || nonce.length != encryptionProperties.getIvLength()) {
            throw new IllegalArgumentException("Invalid nonce");
        }
        
        try {
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(decodeKey(), "AES");
            GCMParameterSpec parameterSpec = new GCMParameterSpec(
                encryptionProperties.getTagLength(), nonce);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);
            
            // Decrypt the data
            byte[] encryptedBytes = Base64.getDecoder().decode(cipherText);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("Decryption failed", e);
        }
    }
    
    /**
     * Helper method to decode the encryption key from the configuration
     */
    private byte[] decodeKey() {
        return KeyUtils.decodeKey(encryptionProperties.getKey());
    }
    
    /**
     * Generate a secure AES key that can be used for the application
     * This is a utility method to help generate a secure key for configuration
     * 
     * @return Base64 encoded AES key
     */
    public static String generateSecureKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // AES-256
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Could not generate key", e);
        }
    }
} 