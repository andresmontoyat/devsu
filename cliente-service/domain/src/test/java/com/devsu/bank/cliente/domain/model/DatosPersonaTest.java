package com.devsu.bank.cliente.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cliente.domain.exception.DatoInvalidoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DatosPersonaTest {

  private static final Identificacion IDENTIFICACION = new Identificacion("1712345678");
  private static final Telefono TELEFONO = new Telefono("098254785");
  private static final String NOMBRE = "Jose Lema";
  private static final String DIRECCION = "Otavalo sn y principal";
  private static final int EDAD = 35;

  @Test
  void should_trim_name_and_address_when_created() {
    DatosPersona datos =
        new DatosPersona(
            "  Jose Lema ", Genero.MASCULINO, EDAD, IDENTIFICACION, " Otavalo ", TELEFONO);

    assertThat(datos.nombre()).isEqualTo("Jose Lema");
    assertThat(datos.direccion()).isEqualTo("Otavalo");
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void should_reject_when_name_is_blank(String nombre) {
    assertThatThrownBy(
            () ->
                new DatosPersona(
                    nombre, Genero.MASCULINO, EDAD, IDENTIFICACION, DIRECCION, TELEFONO))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("nombre");
  }

  @Test
  void should_reject_when_name_exceeds_the_maximum_length() {
    String nombreLargo = "x".repeat(DatosPersona.NOMBRE_MAX + 1);

    assertThatThrownBy(
            () ->
                new DatosPersona(
                    nombreLargo, Genero.MASCULINO, EDAD, IDENTIFICACION, DIRECCION, TELEFONO))
        .isInstanceOf(DatoInvalidoException.class);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void should_reject_when_address_is_blank(String direccion) {
    assertThatThrownBy(
            () ->
                new DatosPersona(
                    NOMBRE, Genero.MASCULINO, EDAD, IDENTIFICACION, direccion, TELEFONO))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("direccion");
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, DatosPersona.EDAD_MAX + 1})
  void should_reject_when_age_is_out_of_range(int edad) {
    assertThatThrownBy(
            () ->
                new DatosPersona(
                    NOMBRE, Genero.MASCULINO, edad, IDENTIFICACION, DIRECCION, TELEFONO))
        .isInstanceOf(DatoInvalidoException.class)
        .hasMessageContaining("edad");
  }

  @Test
  void should_reject_when_gender_identification_or_phone_is_missing() {
    assertThatThrownBy(
            () -> new DatosPersona(NOMBRE, null, EDAD, IDENTIFICACION, DIRECCION, TELEFONO))
        .isInstanceOf(DatoInvalidoException.class);
    assertThatThrownBy(
            () -> new DatosPersona(NOMBRE, Genero.FEMENINO, EDAD, null, DIRECCION, TELEFONO))
        .isInstanceOf(DatoInvalidoException.class);
    assertThatThrownBy(
            () -> new DatosPersona(NOMBRE, Genero.FEMENINO, EDAD, IDENTIFICACION, DIRECCION, null))
        .isInstanceOf(DatoInvalidoException.class);
  }
}
