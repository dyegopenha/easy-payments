package com.example.easy_payments.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardEncryptorTest {

   private CardEncryptor cardEncryptor;
   private static final String SECRET_KEY = "1234567890123456"; // 16-byte key for AES-128

   @BeforeEach
   void setUp() throws Exception {
      // Initialize the encryptor with a dummy key for testing
      cardEncryptor = new CardEncryptor(SECRET_KEY);
   }

   @Test
   void testEncryption() {
      String originalCardNumber = "4444555566667777";
      String encryptedData = cardEncryptor.convertToDatabaseColumn(originalCardNumber);
      assertNotNull(encryptedData);
      assertNotEquals(originalCardNumber, encryptedData, "Encrypted data should not match original data");
   }

   @Test
   void testDecryption() {
      String originalCardNumber = "4444555566667777";

      // 1. Encrypt
      String encryptedData = cardEncryptor.convertToDatabaseColumn(originalCardNumber);
      assertNotNull(encryptedData);
      assertNotEquals(originalCardNumber, encryptedData, "Encrypted data should not match original data");

      // 2. Decrypt
      String decryptedData = cardEncryptor.convertToEntityAttribute(encryptedData);
      assertEquals(originalCardNumber, decryptedData, "Decrypted data must match the original card number");
   }

   @Test
   void testNullHandling() {
      assertNull(cardEncryptor.convertToDatabaseColumn(null), "Should return null when input is null");
      assertNull(cardEncryptor.convertToEntityAttribute(null), "Should return null when input is null");
   }

   @Test
   void testKeyLengthValidation() {
      // Must throw exception if key is not 16 bytes
      assertThrows(IllegalArgumentException.class, () -> new CardEncryptor("TooShort"));
   }
}
