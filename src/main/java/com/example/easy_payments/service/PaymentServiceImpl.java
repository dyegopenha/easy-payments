package com.example.easy_payments.service;

import java.util.List;
import java.util.Optional;

import com.example.easy_payments.dto.request.CreatePaymentRequest;
import com.example.easy_payments.dto.response.PaymentResponse;
import com.example.easy_payments.exceptions.BadRequestException;
import com.example.easy_payments.exceptions.PaymentConflictException;
import com.example.easy_payments.messaging.WebhookProducer;
import com.example.easy_payments.model.PaymentEntity;
import com.example.easy_payments.model.PaymentStatus;
import com.example.easy_payments.repository.PaymentRepository;
import com.example.easy_payments.util.LuhnValidator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

   private final PaymentRepository paymentRepository;
   private final WebhookProducer webhookProducer;

   public PaymentServiceImpl(PaymentRepository paymentRepository, WebhookProducer webhookProducer) {
      this.paymentRepository = paymentRepository;
      this.webhookProducer = webhookProducer;
   }

   @Override
   @Transactional
   public PaymentResponse createPayment(CreatePaymentRequest request) {
      validatePayment(request);
      PaymentEntity payment = savePayment(request);
      processPayment(payment);
      notifyPayment(payment);

      return new PaymentResponse(payment.getId(), payment.getExternalId(), payment.getStatus());
   }

   @Override
   @Transactional(readOnly = true)
   public List<PaymentResponse> getAllPayments() {
      return paymentRepository.findAllPayments();
   }

   @Override
   @Transactional(readOnly = true)
   public Optional<PaymentResponse> findByExternalId(String externalId) {
      return paymentRepository.findByExternalId(externalId)
                              .map(p -> new PaymentResponse(p.getId(), p.getExternalId(), p.getStatus()));
   }

   private void validatePayment(CreatePaymentRequest request) {
      if (paymentRepository.findByExternalId(request.getIdempotencyKey()).isPresent()) {
         throw new PaymentConflictException(request.getIdempotencyKey());
      }
      if (!LuhnValidator.isValid(request.getCardNumber())) {
         throw new BadRequestException("Invalid card number");
      }
   }

   private PaymentEntity toPaymentEntity(CreatePaymentRequest request) {
      PaymentEntity payment = new PaymentEntity();
      payment.setExternalId(request.getIdempotencyKey());
      payment.setFirstName(request.getFirstName());
      payment.setLastName(request.getLastName());
      payment.setZipCode(request.getZipCode());
      payment.setCardNumber(request.getCardNumber());
      payment.setAmount(request.getAmount());
      return payment;
   }

   private PaymentEntity savePayment(CreatePaymentRequest request) {
      PaymentEntity payment = toPaymentEntity(request);
      paymentRepository.save(payment);
      log.info("Payment saved securely with ID: {}", payment.getId());
      return payment;
   }

   private void processPayment(PaymentEntity payment) {
      payment.setStatus(PaymentStatus.PROCESSED);// simulating successful payment
      paymentRepository.save(payment);
      log.info("Payment {} with ID: {}", payment.getStatus(), payment.getId());
   }

   private void notifyPayment(PaymentEntity payment) {
      webhookProducer.produce(payment);
   }
}
