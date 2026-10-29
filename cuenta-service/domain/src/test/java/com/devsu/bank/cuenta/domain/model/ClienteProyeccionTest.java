package com.devsu.bank.cuenta.domain.model;

import static com.devsu.bank.cuenta.domain.model.CuentaFixtures.JOSE_LEMA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.domain.exception.ClienteNoDisponibleException;
import org.junit.jupiter.api.Test;

class ClienteProyeccionTest {

  private final ClienteProyeccion v1 = ClienteProyeccion.desde(JOSE_LEMA, "Jose Lema", true, 1);

  @Test
  void should_apply_a_newer_version() {
    ClienteProyeccion v2 = v1.actualizar("Jose A. Lema", false, 2);

    assertThat(v2.nombre()).isEqualTo("Jose A. Lema");
    assertThat(v2.activo()).isFalse();
    assertThat(v2.version()).isEqualTo(2);
  }

  @Test
  void should_ignore_a_stale_or_replayed_version() {
    assertThat(v1.actualizar("Viejo", false, 1)).isSameAs(v1);
    assertThat(v1.actualizar("Viejo", false, 0)).isSameAs(v1);
  }

  @Test
  void should_become_a_tombstone_that_later_updates_cannot_revive() {
    ClienteProyeccion eliminado = v1.eliminar(3);

    assertThat(eliminado.eliminado()).isTrue();
    assertThat(eliminado.actualizar("Revivido", true, 2)).isSameAs(eliminado);
  }

  @Test
  void should_ignore_a_stale_deletion() {
    ClienteProyeccion v5 = v1.actualizar("Jose", true, 5);

    assertThat(v5.eliminar(4)).isSameAs(v5);
  }

  @Test
  void should_only_let_active_existing_clients_operate() {
    v1.exigirDisponible();

    assertThatThrownBy(() -> v1.actualizar("Jose", false, 2).exigirDisponible())
        .isInstanceOf(ClienteNoDisponibleException.class);
    assertThatThrownBy(() -> v1.eliminar(2).exigirDisponible())
        .isInstanceOf(ClienteNoDisponibleException.class);
  }
}
