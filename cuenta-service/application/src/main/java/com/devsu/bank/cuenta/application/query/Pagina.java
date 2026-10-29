package com.devsu.bank.cuenta.application.query;

import java.util.List;

public record Pagina<T>(List<T> contenido, PaginaSolicitud solicitud, long totalElementos) {

  public Pagina {
    contenido = List.copyOf(contenido);
  }

  public long totalPaginas() {
    return (totalElementos + solicitud.tamano() - 1) / solicitud.tamano();
  }
}
