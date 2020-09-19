import GameAssets.{createButton, createRadio, gameTypes}
import indigo._
import Settings._
import indigoextras.ui.{Button, ButtonAssets}

/** Utility functions for placing graphics on the play scene. */
object ViewLogic {
  def moveBy(position: Point, dx: Int, dy: Int): Point =
    Point(position.x + dx, position.y + dy)

  def levelButtons(numberOfLevels: Int): List[Button] =
    (0 until numberOfLevels)
      .map(level => createButton("button-base", levelButtonPosition(level), LevelButtonEvent(level)))
      .toList

  def gameTypeButton: RadioButton = {
    val positions = gameTypes.indices.map(index => moveBy(levelTypesPosition, index * 64, 0)).toList
    createRadio(
      assetName = "type-button",
      positions = positions,
      selectionEvent = i => LevelTypeButtonEvent(i),
      width = 64,
      height = 32,
      initialSelection = 0
    )
  }

  /** Place the given graphic at the grid positions specified by the given matrix for the given level. */
  def planGraphics(plan: Vector[Vector[Boolean]], maze: Level, graphic: Renderable): List[Renderable] =
    GridPoint.planToGridPoints(plan).map(place(_, maze, graphic))

  /** Translate the given grid point to a position on the screen. */
  def gridPointToPoint(gridPoint: GridPoint, maze: Level, offsetX: Double = 0.0, offsetY: Double = 0.0): Point =
    Point(
      ((gridPoint.x + offsetX + (maxGridWidth - maze.width) / 2) * cellSize + leftMargin).toInt,
      ((gridPoint.y + offsetY + (maxGridHeight - maze.height) / 2) * cellSize + headerHeight).toInt
    )

  /** Place the given graphic at the screen position corresponding to the given grid point. */
  def place(
      gridPoint: GridPoint,
      maze: Level,
      graphic: Renderable,
      offsetX: Double = 0.0,
      offsetY: Double = 0.0
  ): Renderable =
    graphic.moveTo(gridPointToPoint(gridPoint, maze, offsetX, offsetY))

  /** Add spaces between digits else they overlap when scaled. */
  def spacedNumber(n: Int): String =
    n.toString.map(_ + " ").mkString.trim

  /** Indent level number to move to middle of square, less so for longer numbers */
  def numberLeftPos(n: Int): Int =
    if (n < 10) 12 else 8

  /** Determine level button, where level numbering starts from 0 */
  def levelButtonPosition(level: Int): Point =
    Point(
      (level % levelsPerRow) * cellSize + levelButtonsPosition.x,
      (level / levelsPerRow) * cellSize + levelButtonsPosition.y
    )

  def levelNumberPosition(level: Int): Point = {
    val box = levelButtonPosition(level)
    Point(box.x + numberLeftPos(level + 1), box.y + 10)
  }

  def levelTypeTextPosition(kind: String): Point =
    moveBy(levelTypesPosition, gameTypes.indexOf(kind) * 64 + 8, 10)

  def placeIndicator(indicator: Indicator, model: PlayModel, highlight: Sprite): Option[Sprite] =
    indicator match {
      case NoIndicator =>
        None

      case PlayerIndicator =>
        Some(
          highlight
            .moveTo(gridPointToPoint(model.position, model.maze))
            .play()
        )

      case DiamondIndicator =>
        Some(
          highlight
            .moveTo(gridPointToPoint(model.maze.diamond, model.maze))
            .play()
        )

      case ExitIndicator =>
        Some(
          highlight
            .moveTo(gridPointToPoint(model.maze.exit, model.maze))
            .play()
        )

      case LeftIndicator =>
        Some(highlight.moveTo(Settings.leftControlPosition).play())

      case RightIndicator =>
        Some(highlight.moveTo(Settings.rightControlPosition).play())

      case ExtendIndicator =>
        Some(highlight.moveTo(Settings.extendControlPosition).play())

      case ReplayIndicator =>
        Some(highlight.moveTo(Settings.replayBoxPosition).play())
    }
}
