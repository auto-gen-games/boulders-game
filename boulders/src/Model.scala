import indigo.scenes.Lens
import indigo.shared.formats.SpriteAndAnimations

/** The overall model for the game is the set of levels and the current play grid. */
final case class Model (tutorial: Level, levels: Vector[Level], playModel: PlayModel,
                        guide: Vector[TutorialGuideLine], highlight: SpriteAndAnimations)

object Model {
  /** The default model contains no level and the default play grid. */
//  val empty: Model =
//    Model (Level.uninitiated, Vector.empty, PlayModel.uninitiated, Vector.empty)

  val playLens: Lens[Model, PlayModel] = Lens (_.playModel, (m, v) => m.copy (playModel = v))
}
