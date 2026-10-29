package com.devsu.bank.cliente.application.outbox;

import java.time.Clock;

public record OutboxDispatchSettings(
    ClaimRequest claimRequest, RetryPolicy retryPolicy, Clock clock) {}
