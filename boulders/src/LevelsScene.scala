import indigo._
import indigo.scenes._
import indigo.shared.events.MouseEvent.Click
import Model.Lenses.playLens
import PlayModel.play
import Settings._

/** The level selection page. */
object LevelsScene extends Scene[StartupData, Model, Unit] {
  type SceneModel     = Model
  type SceneViewModel = Unit

  val name: SceneName = SceneName ("levels scene")
  val modelLens: Lens[Model, Model] = Lens.keepLatest
  val viewModelLens: Lens[Unit, Unit] = Lens.keepLatest
  val eventFilters: EventFilters = EventFilters.Default.withViewModelFilter (_ => None)
  val subSystems: Set[SubSystem] = Set ()

  def updateModel (context: FrameContext[StartupData], model: SceneModel): GlobalEvent => Outcome[SceneModel] = {
    case Click (x, y) =>
      selectLevel (x, y, model.levels.size) match {
        case Some (level) =>
          Outcome (playLens.set (model, play (model.levels (level)))).
            addGlobalEvents (SceneEvent.JumpTo (PlayScene.name))
        case None =>
          Outcome (model)
      }
    case _ =>
      Outcome (model)
  }

  def updateViewModel (context: FrameContext[StartupData], model: SceneModel, viewModel: SceneViewModel): GlobalEvent => Outcome[SceneViewModel] =
    _ => Outcome (viewModel)

  def present (context: FrameContext[StartupData], model: SceneModel, viewModel: SceneViewModel): SceneUpdateFragment = {
    SceneUpdateFragment.empty
      .addUiLayerNodes (
        drawLevelBoxes (model),
        Text ("Select level", horizontalCenter, footerStart, 1, GameAssets.fontKey).alignCenter
      )
  }

  def drawLevelBoxes (model: SceneModel): Group = {
    /** Add spaces between digits else they overlap when scaled. */
    def spacedNumber (n: Int): String =
      n.toString.map (_ + " ").mkString.trim
    /** Indent number to move to middle of square, less so for longer numbers */
    def leftPos (n: Int): Int =
      if (n < 10) 16 else 8
    Group (model.levels.indices.map { n =>
      GameAssets.levelNumber.moveTo (levelBoxLeft (n), levelBoxTop (n)).
        withTint (1.0, 0.0, 0.0, (Level.area (model.levels (n)) - 36) / 60.0)
    }.toList ++
      model.levels.indices.map { n =>
        Text (spacedNumber (n + 1), (n % levelsPerRow) * levelBoxSize + leftMargin + leftPos (n + 1),
          (n / levelsPerRow) * levelBoxSize + headerHeight + 16, 1, GameAssets.fontKey).scaleBy (3, 3)
      }.toList)
  }

  def levelBoxLeft (level: Int): Int =
    (level % levelsPerRow) * levelBoxSize + leftMargin

  def levelBoxTop (level: Int): Int =
    (level / levelsPerRow) * levelBoxSize + headerHeight

  def selectLevel (x: Int, y: Int, numberOfLevels: Int): Option[Int] =
    (0 until numberOfLevels).find { level =>
      x >= levelBoxLeft (level) && x < levelBoxLeft (level) + levelBoxSize &&
        y >= levelBoxTop (level) && y < levelBoxTop (level) + levelBoxSize
    }

}
