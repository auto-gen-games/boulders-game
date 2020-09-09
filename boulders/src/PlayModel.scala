import Level.{hasFloor, hasLeftWall, inBounds}
import Matrix.updated
import Settings.stepTime
import indigo.shared.scenegraph.{Graphic, Sprite}
import indigo.shared.time.Seconds

import scala.annotation.tailrec

/** The status of play: still playing, lost (with reason) or won. */
sealed trait PlayStatus
case object Playing              extends PlayStatus
case class Lost(message: String) extends PlayStatus
case object Won                  extends PlayStatus

/** Records a single movement of the player or a boulder, from which to produce animations */
case class Movement(from: GridPoint, dx: Int, dy: Int, collecting: Boolean, started: Seconds, image: Graphic)

/** The play model is defined by the level being played, the current position of the player and boulders,
  * whether the player is extended upwards, whether the diamond has been collected, and the status of play.
  */
final case class PlayModel(
    maze: Level,
    position: GridPoint,
    boulders: Vector[Vector[Boolean]],
    extended: Boolean,
    diamondTaken: Boolean,
    status: PlayStatus,
    playerMoves: Vector[Movement],
    boulderMoves: Vector[Movement],
    tutorial: Vector[TutorialGuideLine]
)

object PlayModel {

  /** Enable all buttons on the play scene by default */
  val allButtonEvents: Set[PlaySceneButtonEvent] =
    Set(BackButtonEvent, ReplayButtonEvent, LeftButtonEvent, RightButtonEvent, ExtendButtonEvent)

  /** Creates a play model from the given level, with the player and boulders in their start positions,
    * the player not extended, the diamond not collected, and the play status as currently playing (not lost or won).
    */
  def play(maze: Level, tutorial: Vector[TutorialGuideLine], highlight: Sprite): PlayModel =
    PlayModel(
      maze,
      maze.start,
      maze.boulders,
      extended = false,
      diamondTaken = false,
      status = Playing,
      playerMoves = Vector.empty,
      boulderMoves = Vector.empty,
      tutorial
    )

  def play(maze: Level, highlight: Sprite): PlayModel = play(maze, Vector.empty, highlight)

  /** Returns true if the player or boulder could move from the given position left or right (as given by dx)
    * and considering whether it can push a boulder in doing so.
    */
  def canMove(model: PlayModel, current: GridPoint, dx: Int, allowPush: Boolean): Boolean = {
    val moved = current.moveBy(dx, 0)
    val above = current.moveBy(0, -1)
    moved.x >= 0 && moved.x < model.maze.width &&
    (dx != 1 || !hasLeftWall(model.maze, moved)) &&
    (dx != -1 || !hasLeftWall(model.maze, current)) &&
    (hasFloor(model.maze, above) || !hasBoulder(model, above)) &&
    (!hasBoulder(model, moved) || (allowPush && canMove(model, moved, dx, allowPush = false)))
  }

  /** Returns true if there is a boulder at given grid position, where every position outside the level's
    * grid is assumed not to have a wall (returns false).
    */
  def hasBoulder(model: PlayModel, point: GridPoint): Boolean =
    inBounds(model.maze, point) && model.boulders(point.x)(point.y)

  /** Move the player left or right (specified by dx) if it is possible. If there is a pushable boulder, this
    * will be pushed. If there is nothing under the player or boulder on moving, they will fall until stopping.
    */
  def move(model: PlayModel, dx: Int, started: Seconds): PlayModel =
    if (canMove(model, model.position, dx, allowPush = true))
      playerFall(
        push(movePlayer(model, dx, 0, hasBoulder(model, model.position.moveBy(dx, 0)), started), dx, started),
        started + stepTime
      )
    else
      model.copy(playerMoves = model.playerMoves :+ Movement(model.position, 0, 0, false, started, tryGraphic(dx, 0)))

  /** Extend the player upwards if possible. */
  def extend(model: PlayModel, started: Seconds): PlayModel =
    if (model.extended || hasFloor(model.maze, model.position.moveBy(0, -1)))
      model
    else
      movePlayer(model, 0, -1, false, started)

  /** Unextend the player if extended. */
  def unextend(model: PlayModel, started: Seconds): PlayModel =
    if (!model.extended)
      model
    else
      movePlayer(model, 0, 1, false, started)

  /** Called after a player moves, this pushes a boulder at the moved to position if there is one, and then
    * lets it fall until stopped.
    */
  def push(model: PlayModel, dx: Int, started: Seconds): PlayModel =
    if (hasBoulder(model, model.position))
      boulderFall(moveBoulder(model, model.position, dx, 0, started), model.position.moveBy(dx, 0), started + stepTime)
    else
      model

  /** Moves the player down until it reaches a supporting floor or boulder. */
  @tailrec
  def playerFall(model: PlayModel, started: Seconds): PlayModel =
    if (model.status != Won && !hasFloor(model.maze, model.position) && !hasBoulder(model, model.position.moveBy(0, 1)))
      playerFall(movePlayer(model, 0, 1, false, started), started + stepTime)
    else model

