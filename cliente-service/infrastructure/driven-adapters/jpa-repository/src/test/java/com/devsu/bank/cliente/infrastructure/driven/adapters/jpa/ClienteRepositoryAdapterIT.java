package com.devsu.bank.cliente.infrastructure.driven.adapters.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.application.query.Pagina;
import com.devsu.bank.cliente.application.query.PaginaSolicitud;
import com.devsu.bank.cliente.domain.exception.ClienteModificadoConcurrentementeException;
import com.devsu.bank.cliente.domain.exception.IdentificacionDuplicadaException;
import com.devsu.bank.cliente.domain.model.Cliente;
import com.devsu.bank.cliente.domain.model.ClienteId;
import com.devsu.bank.cliente.domain.model.ContrasenaHash;
import com.devsu.bank.cliente.domain.model.DatosPersona;
import com.devsu.bank.cliente.domain.model.Genero;
import com.devsu.bank.cliente.domain.model.Identificacion;
import com.devsu.bank.cliente.domain.model.Telefono;
import com.devsu.bank.cliente.infrastructure.driven.adapters.jpa.adapter.ClienteRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ClienteRepositoryAdapterIT extends AbstractJpaIT {

  private static final ContrasenaHash HASH = new ContrasenaHash("$2a$10$hashDeLaboratorio");

  @Autowired private ClienteRepositoryAdapter adapter;

  @Test
  void should_store_persona_and_cliente_rows_with_version_zero_when_new() {
    Cliente guardado = enTransaccion(() -> adapter.save(cliente("1712345678")));

    assertThat(guardado.getVersion()).isZero();
    assertThat(contar("persona")).isEqualTo(1);
    assertThat(contar("cliente")).isEqualTo(1);
    assertThat(adapter.findById(guardado.getClienteId())).contains(guardado);
  }

  @Test
  void should_map_every_attribute_back_to_the_domain() {
    Cliente original = cliente("1712345678");
    enTransaccion(() -> adapter.save(original));

    Cliente leido = adapter.findById(original.getClienteId()).orElseThrow();

    assertThat(leido.getDatos()).isEqualTo(original.getDatos());
    assertThat(leido.getContrasena()).isEqualTo(HASH);
    assertThat(leido.isActivo()).isTrue();
  }

  @Test
  void should_increment_the_version_when_updated() {
    Cliente guardado = enTransaccion(() -> adapter.save(cliente("1712345678")));

    Cliente actualizado = enTransaccion(() -> adapter.save(guardado.desactivar()));

    assertThat(actualizado.getVersion()).isEqualTo(1);
    assertThat(actualizado.isActivo()).isFalse();
  }

  @Test
  void should_reject_an_update_based_on_a_stale_version() {
    Cliente guardado = enTransaccion(() -> adapter.save(cliente("1712345678")));
    enTransaccion(() -> adapter.save(guardado.desactivar()));

    assertThatThrownBy(() -> enTransaccion(() -> adapter.save(guardado.activar().desactivar())))
        .isInstanceOf(ClienteModificadoConcurrentementeException.class);
  }

  @Test
  void should_translate_the_unique_constraint_into_a_duplicate_identification() {
    enTransaccion(() -> adapter.save(cliente("1712345678")));

    assertThatThrownBy(() -> enTransaccion(() -> adapter.save(cliente("1712345678"))))
        .isInstanceOf(IdentificacionDuplicadaException.class);
  }

  @Test
  void should_tell_whether_an_identification_exists() {
    enTransaccion(() -> adapter.save(cliente("1712345678")));

    assertThat(adapter.existsByIdentificacion(new Identificacion("1712345678"))).isTrue();
    assertThat(adapter.existsByIdentificacion(new Identificacion("9999999999"))).isFalse();
  }

  @Test
  void should_page_through_clients_in_insertion_order() {
    enTransaccion(() -> adapter.save(cliente("1000000001")));
    enTransaccion(() -> adapter.save(cliente("1000000002")));
    enTransaccion(() -> adapter.save(cliente("1000000003")));

    Pagina<Cliente> pagina = adapter.findAll(new PaginaSolicitud(1, 2));

    assertThat(pagina.totalElementos()).isEqualTo(3);
    assertThat(pagina.contenido())
        .extracting(c -> c.getIdentificacion().value())
        .containsExactly("1000000003");
  }

  @Test
  void should_remove_both_rows_when_deleted() {
    Cliente guardado = enTransaccion(() -> adapter.save(cliente("1712345678")));

    enTransaccion(
        () -> {
          adapter.delete(guardado.getClienteId());
          return null;
        });

    assertThat(adapter.findById(guardado.getClienteId())).isEmpty();
    assertThat(contar("persona")).isZero();
  }

  private long contar(String tabla) {
    return jdbc.sql("SELECT COUNT(*) FROM " + tabla).query(Long.class).single();
  }

  private static Cliente cliente(String identificacion) {
    DatosPersona datos =
        new DatosPersona(
            "Jose Lema",
            Genero.MASCULINO,
            35,
            new Identificacion(identificacion),
            "Otavalo sn y principal",
            new Telefono("098254785"));
    return Cliente.registrar(ClienteId.generar(), datos, HASH);
  }
}
