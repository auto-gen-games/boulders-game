import Model.playLens
import PlayModel._
import Settings._
import indigo._
import indigo.scenes._

object SuccessScene extends Scene[ReferenceData, Model, ViewModel] {
  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName                                = SceneName("success scene")
  val modelLens: Lens[Model, SceneModel]             = Lens.keepLatest
  val viewModelLens: Lens[ViewModel, SceneViewModel] = Lens.keepLatest
  val eventFilters: EventFilters                     = EventFilters.Default
  val subSystems: Set[SubSystem]                     = Set.empty

  def updateModel(context: FrameContext[ReferenceData], model: SceneModel): GlobalEvent => Outcome[SceneModel] = {
    case BackButtonEvent =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(LevelsScene.name))
    case KeyboardEvent.KeyUp(Keys.ESCAPE) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(LevelsScene.name))
    case ReplayButtonEvent =>
      Outcome(playLens.set(model, play(model.playModel.maze)))
    case KeyboardEvent.KeyUp(Keys.KEY_R) =>
      Outcome(playLens.set(model, play(model.playModel.maze)))
    case ForwardButtonEvent =>
      Outcome(
        playLens.set(
          model,
          play(
            context.startUpData.levels((model.playModel.maze.number + 1) % context.startUpData.levels.size)
          )
        )
      )
        .addGlobalEvents(SceneEvent.JumpTo(PlayScene.name))
    case KeyboardEvent.KeyUp(Keys.SPACE) =>
      Outcome(
        playLens.set(
          model,
          play(
            context.startUpData.levels((model.playModel.maze.number + 1) % context.startUpData.levels.size)
          )
        )
      )
        .addGlobalEvents(SceneEvent.JumpTo(PlayScene.name))
    case _ => Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[ReferenceData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] = {
    case FrameTick =>
      viewModel.successSceneButtons
        .map(_.update(context.inputState.mouse))
        .sequence
        .map(newButtons => viewModel.copy(successSceneButtons = newButtons))
    case _ => Outcome(viewModel)
  }

  override def present(
      context: FrameContext[ReferenceData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        Text("Success!!", horizontalCenter, verticalMiddle, 1, GameAssets.fontKey).alignCenter,
        Group(viewModel.successSceneButtons.map(_.draw))
      )
}
