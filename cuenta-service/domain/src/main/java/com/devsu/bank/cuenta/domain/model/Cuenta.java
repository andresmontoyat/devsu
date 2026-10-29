package com.devsu.bank.cuenta.domain.model;

import com.devsu.bank.cuenta.domain.exception.CuentaInactivaException;
import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import com.devsu.bank.cuenta.domain.exception.MovimientoNoModificableException;
import com.devsu.bank.cuenta.domain.exception.SaldoNoDisponibleException;
import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root of the account context. Immutable: every movement returns a new account together
 * with the ledger entry, and the available balance can never go below zero.
 */
public final class Cuenta {

  private final NumeroCuenta numeroCuenta;
  private final TipoCuenta tipo;
  private final Monto saldoInicial;
  private final Monto saldoDisponible;
  private final boolean activa;
  private final ClienteId clienteId;
  private final long version;

  private Cuenta(Builder builder) {
    this.numeroCuenta = Requisitos.presente(builder.numeroCuenta, "numeroCuenta");
    this.tipo = Requisitos.presente(builder.tipo, "tipoCuenta");
    this.saldoInicial = noNegativo(builder.saldoInicial, "saldoInicial");
    this.saldoDisponible = noNegativo(builder.saldoDisponible, "saldoDisponible");
    this.activa = builder.activa;
    this.clienteId = Requisitos.presente(builder.clienteId, "clienteId");
    this.version = builder.version;
  }

  public static Cuenta abrir(
      NumeroCuenta numeroCuenta, TipoCuenta tipo, Monto saldoInicial, ClienteId clienteId) {
    return builder()
        .numeroCuenta(numeroCuenta)
        .tipo(tipo)
        .saldoInicial(saldoInicial)
        .saldoDisponible(saldoInicial)
        .activa(true)
        .clienteId(clienteId)
        .build();
  }

  public static Builder builder() {
    return new Builder();
  }

  /** F2/F3: positive values deposit, negative withdraw; an overdraft is "Saldo no disponible". */
  public ResultadoMovimiento aplicarMovimiento(MovimientoId id, Monto valor, Instant fecha) {
    exigirActiva();
    TipoMovimiento tipoMovimiento = TipoMovimiento.desde(valor);
    Monto nuevoSaldo = disponible(saldoDisponible.plus(valor));
    Movimiento movimiento =
        new Movimiento(id, numeroCuenta, fecha, tipoMovimiento, valor, saldoDisponible, nuevoSaldo);
    return new ResultadoMovimiento(conSaldo(nuevoSaldo), movimiento);
  }

  /**
   * Replaces the value of the account's latest movement, keeping its id and date. The caller
   * guarantees it is the latest; the domain checks it belongs here and still explains the balance.
   */
  public ResultadoMovimiento corregirUltimoMovimiento(Movimiento ultimo, Monto nuevoValor) {
    exigirActiva();
    if (!ultimo.numeroCuenta().equals(numeroCuenta) || !ultimo.saldo().equals(saldoDisponible)) {
      throw new MovimientoNoModificableException(ultimo.id());
    }
    Monto nuevoSaldo = disponible(ultimo.saldoAnterior().plus(nuevoValor));
    Movimiento corregido =
        new Movimiento(
            ultimo.id(),
            numeroCuenta,
            ultimo.fecha(),
            TipoMovimiento.desde(nuevoValor),
            nuevoValor,
            ultimo.saldoAnterior(),
            nuevoSaldo);
    return new ResultadoMovimiento(conSaldo(nuevoSaldo), corregido);
  }

  public Cuenta activar() {
    return activa ? this : toBuilder().activa(true).build();
  }

  public Cuenta desactivar() {
    return activa ? toBuilder().activa(false).build() : this;
  }

  public Cuenta cambiarTipo(TipoCuenta nuevo) {
    return toBuilder().tipo(nuevo).build();
  }

  /** Only meaningful before the first movement; the application layer enforces that. */
  public Cuenta redefinirSaldoInicial(Monto nuevo) {
    return toBuilder().saldoInicial(nuevo).saldoDisponible(nuevo).build();
  }

  public NumeroCuenta getNumeroCuenta() {
    return numeroCuenta;
  }

  public TipoCuenta getTipo() {
    return tipo;
  }

  public Monto getSaldoInicial() {
    return saldoInicial;
  }

  public Monto getSaldoDisponible() {
    return saldoDisponible;
  }

  public boolean isActiva() {
    return activa;
  }

  public ClienteId getClienteId() {
    return clienteId;
  }

  public long getVersion() {
    return version;
  }

  private void exigirActiva() {
    if (!activa) {
      throw new CuentaInactivaException(numeroCuenta);
    }
  }

  private static Monto disponible(Monto saldo) {
    if (saldo.isNegative()) {
      throw new SaldoNoDisponibleException();
    }
    return saldo;
  }

  private static Monto noNegativo(Monto monto, String campo) {
    if (Requisitos.presente(monto, campo).isNegative()) {
      throw new DatoInvalidoException(campo, "no puede ser negativo");
    }
    return monto;
  }

  private Cuenta conSaldo(Monto nuevoSaldo) {
    return toBuilder().saldoDisponible(nuevoSaldo).build();
  }

  private Builder toBuilder() {
    return builder()
        .numeroCuenta(numeroCuenta)
        .tipo(tipo)
        .saldoInicial(saldoInicial)
        .saldoDisponible(saldoDisponible)
        .activa(activa)
        .clienteId(clienteId)
        .version(version);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof Cuenta cuenta && numeroCuenta.equals(cuenta.numeroCuenta);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numeroCuenta);
  }

  @Override
  public String toString() {
    return "Cuenta[numero=" + numeroCuenta.value() + ", tipo=" + tipo + ", activa=" + activa + "]";
  }

  public static final class Builder {
    private NumeroCuenta numeroCuenta;
    private TipoCuenta tipo;
    private Monto saldoInicial;
    private Monto saldoDisponible;
    private boolean activa;
    private ClienteId clienteId;
    private long version;

    private Builder() {}

    public Builder numeroCuenta(NumeroCuenta numeroCuenta) {
      this.numeroCuenta = numeroCuenta;
      return this;
    }

    public Builder tipo(TipoCuenta tipo) {
      this.tipo = tipo;
      return this;
    }

    public Builder saldoInicial(Monto saldoInicial) {
      this.saldoInicial = saldoInicial;
      return this;
    }

    public Builder saldoDisponible(Monto saldoDisponible) {
      this.saldoDisponible = saldoDisponible;
      return this;
    }

    public Builder activa(boolean activa) {
      this.activa = activa;
      return this;
    }

    public Builder clienteId(ClienteId clienteId) {
      this.clienteId = clienteId;
      return this;
    }

    public Builder version(long version) {
      this.version = version;
      return this;
    }

    public Cuenta build() {
      return new Cuenta(this);
    }
  }
}
