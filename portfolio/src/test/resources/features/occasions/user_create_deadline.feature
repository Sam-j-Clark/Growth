Feature: Deadline is only created if it falls within project dates, has no null values, and a name of length less than 51 characters.

  @storyU2
  Scenario Outline: Deadline creation
    Given the user is authenticated: <BoolIsAuthenticatedUser>
    And a project exists from <StartDate> to <EndDate>
    When the user creates a deadline for <DeadlineDateTime> with name <Name>
    Then The deadline exists: <BoolDeadlineExists>

    Examples:
      | BoolIsAuthenticatedUser | StartDate    | EndDate      | DeadlineDateTime      | Name                                                  | BoolDeadlineExists |
      | "true"                  | "2022-01-01" | "2022-12-31" | "2022-05-01T08:00:00" | "valid deadline"                                      | "true"             |
      | "true"                  | "2022-01-01" | "2022-12-31" | "2023-05-01T08:00:00" | "invalid date"                                        | "false"            |
      | "true"                  | "2022-01-01" | "2022-12-31" | "2022-05-01T08:00:00" | "this is fifty-one characters, which is more than 50" | "false"            |
