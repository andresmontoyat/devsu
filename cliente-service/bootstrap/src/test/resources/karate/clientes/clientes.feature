Feature: /clientes — CRUD completo de clientes (F1)

  Background:
    * url baseUrl
    * def sufijo = '' + Java.type('java.lang.System').nanoTime()
    * def identificacion = 'K' + sufijo.substring(sufijo.length - 12)
    * def nuevoCliente =
      """
      {
        "nombre": "Jose Lema",
        "genero": "MASCULINO",
        "edad": 35,
        "identificacion": "#(identificacion)",
        "direccion": "Otavalo sn y principal",
        "telefono": "098254785",
        "contrasena": "1234",
        "estado": true
      }
      """
    * def problemDetails = { type: '#string', title: '#string', status: '#number', detail: '#string', instance: '#string', code: '#string', traceId: '##string' }

  Scenario: crear un cliente devuelve 201, Location y nunca la contrasena
    Given path 'clientes'
    And request nuevoCliente
    When method post
    Then status 201
    And match header Location contains '/api/v1/clientes/'
    And match response ==
      """
      {
        clienteId: '#uuid',
        nombre: 'Jose Lema',
        genero: 'MASCULINO',
        edad: 35,
        identificacion: '#(identificacion)',
        direccion: 'Otavalo sn y principal',
        telefono: '098254785',
        estado: true
      }
      """
    And match response.contrasena == '#notpresent'

  Scenario: ciclo de vida completo - consultar, listar, reemplazar, modificar y eliminar
    Given path 'clientes'
    And request nuevoCliente
    When method post
    Then status 201
    * def clienteId = response.clienteId

    Given path 'clientes', clienteId
    When method get
    Then status 200
    And match response.identificacion == identificacion

    Given path 'clientes'
    And param page = 0
    And param size = 5
    When method get
    Then status 200
    And match response.page == { number: 0, size: 5, totalElements: '#number', totalPages: '#number' }

    Given path 'clientes', clienteId
    And request { nombre: 'Jose Lema', genero: 'MASCULINO', edad: 36, identificacion: '#(identificacion)', direccion: 'Amazonas y NNUU', telefono: '098254785', estado: true }
    When method put
    Then status 200
    And match response contains { edad: 36, direccion: 'Amazonas y NNUU' }

    Given path 'clientes', clienteId
    And header Content-Type = 'application/merge-patch+json'
    And request { estado: false }
    When method patch
    Then status 200
    And match response contains { estado: false, edad: 36 }

    # Clients such as Postman send Accept: application/json even on a DELETE without body.
    Given path 'clientes', clienteId
    And header Accept = 'application/json'
    When method delete
    Then status 204

    Given path 'clientes', clienteId
    When method get
    Then status 404
    And match response == problemDetails
    And match response.code == 'CLIENTE_NO_ENCONTRADO'

  Scenario: una identificacion repetida se rechaza con 409
    Given path 'clientes'
    And request nuevoCliente
    When method post
    Then status 201

    Given path 'clientes'
    And request nuevoCliente
    When method post
    Then status 409
    And match response.code == 'IDENTIFICACION_DUPLICADA'

  Scenario: un cuerpo incompleto devuelve 400 con el detalle por campo
    Given path 'clientes'
    And request { nombre: 'Sin datos' }
    When method post
    Then status 400
    And match response.code == 'VALIDACION_FALLIDA'
    And match response.errors contains { field: 'identificacion', message: '#string' }

  Scenario: un menor de edad no puede ser cliente
    * set nuevoCliente.edad = 15
    Given path 'clientes'
    And request nuevoCliente
    When method post
    Then status 422
    And match response.code == 'CLIENTE_MENOR_DE_EDAD'

  Scenario: la identificacion no se puede modificar con PATCH
    Given path 'clientes'
    And request nuevoCliente
    When method post
    Then status 201

    Given path 'clientes', response.clienteId
    And header Content-Type = 'application/merge-patch+json'
    And request { identificacion: '999999999' }
    When method patch
    Then status 422
    And match response.code == 'IDENTIFICACION_INMUTABLE'
