Feature: Accounts API

  Background:
    Given url baseUrl

  Scenario: Create account, list, get by id, update, and change status
    # First ensure there is a customer to associate
    * def customerId = 'a8adfa00-6680-49b3-bf94-caa8c3f1d823'

    # Create account
    * def accountNumber = Math.floor(Math.random() * 9000000000) + 1000000000
    * def createBody =
    """
    {
      "accountNumber": #(accountNumber),
      "accountType": "SAVINGS",
      "initialBalance": 1000.50,
      "customerId": "#(customerId)"
    }
    """

    Given path 'accounts'
    And request createBody
    When method post
    Then status 201
    And match response.id != null
    And match response.accountNumber == accountNumber
    And match response.accountType == 'SAVINGS'
    And match response.initialBalance == 1000.50
    And match response.status == true
    * def accountId = response.id

    # Get by id
    Given path 'accounts', accountId
    When method get
    Then status 200
    And match response.id == accountId

    # List all
    Given path 'accounts'
    When method get
    Then status 200
    And match response contains any { id: '#(accountId)' }

    # Update account
    * def updateBody =
    """
    {
      "accountType": "CHECKING",
      "initialBalance": 2000.00
    }
    """

    Given path 'accounts', accountId
    And request updateBody
    When method patch
    Then status 200
    And match response.accountType == 'CHECKING'
    And match response.initialBalance == 2000.00

    # Change status
    Given path 'accounts', accountId, 'active', false
    When method patch
    Then status 200
    And match response.status == false
