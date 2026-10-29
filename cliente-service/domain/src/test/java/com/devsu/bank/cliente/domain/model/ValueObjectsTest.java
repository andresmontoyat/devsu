package com.devsu.bank.cliente.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ValueObjectsTest {

  @Test
  void should_trim_identification_when_created() {
    assertThat(new Identificacion(" 1712345678 ").value()).isEqualTo("1712345678");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"1234", "17 1234 5678", "123456789012345678901", "17*2345678"})
  void should_reject_identification_when_format_is_invalid(String value) {
    assertThatThrownBy(() -> new Identificacion(value))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("identificacion");
  }

  @ParameterizedTest
  @ValueSource(strings = {"098254785", "+593982547850", "2234567"})
  void should_accept_phone_when_it_has_seven_to_fifteen_digits(String value) {
    assertThat(new Telefono(value).value()).isEqualTo(value);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"123456", "09825-4785", "abcdefghij", "1234567890123456"})
  void should_reject_phone_when_format_is_invalid(String value) {
    assertThatThrownBy(() -> new Telefono(value))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("telefono");
  }

  @Test
  void should_wrap_the_given_uuid_when_creating_a_client_id_from_text() {
    String texto = "7f1d3c9a-2b4e-4f60-9a1b-3c5d7e9f1a2b";

    assertThat(ClienteId.of(texto).value()).hasToString(texto);
  }

  @Test
  void should_generate_distinct_client_ids() {
    assertThat(ClienteId.generar()).isNotEqualTo(ClienteId.generar());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"no-es-un-uuid"})
  void should_reject_client_id_when_text_is_not_a_uuid(String texto) {
    assertThatThrownBy(() -> ClienteId.of(texto)).isInstanceOf(DatoInvalidoException.class);
  }

  @Test
  void should_reject_client_id_when_uuid_is_missing() {
    assertThatThrownBy(() -> new ClienteId(null)).isInstanceOf(DatoInvalidoException.class);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void should_reject_password_hash_when_blank(String value) {
    assertThatThrownBy(() -> new ContrasenaHash(value)).isInstanceOf(DatoInvalidoException.class);
  }

  @Test
  void should_mask_password_hash_when_printed() {
    assertThat(new ContrasenaHash("$2a$10$secreto").toString()).doesNotContain("secreto");
  }

  @ParameterizedTest
  @ValueSource(strings = {"1234", "una-contrasena-larga"})
  void should_accept_plain_password_within_policy(String value) {
    assertThat(new ContrasenaPlana(value).value()).isEqualTo(value);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"123", "    "})
  void should_reject_plain_password_when_shorter_than_policy(String value) {
    assertThatThrownBy(() -> new ContrasenaPlana(value))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("contrasena");
  }

  @Test
  void should_reject_plain_password_when_longer_than_policy() {
    String larga = "x".repeat(ContrasenaPlana.LONGITUD_MAX + 1);

    assertThatThrownBy(() -> new ContrasenaPlana(larga)).isInstanceOf(DatoInvalidoException.class);
  }

  @Test
  void should_mask_plain_password_when_printed() {
    assertThat(new ContrasenaPlana("1234").toString()).doesNotContain("1234");
  }

  @Test
  void should_resolve_gender_ignoring_case() {
    assertThat(Genero.desde("femenino")).isEqualTo(Genero.FEMENINO);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"X"})
  void should_reject_unknown_gender(String value) {
    assertThatThrownBy(() -> Genero.desde(value))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("genero");
  }
}
