package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.exception.CuentaModificadaConcurrentementeException;
import com.devsu.bank.cuenta.domain.exception.NumeroCuentaDuplicadoException;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import org.junit.jupiter.api.Test;

class CuentaRepositoryAdapterIT extends AbstractJpaIT {

  private static final NumeroCuenta NUMERO = new NumeroCuenta("225487");

  @Test
  void should_store_and_map_back_every_attribute_with_exact_money() {
    Cuenta guardada = abrir("225487", "100.5");

    Cuenta leida = cuentas.findByNumero(NUMERO).orElseThrow();

    assertThat(leida.getSaldoDisponible()).isEqualTo(Monto.of("100.50"));
    assertThat(leida.getTipo()).isEqualTo(TipoCuenta.AHORROS);
    assertThat(leida.getClienteId()).isEqualTo(MARIANELA);
    assertThat(leida.isActiva()).isTrue();
    assertThat(guardada.getVersion()).isZero();
  }

  @Test
  void should_increment_the_version_and_reject_a_stale_write() {
    Cuenta guardada = abrir("225487", "100");
    Cuenta actualizada = enTransaccion(() -> cuentas.update(guardada.desactivar()));

    assertThat(actualizada.getVersion()).isEqualTo(1);
    assertThatThrownBy(
            () -> enTransaccion(() -> cuentas.update(guardada.cambiarTipo(TipoCuenta.CORRIENTE))))
        .isInstanceOf(CuentaModificadaConcurrentementeException.class);
  }

  @Test
  void should_translate_the_unique_number_into_a_duplicate() {
    abrir("225487", "100");

    assertThatThrownBy(() -> abrir("225487", "5"))
        .isInstanceOf(NumeroCuentaDuplicadoException.class);
  }

  @Test
  void should_find_accounts_by_owner_and_page_with_or_without_filter() {
    abrir("225487", "100");
    abrir("496825", "540");

    assertThat(cuentas.findByClienteId(MARIANELA)).hasSize(2);
    assertThat(cuentas.existsByNumero(NUMERO)).isTrue();
    Pagina<Cuenta> todas = cuentas.findAll(null, new PaginaSolicitud(0, 1));
    Pagina<Cuenta> ajenas =
        cuentas.findAll(
            ClienteId.of("11111111-1111-1111-1111-111111111111"), new PaginaSolicitud(0, 10));
    assertThat(todas.totalElementos()).isEqualTo(2);
    assertThat(todas.contenido()).hasSize(1);
    assertThat(ajenas.totalElementos()).isZero();
  }

  @Test
  void should_hold_a_row_lock_until_the_transaction_ends() {
    abrir("225487", "100");

    String modo =
        enTransaccion(
            () -> {
              cuentas.findByNumeroForUpdate(NUMERO);
              return jdbc.sql(
                      "SELECT l.mode FROM pg_locks l JOIN pg_class c ON c.oid = l.relation"
                          + " WHERE c.relname = 'cuenta' AND l.pid = pg_backend_pid()"
                          + " AND l.mode = 'RowShareLock'")
                  .query(String.class)
                  .optional()
                  .orElse("sin bloqueo");
            });

    assertThat(modo).isEqualTo("RowShareLock");
  }

  @Test
  void should_refuse_a_negative_balance_even_if_the_domain_were_bypassed() {
    abrir("225487", "100");

    assertThatThrownBy(
            () ->
                jdbc.sql("UPDATE cuenta SET saldo_disponible = -1 WHERE numero_cuenta = '225487'")
                    .update())
        .hasMessageContaining("ck_cuenta_saldo_disponible");
  }

  @Test
  void should_refuse_to_update_an_account_that_was_never_stored() {
    Cuenta nunca =
        Cuenta.abrir(new NumeroCuenta("999999"), TipoCuenta.AHORROS, Monto.of("1"), MARIANELA);

    assertThatThrownBy(() -> enTransaccion(() -> cuentas.update(nunca)))
        .isInstanceOf(com.devsu.bank.cuenta.domain.exception.CuentaNoEncontradaException.class);
  }
}
