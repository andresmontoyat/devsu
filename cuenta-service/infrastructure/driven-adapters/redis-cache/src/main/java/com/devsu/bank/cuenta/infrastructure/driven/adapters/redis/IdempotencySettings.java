package com.devsu.bank.cuenta.infrastructure.driven.adapters.redis;

import java.time.Duration;

/**
 * {@code ttl}: how long a completed key replays its movement. {@code inProgressTtl}: how long a
 * reservation survives a crashed request before the key can be used again.
 */
public record IdempotencySettings(String keyPrefix, Duration ttl, Duration inProgressTtl) {}
