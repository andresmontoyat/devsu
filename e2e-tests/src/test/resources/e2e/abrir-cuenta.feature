@ignore
Feature: abre una cuenta en cuenta-service cuando ya conoce al cliente

  Scenario:
    # The cliente reaches cuenta-service through Kafka; until then the request is answered with 422.
    Given url cuentaUrl
    And path 'cuentas'
    And request { numeroCuenta: '#(numeroCuenta)', tipoCuenta: '#(tipoCuenta)', saldoInicial: '#(saldoInicial)', estado: true, clienteId: '#(clienteId)' }
    And retry until responseStatus == 201
    When method post
    Then status 201
    And match response contains { numeroCuenta: '#(numeroCuenta)', estado: true, clienteId: '#(clienteId)' }
