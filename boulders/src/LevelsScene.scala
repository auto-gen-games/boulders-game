import indigo._
import indigo.scenes._
import Model.Lenses.playLens
import PlayModel.play
import Settings._
import ViewLogic._

/** The level selection page. */
object LevelsScene extends Scene[StartupData, Model, ViewModel] {
  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName = SceneName ("levels scene")
  val modelLens: Lens[Model, SceneModel] = Lens.keepLatest
  val viewModelLens: Lens[ViewModel, SceneViewModel] = Lens.keepLatest
  val eventFilters: EventFilters = EventFilters.Default
  val subSystems: Set[SubSystem] = Set ()

  def updateModel (context: FrameContext[StartupData], model: SceneModel): GlobalEvent => Outcome[SceneModel] = {
    case LevelButtonEvent (level) =>
      Outcome (playLens.set (model, play (model.levels (level)))).
        addGlobalEvents (SceneEvent.JumpTo (PlayScene.name))
    case _ =>
      Outcome (model)
  }

  def updateViewModel (context: FrameContext[StartupData], model: SceneModel, viewModel: SceneViewModel): GlobalEvent => Outcome[SceneViewModel] = {
    case FrameTick =>
      viewModel.levelSceneButtons.map (_.update (context.inputState.mouse)).sequence
        .map (newButtons => viewModel.copy (levelSceneButtons = newButtons))
    case _ =>
      Outcome (viewModel)
  }

  def present (context: FrameContext[StartupData], model: SceneModel, viewModel: SceneViewModel): SceneUpdateFragment = {
    SceneUpdateFragment.empty
      .addUiLayerNodes (
        Group (viewModel.levelSceneButtons.map (_.draw)), Group (drawNumbersOnButtons (model)),
        Text ("Select level", horizontalCenter, footerStart, 1, GameAssets.fontKey).alignCenter
      )
  }

  def drawNumbersOnButtons (model: SceneModel): List[Text] =
    model.levels.indices.map { n =>
      Text ((n + 1).toString, (n % levelsPerRow) * levelBoxSize + leftMargin + numberLeftPos (n + 1),
        (n / levelsPerRow) * levelBoxSize + headerHeight + 10, 1, GameAssets.fontKey).scaleBy (1, 1)
    }.toList
}
