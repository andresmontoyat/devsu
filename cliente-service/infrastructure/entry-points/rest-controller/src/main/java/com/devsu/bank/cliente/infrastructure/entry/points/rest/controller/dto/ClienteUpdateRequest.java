package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** PUT body: full representation. Omitting the password keeps the current one. */
public record ClienteUpdateRequest(
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank String genero,
    @NotNull @Min(0) @Max(150) Integer edad,
    @NotBlank @Size(max = 20) String identificacion,
    @NotBlank @Size(max = 200) String direccion,
    @NotBlank @Size(max = 16) String telefono,
    @Size(min = 4, max = 72) String contrasena,
    @NotNull Boolean estado) {

  @Override
  public String toString() {
    return "ClienteUpdateRequest[identificacion=" + identificacion + "]";
  }
}
