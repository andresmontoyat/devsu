package com.devsu.bank.cuenta.application.port.outbound;

import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import java.util.List;
import java.util.Optional;

public interface CuentaRepositoryPort {

  /**
   * Stores a new account. The number is a client-supplied key, so uniqueness is enforced by the
   * store itself: fails with NumeroCuentaDuplicadoException, never overwrites an existing account.
   */
  Cuenta insert(Cuenta cuenta);

  /**
   * Updates an existing account written with the version it was read with; the returned account
   * carries the new version. A version mismatch is a lost update and fails.
   */
  Cuenta update(Cuenta cuenta);

  Optional<Cuenta> findByNumero(NumeroCuenta numeroCuenta);

  /**
   * Same as {@link #findByNumero} but locks the account row until the transaction ends, so two
   * movements on one account are serialized and cannot both spend the same balance.
   */
  Optional<Cuenta> findByNumeroForUpdate(NumeroCuenta numeroCuenta);

  boolean existsByNumero(NumeroCuenta numeroCuenta);

  List<Cuenta> findByClienteId(ClienteId clienteId);

  Pagina<Cuenta> findAll(ClienteId clienteId, PaginaSolicitud solicitud);
}
