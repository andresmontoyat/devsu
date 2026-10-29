package com.devsu.bank.cuenta;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cuenta.support.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.RestClient;

/**
 * F6 integration test: HTTP → use case → PostgreSQL (row lock) → Valkey, end to end. F3: a
 * withdrawal without funds answers 422 "Saldo no disponible" and changes nothing.
 */
class MovimientoSaldoNoDisponibleIT extends AbstractIntegrationTest {

  private static final String JUAN_OSORIO = "0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e03";
  private static final ParameterizedTypeReference<Map<String, Object>> JSON =
      new ParameterizedTypeReference<>() {};

  @Autowired private JdbcClient jdbc;

  private RestClient client;
  private String numeroCuenta;

  @BeforeEach
  void abrirCuentaConCincuenta() {
    client =
        RestClient.builder()
            .baseUrl(baseUrl())
            .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {})
            .build();
    numeroCuenta = String.valueOf(100_000_000L + System.nanoTime() % 900_000_000L);
    client
        .post()
        .uri("/cuentas")
        .contentType(MediaType.APPLICATION_JSON)
        .body(
            Map.of(
                "numeroCuenta",
                numeroCuenta,
                "tipoCuenta",
                "Ahorros",
                "saldoInicial",
                50,
                "clienteId",
                JUAN_OSORIO))
        .retrieve()
        .toBodilessEntity();
  }

  @Test
  void should_answer_saldo_no_disponible_and_leave_the_account_untouched() {
    ResponseEntity<Map<String, Object>> respuesta = movimiento("-100", null);

    assertThat(respuesta.getStatusCode().value()).isEqualTo(422);
    assertThat(respuesta.getHeaders().getContentType())
        .isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(respuesta.getBody())
        .containsEntry("code", "SALDO_NO_DISPONIBLE")
        .containsEntry("detail", "Saldo no disponible")
        .containsEntry("status", 422);
    assertThat(saldoDisponible()).isEqualTo(50.0);
    assertThat(movimientosGuardados()).isZero();
  }

  @Test
  void should_apply_a_retried_withdrawal_only_once_when_it_carries_an_idempotency_key() {
    String clave = "it-" + numeroCuenta;

    ResponseEntity<Map<String, Object>> primero = movimiento("-20", clave);
    ResponseEntity<Map<String, Object>> reintento = movimiento("-20", clave);

    assertThat(primero.getStatusCode().value()).isEqualTo(201);
    assertThat(reintento.getBody().get("movimientoId"))
        .isEqualTo(primero.getBody().get("movimientoId"));
    assertThat(saldoDisponible()).isEqualTo(30.0);
    assertThat(movimientosGuardados()).isEqualTo(1);
  }

  private ResponseEntity<Map<String, Object>> movimiento(String valor, String idempotencyKey) {
    RestClient.RequestBodySpec request =
        client.post().uri("/movimientos").contentType(MediaType.APPLICATION_JSON);
    if (idempotencyKey != null) {
      request.header("Idempotency-Key", idempotencyKey);
    }
    return request
        .body(Map.of("numeroCuenta", numeroCuenta, "valor", Double.parseDouble(valor)))
        .retrieve()
        .toEntity(JSON);
  }

  private double saldoDisponible() {
    Map<String, Object> cuenta =
        client.get().uri("/cuentas/{n}", numeroCuenta).retrieve().body(JSON);
    return ((Number) cuenta.get("saldoDisponible")).doubleValue();
  }

  private long movimientosGuardados() {
    return jdbc.sql("SELECT COUNT(*) FROM movimiento WHERE numero_cuenta = ?")
        .param(numeroCuenta)
        .query(Long.class)
        .single();
  }
}
