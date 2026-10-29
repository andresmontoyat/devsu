package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter;

import com.devsu.bank.cuenta.application.port.outbound.ProcessedEventRepositoryPort;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * INSERT … ON CONFLICT DO NOTHING is atomic under concurrent redeliveries, and it joins the
 * consumer's transaction: if applying the event fails, the dedup row is rolled back with it.
 */
@Component
public class ProcessedEventRepositoryAdapter implements ProcessedEventRepositoryPort {

  private static final String INSERT =
      "INSERT INTO consumer_processed_event (message_id) VALUES (:id) ON CONFLICT DO NOTHING";

  private final JdbcClient jdbc;

  public ProcessedEventRepositoryAdapter(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public boolean registrarSiNuevo(UUID messageId) {
    return jdbc.sql(INSERT).param("id", messageId).update() == 1;
  }
}
