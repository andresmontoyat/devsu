package com.devsu.bank.cuenta.application.support;

import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.Monto;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import com.devsu.bank.cuenta.domain.model.TipoCuenta;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** Fakes shared by the account use case tests, plus the exercise's sample data. */
public final class CuentaHarness {

  public static final Instant AHORA = Instant.parse("2022-02-10T15:00:00Z");
  public static final Clock CLOCK = Clock.fixed(AHORA, ZoneOffset.UTC);
  public static final ZoneId ZONA = ZoneId.of("America/Guayaquil");
  public static final ClienteId JOSE_LEMA = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01");
  public static final ClienteId MARIANELA = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02");
  public static final ClienteId JUAN_OSORIO = ClienteId.of("0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e03");

  public final TransactionProbe probe = new TransactionProbe();
  public final InMemoryCuentaRepository cuentas = new InMemoryCuentaRepository(probe);
  public final InMemoryMovimientoRepository movimientos = new InMemoryMovimientoRepository();
  public final InMemoryClienteProyeccionRepository clientes =
      new InMemoryClienteProyeccionRepository();
  public final InMemoryProcessedEvents procesados = new InMemoryProcessedEvents();
  public final FakeIdempotencyStore idempotencia = new FakeIdempotencyStore();

  public <R> FakeTransaction<R> transaction() {
    return new FakeTransaction<>(probe);
  }

  public CuentaHarness conClientesDelEnunciado() {
    clientes.save(ClienteProyeccion.desde(JOSE_LEMA, "Jose Lema", true, 0));
    clientes.save(ClienteProyeccion.desde(MARIANELA, "Marianela Montalvo", true, 0));
    clientes.save(ClienteProyeccion.desde(JUAN_OSORIO, "Juan Osorio", true, 0));
    return this;
  }

  public CuentaHarness conCuentasDelEnunciado() {
    conClientesDelEnunciado();
    abrir("478758", TipoCuenta.AHORROS, "2000", JOSE_LEMA);
    abrir("225487", TipoCuenta.CORRIENTE, "100", MARIANELA);
    abrir("495878", TipoCuenta.AHORROS, "0", JUAN_OSORIO);
    abrir("496825", TipoCuenta.AHORROS, "540", MARIANELA);
    return this;
  }

  public Cuenta abrir(String numero, TipoCuenta tipo, String saldo, ClienteId cliente) {
    return cuentas.save(Cuenta.abrir(new NumeroCuenta(numero), tipo, Monto.of(saldo), cliente));
  }
}
