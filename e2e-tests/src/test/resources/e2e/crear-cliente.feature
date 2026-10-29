@ignore
Feature: crea un cliente en cliente-service con una identificacion unica

  Scenario:
    * def sufijo = '' + Java.type('java.lang.System').nanoTime()
    * def identificacion = 'E' + sufijo.substring(sufijo.length - 12)
    Given url clienteUrl
    And path 'clientes'
    And request { nombre: '#(nombre)', genero: '#(genero)', edad: '#(edad)', identificacion: '#(identificacion)', direccion: '#(direccion)', telefono: '#(telefono)', contrasena: '#(contrasena)', estado: true }
    When method post
    Then status 201
    * def clienteId = response.clienteId
