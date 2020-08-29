import indigo._
import indigo.scenes._
import Settings._

/** The instructions page, a static scene with a button to return to the game. */
object InstructionsScene extends Scene[StartupData, Model, ViewModel] {
  type SceneModel     = Unit
  type SceneViewModel = ViewModel

  val name: SceneName = SceneName ("instructions scene")
  val modelLens: Lens[Model, SceneModel] = Lens.fixed (())
  val viewModelLens: Lens[ViewModel, SceneViewModel] = Lens.keepLatest
  val eventFilters: EventFilters = EventFilters.Default
  val subSystems: Set[SubSystem] = Set ()
  val bufferSize = 5

  val backBoxPosition = Point (horizontalCenter - gridSquareSize / 2, footerStart + 15)

  def updateModel (context: FrameContext[StartupData], model: SceneModel): GlobalEvent => Outcome[SceneModel] = {
    case BackButtonEvent =>
      Outcome (model).addGlobalEvents (SceneEvent.JumpTo (PlayScene.name))
    case KeyboardEvent.KeyUp (Keys.ESCAPE) =>
      Outcome (model).addGlobalEvents (SceneEvent.JumpTo (PlayScene.name))
    case _ =>
      Outcome (model)
  }

  def updateViewModel (context: FrameContext[StartupData], model: SceneModel,
                       viewModel: SceneViewModel): GlobalEvent => Outcome[SceneViewModel] = {
    case FrameTick =>
      viewModel.instructionBackButton.update (context.inputState.mouse)
        .map (newButton => viewModel.copy (instructionBackButton = newButton))
    case _ => Outcome (viewModel)
  }

  def present (context: FrameContext[StartupData], model: SceneModel, viewModel: SceneViewModel): SceneUpdateFragment = {
    SceneUpdateFragment.empty
      .addUiLayerNodes (List (
        textLine (GameAssets.player, "This is you", 0),
        textLine (GameAssets.diamond, "Collect the diamond...", 1),
        textLine (GameAssets.exit, "...then leave by the exit", 2),
        textLine (GameAssets.boulder, "You can push boulders", 3),
        textLine (GameAssets.boulder, "unless anothers on top", 4),
        textLine (GameAssets.diamond, "Boulders squash diamond/exit", 5),
        viewModel.instructionBackButton.draw
      ))
  }

  def textLine (image: Graphic, text: String, lineNumber: Int): Group =
    Group (image.moveTo (leftMargin, headerHeight + (gridSquareSize + bufferSize) * lineNumber),
      Text (text, leftMargin + gridSquareSize + bufferSize,
        headerHeight + (gridSquareSize + bufferSize) * lineNumber, 1, GameAssets.fontKey))
}
