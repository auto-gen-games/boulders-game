import indigo._
import Settings._
import indigoextras.ui.{Button, ButtonAssets}

/** Utility functions for placing graphics on the play scene. */
object ViewLogic {
  /** Creates a button from the given graphics asset, at a position, and triggering an event */
  def createButton (assetName: String, position: Point, buttonEvent: GlobalEvent, size: Int = gridSquareSize): Button = {
    val material = Material.Textured (AssetName (assetName))
    val buttonAssets = ButtonAssets (
      up = Graphic (0, 0, size, size, 2, material).withCrop (0, 0, size, size),
      over = Graphic (0, 0, size, size, 2, material).withCrop (size, 0, gridSquareSize, size),
      down = Graphic (0, 0, size, size, 2, material).withCrop (0, size, size, size)
    )
    Button (buttonAssets = buttonAssets,
      bounds = Rectangle (position.x, position.y, gridSquareSize, gridSquareSize),
      depth = Depth (2)).
      withUpAction { List (buttonEvent) }
  }

  def levelButtons (numberOfLevels: Int): List[Button] =
    (0 until numberOfLevels).map (level =>
      createButton ("button-base", levelButtonPosition (level), LevelButtonEvent (level))).toList

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

  /** Add spaces between digits else they overlap when scaled. */
  def spacedNumber (n: Int): String =
    n.toString.map (_ + " ").mkString.trim

  /** Indent level number to move to middle of square, less so for longer numbers */
  def numberLeftPos (n: Int): Int =
    if (n < 10) 12 else 8

  /** Determine level button, where level numbering starts from 0 */
  def levelButtonPosition (level: Int): Point =
    Point ((level % levelsPerRow) * levelBoxSize + leftMargin,
      (level / levelsPerRow) * levelBoxSize + headerHeight)
}
