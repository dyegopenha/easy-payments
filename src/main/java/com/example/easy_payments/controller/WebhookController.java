package com.example.easy_payments.controller;

import com.example.easy_payments.dto.request.RegisterWebhookRequest;
import com.example.easy_payments.dto.response.WebhookResponse;
import com.example.easy_payments.service.WebhookService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

   private final WebhookService webhookService;

   public WebhookController(WebhookService webhookService) {
      this.webhookService = webhookService;
   }

   @PostMapping
   public ResponseEntity<WebhookResponse> createWebhook(@Valid @RequestBody RegisterWebhookRequest request) {
      WebhookResponse response = webhookService.createWebhook(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
   }
}
