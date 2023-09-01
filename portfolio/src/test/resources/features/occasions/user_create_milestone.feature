Feature: Milestone is only created if it falls within project dates, has no null values, and a name of length less than 51 characters.

  @storyU2
  Scenario Outline: Milestone creation
    Given a project exists from <StartDate> to <EndDate>
    When a user creates a milestone for <MilestoneDate> with name <Name> and type <Type>
    Then The milestone exists: <BoolDeadlineExists>

    Examples:
      | StartDate    | EndDate      | MilestoneDate | Name              | Type | BoolDeadlineExists |
      | "2022-01-01" | "2022-12-31" | "2022-05-01"  | "valid milestone" | 1    | "true"             |
      | "2022-01-01" | "2022-12-31" | "2022-05-01"  | "valid milestone" | 2    | "true"             |
      | "2022-01-01" | "2022-12-31" | "2023-05-01"  | "invalid date"    | 2    | "false"            |
      | "2022-01-01" | "2022-12-31" | "left blank"  | "invalid date"    | 3    | "false"            |
