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
    And with the organization set to <organization>
    And with the priority set to <priority>
    And with the notificationType set to <notificationType>
    And the message is next in the queue
    When I get a message with <queryParams>
    Then I should receive a message

    Examples:
    | organization | priority | notificationType | queryParams                                  |
    | TEST_ORG     | HIGH     | SLACK            | organization=TEST_ORG&notificationType=SLACK |
    | TEST_ORG     | NORMAL   | null             | organization=TEST_ORG                        |
    | TEST_ORG     | null     | SMS              | organization=TEST_ORG&notificationType=SMS   |
    | TEST_ORG     | NORMAL   | ALL              | organization=TEST_ORG                        |