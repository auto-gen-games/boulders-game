/** A position on the game play grid. */
final case class GridPoint(x: Int, y: Int) {

  /** Find the grid position from this one offset by dx and dy. */
  def moveBy(dx: Int, dy: Int): GridPoint =
    GridPoint(x + dx, y + dy)
}

object GridPoint {

  /** Translate the given matrix of whether something is placed at each grid point to a list of grid points. */
  def planToGridPoints(plan: Vector[Vector[Boolean]]): List[GridPoint] =
    (for {
      x <- plan.indices
      y <- plan.head.indices
    } yield if (plan(x)(y)) Some(GridPoint(x, y)) else None).flatten.toList
}
