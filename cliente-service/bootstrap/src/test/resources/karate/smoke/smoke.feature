Feature: Service smoke check

  Scenario: an unknown resource answers 404 under the /api base path
    Given url baseUrl
    And path 'recurso-inexistente'
    When method get
    Then status 404
