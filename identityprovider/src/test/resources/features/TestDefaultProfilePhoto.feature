Feature: Default Profile Photo

  Scenario: Profile photo not present.
    Given I am logged in as user id 1
    And I have no profile photo
    When I request my profile photo Image
    Then I receive the default profile photo icon