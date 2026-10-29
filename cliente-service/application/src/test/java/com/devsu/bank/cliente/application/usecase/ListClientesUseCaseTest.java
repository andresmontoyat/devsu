package com.devsu.bank.cliente.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.application.support.ClienteTestData;
import com.devsu.bank.cliente.application.support.InMemoryClienteRepository;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.DatosPersona;
import com.devsu.bank.cliente.domain.model.Identificacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListClientesUseCaseTest {

  private static final int TAMANO = 2;

  private InMemoryClienteRepository repository;
  private ListClientesUseCase useCase;

  @BeforeEach
  void setUp() {
    repository = new InMemoryClienteRepository();
    useCase = new ListClientesUseCase(repository);
    repository.guardarExistente(cliente("1000000001"));
    repository.guardarExistente(cliente("1000000002"));
    repository.guardarExistente(cliente("1000000003"));
  }

  @Test
  void should_return_the_requested_page_with_totals() {
    Pagina<Cliente> pagina = useCase.execute(new PaginaSolicitud(1, TAMANO));

    assertThat(pagina.contenido()).hasSize(1);
    assertThat(pagina.totalElementos()).isEqualTo(3);
    assertThat(pagina.totalPaginas()).isEqualTo(2);
    assertThat(pagina.solicitud().pagina()).isEqualTo(1);
  }

  private static Cliente cliente(String identificacion) {
    DatosPersona base = ClienteTestData.datosJoseLema();
    DatosPersona datos =
        new DatosPersona(
            base.nombre(),
            base.genero(),
            base.edad(),
            new Identificacion(identificacion),
            base.direccion(),
            base.telefono());
    return Cliente.registrar(
        ClienteId.generar(), datos, ClienteTestData.joseLemaPersistido().getContrasena());
  }
}
