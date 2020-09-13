import indigo.shared.datatypes.Rectangle
import indigo.{Millis, Point, Seconds}

/** Constants for the display settings. */
object Settings {

  /** The size of each grid square on the play scene. */
  val cellSize = 32
  val halfSize = cellSize / 2

  // Margins around the play area, where the footer is used for instructions and controls
  val leftMargin: Int   = halfSize
  val rightMargin: Int  = halfSize
  val headerHeight: Int = cellSize + halfSize
  val footerHeight: Int = cellSize + halfSize

  // The maximum grid size for levels determines the viewport size
  val maxGridWidth  = 10
  val maxGridHeight = 8

  val areaWidth: Int  = cellSize * maxGridWidth + leftMargin + rightMargin
  val areaHeight: Int = cellSize * maxGridHeight + footerHeight + headerHeight

  // Useful points on the screen
  val horizontalCenter: Int = areaWidth / 2
  val verticalMiddle: Int   = areaHeight / 2
  val rightStart: Int       = areaWidth - rightMargin
  val footerStart: Int      = areaHeight - footerHeight + 1

  /** The size of the level number boxes on the levels selection scene */
  val levelBoxSize = cellSize

  /** The time that each movement step by the player or a boulder should take */
  val stepTime = Seconds(0.1)

  /** The number of levels to fit on each row on the levels scene */
  val levelsPerRow: Int = (areaWidth - leftMargin - rightMargin) / levelBoxSize

  val backBoxPosition          = Point(0, 0)
  val undoBoxPosition          = Point(horizontalCenter - cellSize / 2, 0)
  val replayBoxPosition        = Point(areaWidth - cellSize, 0)
  val forwardBoxPosition       = Point(horizontalCenter - cellSize / 2, verticalMiddle + cellSize)
  val leftControlPosition      = Point(horizontalCenter - cellSize * 2, footerStart)
  val extendControlPosition    = Point(horizontalCenter - cellSize / 2, footerStart)
  val rightControlPosition     = Point(horizontalCenter + cellSize, footerStart)
  val tutorialLevelPosition    = Point(leftMargin, headerHeight)
  val tutorialGuideBoxPosition = Point(cellSize + halfSize, halfSize)

  val magnificationLevel: Int = 2
  val viewportWidth: Int      = areaWidth * magnificationLevel
  val viewportHeight: Int     = areaHeight * magnificationLevel
}
