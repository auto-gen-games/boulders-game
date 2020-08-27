import indigo._
import Settings._

/** Utility functions for placing graphics on the play scene. */
object ViewLogic {
  /** Place the given graphic at the grid positions specified by the given matrix for the given level. */
  def planGraphics (plan: Vector[Vector[Boolean]], maze: Level, graphic: Graphic): List[Graphic] =
    planToGridPoints (plan).map (place (_, maze, graphic))

  /** Translate the given matrix of whether something is placed at each grid point to a list of grid points. */
  def planToGridPoints (plan: Vector[Vector[Boolean]]): List[GridPoint] =
    (for (x <- plan.indices; y <- plan.head.indices) yield
      if (plan (x)(y)) Some (GridPoint (x, y)) else None).flatten.toList

  /** Translate the given grid point to a position on the screen. */
  def gridPointToPoint (gridPoint: GridPoint, maze: Level): Point =
    Point ((gridPoint.x + (maxGridWidth - maze.width) / 2) * gridSquareSize + leftMargin,
      (gridPoint.y + (maxGridHeight - maze.height) / 2) * gridSquareSize + headerHeight)

  /** Place the given graphic at the screen position corresponding to the given grid point. */
  def place (gridPoint: GridPoint, maze: Level, graphic: Graphic): Graphic =
    graphic.moveTo (gridPointToPoint (gridPoint, maze))

  /** Returns true if the (x, y) coordinates are within the box for which the given position is the top left
   * and the box is the size of a play grid square. */
  def inBox (x: Int, y: Int, position: Point): Boolean =
    x >= position.x && x < position.x + gridSquareSize && y >= position.y && y < position.y + gridSquareSize
}
