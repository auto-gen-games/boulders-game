import indigo.scenes.Lens

/** The overall model for the game is the current play grid and the set of levels completed. */
final case class Model(selectedType: String, playModel: PlayModel, completed: Set[Int])

object Model {
  val playLens: Lens[Model, PlayModel]     = Lens(_.playModel, (m, v) => m.copy(playModel = v))
  val completedLens: Lens[Model, Set[Int]] = Lens(_.completed, (m, v) => m.copy(completed = v))
}
