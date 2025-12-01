package com.example.easy_payments.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.easy_payments.messaging.WebhookProducer;
import com.example.easy_payments.model.PaymentEntity;
import com.example.easy_payments.model.PaymentStatus;
import com.example.easy_payments.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentProcessorImpl implements IPaymentProcessor {

   private final PaymentRepository paymentRepository;
   private final WebhookProducer webhookProducer;

   @Override
   @Async
   public void process(PaymentEntity payment) {
      payment.setStatus(PaymentStatus.PROCESSED);// simulating successful payment
      paymentRepository.save(payment);
      log.info("Payment {} with ID: {}", payment.getStatus(), payment.getId());

      notifyPayment(payment);
   }

   private void notifyPayment(PaymentEntity payment) {
      webhookProducer.produce(payment);
   }
}
