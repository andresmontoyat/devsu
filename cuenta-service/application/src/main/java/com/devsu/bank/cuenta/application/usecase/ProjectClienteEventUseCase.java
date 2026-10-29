package com.devsu.bank.cuenta.application.usecase;

import com.devsu.bank.cuenta.application.command.ReceivedClienteEvent;
import com.devsu.bank.cuenta.application.port.inbound.ProjectClienteEventPort;
import com.devsu.bank.cuenta.application.port.outbound.ClienteProyeccionRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.CuentaRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.ProcessedEventRepositoryPort;
import com.devsu.bank.cuenta.application.port.outbound.TransactionPort;
import com.devsu.bank.cuenta.domain.model.ClienteId;
import com.devsu.bank.cuenta.domain.model.ClienteProyeccion;
import java.util.Optional;

/**
 * Applies a cliente-service event to the local projection. Idempotent twice over: the dedup row and
 * the projection change commit in one transaction (redeliveries are skipped), and versions make
 * out-of-order events harmless. Unknown event types are recorded and ignored.
 */
public final class ProjectClienteEventUseCase implements ProjectClienteEventPort {

  private static final String NOMBRE_DESCONOCIDO = "(cliente eliminado)";

  private final TransactionPort<Void> transaction;
  private final ClienteProyeccionRepositoryPort clientes;
  private final CuentaRepositoryPort cuentas;
  private final ProcessedEventRepositoryPort procesados;

  public ProjectClienteEventUseCase(
      TransactionPort<Void> transaction,
      ClienteProyeccionRepositoryPort clientes,
      CuentaRepositoryPort cuentas,
      ProcessedEventRepositoryPort procesados) {
    this.transaction = transaction;
    this.clientes = clientes;
    this.cuentas = cuentas;
    this.procesados = procesados;
  }

  @Override
  public void execute(ReceivedClienteEvent event) {
    transaction.run(
        () -> {
          if (procesados.registrarSiNuevo(event.messageId())) {
            aplicar(event);
          }
          return null;
        });
  }

  private void aplicar(ReceivedClienteEvent event) {
    ClienteId id = ClienteId.of(event.clienteId());
    Optional<ClienteProyeccion> actual = clientes.findById(id);
    switch (event.eventName()) {
      case ReceivedClienteEvent.CLIENTE_CREADO, ReceivedClienteEvent.CLIENTE_ACTUALIZADO ->
          clientes.save(
              actual
                  .map(p -> p.actualizar(event.nombre(), event.estado(), event.version()))
                  .orElseGet(
                      () ->
                          ClienteProyeccion.desde(
                              id, event.nombre(), event.estado(), event.version())));
      case ReceivedClienteEvent.CLIENTE_ELIMINADO -> eliminar(id, actual, event.version());
      default -> {
        // Tolerant reader: newer event types are acknowledged and skipped.
      }
    }
  }

  /** History is kept for statements; the client's accounts just stop accepting movements. */
  private void eliminar(ClienteId id, Optional<ClienteProyeccion> actual, long version) {
    ClienteProyeccion base =
        actual.orElseGet(() -> ClienteProyeccion.desde(id, NOMBRE_DESCONOCIDO, false, -1));
    clientes.save(base.eliminar(version));
    cuentas.findByClienteId(id).forEach(cuenta -> cuentas.update(cuenta.desactivar()));
  }
}
