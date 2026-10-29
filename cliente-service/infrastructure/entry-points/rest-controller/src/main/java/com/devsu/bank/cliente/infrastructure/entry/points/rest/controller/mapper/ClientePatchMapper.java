package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.mapper;

import com.devsu.bank.cliente.application.command.PatchClienteCommand;
import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import com.devsu.bank.cliente.domain.exception.IdentificacionInmutableException;
import com.devsu.bank.cliente.domain.model.ClienteId;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import tools.jackson.databind.JsonNode;

/**
 * JSON Merge Patch (RFC 7396) → command. Absent member = unchanged; {@code null} would mean
 * "remove", which no client attribute allows, so it is rejected instead of silently ignored.
 */
public final class ClientePatchMapper {

  private static final Map<String, Campo<?>> CAMPOS =
      Map.of(
          "nombre", texto(PatchClienteCommand.Builder::nombre),
          "genero", texto(PatchClienteCommand.Builder::genero),
          "direccion", texto(PatchClienteCommand.Builder::direccion),
          "telefono", texto(PatchClienteCommand.Builder::telefono),
          "contrasena", texto(PatchClienteCommand.Builder::contrasena),
          "edad",
              new Campo<>(JsonNode::isInt, JsonNode::intValue, PatchClienteCommand.Builder::edad),
          "estado",
              new Campo<>(
                  JsonNode::isBoolean,
                  JsonNode::booleanValue,
                  PatchClienteCommand.Builder::estado));

  private ClientePatchMapper() {}

  public static PatchClienteCommand toCommand(ClienteId id, JsonNode patch) {
    if (patch == null || !patch.isObject()) {
      throw new DatoInvalidoException("body", "debe ser un objeto JSON");
    }
    PatchClienteCommand.Builder builder = PatchClienteCommand.builder(id);
    patch.properties().forEach(entry -> aplicar(builder, entry.getKey(), entry.getValue()));
    return builder.build();
  }

  private static void aplicar(PatchClienteCommand.Builder builder, String nombre, JsonNode valor) {
    if ("identificacion".equals(nombre)) {
      throw new IdentificacionInmutableException();
    }
    Campo<?> campo = CAMPOS.get(nombre);
    if (campo == null) {
      throw new DatoInvalidoException(nombre, "no es un campo modificable");
    }
    if (valor.isNull()) {
      throw new DatoInvalidoException(nombre, "no admite null");
    }
    campo.aplicar(builder, nombre, valor);
  }

  private static Campo<String> texto(BiConsumer<PatchClienteCommand.Builder, String> setter) {
    return new Campo<>(JsonNode::isString, JsonNode::stringValue, setter);
  }

  private record Campo<T>(
      Predicate<JsonNode> tipoValido,
      Function<JsonNode, T> lector,
      BiConsumer<PatchClienteCommand.Builder, T> setter) {

    void aplicar(PatchClienteCommand.Builder builder, String nombre, JsonNode valor) {
      if (!tipoValido.test(valor)) {
        throw new DatoInvalidoException(nombre, "tipo de dato invalido");
      }
      setter.accept(builder, lector.apply(valor));
    }
  }
}
