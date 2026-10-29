Feature: /reportes — estado de cuenta por rango de fechas y cliente (F4)

  Background:
    * url baseUrl
    * def juan = '0b6f5c9e-1d2a-4c3b-8e7f-1a2b3c4d5e03'
    * def hoy = '' + Java.type('java.time.LocalDate').now(Java.type('java.time.ZoneId').of('America/Guayaquil'))
    * def sufijo = '' + Java.type('java.lang.System').nanoTime()
    * def numero = '9' + sufijo.substring(sufijo.length - 9)

  Scenario: el reporte trae las cuentas con saldos y el detalle con el formato del enunciado
    Given path 'cuentas'
    And request { numeroCuenta: '#(numero)', tipoCuenta: 'Ahorros', saldoInicial: 0, clienteId: '#(juan)' }
    When method post
    Then status 201
    Given path 'movimientos'
    And request { numeroCuenta: '#(numero)', valor: 150 }
    When method post
    Then status 201

    Given path 'reportes'
    And param fechaInicio = hoy
    And param fechaFin = hoy
    And param cliente = juan
    When method get
    Then status 200
    And match response.cliente == { clienteId: '#(juan)', nombre: 'Juan Osorio' }
    And match response.cuentas[*].numeroCuenta contains numero
    * def fila = karate.filter(response.movimientos, function(m){ return m['Numero Cuenta'] == numero })[0]
    And match fila ==
      """
      {
        "Fecha": "#string",
        "Cliente": "Juan Osorio",
        "Numero Cuenta": "#(numero)",
        "Tipo": "Ahorros",
        "Saldo Inicial": 0.00,
        "Estado": true,
        "Movimiento": 150.00,
        "Saldo Disponible": 150.00
      }
      """

  Scenario: un rango invertido se rechaza
    Given path 'reportes'
    And param fechaInicio = '2022-02-28'
    And param fechaFin = '2022-02-01'
    And param cliente = juan
    When method get
    Then status 400
    And match response.code == 'RANGO_FECHAS_INVALIDO'

  Scenario: un cliente desconocido devuelve 404
    Given path 'reportes'
    And param fechaInicio = hoy
    And param fechaFin = hoy
    And param cliente = '11111111-1111-1111-1111-111111111111'
    When method get
    Then status 404
    And match response.code == 'CLIENTE_NO_ENCONTRADO'
