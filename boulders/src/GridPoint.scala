/** A position on the game play grid. */
final case class GridPoint (x: Int, y: Int) {
  /** Find the grid position from this one offset by dx and dy. */
  def moveBy (dx: Int, dy: Int): GridPoint =
    GridPoint (x + dx, y + dy)
}