@e2e
Feature: casos de uso del enunciado contra el stack de docker compose

  Background:
    * def zona = Java.type('java.time.ZoneId').of('America/Guayaquil')
    * def hoy = Java.type('java.time.LocalDate').now(zona)
    * def fechaReporte = hoy.format(Java.type('java.time.format.DateTimeFormatter').ofPattern('d/M/yyyy'))
    * def sufijo = '' + Java.type('java.lang.System').nanoTime()
    # Fresh account numbers per run, so the suite can be repeated against the same stack.
    * def cuenta = function(n){ return '6' + sufijo.substring(sufijo.length - 8) + n }

  Scenario: casos 1 a 5 — clientes, cuentas, movimientos, saldo no disponible y estado de cuenta
    # 1. Creacion de usuarios
    * def jose = call read('crear-cliente.feature') { nombre: 'Jose Lema', genero: 'MASCULINO', edad: 35, direccion: 'Otavalo sn y principal', telefono: '098254785', contrasena: '1234' }
    * def marianela = call read('crear-cliente.feature') { nombre: 'Marianela Montalvo', genero: 'FEMENINO', edad: 30, direccion: 'Amazonas y NNUU', telefono: '097548965', contrasena: '5678' }
    * def juan = call read('crear-cliente.feature') { nombre: 'Juan Osorio', genero: 'MASCULINO', edad: 40, direccion: '13 junio y Equinoccial', telefono: '098874587', contrasena: '1245' }

    # 2. Creacion de cuentas de usuario (espera a que cuenta-service reciba cada cliente por Kafka)
    * call read('abrir-cuenta.feature') { numeroCuenta: '#(cuenta(1))', tipoCuenta: 'Ahorros', saldoInicial: 2000, clienteId: '#(jose.clienteId)' }
    * call read('abrir-cuenta.feature') { numeroCuenta: '#(cuenta(2))', tipoCuenta: 'Corriente', saldoInicial: 100, clienteId: '#(marianela.clienteId)' }
    * call read('abrir-cuenta.feature') { numeroCuenta: '#(cuenta(3))', tipoCuenta: 'Ahorros', saldoInicial: 0, clienteId: '#(juan.clienteId)' }
    * call read('abrir-cuenta.feature') { numeroCuenta: '#(cuenta(4))', tipoCuenta: 'Ahorros', saldoInicial: 540, clienteId: '#(marianela.clienteId)' }

    # 3. Nueva cuenta corriente para Jose Lema
    * call read('abrir-cuenta.feature') { numeroCuenta: '#(cuenta(5))', tipoCuenta: 'Corriente', saldoInicial: 1000, clienteId: '#(jose.clienteId)' }

    # 4. Movimientos (F2): actualizan el saldo y quedan registrados
    * call read('registrar-movimiento.feature') { numeroCuenta: '#(cuenta(1))', valor: -575, tipoMovimiento: 'RETIRO', saldoInicial: 2000, saldoDisponible: 1425 }
    * call read('registrar-movimiento.feature') { numeroCuenta: '#(cuenta(2))', valor: 600, tipoMovimiento: 'DEPOSITO', saldoInicial: 100, saldoDisponible: 700 }
    * call read('registrar-movimiento.feature') { numeroCuenta: '#(cuenta(3))', valor: 150, tipoMovimiento: 'DEPOSITO', saldoInicial: 0, saldoDisponible: 150 }
    * call read('registrar-movimiento.feature') { numeroCuenta: '#(cuenta(4))', valor: -540, tipoMovimiento: 'RETIRO', saldoInicial: 540, saldoDisponible: 0 }

    # F3: un retiro sin saldo se rechaza con "Saldo no disponible" y no toca el saldo
    Given url cuentaUrl
    And path 'movimientos'
    And request { numeroCuenta: '#(cuenta(4))', valor: -1 }
    When method post
    Then status 422
    And match response contains { code: 'SALDO_NO_DISPONIBLE', detail: 'Saldo no disponible' }
    Given path 'cuentas', cuenta(4)
    When method get
    Then status 200
    And match response.saldoDisponible == 0

    # 5. Listado de movimientos por fechas y usuario (F4)
    Given path 'reportes'
    And param fechaInicio = hoy
    And param fechaFin = hoy
    And param cliente = marianela.clienteId
    When method get
    Then status 200
    And match response.cliente == { clienteId: '#(marianela.clienteId)', nombre: 'Marianela Montalvo' }
    And match response.cuentas[*].numeroCuenta contains only [ '#(cuenta(2))', '#(cuenta(4))' ]
    And match response.movimientos contains only
      """
      [
        { "Fecha": "#(fechaReporte)", "Cliente": "Marianela Montalvo", "Numero Cuenta": "#(cuenta(2))", "Tipo": "Corriente", "Saldo Inicial": 100, "Estado": true, "Movimiento": 600, "Saldo Disponible": 700 },
        { "Fecha": "#(fechaReporte)", "Cliente": "Marianela Montalvo", "Numero Cuenta": "#(cuenta(4))", "Tipo": "Ahorros", "Saldo Inicial": 540, "Estado": true, "Movimiento": -540, "Saldo Disponible": 0 }
      ]
      """

  Scenario: los cambios del cliente llegan a cuenta-service de forma asincrona
    * def cliente = call read('crear-cliente.feature') { nombre: 'Ana Torres', genero: 'FEMENINO', edad: 28, direccion: 'Av. Shyris', telefono: '0991234567', contrasena: '9876' }
    * call read('abrir-cuenta.feature') { numeroCuenta: '#(cuenta(6))', tipoCuenta: 'Ahorros', saldoInicial: 50, clienteId: '#(cliente.clienteId)' }

    # PATCH en cliente-service: el nuevo nombre aparece en el estado de cuenta
    Given url clienteUrl
    And path 'clientes', cliente.clienteId
    And header Content-Type = 'application/merge-patch+json'
    And request { nombre: 'Ana Torres Vega' }
    When method patch
    Then status 200
    Given url cuentaUrl
    And path 'reportes'
    And param fechaInicio = hoy
    And param fechaFin = hoy
    And param cliente = cliente.clienteId
    And retry until responseStatus == 200 && response.cliente.nombre == 'Ana Torres Vega'
    When method get
    Then status 200

    # DELETE en cliente-service: cuenta-service deja de abrirle cuentas
    Given url clienteUrl
    And path 'clientes', cliente.clienteId
    When method delete
    Then status 204
    Given path 'clientes', cliente.clienteId
    When method get
    Then status 404
    # Each attempt uses a new number: one opened before the event arrives must not mask the 422.
    * def abrir = function(i){ return karate.call('intentar-abrir-cuenta.feature', { numeroCuenta: cuenta(7) + i, clienteId: cliente.clienteId }) }
    * def rechazo = karate.repeat(30, function(i){ var r = abrir(i); if (r.responseStatus == 422) return r.response; Java.type('java.lang.Thread').sleep(1000); return null })
    * def rechazos = karate.filter(rechazo, function(r){ return r != null })
    And match rechazos[0].code == 'CLIENTE_NO_DISPONIBLE'
