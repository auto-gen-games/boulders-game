import GameAssets.levelButtonGraphic
import indigo._
import indigo.scenes._
import indigo.shared.events.MouseEvent.Click
import indigoextras.ui._
import Model.Lenses.playLens
import PlayModel.play
import Settings._
import ViewLogic._

/** The level selection page. */
object LevelsScene extends Scene[StartupData, Model, ViewModel] {
  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName = SceneName ("levels scene")
  val modelLens: Lens[Model, Model] = Lens.keepLatest
  val viewModelLens: Lens[ViewModel, ViewModel] = Lens.keepLatest
  val eventFilters: EventFilters = EventFilters.Default
  val subSystems: Set[SubSystem] = Set ()

  def updateModel (context: FrameContext[StartupData], model: SceneModel): GlobalEvent => Outcome[SceneModel] = {
    case LevelButtonEvent (level) =>
      Outcome (playLens.set (model, play (model.levels (level)))).
        addGlobalEvents (SceneEvent.JumpTo (PlayScene.name))
    /*case Click (x, y) =>
      selectLevel (x, y, model.levels.size) match {
        case Some (level) =>
          Outcome (playLens.set (model, play (model.levels (level)))).
            addGlobalEvents (SceneEvent.JumpTo (PlayScene.name))
        case None =>
          Outcome (model)
      }*/
    case _ =>
      Outcome (model)
  }

  def updateViewModel (context: FrameContext[StartupData], model: SceneModel, viewModel: SceneViewModel): GlobalEvent => Outcome[SceneViewModel] = {
    case FrameTick =>
      viewModel.levelButtons.map (_.update (context.inputState.mouse)).
        foldLeft (Outcome[List[Button]] (List.empty)) {
          (listOutcome, buttonOutcome) => listOutcome.merge (buttonOutcome)((list, button) => list :+ button)
        }.map (newButtons => viewModel.copy (levelButtons = newButtons))
    case _ =>
      Outcome (viewModel)
  }

  def present (context: FrameContext[StartupData], model: SceneModel, viewModel: SceneViewModel): SceneUpdateFragment = {
    SceneUpdateFragment.empty
      .addUiLayerNodes (
        //drawLevelBoxes (model),
        Group (viewModel.levelButtons.map (_.draw)), Group (drawNumbersOnButtons (model)),
        Text ("Select level", horizontalCenter, footerStart, 1, GameAssets.fontKey).alignCenter
      )
  }

  def drawNumbersOnButtons (model: SceneModel): List[Text] =
    model.levels.indices.map { n =>
      Text (spacedNumber (n + 1), (n % levelsPerRow) * levelBoxSize + leftMargin + numberLeftPos (n + 1),
        (n / levelsPerRow) * levelBoxSize + headerHeight + 16, 1, GameAssets.fontKey).scaleBy (3, 3)
    }.toList

  /*
  def selectLevel (x: Int, y: Int, numberOfLevels: Int): Option[Int] =
    (0 until numberOfLevels).find { level =>
      x >= levelBoxLeft (level) && x < levelBoxLeft (level) + levelBoxSize &&
        y >= levelBoxTop (level) && y < levelBoxTop (level) + levelBoxSize
    }
*/
}
