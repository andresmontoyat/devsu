package com.devsu.bank.cuenta.domain.reporte;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.MovimientoId;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.ResultadoMovimiento;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/** The exercise's sample report: Marianela Montalvo, accounts 225487 and 496825. */
class EstadoCuentaTest {

  private static final ClienteId MARIANELA = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02");
  private static final ClienteProyeccion CLIENTE =
      ClienteProyeccion.desde(MARIANELA, "Marianela Montalvo", true, 0);
  private static final RangoFechas FEBRERO = RangoFechas.parse("2022-02-01,2022-02-28");
  private static final Instant DIA_8 = Instant.parse("2022-02-08T15:00:00Z");
  private static final Instant DIA_10 = Instant.parse("2022-02-10T15:00:00Z");

  @Test
  void should_list_the_accounts_with_balances_and_totals_and_the_movements_in_date_order() {
    ResultadoMovimiento deposito =
        cuenta("225487", TipoCuenta.CORRIENTE, "100")
            .aplicarMovimiento(MovimientoId.generar(), Monto.of("600"), DIA_10);
    ResultadoMovimiento retiro =
        cuenta("496825", TipoCuenta.AHORROS, "540")
            .aplicarMovimiento(MovimientoId.generar(), Monto.of("-540"), DIA_8);

    EstadoCuenta estado =
        EstadoCuenta.generar(
            CLIENTE,
            FEBRERO,
            List.of(
                new CuentaConMovimientos(deposito.cuenta(), List.of(deposito.movimiento())),
                new CuentaConMovimientos(retiro.cuenta(), List.of(retiro.movimiento()))));

    assertThat(estado.cuentas())
        .containsExactly(
            new ResumenCuenta(
                new NumeroCuenta("225487"),
                TipoCuenta.CORRIENTE,
                Monto.of("100"),
                Monto.of("700"),
                true,
                Monto.of("600"),
                Monto.ZERO),
            new ResumenCuenta(
                new NumeroCuenta("496825"),
                TipoCuenta.AHORROS,
                Monto.of("540"),
                Monto.ZERO,
                true,
                Monto.ZERO,
                Monto.of("540")));
    assertThat(estado.movimientos())
        .containsExactly(
            new DetalleMovimiento(
                DIA_8,
                "Marianela Montalvo",
                new NumeroCuenta("496825"),
                TipoCuenta.AHORROS,
                Monto.of("540"),
                true,
                Monto.of("-540"),
                Monto.ZERO),
            new DetalleMovimiento(
                DIA_10,
                "Marianela Montalvo",
                new NumeroCuenta("225487"),
                TipoCuenta.CORRIENTE,
                Monto.of("100"),
                true,
                Monto.of("600"),
                Monto.of("700")));
    assertThat(estado.cliente()).isEqualTo(CLIENTE);
    assertThat(estado.rango()).isEqualTo(FEBRERO);
  }

  @Test
  void should_report_accounts_without_movements_in_the_range() {
    Cuenta sinMovimientos = cuenta("495878", TipoCuenta.AHORROS, "0");

    EstadoCuenta estado =
        EstadoCuenta.generar(
            CLIENTE, FEBRERO, List.of(new CuentaConMovimientos(sinMovimientos, List.of())));

    assertThat(estado.cuentas()).hasSize(1);
    assertThat(estado.movimientos()).isEmpty();
  }

  private static Cuenta cuenta(String numero, TipoCuenta tipo, String saldo) {
    return Cuenta.abrir(new NumeroCuenta(numero), tipo, Monto.of(saldo), MARIANELA);
  }
}
