import PlayModel._
import Settings._
import ViewLogic._
import indigo._
import indigo.scenes._

/** The main gameplay scene, a grid with a maze level and a player on it. */
object ReplayScene extends Scene[ReferenceData, Model, ViewModel] {
  type SceneModel     = ReplayModel
  type SceneViewModel = ViewModel

  val name: SceneName                                = SceneName("replay scene")
  val modelLens: Lens[Model, SceneModel]             = Model.replayLens
  val viewModelLens: Lens[ViewModel, SceneViewModel] = Lens.keepLatest
  val eventFilters: EventFilters                     = EventFilters.Default
  val subSystems: Set[SubSystem]                     = Set()

  def updateModel(context: FrameContext[ReferenceData], model: ReplayModel): GlobalEvent => Outcome[ReplayModel] = {
    case FrameTick =>
      val now    = context.gameTime.running
      val fallen = updateMovement(model.state, now)
      if (context.gameTime.running >= model.lastStepped + replayStepTime)
        model.steps match {
          case next :: remaining =>
            Outcome(ReplayModel(next.enact(fallen, now).getOrElse(fallen), remaining, now))
          case _ =>
            Outcome(model.copy(state = fallen)).addGlobalEvents(SceneEvent.JumpTo(SuccessScene.name))
        }
      else
        Outcome(model.copy(state = fallen))
    case BackButtonEvent =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(LevelsScene.name))
    case KeyboardEvent.KeyUp(Key.ESCAPE) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(LevelsScene.name))
    case _ => Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[ReferenceData],
      gameModel: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] = {
    case FrameTick =>
      viewModel.replaySceneButton
        .update(context.inputState.mouse)
        .map(updated => viewModel.copy(replaySceneButton = updated))
    case _ => Outcome(viewModel)
  }

  /** The screen either presents the game state if play status is Playing, or a message and control buttons if
    * the player has won or lost.
    */
  def present(
      context: FrameContext[ReferenceData],
      model: ReplayModel,
      viewModel: ViewModel
  ): SceneUpdateFragment =
    SceneUpdateFragment.empty
      .addGameLayerNodes(
        Group(planGraphics(model.state.maze.leftWalls, model.state.maze, GameAssets.wall)),
        Group(planGraphics(model.state.maze.floors, model.state.maze, GameAssets.floor)),
        PlayScene.drawRightWall(model.state),
        PlayScene.drawCeiling(model.state),
        place(model.state.maze.exit, model.state.maze, GameAssets.exit),
        PlayScene.drawPlayer(model.state, context.gameTime.running),
        PlayScene.drawDiamond(model.state),
        Group(planGraphics(staticBoulders(model.state), model.state.maze, GameAssets.boulder)),
        PlayScene.drawMovingBoulders(model.state, context.gameTime.running),
        viewModel.replaySceneButton.draw
      )
}
