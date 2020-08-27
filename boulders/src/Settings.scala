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

  val viewportWidth: Int =
    (gridSquareSize * maxGridWidth + leftMargin + rightMargin) * magnificationLevel
  val viewportHeight: Int =
    (gridSquareSize * maxGridHeight + footerHeight + headerHeight) * magnificationLevel

  // Useful points on the screen
  val horizontalCenter: Int = (viewportWidth / magnificationLevel) / 2
  val verticalMiddle: Int = (viewportHeight / magnificationLevel) / 2
  val footerStart: Int = (viewportHeight / magnificationLevel) - footerHeight + 1

  /** The size of the level number boxes on the levels selection scene */
  val levelBoxSize = 64

  /** The number of levels to fit on each row on the levels scene */
  val levelsPerRow: Int =
    (viewportWidth - (leftMargin + rightMargin) * magnificationLevel) / (levelBoxSize * magnificationLevel)
}
