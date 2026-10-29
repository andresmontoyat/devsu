Feature: /movimientos — registro de movimientos (F1, F2, F3)

  Background:
    * url baseUrl
    * def marianela = '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e02'
    * def sufijo = '' + Java.type('java.lang.System').nanoTime()
    * def numero = '8' + sufijo.substring(sufijo.length - 9)
    Given path 'cuentas'
    And request { numeroCuenta: '#(numero)', tipoCuenta: 'Corriente', saldoInicial: 100, clienteId: '#(marianela)' }
    When method post
    Then status 201

  Scenario: un deposito suma al saldo y queda registrado con ambos saldos (F2)
    Given path 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: 600 }
    When method post
    Then status 201
    And match response contains { numeroCuenta: '#(numero)', tipoMovimiento: 'DEPOSITO', valor: 600.00, saldoInicial: 100.00, saldoDisponible: 700.00 }
    And match response.movimientoId == '#uuid'

    Given path 'cuentas', numero
    When method get
    Then status 200
    And match response.saldoDisponible == 700.00

  Scenario: un retiro sin saldo devuelve "Saldo no disponible" (F3)
    Given path 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: -100.01 }
    When method post
    Then status 422
    And match response.code == 'SALDO_NO_DISPONIBLE'
    And match response.detail == 'Saldo no disponible'

    Given path 'cuentas', numero
    When method get
    Then match response.saldoDisponible == 100.00

  Scenario: retirar todo el saldo deja la cuenta en cero
    Given path 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: -100, tipoMovimiento: 'Retiro' }
    When method post
    Then status 201
    And match response.saldoDisponible == 0.00

  Scenario: un tipo que contradice el signo se rechaza
    Given path 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: 50, tipoMovimiento: 'Retiro' }
    When method post
    Then status 400
    And match response.code == 'DATO_INVALIDO'

  Scenario: un reintento con la misma Idempotency-Key no duplica el movimiento
    * def clave = 'karate-' + numero
    Given path 'movimientos'
    And header Idempotency-Key = clave
    And request { numeroCuenta: '#(numero)', valor: -40 }
    When method post
    Then status 201
    * def original = response.movimientoId

    Given path 'movimientos'
    And header Idempotency-Key = clave
    And request { numeroCuenta: '#(numero)', valor: -40 }
    When method post
    Then status 201
    And match response.movimientoId == original

    Given path 'movimientos'
    And header Idempotency-Key = clave
    And request { numeroCuenta: '#(numero)', valor: -10 }
    When method post
    Then status 422
    And match response.code == 'IDEMPOTENCY_KEY_REUTILIZADA'

  Scenario: solo el ultimo movimiento se puede corregir y ninguno eliminar
    Given path 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: 10 }
    When method post
    * def primero = response.movimientoId
    Given path 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: 20 }
    When method post
    * def ultimo = response.movimientoId

    Given path 'movimientos', primero
    And request { valor: 5 }
    When method put
    Then status 409
    And match response.code == 'MOVIMIENTO_NO_MODIFICABLE'

    Given path 'movimientos', ultimo
    And request { valor: -30 }
    When method put
    Then status 200
    And match response contains { tipoMovimiento: 'RETIRO', saldoInicial: 110.00, saldoDisponible: 80.00 }

    Given path 'movimientos', ultimo
    When method delete
    Then status 405

  Scenario: listar los movimientos de la cuenta
    Given path 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: 25 }
    When method post
    Then status 201

    Given path 'movimientos'
    And param cuenta = numero
    When method get
    Then status 200
    And match response.items == '#[1]'
    And match response.page.totalElements == 1
