package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Boots only this module's adapters against a real PostgreSQL for the integration tests. */
@SpringBootApplication
class JpaTestApplication {}
