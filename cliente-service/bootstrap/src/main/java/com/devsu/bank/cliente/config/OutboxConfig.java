package com.devsu.bank.cliente.config;

import com.devsu.bank.cliente.application.event.ClienteOutboxEvents;
import com.devsu.bank.cliente.application.outbox.ClaimRequest;
import com.devsu.bank.cliente.application.outbox.OutboxDispatchSettings;
import com.devsu.bank.cliente.application.outbox.OutboxEventFactory;
import com.devsu.bank.cliente.application.outbox.RetryPolicy;
import com.devsu.bank.cliente.application.outbox.dispatch.SendOutboxMessagesUseCase;
import com.devsu.bank.cliente.application.port.inbound.SendOutboxMessagesPort;
import com.devsu.bank.cliente.application.port.outbound.MessagePublisherPort;
import com.devsu.bank.cliente.application.port.outbound.OutboxDispatchStore;
import com.devsu.bank.cliente.application.port.outbound.OutboxWriter;
import com.devsu.bank.cliente.application.port.outbound.TraceIdSource;
import com.devsu.bank.cliente.infrastructure.driven.adapters.kafka.PublisherSettings;
import com.devsu.bank.cliente.infrastructure.entry.points.scheduler.task.OutboxDispatchScheduler;
import java.lang.management.ManagementFactory;
import java.time.Clock;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class OutboxConfig {

  @Bean
  OutboxEventFactory outboxEventFactory(
      @Value("${spring.application.name}") String origin, Clock clock, TraceIdSource traceIds) {
    return new OutboxEventFactory(origin, clock, traceIds);
  }

  @Bean
  ClienteOutboxEvents clienteOutboxEvents(OutboxWriter writer, OutboxEventFactory factory) {
    return new ClienteOutboxEvents(writer, factory);
  }

  @Bean
  PublisherSettings publisherSettings(OutboxProperties properties) {
    return new PublisherSettings(properties.publisher().sendTimeout());
  }

  @Bean
  SendOutboxMessagesPort sendOutboxMessagesPort(
      OutboxDispatchStore store,
      MessagePublisherPort publisher,
      OutboxProperties properties,
      @Value("${spring.application.name}") String origin) {
    return new SendOutboxMessagesUseCase(store, publisher, settings(properties, origin));
  }

  /** One dispatcher thread per instance; other instances get disjoint rows via SKIP LOCKED. */
  @Bean(destroyMethod = "shutdown")
  ExecutorService outboxDispatchExecutor() {
    return Executors.newSingleThreadExecutor(
        Thread.ofPlatform().name("outbox-dispatcher").daemon().factory());
  }

  @Bean
  @ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true")
  OutboxDispatchScheduler outboxDispatchScheduler(
      SendOutboxMessagesPort sendOutboxMessages, ExecutorService outboxDispatchExecutor) {
    return new OutboxDispatchScheduler(sendOutboxMessages, outboxDispatchExecutor);
  }

  private static OutboxDispatchSettings settings(OutboxProperties properties, String origin) {
    String instance = origin + "@" + ManagementFactory.getRuntimeMXBean().getName();
    ClaimRequest claim =
        new ClaimRequest(origin, properties.batchSize(), properties.claimTtl(), instance);
    RetryPolicy retry =
        new RetryPolicy(
            properties.maxAttempts(),
            properties.backoff().baseDelay(),
            properties.backoff().maxDelay());
    return new OutboxDispatchSettings(claim, retry, Clock.systemUTC());
  }
}
