Feature: Users API

  Background:
    Given url baseUrl

  Scenario: Create, get and list users
    * def clientId = karate.uuid()
    * def requestBody =
    """
    {
      "name": "Pedro Perez",
      "genre": "M",
      "age": 30,
      "identification": "ID-" + clientId,
      "address": "Calle 1",
      "phone": "345",
      "clientId": clientId,
      "password": "4nDr3s"
    }
    """

    Given path 'customers'
    And request requestBody
    When method post
    Then status 201
    And match response.id != null
    And match response.name == "Pedro Perez"
    * def userId = response.id

    Given path 'customers', userId
    When method get
    Then status 200
    And match response.id == userId
    And match response.name == "Pedro Perez"

    Given path 'customers'
    When method get
    Then status 200
    And match response contains any { id: '#(userId)' }
