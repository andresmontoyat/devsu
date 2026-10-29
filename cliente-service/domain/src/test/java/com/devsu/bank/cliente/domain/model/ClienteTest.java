package com.devsu.bank.cliente.domain.model;

import static com.devsu.bank.cliente.domain.model.ClienteFixtures.CLIENTE_ID;
import static com.devsu.bank.cliente.domain.model.ClienteFixtures.HASH;
import static com.devsu.bank.cliente.domain.model.ClienteFixtures.OTRO_HASH;
import static com.devsu.bank.cliente.domain.model.ClienteFixtures.clienteRegistrado;
import static com.devsu.bank.cliente.domain.model.ClienteFixtures.joseLema;
import static com.devsu.bank.cliente.domain.model.ClienteFixtures.joseLemaConDireccion;
import static com.devsu.bank.cliente.domain.model.ClienteFixtures.joseLemaConEdad;
import static com.devsu.bank.cliente.domain.model.ClienteFixtures.joseLemaConIdentificacion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.domain.exception.ClienteMenorDeEdadException;
import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import com.devsu.bank.cliente.domain.exception.IdentificacionInmutableException;
import org.junit.jupiter.api.Test;

class ClienteTest {

  private static final long VERSION_PERSISTIDA = 3L;

  @Test
  void should_be_a_persona_with_every_personal_attribute_when_registered() {
    Cliente cliente = clienteRegistrado();

    assertThat(cliente).isInstanceOf(Persona.class);
    assertThat(cliente.getNombre()).isEqualTo("Jose Lema");
    assertThat(cliente.getGenero()).isEqualTo(Genero.MASCULINO);
    assertThat(cliente.getEdad()).isEqualTo(ClienteFixtures.EDAD_ADULTO);
    assertThat(cliente.getIdentificacion()).isEqualTo(new Identificacion("1712345678"));
    assertThat(cliente.getDireccion()).isEqualTo("Otavalo sn y principal");
    assertThat(cliente.getTelefono()).isEqualTo(new Telefono("098254785"));
  }

  @Test
  void should_start_active_with_its_password_and_no_version_when_registered() {
    Cliente cliente = clienteRegistrado();

    assertThat(cliente.getClienteId()).isEqualTo(CLIENTE_ID);
    assertThat(cliente.getContrasena()).isEqualTo(HASH);
    assertThat(cliente.isActivo()).isTrue();
    assertThat(cliente.getVersion()).isZero();
  }

  @Test
  void should_reject_registration_when_the_person_is_under_age() {
    DatosPersona menor = joseLemaConEdad(Cliente.EDAD_MINIMA - 1);

    assertThatThrownBy(() -> Cliente.registrar(CLIENTE_ID, menor, HASH))
        .isInstanceOf(ClienteMenorDeEdadException.class);
  }

  @Test
  void should_accept_registration_when_the_person_is_exactly_of_age() {
    Cliente cliente = Cliente.registrar(CLIENTE_ID, joseLemaConEdad(Cliente.EDAD_MINIMA), HASH);

    assertThat(cliente.getEdad()).isEqualTo(Cliente.EDAD_MINIMA);
  }

  @Test
  void should_reject_registration_when_a_mandatory_part_is_missing() {
    assertThatThrownBy(() -> Cliente.registrar(null, joseLema(), HASH))
        .isInstanceOf(DatoInvalidoException.class);
    assertThatThrownBy(() -> Cliente.registrar(CLIENTE_ID, null, HASH))
        .isInstanceOf(DatoInvalidoException.class);
    assertThatThrownBy(() -> Cliente.registrar(CLIENTE_ID, joseLema(), null))
        .isInstanceOf(DatoInvalidoException.class);
  }

  @Test
  void should_return_a_new_instance_with_new_data_when_updating_personal_data() {
    Cliente original = clienteRegistrado();

    Cliente actualizado = original.actualizarDatos(joseLemaConDireccion("Amazonas y NNUU"));

    assertThat(actualizado).isNotSameAs(original);
    assertThat(actualizado.getDireccion()).isEqualTo("Amazonas y NNUU");
    assertThat(original.getDireccion()).isEqualTo("Otavalo sn y principal");
    assertThat(actualizado.getClienteId()).isEqualTo(original.getClienteId());
  }

  @Test
  void should_reject_the_update_when_it_changes_the_identification() {
    Cliente cliente = clienteRegistrado();

    assertThatThrownBy(() -> cliente.actualizarDatos(joseLemaConIdentificacion("0999999999")))
        .isInstanceOf(IdentificacionInmutableException.class);
  }

  @Test
  void should_reject_the_update_when_it_leaves_the_client_under_age() {
    Cliente cliente = clienteRegistrado();

    assertThatThrownBy(() -> cliente.actualizarDatos(joseLemaConEdad(Cliente.EDAD_MINIMA - 1)))
        .isInstanceOf(ClienteMenorDeEdadException.class);
  }

  @Test
  void should_replace_the_password_hash_without_touching_the_original_when_changing_password() {
    Cliente original = clienteRegistrado();

    Cliente actualizado = original.cambiarContrasena(OTRO_HASH);

    assertThat(actualizado.getContrasena()).isEqualTo(OTRO_HASH);
    assertThat(original.getContrasena()).isEqualTo(HASH);
  }

  @Test
  void should_be_inactive_when_deactivated() {
    Cliente inactivo = clienteRegistrado().desactivar();

    assertThat(inactivo.isActivo()).isFalse();
  }

  @Test
  void should_be_active_again_when_reactivated() {
    Cliente reactivado = clienteRegistrado().desactivar().activar();

    assertThat(reactivado.isActivo()).isTrue();
  }

  @Test
  void should_return_the_same_instance_when_the_state_does_not_change() {
    Cliente activo = clienteRegistrado();

    assertThat(activo.activar()).isSameAs(activo);
  }

  @Test
  void should_restore_every_attribute_when_rebuilt_from_storage() {
    Cliente cliente =
        Cliente.builder()
            .clienteId(CLIENTE_ID)
            .datos(joseLema())
            .contrasena(HASH)
            .activo(false)
            .version(VERSION_PERSISTIDA)
            .build();

    assertThat(cliente.isActivo()).isFalse();
    assertThat(cliente.getVersion()).isEqualTo(VERSION_PERSISTIDA);
    assertThat(cliente.getDatos()).isEqualTo(joseLema());
  }

  @Test
  void should_keep_the_version_when_its_state_changes() {
    Cliente persistido =
        Cliente.builder()
            .clienteId(CLIENTE_ID)
            .datos(joseLema())
            .contrasena(HASH)
            .activo(true)
            .version(VERSION_PERSISTIDA)
            .build();

    assertThat(persistido.desactivar().getVersion()).isEqualTo(VERSION_PERSISTIDA);
  }

  @Test
  void should_be_equal_by_client_id_regardless_of_state() {
    Cliente activo = clienteRegistrado();

    assertThat(activo.desactivar()).isEqualTo(activo).hasSameHashCodeAs(activo);
  }

  @Test
  void should_never_expose_the_password_hash_when_printed() {
    assertThat(clienteRegistrado().toString()).doesNotContain(HASH.value());
  }
}
