package com.devsu.bank.cliente.infrastructure.driven.adapters.kafka;

import java.time.Duration;

/** How long a publish may wait for the broker acknowledgement before it counts as failed. */
public record PublisherSettings(Duration sendTimeout) {}
