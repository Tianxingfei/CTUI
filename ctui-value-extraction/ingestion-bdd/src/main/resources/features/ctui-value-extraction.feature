Feature: ctui-value-extraction

  Scenario: Scenario for the project
    Given new file is arriving in raw zone at "path/to/data"
    When the ingestion in triggered
    Then the input gets transformed to the defined format
    And the ingested data matches the expected data