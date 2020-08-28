import GameAssets.{levelButtonGraphic, levelNumber, levelNumberDown, levelNumberOver}
import indigo._
import Settings._
import indigoextras.ui.{Button, ButtonAssets}

/** Utility functions for placing graphics on the play scene. */
object ViewLogic {
  /** Place the given graphic at the grid positions specified by the given matrix for the given level. */
  def planGraphics(plan: Vector[Vector[Boolean]], maze: Level, graphic: Graphic): List[Graphic] =
    planToGridPoints(plan).map(place(_, maze, graphic))

  /** Translate the given matrix of whether something is placed at each grid point to a list of grid points. */
  def planToGridPoints (plan: Vector[Vector[Boolean]]): List[GridPoint] =
    (for (x <- plan.indices; y <- plan.head.indices) yield
      if (plan(x)(y)) Some (GridPoint(x, y)) else None).flatten.toList

  /** Translate the given grid point to a position on the screen. */
  def gridPointToPoint (gridPoint: GridPoint, maze: Level): Point =
    Point ((gridPoint.x + (maxGridWidth - maze.width) / 2) * gridSquareSize + leftMargin,
      (gridPoint.y + (maxGridHeight - maze.height) / 2) * gridSquareSize + headerHeight)

  /** Place the given graphic at the screen position corresponding to the given grid point. */
  def place (gridPoint: GridPoint, maze: Level, graphic: Graphic): Graphic =
    graphic.moveTo(gridPointToPoint(gridPoint, maze))

  /** Returns true if the (x, y) coordinates are within the box for which the given position is the top left
   * and the box is the size of a play grid square. */
  def inBox(x: Int, y: Int, position: Point): Boolean =
    x >= position.x && x < position.x + gridSquareSize && y >= position.y && y < position.y + gridSquareSize

  /** Add spaces between digits else they overlap when scaled. */
  def spacedNumber (n: Int): String =
    n.toString.map (_ + " ").mkString.trim

  /** Indent number to move to middle of square, less so for longer numbers */
  def numberLeftPos (n: Int): Int =
    if (n < 10) 16 else 8

  /** Determine the x position of the level number box for the given level */
  def levelBoxLeft (level: Int): Int =
    (level % levelsPerRow) * levelBoxSize + leftMargin

  /** Determine the y position of the level number box for the given level */
  def levelBoxTop (level: Int): Int =
    (level / levelsPerRow) * levelBoxSize + headerHeight

  /** Create a level button assets container, tinted to indicate the level size (area) */
  def levelButtonAssets (area: Int): ButtonAssets =
    ButtonAssets (up = levelNumber.withTint (1.0, 0.0, 0.0, (area - 36) / 60.0),
      over = levelNumberOver.withTint (1.0, 0.0, 0.0, (area - 36) / 60.0),
      down = levelNumberDown.withTint (1.0, 0.0, 0.0, (area - 36) / 60.0))

  /** Create a level button at a given x and y position, for a given level number,
   * where the level has a given grid area */
  def levelButton (x: Int, y: Int, index: Int, area: Int): Button =
    Button (buttonAssets = levelButtonAssets (area),
      bounds = Rectangle (x, y, levelBoxSize, levelBoxSize),
      depth = Depth (2)).
      withUpAction { List (LevelButtonEvent (index)) }

  /** Create all level buttons for the game */
  def createLevelButtons (model: Model): List[Button] =
    model.levels.indices.map { n =>
      levelButton (levelBoxLeft (n), levelBoxTop (n), n, Level.area (model.levels (n)))
    }.toList
}