  /** Moves the boulder at the given position down until it reaches a supporting floor or boulder. */
  @tailrec
  def boulderFall(model: PlayModel, position: GridPoint, started: Seconds): PlayModel =
    if (model.status == Playing && !hasFloor(model.maze, position) && !hasBoulder(model, position.moveBy(0, 1)))
      boulderFall(moveBoulder(model, position, 0, 1, started), position.moveBy(0, 1), started + stepTime)
    else model

  /** Gives the default graphic for the player moving in a given direction, pushing a boulder or not */
  def moveGraphic(dx: Int, dy: Int, pushing: Boolean): Graphic =
    (dx, dy, pushing) match {
      case (0, 1, _)      => GameAssets.playerFalling
      case (-1, 0, false) => GameAssets.playerLeft
      case (-1, 0, true)  => GameAssets.playerLeftPush
      case (1, 0, false)  => GameAssets.playerRight
      case (1, 0, true)   => GameAssets.playerRightPush
      case _              => GameAssets.player
    }

  /** Gives the graphic for the player trying but failing to move in a given direction */
  def tryGraphic(dx: Int, dy: Int): Graphic =
    (dx, dy) match {
      case (-1, 0) => GameAssets.playerLeftPush
      case (1, 0)  => GameAssets.playerRightPush
      case _       => GameAssets.player
    }

  /** Move the player by the given dx and dy, collecting the diamond or exiting successfully if appropriate,
    * and unextending unless the move is upwards.
    */
  def movePlayer(model: PlayModel, dx: Int, dy: Int, pushing: Boolean, started: Seconds): PlayModel =
    collect(
      model.copy(
        position = model.position.moveBy(dx, dy),
        extended = dy == -1,
        playerMoves = model.playerMoves :+ Movement(
          model.position,
          dx,
          dy,
          !model.diamondTaken && model.maze.diamond == model.position.moveBy(dx, dy),
          started,
          moveGraphic(dx, dy, pushing)
        )
      )
    )

  /** Checks whether the player's new position is the same as the diamond, collecting it if so, or
    * the exit, changing status to won if the diamond has been collected.
    */
  def collect(model: PlayModel): PlayModel =
    if (!model.diamondTaken && model.position == model.maze.diamond)
      model.copy(diamondTaken = true)
    else if (model.diamondTaken && model.position == model.maze.exit)
      model.copy(status = Won)
    else model

  /** Sets whether a boulder exists or not at the given position. */
  def setBoulder(model: PlayModel, position: GridPoint, exists: Boolean): PlayModel =
    model.copy(boulders = updated(model.boulders, position.x, position.y, value = exists))

  /** Moves the boulder at the given position by a dx and dy, squashing the diamond or exit if the new
    * position coincides with them.
    */
  def moveBoulder(model: PlayModel, position: GridPoint, dx: Int, dy: Int, started: Seconds): PlayModel =
    squash(
      setBoulder(setBoulder(model, position, false), position.moveBy(dx, dy), true).copy(boulderMoves =
        model.boulderMoves :+
          Movement(position, dx, dy, false, started, GameAssets.boulder)
      ),
      position.moveBy(dx, dy)
    )

  /** Checks whether a boulder being at the given position squashes the diamond or exit, changing play status
    * to lost if so.
    */
  def squash(model: PlayModel, boulder: GridPoint): PlayModel =
    if (!model.diamondTaken && model.maze.diamond == boulder)
      model.copy(status = Lost("Diamond crushed by boulder!"))
    else if (model.maze.exit == boulder)
      model.copy(status = Lost("Exit destroyed by boulder!"))
    else model

  /** Checks whether the time has completed for the first movement steps, and removes them if so */
  def updateMovement(model: PlayModel, time: Seconds): PlayModel = {
    val forPlayer =
      if (model.playerMoves.isEmpty || model.playerMoves.head.started + stepTime >= time)
        model.playerMoves
      else model.playerMoves.tail
    val forBoulder =
      if (model.boulderMoves.isEmpty || model.boulderMoves.head.started + stepTime >= time)
        model.boulderMoves
      else model.boulderMoves.tail
    model.copy(playerMoves = forPlayer, boulderMoves = forBoulder)
  }

  def staticBoulders(model: PlayModel): Vector[Vector[Boolean]] =
    movingBoulderDestination(model) match {
      case None          => model.boulders
      case Some(boulder) => Matrix.updated(model.boulders, boulder.x, boulder.y, false)
    }

  def movingBoulderDestination(model: PlayModel): Option[GridPoint] =
    if (model.boulderMoves.isEmpty) None
    else Some(model.boulderMoves.last.from.moveBy(model.boulderMoves.last.dx, model.boulderMoves.last.dy))

  def reachingDiamond(model: PlayModel): Boolean =
    model.playerMoves.exists(_.collecting)

  def enabled(model: PlayModel): Set[PlaySceneButtonEvent] =
    if (model.tutorial.isEmpty) allButtonEvents else model.tutorial.head.enabled

  def stepTutorial(model: PlayModel): PlayModel =
    model.copy(tutorial = model.tutorial.drop(1))
}
