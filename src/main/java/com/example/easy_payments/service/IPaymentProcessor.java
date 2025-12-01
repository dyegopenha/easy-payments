package com.example.easy_payments.service;

import org.springframework.scheduling.annotation.Async;

import com.example.easy_payments.model.PaymentEntity;

public interface IPaymentProcessor {

   void process(PaymentEntity payment);
}
