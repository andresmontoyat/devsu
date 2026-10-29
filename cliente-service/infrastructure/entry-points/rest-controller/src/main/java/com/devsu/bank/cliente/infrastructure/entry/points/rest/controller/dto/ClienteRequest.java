package com.devsu.bank.cliente.infrastructure.entry.points.rest.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** POST body. The password is write-only: it never appears in a response. */
public record ClienteRequest(
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank String genero,
    @NotNull @Min(0) @Max(150) Integer edad,
    @NotBlank @Size(max = 20) String identificacion,
    @NotBlank @Size(max = 200) String direccion,
    @NotBlank @Size(max = 16) String telefono,
    @NotBlank @Size(min = 4, max = 72) String contrasena,
    Boolean estado) {

  @Override
  public String toString() {
    return "ClienteRequest[identificacion=" + identificacion + "]";
  }
}
