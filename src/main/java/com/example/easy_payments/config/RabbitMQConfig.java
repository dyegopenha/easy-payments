package com.example.easy_payments.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
   public static final String EXCHANGE = "payment.webhook.exchange";
   public static final String RETRY_EXCHANGE = "payment.webhook.retry.exchange";
   public static final String MAIN_QUEUE = "payment.webhook.queue";
   public static final String DLQ = "payment.webhook.dlq";
   public static final String RETRY_PREFIX = "payment.webhook.retry.";

   public static final int RETRY_1_TTL = 2000; // 2s
   public static final int RETRY_2_TTL = 5000; // 5s
   public static final int RETRY_3_TTL = 10000; // 10s

   @Bean
   public Exchange exchange() { return ExchangeBuilder.directExchange(EXCHANGE).durable(true).build(); }

   @Bean
   public Exchange retryExchange() { return ExchangeBuilder.directExchange(RETRY_EXCHANGE).durable(true).build(); }

   @Bean
   public Queue mainQueue() {
      // when consumer rejects without requeue, messages will be routed to retry exchange -> retry.1
      return QueueBuilder.durable(MAIN_QUEUE)
                         .withArgument("x-dead-letter-exchange", RETRY_EXCHANGE)
                         .withArgument("x-dead-letter-routing-key", "retry.1")
                         .build();
   }

   @Bean
   public Queue retryQueue1() {
      return QueueBuilder.durable(RETRY_PREFIX + "1")
                         .withArgument("x-message-ttl", RETRY_1_TTL)
                         .withArgument("x-dead-letter-exchange", EXCHANGE)
                         .withArgument("x-dead-letter-routing-key", "payment.created")
                         .build();
   }

   @Bean
   public Queue retryQueue2() {
      return QueueBuilder.durable(RETRY_PREFIX + "2")
                         .withArgument("x-message-ttl", RETRY_2_TTL)
                         .withArgument("x-dead-letter-exchange", EXCHANGE)
                         .withArgument("x-dead-letter-routing-key", "payment.created")
                         .build();
   }

   @Bean
   public Queue retryQueue3() {
      return QueueBuilder.durable(RETRY_PREFIX + "3")
                         .withArgument("x-message-ttl", RETRY_3_TTL)
                         .withArgument("x-dead-letter-exchange", EXCHANGE)
                         .withArgument("x-dead-letter-routing-key", "payment.created")
                         .build();
   }

   @Bean
   public Queue dlq() { return QueueBuilder.durable(DLQ).build(); }

   @Bean
   public Binding binding() { return BindingBuilder.bind(mainQueue()).to(exchange()).with("payment.created").noargs(); }

   @Bean
   public Binding retry1Binding() { return BindingBuilder.bind(retryQueue1()).to(retryExchange()).with("retry.1").noargs(); }

   @Bean
   public Binding retry2Binding() { return BindingBuilder.bind(retryQueue2()).to(retryExchange()).with("retry.2").noargs(); }

   @Bean
   public Binding retry3Binding() { return BindingBuilder.bind(retryQueue3()).to(retryExchange()).with("retry.3").noargs(); }
}
