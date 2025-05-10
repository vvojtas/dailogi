package com.github.vvojtas.dailogi_server.service.util;

import com.github.vvojtas.dailogi_server.exception.CryptoException;
import com.github.vvojtas.dailogi_server.properties.OpenRouterEncryptionProperties;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Disabled;

@ExtendWith(MockitoExtension.class)
class CryptoServiceTest {

    @Mock
    private OpenRouterEncryptionProperties encryptionProperties;

    @InjectMocks
    private CryptoService cryptoService;

    private String testKey;
    private String plainText;

    @BeforeEach
    void setUp() {
        // Generate a test key (base64 encoded 32 bytes for AES-256)
        testKey = Base64.getEncoder().encodeToString(new byte[32]);
        plainText = "this is a test message";

        // Configure default properties with lenient stubs
        lenient().when(encryptionProperties.getKey()).thenReturn(testKey);
        lenient().when(encryptionProperties.getIvLength()).thenReturn(12);
        lenient().when(encryptionProperties.getTagLength()).thenReturn(128);
    }

    @Test
    @DisplayName("encrypt should create encrypted text with nonce")
    void encryptShouldCreateEncryptedTextWithNonce() {
        // Act
        EncryptionResult result = cryptoService.encrypt(plainText);

        // Assert
        assertNotNull(result.getCipherText());
        assertNotNull(result.getNonce());
        assertNotEquals(plainText, result.getCipherText());
        assertEquals(12, result.getNonce().length);
    }

    @Test
    @DisplayName("decrypt should correctly decrypt encrypted text")
    void decryptShouldCorrectlyDecryptEncryptedText() {
        // Arrange
        EncryptionResult encryptionResult = cryptoService.encrypt(plainText);

        // Act
        String decrypted = cryptoService.decrypt(
            encryptionResult.getCipherText(), 
            encryptionResult.getNonce()
        );

        // Assert
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("encrypt should throw IllegalArgumentException for empty plaintext")
    void encryptShouldThrowExceptionForEmptyPlaintext() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cryptoService.encrypt("")
        );
        
        assertEquals("Plain text cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("decrypt should throw IllegalArgumentException for empty ciphertext")
    void decryptShouldThrowExceptionForEmptyCiphertext() {
        // Arrange
        byte[] validNonce = new byte[12];
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cryptoService.decrypt("", validNonce)
        );
        
        assertEquals("Cipher text cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("decrypt should throw IllegalArgumentException for invalid nonce")
    void decryptShouldThrowExceptionForInvalidNonce() {
        // Arrange
        String validCipherText = "validCipherText";
        byte[] invalidNonce = new byte[8]; // Wrong length
        
        when(encryptionProperties.getIvLength()).thenReturn(12);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> cryptoService.decrypt(validCipherText, invalidNonce)
        );
        
        assertEquals("Invalid nonce", exception.getMessage());
    }

    @Test
    @DisplayName("decrypt should throw CryptoException for tampered ciphertext")
    void decryptShouldThrowExceptionForTamperedCiphertext() {
        // Arrange
        EncryptionResult encryptionResult = cryptoService.encrypt(plainText);
        String tamperedCipherText = encryptionResult.getCipherText() + "tampered";
        
        // Act & Assert
        assertThrows(
            CryptoException.class,
            () -> cryptoService.decrypt(tamperedCipherText, encryptionResult.getNonce())
        );
    }

    @Test
    @DisplayName("decrypt should throw CryptoException for tampered nonce")
    void decryptShouldThrowExceptionForTamperedNonce() {
        // Arrange
        EncryptionResult encryptionResult = cryptoService.encrypt(plainText);
        byte[] tamperedNonce = new byte[12];
        
        // Act & Assert
        assertThrows(
            CryptoException.class,
            () -> cryptoService.decrypt(encryptionResult.getCipherText(), tamperedNonce)
        );
    }

    @Test
    @DisplayName("generateSecureKey should return Base64 encoded string")
    void generateSecureKeyShouldReturnBase64EncodedString() {
        // Act
        String generatedKey = CryptoService.generateSecureKey();
        
        // Assert
        assertNotNull(generatedKey);
        // Verify it's base64 encoded and can be decoded
        byte[] decodedKey = Base64.getDecoder().decode(generatedKey);
        // AES-256 key should be 32 bytes (256 bits)
        assertEquals(32, decodedKey.length);
    }

    @Test
    //@Disabled("Enable manually to generate a secure key")
    @DisplayName("Generate and print a secure key")
    void generateAndPrintSecureKey() {
        String secureKey = CryptoService.generateSecureKey();
        System.out.println("Generated secure key: " + secureKey);
        assertNotNull(secureKey);
    }
} 