import scala.annotation.tailrec
import Level.{inBounds, hasLeftWall, hasFloor}
import Matrix.updated

/** The status of play: still playing, lost (with reason) or won. */
sealed trait PlayStatus
case object Playing extends PlayStatus
case class Lost (message: String) extends PlayStatus
case object Won extends PlayStatus

/** The play model is defined by the level being played, the current position of the player and boulders,
 * whether the player is extended upwards, whether the diamond has been collected, and the status of play. */
final case class PlayModel (maze: Level, position: GridPoint, boulders: Vector[Vector[Boolean]],
                            extended: Boolean, diamondTaken: Boolean, status: PlayStatus)

object PlayModel {
  /** The default play model is initialised from the default level. */
  val uninitiated: PlayModel = play (Level.uninitiated)

  /** Creates a play model from the given level, with the player and boulders in their start positions,
   * the player not extended, the diamond not collected, and the play status as currently playing (not lost or won). */
  def play (maze: Level): PlayModel =
    PlayModel (maze, maze.start, maze.boulders, extended = false, diamondTaken = false, status = Playing)

  /** Returns true if the player or boulder could move from the given position left or right (as given by dx)
   * and considering whether it can push a boulder in doing so. */
  def canMove (model: PlayModel, current: GridPoint, dx: Int, allowPush: Boolean): Boolean = {
    val moved = current.moveBy (dx, 0)
    val above = current.moveBy (0, -1)
    moved.x >= 0 && moved.x < model.maze.width &&
      (dx != 1 || !hasLeftWall (model.maze, moved)) &&
      (dx != -1 || !hasLeftWall (model.maze, current)) &&
      (hasFloor (model.maze, above) || !hasBoulder (model, above)) &&
      (!hasBoulder (model, moved) || (allowPush && canMove (model, moved, dx, allowPush = false)))
  }

  /** Returns true if there is a boulder at given grid position, where every position outside the level's
   * grid is assumed not to have a wall (returns false). */
  def hasBoulder (model: PlayModel, point: GridPoint): Boolean =
    inBounds (model.maze, point) && model.boulders (point.x)(point.y)

  /** Move the player left or right (specified by dx) if it is possible. If there is a pushable boulder, this
   * will be pushed. If there is nothing under the player or boulder on moving, they will fall until stopping. */
  def move (model: PlayModel, dx: Int): PlayModel =
    if (canMove (model, model.position, dx, allowPush = true))
      playerFall (push (movePlayer (model, dx, 0), dx))
    else
      model

  /** Extend the player upwards if possible. */
  def extend (model: PlayModel): PlayModel =
    if (model.extended || hasFloor (model.maze, model.position.moveBy (0, -1 )))
      model
    else
      movePlayer (model, 0, -1)

  /** Unextend the player if extended. */
  def unextend (model: PlayModel): PlayModel =
    if (!model.extended)
      model
    else
      movePlayer (model, 0, 1)

  /** Called after a player moves, this pushes a boulder at the moved to position if there is one, and then
   * lets it fall until stopped. */
  def push (model: PlayModel, dx: Int): PlayModel =
    if (hasBoulder (model, model.position))
      boulderFall (moveBoulder (model, model.position, dx, 0), model.position.moveBy (dx, 0))
    else
      model

  /** Moves the player down until it reaches a supporting floor or boulder. */
  @tailrec
  def playerFall (model: PlayModel): PlayModel =
    if (!hasFloor (model.maze, model.position) && !hasBoulder (model, model.position.moveBy (0, 1)))
      playerFall (movePlayer (model, 0, 1))
    else model

  /** Moves the boulder at the given position down until it reaches a supporting floor or boulder. */
  @tailrec
  def boulderFall (model: PlayModel, position: GridPoint): PlayModel =
    if (hasFloor (model.maze, position) || hasBoulder (model, position.moveBy (0, 1)))
      model
    else
      boulderFall (moveBoulder (model, position, 0, 1), position.moveBy (0, 1))

  /** Move the player by the given dx and dy, collecting the diamond or exiting successfully if appropriate,
   * and unextending unless the move is upwards. */
  def movePlayer (model: PlayModel, dx: Int, dy: Int): PlayModel =
    collect (model.copy (position = model.position.moveBy (dx, dy), extended = dy == -1))

  /** Checks whether the player's new position is the same as the diamond, collecting it if so, or
   * the exit, changing status to won if the diamond has been collected. */
  def collect (model: PlayModel): PlayModel =
    if (!model.diamondTaken && model.position == model.maze.diamond)
      model.copy (diamondTaken = true)
    else if (model.diamondTaken && model.position == model.maze.exit)
      model.copy (status = Won)
    else model

  /** Sets whether a boulder exists or not at the given position. */
  def setBoulder (model: PlayModel, position: GridPoint, exists: Boolean): PlayModel =
    model.copy (boulders = updated (model.boulders, position.x, position.y, value = exists))

  /** Moves the boulder at the given position by a dx and dy, squashing the diamond or exit if the new
   * position coincides with them. */
  def moveBoulder (model: PlayModel, position: GridPoint, dx: Int, dy: Int): PlayModel =
    squash (setBoulder (setBoulder (model, position, false), position.moveBy (dx, dy), true),
      position.moveBy (dx, dy))

  /** Checks whether a boulder being at the given position squashes the diamond or exit, changing play status
   * to lost if so. */
  def squash (model: PlayModel, boulder: GridPoint): PlayModel =
    if (!model.diamondTaken && model.maze.diamond == boulder)
      model.copy (status = Lost ("Diamond crushed by boulder!"))
    else if (model.maze.exit == boulder)
      model.copy (status = Lost ("Exit destroyed by boulder!"))
    else model
}
