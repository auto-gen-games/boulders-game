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

  /** The time that each movement step by the player or a boulder should take */
  val stepTime       = Seconds(0.1)
  val replayStepTime = Seconds(0.3)

  /** The number of levels to fit on each row on the levels scene */
  val levelTypesPosition    = Point(halfSize, halfSize)
  val tutorialLevelPosition = Point(halfSize, cellSize * 2)
  val levelButtonsPosition  = Point(halfSize, cellSize * 3 + halfSize)
  val levelsPerRow: Int     = (areaWidth - leftMargin - rightMargin) / cellSize

  val backBoxPosition          = Point(0, 0)
  val undoBoxPosition          = Point(horizontalCenter - cellSize / 2, 0)
  val replayBoxPosition        = Point(areaWidth - cellSize, 0)
  val forwardBoxPosition       = Point(horizontalCenter - halfSize, verticalMiddle + cellSize)
  val leftControlPosition      = Point(horizontalCenter - cellSize * 2, footerStart)
  val extendControlPosition    = Point(horizontalCenter - halfSize, footerStart)
  val rightControlPosition     = Point(horizontalCenter + cellSize, footerStart)
  val flipControlPosition      = Point(horizontalCenter + cellSize * 2 + halfSize, footerStart)
  val tutorialGuideBoxPosition = Point(cellSize + halfSize, halfSize)

  val magnificationLevel: Int = 2
  val viewportWidth: Int      = areaWidth * magnificationLevel
  val viewportHeight: Int     = areaHeight * magnificationLevel
}
