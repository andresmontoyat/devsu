package com.devsu.bank.cuenta.application.support;

import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.exception.CuentaNoEncontradaException;
import com.devsu.bank.cuenta.domain.exception.NumeroCuentaDuplicadoException;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Mimics the JPA adapter: version 0 on insert, +1 per update; records row locks. */
public final class InMemoryCuentaRepository implements CuentaRepositoryPort {

  private final Map<NumeroCuenta, Cuenta> cuentas = new LinkedHashMap<>();
  private final List<Boolean> bloqueosDentroDeTransaccion = new ArrayList<>();
  private final TransactionProbe probe;

  public InMemoryCuentaRepository(TransactionProbe probe) {
    this.probe = probe;
  }

  public Cuenta guardarExistente(Cuenta cuenta) {
    cuentas.put(cuenta.getNumeroCuenta(), cuenta);
    return cuenta;
  }

  @Override
  public Cuenta insert(Cuenta cuenta) {
    if (cuentas.containsKey(cuenta.getNumeroCuenta())) {
      throw new NumeroCuentaDuplicadoException(cuenta.getNumeroCuenta());
    }
    return guardar(cuenta, 0);
  }

  @Override
  public Cuenta update(Cuenta cuenta) {
    Cuenta previa = cuentas.get(cuenta.getNumeroCuenta());
    if (previa == null) {
      throw new CuentaNoEncontradaException(cuenta.getNumeroCuenta());
    }
    return guardar(cuenta, previa.getVersion() + 1);
  }

  private Cuenta guardar(Cuenta cuenta, long version) {
    Cuenta persistida =
        Cuenta.builder()
            .numeroCuenta(cuenta.getNumeroCuenta())
            .tipo(cuenta.getTipo())
            .saldoInicial(cuenta.getSaldoInicial())
            .saldoDisponible(cuenta.getSaldoDisponible())
            .activa(cuenta.isActiva())
            .clienteId(cuenta.getClienteId())
            .version(version)
            .build();
    cuentas.put(persistida.getNumeroCuenta(), persistida);
    return persistida;
  }

  @Override
  public Optional<Cuenta> findByNumero(NumeroCuenta numeroCuenta) {
    return Optional.ofNullable(cuentas.get(numeroCuenta));
  }

  @Override
  public Optional<Cuenta> findByNumeroForUpdate(NumeroCuenta numeroCuenta) {
    bloqueosDentroDeTransaccion.add(probe.isActive());
    return findByNumero(numeroCuenta);
  }

  @Override
  public boolean existsByNumero(NumeroCuenta numeroCuenta) {
    return cuentas.containsKey(numeroCuenta);
  }

  @Override
  public List<Cuenta> findByClienteId(ClienteId clienteId) {
    return cuentas.values().stream().filter(c -> c.getClienteId().equals(clienteId)).toList();
  }

  @Override
  public Pagina<Cuenta> findAll(ClienteId clienteId, PaginaSolicitud solicitud) {
    List<Cuenta> todas =
        cuentas.values().stream()
            .filter(c -> clienteId == null || c.getClienteId().equals(clienteId))
            .toList();
    int desde = Math.min(solicitud.offset(), todas.size());
    int hasta = Math.min(desde + solicitud.tamano(), todas.size());
    return new Pagina<>(todas.subList(desde, hasta), solicitud, todas.size());
  }

  public Cuenta get(String numero) {
    return cuentas.get(new NumeroCuenta(numero));
  }

  public boolean siempreBloqueoDentroDeTransaccion() {
    return !bloqueosDentroDeTransaccion.isEmpty()
        && bloqueosDentroDeTransaccion.stream().allMatch(b -> b);
  }
}
