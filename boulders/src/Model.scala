import indigo.Seconds
import indigo.scenes.Lens

/** The overall model for the game is the current play grid and the set of levels completed. */
final case class Model(
    selectedType: String,
    playModel: PlayModel,
    completed: Map[String, Set[Int]],
    replay: ReplayModel
)

case class ReplayModel(state: PlayModel, steps: List[Solve.Move], lastStepped: Seconds)

object Model {
  val playLens: Lens[Model, PlayModel]     = Lens(_.playModel, (m, v) => m.copy(playModel = v))
  val replayLens: Lens[Model, ReplayModel] = Lens(_.replay, (m, v) => m.copy(replay = v))
  val completedLens: Lens[Model, Set[Int]] =
    Lens(
      m => m.completed(m.selectedType),
      (m, v) => m.copy(completed = m.completed + (m.selectedType -> v))
    )
}
