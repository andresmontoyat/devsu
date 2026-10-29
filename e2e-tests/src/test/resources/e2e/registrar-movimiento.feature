@ignore
Feature: registra un movimiento y comprueba el saldo resultante

  Scenario:
    Given url cuentaUrl
    And path 'movimientos'
    And request { numeroCuenta: '#(numeroCuenta)', valor: '#(valor)' }
    When method post
    Then status 201
    And match response contains { numeroCuenta: '#(numeroCuenta)', tipoMovimiento: '#(tipoMovimiento)', valor: '#(valor)', saldoInicial: '#(saldoInicial)', saldoDisponible: '#(saldoDisponible)' }
