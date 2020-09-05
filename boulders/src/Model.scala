import indigo.scenes.Lens
import indigo.shared.scenegraph.Sprite

/** The overall model for the game is the set of levels and the current play grid. */
final case class Model (tutorial: Level, levels: Vector[Level], playModel: PlayModel,
                        guide: Vector[TutorialGuideLine], highlight: Sprite)

object Model {
  val playLens: Lens[Model, PlayModel] = Lens (_.playModel, (m, v) => m.copy (playModel = v))
}
