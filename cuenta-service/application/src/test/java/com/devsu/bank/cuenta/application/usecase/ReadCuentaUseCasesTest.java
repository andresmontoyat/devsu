package com.devsu.bank.cuenta.application.usecase;

import static com.devsu.bank.cuenta.application.support.CuentaHarness.MARIANELA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.devsu.bank.cuenta.application.query.CuentasQuery;
import com.devsu.bank.cuenta.application.query.Pagina;
import com.devsu.bank.cuenta.application.query.PaginaSolicitud;
import com.devsu.bank.cuenta.application.support.CuentaHarness;
import com.devsu.bank.cuenta.domain.exception.CuentaNoEncontradaException;
import com.devsu.bank.cuenta.domain.model.Cuenta;
import com.devsu.bank.cuenta.domain.model.NumeroCuenta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReadCuentaUseCasesTest {

  private static final PaginaSolicitud PAGINA = new PaginaSolicitud(0, 10);

  private CuentaHarness harness;

  @BeforeEach
  void setUp() {
    harness = new CuentaHarness().conCuentasDelEnunciado();
  }

  @Test
  void should_return_an_existing_account() {
    Cuenta cuenta = new GetCuentaUseCase(harness.cuentas).execute(new NumeroCuenta("478758"));

    assertThat(cuenta.getClienteId()).isEqualTo(CuentaHarness.JOSE_LEMA);
  }

  @Test
  void should_fail_with_not_found_for_an_unknown_account() {
    GetCuentaUseCase useCase = new GetCuentaUseCase(harness.cuentas);

    assertThatThrownBy(() -> useCase.execute(new NumeroCuenta("999999")))
        .isInstanceOf(CuentaNoEncontradaException.class);
  }

  @Test
  void should_list_only_the_accounts_of_the_requested_client() {
    Pagina<Cuenta> pagina =
        new ListCuentasUseCase(harness.cuentas).execute(new CuentasQuery(MARIANELA, PAGINA));

    assertThat(pagina.contenido())
        .extracting(c -> c.getNumeroCuenta().value())
        .containsExactly("225487", "496825");
  }

  @Test
  void should_list_every_account_without_filter() {
    Pagina<Cuenta> pagina =
        new ListCuentasUseCase(harness.cuentas).execute(new CuentasQuery(null, PAGINA));

    assertThat(pagina.totalElementos()).isEqualTo(4);
  }
}
