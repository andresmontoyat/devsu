package com.devsu.bank.cliente.application.port.inbound;

import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.domain.model.Cliente;

@FunctionalInterface
public interface ListClientesPort {

  Pagina<Cliente> execute(PaginaSolicitud solicitud);
}
