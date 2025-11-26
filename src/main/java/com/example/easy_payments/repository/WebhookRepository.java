package com.example.easy_payments.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.easy_payments.model.WebhookEntity;

@Repository
public interface WebhookRepository extends JpaRepository<WebhookEntity, Long> {

   Optional<WebhookEntity> findByUrl(String url);
}
