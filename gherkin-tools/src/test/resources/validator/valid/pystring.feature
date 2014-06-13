Feature: A py string feature
"""
      a string with #something
      """

  Scenario:
    Then I should see
    """
      a string with #something
      """

  Scenario:
  """
      a string with #something
      """
    When I'm alive

  