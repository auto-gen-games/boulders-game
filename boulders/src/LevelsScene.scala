import indigo._
import indigo.scenes._
import GameAssets.{fontKey, tick}
import Level.levelKinds
import Model.playLens
import PlayModel.play
import Settings._
import ViewLogic._

/** The level selection page. */
object LevelsScene extends Scene[ReferenceData, Model, ViewModel] {
  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName                                = SceneName("levels scene")
  val modelLens: Lens[Model, SceneModel]             = Lens.keepLatest
  val viewModelLens: Lens[ViewModel, SceneViewModel] = Lens.keepLatest
  val eventFilters: EventFilters                     = EventFilters.AllowAll
  val subSystems: Set[SubSystem]                     = Set()

  def updateModel(context: FrameContext[ReferenceData], model: SceneModel): GlobalEvent => Outcome[SceneModel] = {
    case LevelTypeButtonEvent(index) =>
      Outcome(model.copy(selectedType = levelKinds(index)))
    case TutorialButtonEvent =>
      Outcome(
        playLens.set(
          model,
          play(context.startUpData.tutorial(model.selectedType), context.startUpData.guide(model.selectedType))
        )
      )
        .addGlobalEvents(SceneEvent.JumpTo(PlayScene.name))
    case LevelButtonEvent(level) =>
      Outcome(playLens.set(model, play(context.startUpData.levels(model.selectedType)(level))))
        .addGlobalEvents(SceneEvent.JumpTo(PlayScene.name))
    case _ =>
      Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[ReferenceData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] = {
    case FrameTick =>
      viewModel.levelSceneButtons
        .update(model.selectedType, context.inputState.mouse)
        .map(newButtons => viewModel.copy(levelSceneButtons = newButtons))
    case _ =>
      Outcome(viewModel)
  }

  def present(
      context: FrameContext[ReferenceData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment.empty
        .addUiLayerNodes(
          Group(viewModel.levelSceneButtons.draw(model.selectedType)),
          Group(drawTypeTexts),
          Group(drawNumbersOnButtons(context.startUpData.levels(model.selectedType))),
          Group(
            drawTicksOnButtons(context.startUpData.levels(model.selectedType), model.completed(model.selectedType))
          ),
          Text("Tutorial", tutorialLevelPosition.x + cellSize + 12, tutorialLevelPosition.y + 10, 1, fontKey),
          Text("Select level", horizontalCenter, footerStart, 1, fontKey).alignCenter
        )
    )

  def drawTypeTexts: List[Text] =
    levelKinds.map { kind =>
      val position = levelTypeTextPosition(kind)
      Text(kind.name, position.x, position.y, 1, fontKey)
    }

  def drawNumbersOnButtons(levels: Vector[Level]): List[Text] =
    levels.indices.map { n =>
      val position = levelNumberPosition(n)
      Text((n + 1).toString, position.x, position.y, 1, fontKey)
    }.toList :+
      Text("T", tutorialLevelPosition.x + 12, tutorialLevelPosition.y + 10, 1, fontKey)

  def drawTicksOnButtons(levels: Vector[Level], completed: Set[Int]): List[Graphic] =
    levels.indices.flatMap { n =>
      if (completed.contains(n)) Some(tick.moveTo(levelButtonPosition(n)))
      else None
    }.toList
}
