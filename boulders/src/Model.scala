import indigo.scenes.Lens

/** The overall model for the game is the set of levels and the current play grid. */
final case class Model (tutorial: Level, levels: Vector[Level], playModel: PlayModel)

object Model {
  /** The default model contains no level and the default play grid. */
  val empty: Model =
    Model (Level.uninitiated, Vector.empty, PlayModel.uninitiated)

  object Lenses {
    val playLens: Lens[Model, PlayModel] = Lens (_.playModel, (m, v) => m.copy (playModel = v))
  }


}
