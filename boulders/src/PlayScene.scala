import indigo._
import indigo.scenes._
import Settings.{footerStart, horizontalCenter, verticalMiddle}
import ViewLogic._

/** The main gameplay scene, a grid with a maze level and a player on it. */
object PlayScene extends Scene[StartupData, Model, ViewModel] {
  type SceneModel     = PlayModel
  type SceneViewModel = ViewModel

  val name: SceneName = SceneName ("play scene")
  val modelLens: Lens[Model, PlayModel] = Model.Lenses.playLens
  val viewModelLens: Lens[ViewModel, ViewModel] = Lens.keepLatest
  val eventFilters: EventFilters = EventFilters.Default
  val subSystems: Set[SubSystem] = Set ()

  // The footer instructions and control buttons.
  val instructionLine1 = "Move: Arrow keys"

  def updateModel (context: FrameContext[StartupData], model: PlayModel): GlobalEvent => Outcome[PlayModel] = {
    case KeyboardEvent.KeyUp (Keys.LEFT_ARROW) => Outcome (PlayModel.move (model, -1))
    case KeyboardEvent.KeyUp (Keys.RIGHT_ARROW) => Outcome (PlayModel.move (model, 1))
    case KeyboardEvent.KeyUp (Keys.UP_ARROW) => Outcome (PlayModel.extend (model))
    case KeyboardEvent.KeyUp (Keys.DOWN_ARROW) => Outcome (PlayModel.unextend (model))
    case BackButtonEvent => Outcome (model).addGlobalEvents (SceneEvent.JumpTo (LevelsScene.name))
    case KeyboardEvent.KeyUp (Keys.ESCAPE) => Outcome (model).addGlobalEvents (SceneEvent.JumpTo (LevelsScene.name))
    case InfoButtonEvent => Outcome (model).addGlobalEvents (SceneEvent.JumpTo (InstructionsScene.name))
    case KeyboardEvent.KeyUp (Keys.KEY_I) => Outcome (model).addGlobalEvents (SceneEvent.JumpTo (InstructionsScene.name))
    case ReplayButtonEvent => Outcome (PlayModel.play (model.maze))
    case KeyboardEvent.KeyUp (Keys.KEY_R) => Outcome (PlayModel.play (model.maze))
    case _ => Outcome (model)
  }

  def updateViewModel (context: FrameContext[StartupData], gameModel: PlayModel, viewModel: SceneViewModel): GlobalEvent => Outcome[SceneViewModel] = {
    case FrameTick =>
      viewModel.playButtons.map (_.update (context.inputState.mouse)).sequence
        .map (newButtons => viewModel.copy (playButtons = newButtons))
    case _ => Outcome (viewModel)
  }

  /** The screen either presents the game state if play status is Playing, or a message and control buttons if
   * the player has won or lost. */
  def present (context: FrameContext[StartupData], model: PlayModel, viewModel: SceneViewModel): SceneUpdateFragment = {
    val drawControls = Group (viewModel.playButtons.map (_.draw))
    model.status match {
      case Playing =>
        SceneUpdateFragment.empty
          .addGameLayerNodes (
            Group (planGraphics (model.maze.leftWalls, model.maze, GameAssets.wall)),
            Group (planGraphics (model.maze.floors, model.maze, GameAssets.floor)),
            drawRightWall (model),
            drawCeiling (model),
            place (model.maze.exit, model.maze, GameAssets.exit),
            drawPlayerAndDiamond (model),
            Group (planGraphics (model.boulders, model.maze, GameAssets.boulder)),
            Text (instructionLine1, horizontalCenter, footerStart, 1, GameAssets.fontKey).alignCenter,
            drawControls
          )
      case Lost (message) =>
        SceneUpdateFragment.empty
          .addGameLayerNodes (
            Text (message, horizontalCenter, verticalMiddle, 1, GameAssets.fontKey).alignCenter,
            drawControls
          )
      case Won =>
        SceneUpdateFragment.empty
          .addGameLayerNodes (
            Text ("Success!", horizontalCenter, verticalMiddle, 1, GameAssets.fontKey).alignCenter,
            drawControls
          )
    }
  }

  def drawPlayerAndDiamond (model: PlayModel): Group = {
    val drawPlayer =
      if (!model.extended) List (place (model.position, model.maze, GameAssets.player))
      else List (place (model.position, model.maze, GameAssets.playerTop),
        place (model.position.moveBy (0, 1), model.maze, GameAssets.playerBottom))
    val drawDiamond =
      if (model.diamondTaken) None
      else Some (place (model.maze.diamond, model.maze, GameAssets.diamond))
    Group (drawPlayer ++ drawDiamond)
  }

  /** Draw the walls on the right hand side of the level (not included in the level spec). */
  def drawRightWall (model: PlayModel): Group =
    Group ((0 until model.maze.height).map (y =>
      place (GridPoint (model.maze.width, y), model.maze, GameAssets.wall)).toList)

  /** Draw the ceiling at the top of the level (not included in the level spec). */
  def drawCeiling (model: PlayModel): Group =
    Group ((0 until model.maze.width).map (x =>
      place (GridPoint (x, -1), model.maze, GameAssets.floor)).toList)
}
