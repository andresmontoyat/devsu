@ignore
Feature: un intento de abrir cuenta, sin aserciones sobre el resultado

  Scenario:
    Given url cuentaUrl
    And path 'cuentas'
    And request { numeroCuenta: '#(numeroCuenta)', tipoCuenta: 'Ahorros', saldoInicial: 0, clienteId: '#(clienteId)' }
    When method post
