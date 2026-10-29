Feature: /cuentas — CRU de cuentas (F1)

  Background:
    * url baseUrl
    * def joseLema = '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e01'
    * def sufijo = '' + Java.type('java.lang.System').nanoTime()
    * def numero = '7' + sufijo.substring(sufijo.length - 9)
    * def nuevaCuenta = { numeroCuenta: '#(numero)', tipoCuenta: 'Corriente', saldoInicial: 1000, estado: true, clienteId: '#(joseLema)' }

  Scenario: crear una cuenta corriente para Jose Lema (caso de uso 3)
    Given path 'cuentas'
    And request nuevaCuenta
    When method post
    Then status 201
    And match header Location contains '/api/cuentas/' + numero
    And match response == { numeroCuenta: '#(numero)', tipoCuenta: 'CORRIENTE', saldoInicial: 1000.00, saldoDisponible: 1000.00, estado: true, clienteId: '#(joseLema)' }

  Scenario: consultar, listar por cliente, reemplazar y desactivar
    Given path 'cuentas'
    And request nuevaCuenta
    When method post
    Then status 201

    Given path 'cuentas', numero
    When method get
    Then status 200
    And match response.saldoDisponible == 1000.00

    Given path 'cuentas'
    And param cliente = joseLema
    And param size = 100
    When method get
    Then status 200
    And match response.items[*].numeroCuenta contains numero

    Given path 'cuentas', numero
    And request { tipoCuenta: 'Ahorros', saldoInicial: 1500, estado: true, clienteId: '#(joseLema)' }
    When method put
    Then status 200
    And match response contains { tipoCuenta: 'AHORROS', saldoDisponible: 1500.00 }

    Given path 'cuentas', numero
    And header Content-Type = 'application/merge-patch+json'
    And request { estado: false }
    When method patch
    Then status 200
    And match response.estado == false

  Scenario: un numero de cuenta repetido se rechaza con 409
    Given path 'cuentas'
    And request nuevaCuenta
    When method post
    Then status 201

    Given path 'cuentas'
    And request nuevaCuenta
    When method post
    Then status 409
    And match response.code == 'NUMERO_CUENTA_DUPLICADO'

  Scenario: no se abren cuentas para un cliente desconocido
    * set nuevaCuenta.clienteId = '11111111-1111-1111-1111-111111111111'
    Given path 'cuentas'
    And request nuevaCuenta
    When method post
    Then status 422
    And match response.code == 'CLIENTE_NO_DISPONIBLE'

  Scenario: las cuentas no se eliminan
    Given path 'cuentas', '478758'
    When method delete
    Then status 405
