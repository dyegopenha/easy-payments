package com.example.easy_payments.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "webhooks")
@Data
@NoArgsConstructor
public class WebhookEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "url", nullable = false, unique = true)
   private String url;
}
