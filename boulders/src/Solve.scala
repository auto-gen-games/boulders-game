import indigo._
import PlayModel._
import scala.annotation.tailrec

object Solve {
  def ifNotLost(model: PlayModel): Option[PlayModel] =
    model.status match {
      case Lost(_) => None
      case _       => Some(model)
    }

  sealed trait Move {
    def canEnact(game: PlayModel): Boolean
    def enact(game: PlayModel, started: Seconds): Option[PlayModel]
  }
  case object MoveLeft extends Move {
    def canEnact(game: PlayModel): Boolean =
      canMove(game, game.position, -1, true)
    def enact(game: PlayModel, started: Seconds): Option[PlayModel] =
      ifNotLost(move(game, -1, started))
  }
  case object MoveRight extends Move {
    def canEnact(game: PlayModel): Boolean =
      canMove(game, game.position, 1, true)
    def enact(game: PlayModel, started: Seconds): Option[PlayModel] =
      ifNotLost(move(game, 1, started))
  }
  case object MoveExtend extends Move {
    def canEnact(game: PlayModel): Boolean =
      canExtend(game)
    def enact(game: PlayModel, started: Seconds): Option[PlayModel] =
      ifNotLost(extend(game, started))
  }
  case object MoveUnextend extends Move {
    def canEnact(game: PlayModel): Boolean =
      canUnextend(game)
    def enact(game: PlayModel, started: Seconds): Option[PlayModel] =
      ifNotLost(unextend(game, started))
  }
  case object MoveFlip extends Move {
    def canEnact(game: PlayModel): Boolean =
      !game.flipped
    def enact(game: PlayModel, started: Seconds): Option[PlayModel] =
      ifNotLost(flip(game, started))
  }
  val moves: Map[String, Set[Move]] =
    Map(
      "base" -> Set(MoveLeft, MoveRight, MoveExtend, MoveUnextend),
      "flip" -> Set(MoveLeft, MoveRight, MoveExtend, MoveUnextend, MoveFlip)
    )

  def equivalentGames(model1: PlayModel, model2: PlayModel): Boolean =
    model1.position == model2.position &&
      model1.boulders == model2.boulders &&
      model1.extended == model2.extended &&
      model1.diamondTaken == model2.diamondTaken &&
      model1.flipped == model2.flipped

  def distanceBetween(point1: GridPoint, point2: GridPoint): Double =
    Math.sqrt(
      ((point1.x - point2.x) * (point1.x - point2.x)) +
        ((point1.y - point2.y) * (point1.y - point2.y))
    )

  def distance(model: PlayModel): Double =
    if (!model.diamondTaken) distanceBetween(model.position, model.maze.diamond)
    else distanceBetween(model.position, model.maze.exit)

  def getAvailableMoves(playModel: PlayModel): Set[Move] = {
    val x = moves(playModel.maze.kind).filter(_.canEnact(playModel))
    System.err.println(s"possible moves: $x")
    x
  }

  def aStarSearch(start: PlayModel): Option[List[Move]] = {
    // A list of actions performed plus the state reached
    type SolveState = (List[Move], PlayModel)

    // Cost function calculated as the length of the path traversed plus the distance from the solution
    def cost(state: SolveState): Double =
      state._1.size + distance(state._2)

    // Orders two states with lowest cost first
    def compare(stateA: SolveState, stateB: SolveState): Boolean =
      cost(stateA) < cost(stateB)

    // Returns true if the LHS reaches the same state as the RHS but in equal or less actions
    def subsumes(existing: (List[Move], PlayModel), newState: (List[Move], PlayModel)): Boolean =
      equivalentGames(existing._2, newState._2) && existing._1.size <= newState._1.size

    // Perform the a-star search recursively
    @tailrec
    def solve(states: Vector[SolveState], tried: Vector[SolveState]): Option[List[Move]] =
      if (states.isEmpty) None
      else {
        val state = states.head
        if (state._2.status == Won) Some(state._1)
        else {
          val filtered = getAvailableMoves(state._2)
            .flatMap(move => move.enact(state._2, Seconds.zero).map(result => new SolveState(state._1 :+ move, result)))
            .filter(state => !tried.exists(subsumes(_, state)))
          solve((states.tail ++ filtered).sortWith(compare), tried ++ filtered)
        }
      }

    solve(Vector(new SolveState(Nil, start)), Vector((Nil, start)))
  }
}
