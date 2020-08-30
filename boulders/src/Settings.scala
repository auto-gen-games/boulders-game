import indigo.{Millis, Point, Seconds}

/** Constants for the display settings. */
object Settings {
  /** The size of each grid square on the play scene. */
  val gridSquareSize = 32

  // Margins around the play area, where the footer is used for instructions and controls
  val leftMargin: Int = 16
  val rightMargin: Int = 16
  val headerHeight: Int = 32
  val footerHeight: Int = gridSquareSize + 20

  val magnificationLevel: Int = 2

  // The maximum grid size for levels determines the viewport size
  val maxGridWidth = 10
  val maxGridHeight = 8

  /** The value below can be larger than the actual number of levels.
   * It is just used for generating level buttons in case needed.
   */
  val maximumLevel = 100

  val viewportWidth: Int =
    (gridSquareSize * maxGridWidth + leftMargin + rightMargin) * magnificationLevel
  val viewportHeight: Int =
    (gridSquareSize * maxGridHeight + footerHeight + headerHeight) * magnificationLevel

  // Useful points on the screen
  val horizontalCenter: Int = (viewportWidth / magnificationLevel) / 2
  val verticalMiddle: Int = (viewportHeight / magnificationLevel) / 2
  val footerStart: Int = (viewportHeight / magnificationLevel) - footerHeight + 1

  /** The size of the level number boxes on the levels selection scene */
  val levelBoxSize = gridSquareSize

  /** The time that each movement step by the player or a boulder should take */
  val stepTime = Seconds (0.1)

  /** The number of levels to fit on each row on the levels scene */
  val levelsPerRow: Int =
    (viewportWidth - (leftMargin + rightMargin) * magnificationLevel) / (levelBoxSize * magnificationLevel)

  val backBoxPosition = Point (horizontalCenter - gridSquareSize * 2, footerStart + 15)
  val infoBoxPosition = Point (horizontalCenter - gridSquareSize / 2, footerStart + 15)
  val replayBoxPosition = Point (horizontalCenter + gridSquareSize, footerStart + 15)
}
