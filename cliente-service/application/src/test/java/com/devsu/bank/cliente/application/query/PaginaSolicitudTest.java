package com.devsu.bank.cliente.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PaginaSolicitudTest {

  @Test
  void should_compute_the_offset_from_page_and_size() {
    assertThat(new PaginaSolicitud(3, 20).offset()).isEqualTo(60);
  }

  @Test
  void should_reject_a_negative_page() {
    assertThatThrownBy(() -> new PaginaSolicitud(-1, 20))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("page");
  }

  @ParameterizedTest
  @ValueSource(ints = {0, PaginaSolicitud.TAMANO_MAX + 1})
  void should_reject_a_size_out_of_range(int tamano) {
    assertThatThrownBy(() -> new PaginaSolicitud(0, tamano))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("size");
  }

  @Test
  void should_report_zero_pages_when_there_are_no_elements() {
    Pagina<String> vacia = new Pagina<>(List.of(), new PaginaSolicitud(0, 20), 0);

    assertThat(vacia.totalPaginas()).isZero();
  }
}
