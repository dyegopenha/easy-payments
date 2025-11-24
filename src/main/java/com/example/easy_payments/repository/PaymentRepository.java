package com.example.easy_payments.repository;

import com.example.easy_payments.model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

   Optional<PaymentEntity> findByExternalId(String externalId);
}
