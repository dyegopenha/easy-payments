package com.example.easy_payments.model;

import com.example.easy_payments.security.CardEncryptor;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
public class PaymentEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "external_id", unique = true, nullable = false)
   private String externalId;

   @Column(name = "first_name", nullable = false)
   private String firstName;

   @Column(name = "last_name", nullable = false)
   private String lastName;

   @Column(name = "zip_code", nullable = false)
   private String zipCode;

   @Convert(converter = CardEncryptor.class)
   @Column(name = "card_number_encrypted", nullable = false, length = 256)
   private String cardNumber;

   @Column(name = "created_at", nullable = false)
   private LocalDateTime createdAt = LocalDateTime.now();
}
