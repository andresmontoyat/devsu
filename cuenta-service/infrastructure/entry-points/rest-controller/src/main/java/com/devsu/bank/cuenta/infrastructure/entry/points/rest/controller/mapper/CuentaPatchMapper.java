package com.devsu.bank.cuenta.infrastructure.entry.points.rest.controller.mapper;

import com.devsu.bank.cuenta.application.command.PatchCuentaCommand;
import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * JSON Merge Patch (RFC 7396) for accounts. Only the type and the state are patchable: balances
 * change through movements, the owner never changes.
 */
public final class CuentaPatchMapper {

  private static final String TIPO = "tipoCuenta";
  private static final String ESTADO = "estado";
  private static final Set<String> MODIFICABLES = Set.of(TIPO, ESTADO);

  private CuentaPatchMapper() {}

  public static PatchCuentaCommand toCommand(String numeroCuenta, JsonNode patch) {
    if (patch == null || !patch.isObject()) {
      throw new DatoInvalidoException("body", "debe ser un objeto JSON");
    }
    patch.propertyNames().forEach(nombre -> validar(nombre, patch.get(nombre)));
    return new PatchCuentaCommand(
        numeroCuenta,
        patch.has(TIPO) ? patch.get(TIPO).stringValue() : null,
        patch.has(ESTADO) ? patch.get(ESTADO).booleanValue() : null);
  }

  private static void validar(String nombre, JsonNode valor) {
    if (!MODIFICABLES.contains(nombre)) {
      throw new DatoInvalidoException(nombre, "no es un campo modificable");
    }
    if (valor.isNull()) {
      throw new DatoInvalidoException(nombre, "no admite null");
    }
    boolean tipoValido = TIPO.equals(nombre) ? valor.isString() : valor.isBoolean();
    if (!tipoValido) {
      throw new DatoInvalidoException(nombre, "tipo de dato invalido");
    }
  }
}
