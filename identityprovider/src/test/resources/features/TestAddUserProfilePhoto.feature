Feature: Add User Profile Photo

  Scenario: Profile photo not present.
    Given I am logged in as user id 1
    And I have no profile photo
    When I change my profile photo
    And I request my profile photo Image
    Then I receive the profile photo for id 1