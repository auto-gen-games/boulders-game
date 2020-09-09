import indigo.scenes.Lens
import indigo.shared.formats.SpriteAndAnimations
import indigo.shared.scenegraph.Sprite

/** The overall model for the game is the current play grid. */
final case class Model(playModel: PlayModel)

object Model {
  val playLens: Lens[Model, PlayModel] = Lens(_.playModel, (m, v) => m.copy(playModel = v))
}
