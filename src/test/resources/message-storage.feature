@MessageStorage
Feature: Message Storage
  As a user
  I want to persist messages
  So that they can be relayed to other applications

  Scenario: Add an empty message
    Given I have a message
    When I add the message
    Then I should receive an InvalidPayloadException

  Scenario: Add a message with only an organization provided
    Given I have a message
    And I provide an organization
    When I add the message
    Then I should receive a message added response