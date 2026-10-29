package com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cuenta.application.query.MovimientoFiltro;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.Movimiento;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.infrastructure.driven.adapters.jpa.adapter.MovimientoRepositoryAdapter;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MovimientoRepositoryAdapterIT extends AbstractJpaIT {

  private static final NumeroCuenta CORRIENTE = new NumeroCuenta("225487");
  private static final NumeroCuenta AHORROS = new NumeroCuenta("496825");
  private static final Instant DIA_8 = Instant.parse("2022-02-08T15:00:00Z");
  private static final Instant DIA_10 = Instant.parse("2022-02-10T15:00:00Z");
  private static final Instant MARZO = Instant.parse("2022-03-01T15:00:00Z");
  private static final PaginaSolicitud PAGINA = new PaginaSolicitud(0, 10);

  @Autowired private MovimientoRepositoryAdapter movimientos;

  @Test
  void should_store_a_movement_and_map_it_back_exactly() {
    Movimiento movimiento = registrar(abrir("225487", "100"), "600", DIA_10).movimiento();

    assertThat(movimientos.findById(movimiento.id())).contains(movimiento);
    assertThat(movimientos.existsByCuenta(CORRIENTE)).isTrue();
    assertThat(movimientos.existsByCuenta(AHORROS)).isFalse();
  }

  @Test
  void should_return_the_latest_movement_by_insertion_order() {
    ResultadoMovimiento primero = registrar(abrir("225487", "100"), "600", DIA_10);
    ResultadoMovimiento segundo = registrar(primero.cuenta(), "-50", DIA_8);

    assertThat(movimientos.findLatestByCuenta(CORRIENTE)).contains(segundo.movimiento());
  }

  @Test
  void should_overwrite_a_corrected_movement_instead_of_inserting_a_new_one() {
    ResultadoMovimiento deposito = registrar(abrir("225487", "100"), "600", DIA_10);
    ResultadoMovimiento corregido =
        deposito.cuenta().corregirUltimoMovimiento(deposito.movimiento(), Monto.of("-50"));

    enTransaccion(() -> movimientos.save(corregido.movimiento()));

    assertThat(movimientos.findById(deposito.movimiento().id())).contains(corregido.movimiento());
    assertThat(jdbc.sql("SELECT COUNT(*) FROM movimiento").query(Long.class).single()).isEqualTo(1);
  }

  @Test
  void should_fetch_the_movements_of_several_accounts_in_a_range_oldest_first() {
    registrar(abrir("225487", "100"), "600", DIA_10);
    registrar(abrir("496825", "540"), "-540", DIA_8);
    registrar(cuentas.findByNumero(CORRIENTE).orElseThrow(), "10", MARZO);

    List<Movimiento> febrero =
        movimientos.findByCuentasBetween(
            List.of(CORRIENTE, AHORROS),
            Instant.parse("2022-02-01T05:00:00Z"),
            Instant.parse("2022-03-01T05:00:00Z"));

    assertThat(febrero).extracting(Movimiento::fecha).containsExactly(DIA_8, DIA_10);
  }

  @Test
  void should_filter_and_page_with_optional_criteria() {
    registrar(abrir("225487", "100"), "600", DIA_10);
    registrar(abrir("496825", "540"), "-540", DIA_8);

    Pagina<Movimiento> todos = movimientos.findAll(new MovimientoFiltro(null, null, null), PAGINA);
    Pagina<Movimiento> deUnaCuenta =
        movimientos.findAll(new MovimientoFiltro(AHORROS, null, null), PAGINA);
    Pagina<Movimiento> desdeElDia9 =
        movimientos.findAll(
            new MovimientoFiltro(null, Instant.parse("2022-02-09T00:00:00Z"), MARZO), PAGINA);

    assertThat(todos.totalElementos()).isEqualTo(2);
    assertThat(deUnaCuenta.contenido()).extracting(Movimiento::numeroCuenta).containsOnly(AHORROS);
    assertThat(desdeElDia9.contenido()).extracting(Movimiento::fecha).containsExactly(DIA_10);
  }

  private ResultadoMovimiento registrar(Cuenta cuenta, String valor, Instant fecha) {
    ResultadoMovimiento resultado =
        cuenta.aplicarMovimiento(MovimientoId.generar(), Monto.of(valor), fecha);
    return enTransaccion(
        () ->
            new ResultadoMovimiento(
                cuentas.update(resultado.cuenta()), movimientos.save(resultado.movimiento())));
  }
}
