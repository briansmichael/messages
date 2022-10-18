@MessageRetrieval
Feature: Message Retrieval
  As a user
  I want to retrieve messages
  So that they can be processed

  Scenario: Get messages when none are present
    Given No messages are available
    When I get a message
    Then I should receive an empty response

  Scenario Outline: Get a message
    Given A message is available
    And with the priority set to <priority>
    And with the notificationType set to <notificationType>
    And I provide an organization
    And I provide a correlation id
    And the message is next in the queue
    When I get a message with <queryParams>
    Then I should receive a message

    Examples:
    | priority | notificationType | queryParams            |
    | HIGH     | SLACK            | notificationType=SLACK |
    | NORMAL   | null             |                        |
    | null     | SMS              | notificationType=SMS   |
    | NORMAL   | ALL              |                        |