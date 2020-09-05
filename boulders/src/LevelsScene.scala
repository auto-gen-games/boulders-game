import indigo._
import indigo.scenes._
import Model.playLens
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
    case TutorialButtonEvent =>
      Outcome (playLens.set (model, play (model.tutorial, model.guide, model.highlight))).
        addGlobalEvents (SceneEvent.JumpTo (PlayScene.name))
    case LevelButtonEvent (level) =>
      Outcome (playLens.set (model, play (model.levels (level), model.highlight))).
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
        Text ("Tutorial", tutorialLevelPosition.x + cellSize + 12, tutorialLevelPosition.y + 10, 1, GameAssets.fontKey),
        Text ("Select level", horizontalCenter, footerStart, 1, GameAssets.fontKey).alignCenter
      )
  }

  def drawNumbersOnButtons (model: SceneModel): List[Text] =
    model.levels.indices.map { n =>
      val position = levelNumberPosition (n)
      Text ((n + 1).toString, position.x, position.y, 1, GameAssets.fontKey)
    }.toList :+
      Text ("T", tutorialLevelPosition.x + 12, tutorialLevelPosition.y + 10, 1, GameAssets.fontKey)
}
