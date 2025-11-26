package com.example.easy_payments.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "failed_messages")
@Data
@NoArgsConstructor
public class FailedMessageEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "payload", nullable = false)
   private String payload;

   @Column(name = "reason", nullable = false)
   private String reason;

   @Column(name = "created_at", nullable = false)
   private LocalDateTime createdAt = LocalDateTime.now();
}
