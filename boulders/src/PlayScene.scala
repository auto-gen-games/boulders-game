import PlayModel.staticBoulders
import indigo._
import indigo.scenes._
import Settings.{footerStart, cellSize, horizontalCenter, stepTime, verticalMiddle}
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

  // The footer instructions
  val instructionLine1 = "Move: Arrow keys"

  def updateModel (context: FrameContext[StartupData], model: PlayModel): GlobalEvent => Outcome[PlayModel] = {
    case FrameTick =>
      Outcome (PlayModel.updateMovement (model, context.gameTime.running))
    case LeftButtonEvent =>
      Outcome (PlayModel.move (model, -1, context.gameTime.running))
    case KeyboardEvent.KeyDown (Keys.LEFT_ARROW) =>
      Outcome (PlayModel.move (model, -1, context.gameTime.running))
    case RightButtonEvent =>
      Outcome (PlayModel.move (model, 1, context.gameTime.running))
    case KeyboardEvent.KeyDown (Keys.RIGHT_ARROW) =>
      Outcome (PlayModel.move (model, 1, context.gameTime.running))
    case ExtendButtonEvent =>
      if (!model.extended) Outcome (PlayModel.extend (model, context.gameTime.running))
      else Outcome (PlayModel.unextend (model, context.gameTime.running))
    case KeyboardEvent.KeyDown (Keys.UP_ARROW) =>
      Outcome (PlayModel.extend (model, context.gameTime.running))
    case KeyboardEvent.KeyDown (Keys.DOWN_ARROW) =>
      Outcome (PlayModel.unextend (model, context.gameTime.running))
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
      viewModel.playSceneButtons.map (_.update (context.inputState.mouse)).sequence
        .map (newButtons => viewModel.copy (playSceneButtons = newButtons))
    case _ => Outcome (viewModel)
  }

  /** The screen either presents the game state if play status is Playing, or a message and control buttons if
   * the player has won or lost. */
  def present (context: FrameContext[StartupData], model: PlayModel, viewModel: SceneViewModel): SceneUpdateFragment = {
    val drawControls = Group (viewModel.playSceneButtons.map (_.draw))
    if (model.status == Playing || model.playerMoves.nonEmpty || model.boulderMoves.nonEmpty)
        SceneUpdateFragment.empty
          .addGameLayerNodes (
            Group (planGraphics (model.maze.leftWalls, model.maze, GameAssets.wall)),
            Group (planGraphics (model.maze.floors, model.maze, GameAssets.floor)),
            drawRightWall (model),
            drawCeiling (model),
            place (model.maze.exit, model.maze, GameAssets.exit),
            drawPlayer (model, context.gameTime.running),
            drawDiamond (model),
            Group (planGraphics (staticBoulders (model), model.maze, GameAssets.boulder)),
            drawMovingBoulder (model, context.gameTime.running),
            //Text (instructionLine1, horizontalCenter, footerStart, 1, GameAssets.fontKey).alignCenter,
            drawControls
          )
    else {
      val message =
        model.status match {
          case Lost(reason) => reason
          case _ => "Success!"
        }
      SceneUpdateFragment.empty
        .addGameLayerNodes (
          Text (message, horizontalCenter, verticalMiddle, 1, GameAssets.fontKey).alignCenter,
          drawControls
        )
    }
  }

  def drawPlayer (model: PlayModel, time: Seconds): Group = {
    val position = if (model.playerMoves.isEmpty) (model.position) else model.playerMoves.head.from
    val stepCompletion = if (model.playerMoves.isEmpty) 0.0 else (time - model.playerMoves.head.started).toDouble / stepTime.toDouble
    val offsetX = if (model.playerMoves.isEmpty) 0.0 else stepCompletion * model.playerMoves.head.dx
    val offsetY = if (model.playerMoves.isEmpty) 0.0 else stepCompletion * model.playerMoves.head.dy
    val graphic = if (model.playerMoves.isEmpty) GameAssets.player else
      (model.playerMoves.head.dx, model.playerMoves.head.dy) match {
        case (0, -1) => GameAssets.player
        case (0, 1) => GameAssets.playerFalling
        case (-1, 0) => GameAssets.playerLeft
        case (1, 0) => GameAssets.playerRight
      }

    if (!model.extended)
      Group (place (position, model.maze, graphic, offsetX, offsetY))
    else Group (place (position, model.maze, GameAssets.playerTop, offsetX, offsetY),
      place (model.position.moveBy (0, 1), model.maze, GameAssets.playerBottom))
  }

  def drawMovingBoulder (model: PlayModel, time: Seconds): Group =
    if (model.boulderMoves.isEmpty) Group (List.empty)
    else {
      val position = model.boulderMoves.head.from
      val stepCompletion = (time - model.boulderMoves.head.started).toDouble / stepTime.toDouble
      val offsetX = stepCompletion * model.boulderMoves.head.dx
      val offsetY = stepCompletion * model.boulderMoves.head.dy
      Group (place (position, model.maze, GameAssets.boulder, offsetX, offsetY))
    }

  def drawDiamond (model: PlayModel): Group =
    if (model.diamondTaken) Group (List.empty)
    else Group (place (model.maze.diamond, model.maze, GameAssets.diamond))

  /** Draw the walls on the right hand side of the level (not included in the level spec). */
  def drawRightWall (model: PlayModel): Group =
    Group ((0 until model.maze.height).map (y =>
      place (GridPoint (model.maze.width, y), model.maze, GameAssets.wall)).toList)

  /** Draw the ceiling at the top of the level (not included in the level spec). */
  def drawCeiling (model: PlayModel): Group =
    Group ((0 until model.maze.width).map (x =>
      place (GridPoint (x, -1), model.maze, GameAssets.floor)).toList)
}
