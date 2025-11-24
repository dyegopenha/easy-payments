package com.example.easy_payments.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

/**
 * JPA Attribute Converter to automatically encrypt card numbers before saving
 * to the database and decrypt them when loading.
 */
@Component
@Converter
public class CardEncryptor implements AttributeConverter<String, String> {

   private static final String ALGORITHM = "AES";
   private final Key key;
   private final Cipher cipherEncrypt;
   private final Cipher cipherDecrypt;

   public CardEncryptor(@Value("${encryption.secret-key}") String secretKey) throws Exception {
      // In a production environment, use a robust key management solution (e.g., Vault).
      // The key MUST be 16, 24, or 32 bytes long for AES-128, AES-192, or AES-256.
      if (secretKey.length() != 16) {
         throw new IllegalArgumentException("Encryption key must be exactly 16 bytes (128 bits).");
      }
      this.key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
      this.cipherEncrypt = Cipher.getInstance(ALGORITHM);
      this.cipherDecrypt = Cipher.getInstance(ALGORITHM);

      cipherEncrypt.init(Cipher.ENCRYPT_MODE, key);
      cipherDecrypt.init(Cipher.DECRYPT_MODE, key);
   }

   @Override
   public String convertToDatabaseColumn(String attribute) {
      if (attribute == null) return null;
      try {
         byte[] encryptedBytes = cipherEncrypt.doFinal(attribute.getBytes());
         return Base64.getUrlEncoder().encodeToString(encryptedBytes);
      } catch (Exception e) {
         throw new IllegalStateException("Error encrypting card number", e);
      }
   }

   @Override
   public String convertToEntityAttribute(String dbData) {
      if (dbData == null) return null;
      try {
         byte[] decodedBytes = Base64.getUrlDecoder().decode(dbData);
         byte[] decryptedBytes = cipherDecrypt.doFinal(decodedBytes);
         return new String(decryptedBytes);
      } catch (Exception e) {
         throw new IllegalStateException("Error decrypting card number from database", e);
      }
   }
}
