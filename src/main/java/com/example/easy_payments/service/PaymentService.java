package com.example.easy_payments.service;

import com.example.easy_payments.dto.request.CreatePaymentRequest;
import com.example.easy_payments.dto.response.PaymentResponse;
import com.example.easy_payments.exceptions.PaymentConflictException;
import com.example.easy_payments.messaging.WebhookProducer;
import com.example.easy_payments.model.PaymentEntity;
import com.example.easy_payments.model.PaymentStatus;
import com.example.easy_payments.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PaymentService {

   private final PaymentRepository paymentRepository;
   private final WebhookProducer webhookProducer;

   public PaymentService(PaymentRepository paymentRepository, WebhookProducer webhookProducer) {
      this.paymentRepository = paymentRepository;
      this.webhookProducer = webhookProducer;
   }

   @Transactional
   public PaymentResponse createPayment(CreatePaymentRequest request) {

      if (paymentRepository.findByExternalId(request.getIdempotencyKey()).isPresent()) {
         throw new PaymentConflictException(request.getIdempotencyKey());
      }

      PaymentEntity payment = new PaymentEntity();
      payment.setExternalId(request.getIdempotencyKey());
      payment.setFirstName(request.getFirstName());
      payment.setLastName(request.getLastName());
      payment.setZipCode(request.getZipCode());
      payment.setCardNumber(request.getCardNumber());
      payment.setAmount(request.getAmount());
      payment.setStatus(PaymentStatus.PROCESSED);

      PaymentEntity savedPayment = paymentRepository.save(payment);

      log.info("Payment saved securely with ID: {}", savedPayment.getId());

      webhookProducer.produce(savedPayment);

      return new PaymentResponse(savedPayment.getId(), savedPayment.getExternalId(), savedPayment.getStatus());
   }
}
