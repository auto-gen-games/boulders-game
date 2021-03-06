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
  val eventFilters: EventFilters                     = EventFilters.AllowAll
  val subSystems: Set[SubSystem]                     = Set.empty

  def updateModel(context: FrameContext[ReferenceData], model: SceneModel): GlobalEvent => Outcome[SceneModel] = {
    case BackButtonEvent =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(LevelsScene.name))
    case KeyboardEvent.KeyUp(Key.ESCAPE) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(LevelsScene.name))
    case ReplayButtonEvent =>
      Outcome(
        playLens.set(
          model,
          play(
            context.startUpData.levels(model.playModel.maze.kind)(model.playModel.maze.number),
            model.playModel.tutorial
          )
        )
      ).addGlobalEvents(SceneEvent.JumpTo(PlayScene.name))
    case KeyboardEvent.KeyUp(Key.KEY_R) =>
      Outcome(
        playLens.set(
          model,
          play(
            context.startUpData.levels(model.playModel.maze.kind)(model.playModel.maze.number),
            model.playModel.tutorial
          )
        )
      ).addGlobalEvents(SceneEvent.JumpTo(PlayScene.name))
    case ForwardButtonEvent =>
      Outcome(goToNextLevel(model, context.startUpData))
        .addGlobalEvents(SceneEvent.JumpTo(PlayScene.name))
    case KeyboardEvent.KeyUp(Key.SPACE) =>
      Outcome(goToNextLevel(model, context.startUpData))
        .addGlobalEvents(SceneEvent.JumpTo(PlayScene.name))
    case _ => Outcome(model)
  }

  def goToNextLevel(model: Model, referenceData: ReferenceData): Model =
    playLens.set(
      model,
      play(
        referenceData.levels(model.playModel.maze.kind)(
          (model.playModel.maze.number + 1) % referenceData.levels(model.playModel.maze.kind).size
        )
      )
    )

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
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addGameLayerNodes(
          Text("Success!!", horizontalCenter, verticalMiddle, 1, GameAssets.fontKey).alignCenter,
          Group(viewModel.successSceneButtons.map(_.draw))
        )
    )
}
