import indigo._
import indigo.scenes._
import indigo.shared.events.MouseEvent.Click
import Settings._

import scala.annotation.tailrec

/** The opening screen, with a big title and an instruction to click. */
object StartScene extends Scene[ReferenceData, Model, ViewModel] {
  type SceneModel     = Model
  type SceneViewModel = Unit

  val name: SceneName                                = SceneName("start scene")
  val modelLens: Lens[Model, SceneModel]             = Lens.keepLatest
  val viewModelLens: Lens[ViewModel, SceneViewModel] = Lens.fixed(())
  val eventFilters: EventFilters                     = EventFilters.Default.withViewModelFilter(_ => None)
  val subSystems: Set[SubSystem]                     = Set()

  def updateModel(context: FrameContext[ReferenceData], model: SceneModel): GlobalEvent => Outcome[SceneModel] = {
    case Click(_, _) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(LevelsScene.name))
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(LevelsScene.name))
    case _ =>
      Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[ReferenceData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[Unit] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[ReferenceData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): SceneUpdateFragment = {
    val horizontalCenter: Int = (Settings.viewportWidth / Settings.magnificationLevel) / 2
    val verticalMiddle: Int   = (Settings.viewportHeight / Settings.magnificationLevel) / 2

    SceneUpdateFragment.empty
      .addUiLayerNodes(
        List(
          Group(bigTitle("Boulders", leftMargin, false, List.empty)),
          Text("by Simon Miles", horizontalCenter, verticalMiddle, 1, GameAssets.fontKey).alignCenter,
          Text("Click anywhere to play", horizontalCenter, verticalMiddle + 20, 1, GameAssets.fontKey).alignCenter
        )
      )
  }

  /** Draw the title in big letters, alternating light and dark grey oscillating up and down. */
  @tailrec
  def bigTitle(text: String, left: Int, indented: Boolean, already: List[Renderable]): List[Renderable] = {
    val tint   = if (indented) 0.7 else 0.3
    val indent = if (indented) 10 else 0
    if (text.isEmpty) already
    else
      bigTitle(
        text.tail,
        left + 40,
        indented = !indented,
        already :+ Text(text.substring(0, 1), left, headerHeight + indent, 1, GameAssets.fontKey)
          .scaleBy(4, 4)
          .withTint(tint, tint, tint, 1.0)
      )
  }
}
