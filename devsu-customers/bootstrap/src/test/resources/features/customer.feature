Feature: Customer API integration tests

  Background:
    * url baseUrl

  Scenario: Create a new customer
    * def payload =
    """
    {
      "name": "John Doe",
      "genre": "MALE",
      "age": 30,
      "identification": "ID-10000",
      "address": "123 Main St",
      "phone": "+1-555-1234",
      "clientId": "client-id-10000",
      "password": "secretPass123"
    }
    """
    Given path 'customers'
    And request payload
    When method post
    Then status 201
    And match response.id != null
    And match response.name == payload.name
    * def createdId = response.id

    Given path 'customers', createdId
    When method get
    Then status 200
    And match response.id == createdId

    * def update = { name: 'John Updated', age: 31, address: '456 New Ave' }
    Given path 'customers', createdId
    And request update
    When method patch
    Then status 200
    And match response.name == update.name
    And match response.age == update.age
    And match response.address == update.address

    Given path 'customers', createdId, 'active', false
    When method patch
    Then status 200
    And match response.active == false

    Given path 'customers', createdId
    When method delete
    Then status 204

  Scenario: Get all customers
    Given path 'customers'
    When method get
    Then status 200
    And match response == '#[]'
