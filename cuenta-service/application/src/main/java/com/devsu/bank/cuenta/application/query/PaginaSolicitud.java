package com.devsu.bank.cuenta.application.query;

import com.devsu.bank.cuenta.domain.exception.DatoInvalidoException;

/** Zero-based page request; the size is capped so a single call cannot scan the table. */
public record PaginaSolicitud(int pagina, int tamano) {

  public static final int TAMANO_MAX = 100;

  public PaginaSolicitud {
    if (pagina < 0) {
      throw new DatoInvalidoException("page", "debe ser mayor o igual a 0");
    }
    if (tamano < 1 || tamano > TAMANO_MAX) {
      throw new DatoInvalidoException("size", "debe estar entre 1 y " + TAMANO_MAX);
    }
  }

  public int offset() {
    return pagina * tamano;
  }
}
