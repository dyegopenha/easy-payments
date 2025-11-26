package com.example.easy_payments.controller;

import java.util.List;

import com.example.easy_payments.dto.request.CreatePaymentRequest;
import com.example.easy_payments.dto.response.PaymentResponse;
import com.example.easy_payments.service.IPaymentService;
import com.example.easy_payments.service.PaymentServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
public class PaymentController {

   private final IPaymentService paymentService;

   public PaymentController(PaymentServiceImpl paymentService) {
      this.paymentService = paymentService;
   }

   @PostMapping
   public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
      PaymentResponse response = paymentService.createPayment(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
   }

   @GetMapping
   public ResponseEntity<List<PaymentResponse>> getAllPayments() {
      return ResponseEntity.ok(paymentService.getAllPayments());
   }

   @GetMapping("{externalId}")
   public ResponseEntity<PaymentResponse> getPaymentByExternalId(@PathVariable String externalId) {
      return paymentService.findByExternalId(externalId)
                           .map(ResponseEntity::ok)
                           .orElse(ResponseEntity.notFound().build());
   }
}
