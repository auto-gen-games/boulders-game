import GameAssets.{boulder, fontKey}
import Level.flipKind
import Model.{completedLens, replayLens}
import PlayModel._
import Settings._
import ViewLogic._
import indigo._
import indigo.scenes._
import indigo.shared.events.MouseEvent.Click

/** The main gameplay scene, a grid with a maze level and a player on it. */
object PlayScene extends Scene[ReferenceData, Model, ViewModel] {
  type SceneModel     = PlayModel
  type SceneViewModel = ViewModel

  val name: SceneName                                = SceneName("play scene")
  val modelLens: Lens[Model, SceneModel]             = Model.playLens
  val viewModelLens: Lens[ViewModel, SceneViewModel] = Lens.keepLatest
  val eventFilters: EventFilters                     = EventFilters.AllowAll
  val subSystems: Set[SubSystem]                     = Set()

  def updateModel(context: FrameContext[ReferenceData], model: SceneModel): GlobalEvent => Outcome[SceneModel] = {
    case FrameTick =>
      checkSuccess(Outcome(updateMovement(model, context.gameTime.running)))
    case Click(_, _) if enabled(model).contains(SpaceContinueEvent) =>
      Outcome(stepTutorial(model))
    case KeyboardEvent.KeyUp(Key.SPACE) if enabled(model).contains(SpaceContinueEvent) =>
      Outcome(stepTutorial(model))
    case LeftButtonEvent if enabled(model).contains(LeftButtonEvent) =>
      addBoulderRoll(Outcome(stepTutorial(move(model, -1, context.gameTime.running))))
    case KeyboardEvent.KeyDown(Key.LEFT_ARROW) if enabled(model).contains(LeftButtonEvent) =>
      addBoulderRoll(Outcome(stepTutorial(move(model, -1, context.gameTime.running))))
    case RightButtonEvent if enabled(model).contains(RightButtonEvent) =>
      addBoulderRoll(Outcome(stepTutorial(move(model, 1, context.gameTime.running))))
    case KeyboardEvent.KeyDown(Key.RIGHT_ARROW) if enabled(model).contains(RightButtonEvent) =>
      addBoulderRoll(Outcome(stepTutorial(move(model, 1, context.gameTime.running))))
    case ExtendButtonEvent if enabled(model).contains(ExtendButtonEvent) =>
      if (!model.extended) Outcome(stepTutorial(extend(model, context.gameTime.running)))
      else Outcome(stepTutorial(unextend(model, context.gameTime.running)))
    case KeyboardEvent.KeyDown(Key.UP_ARROW) if enabled(model).contains(ExtendButtonEvent) =>
      Outcome(stepTutorial(extend(model, context.gameTime.running)))
    case KeyboardEvent.KeyDown(Key.DOWN_ARROW) if enabled(model).contains(ExtendButtonEvent) =>
      Outcome(stepTutorial(unextend(model, context.gameTime.running)))
    case FlipButtonEvent if enabled(model).contains(FlipButtonEvent) =>
      Outcome(stepTutorial(flip(model, context.gameTime.running)))
    case KeyboardEvent.KeyDown(Key.KEY_F) if enabled(model).contains(FlipButtonEvent) =>
      Outcome(stepTutorial(flip(model, context.gameTime.running)))
    case BackButtonEvent if enabled(model).contains(BackButtonEvent) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(LevelsScene.name))
    case KeyboardEvent.KeyUp(Key.ESCAPE) if enabled(model).contains(BackButtonEvent) =>
      Outcome(model).addGlobalEvents(SceneEvent.JumpTo(LevelsScene.name))
    case ReplayButtonEvent if enabled(model).contains(ReplayButtonEvent) =>
      Outcome(stepTutorial(play(context.startUpData.getLevel(model.maze.kind, model.maze.number), model.tutorial)))
    case KeyboardEvent.KeyUp(Key.KEY_R) if enabled(model).contains(ReplayButtonEvent) =>
      Outcome(stepTutorial(play(context.startUpData.getLevel(model.maze.kind, model.maze.number), model.tutorial)))
    case KeyboardEvent.KeyDown(Key.KEY_S)
        if context.keyboard.keysDown.contains(Key.CTRL) && context.keyboard.keysDown.contains(Key.SHIFT) =>
      Solve.aStarSearch(model) match {
        case None => Outcome(model).addGlobalEvents(PlaySound(AssetName("rolling"), Volume.Max))
        case Some(solution) =>
          Outcome(model).addGlobalEvents(
            //ModifyEvent[Model, ReplayModel](replayLens, _ => ReplayModel(model, solution, context.gameTime.running)),
            ReplayEvent(ReplayModel(model, solution, context.gameTime.running)),
            SceneEvent.JumpTo(ReplayScene.name)
          )
      }
    case _ => Outcome(model)
  }

  def checkSuccess(outcome: Outcome[PlayModel]): Outcome[PlayModel] =
    if (outcome.unsafeGet.status == Won && outcome.unsafeGet.playerMoves.isEmpty)
      outcome.addGlobalEvents(
        //ModifyEvent[Model, Set[Int]](completedLens, _ + outcome.unsafeGet.maze.number),
        SolvedLevel(outcome.unsafeGet.maze.kind, outcome.unsafeGet.maze.number),
        SceneEvent.JumpTo(SuccessScene.name)
      )
    else outcome

  def addBoulderRoll(outcome: Outcome[PlayModel]): Outcome[PlayModel] =
    if (outcome.unsafeGet.boulderMoves.isEmpty) outcome
    else outcome.addGlobalEvents(PlaySound(AssetName("rolling"), Volume.Max))

  def updateViewModel(
      context: FrameContext[ReferenceData],
      gameModel: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] = {
    case FrameTick =>
      viewModel.playSceneButtons
        .update(gameModel.maze.kind, context.inputState.mouse)
        .map(newButtons => viewModel.copy(playSceneButtons = newButtons))
    case _ => Outcome(viewModel)
  }

  /** The screen either presents the game state if play status is Playing, or a message and control buttons if
    * the player has won or lost.
    */
  def present(
      context: FrameContext[ReferenceData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] = {
    val drawControls =
      Group(
        viewModel.playSceneButtons.draw(model.maze.kind) :+
          Text("ESC", backBoxPosition.x + 2, backBoxPosition.y + cellSize, 1, fontKey) :+
          Text("R", replayBoxPosition.x + 12, replayBoxPosition.y + cellSize, 1, fontKey)
      )
    val base =
      if (model.status == Playing || model.playerMoves.nonEmpty || model.boulderMoves.nonEmpty)
        SceneUpdateFragment.empty
          .addGameLayerNodes(
            Group(planGraphics(model.maze.leftWalls, model.maze, GameAssets.wall)),
            Group(planGraphics(model.maze.floors, model.maze, GameAssets.floor)),
            drawRightWall(model),
            drawCeiling(model),
            place(model.maze.exit, model.maze, GameAssets.exit),
            drawPlayer(model, context.gameTime.running),
            drawDiamond(model),
            Group(planGraphics(staticBoulders(model), model.maze, GameAssets.boulder)),
            drawMovingBoulders(model, context.gameTime.running),
            Text(
              controlInstructions(model),
              horizontalCenter,
              footerStart + cellSize,
              1,
              GameAssets.fontKey
            ).alignCenter,
            drawControls
          )
      else
        model.status match {
          case Lost(reason) =>
            SceneUpdateFragment.empty
              .addGameLayerNodes(
                Text(reason, horizontalCenter, verticalMiddle, 1, GameAssets.fontKey).alignCenter,
                drawControls
              )
          case _ => SceneUpdateFragment.empty
        }
    val fragment =
      if (model.tutorial.isEmpty)
        base
          .addGameLayerNodes(
            Text(s"Level ${model.maze.number + 1}", horizontalCenter, headerHeight, 1, GameAssets.fontKey).alignCenter
          )
      else
        base
          .addGameLayerNodes(placeIndicator(model.tutorial.head.indicator, model, context.startUpData.highlight).toList)
          .addGameLayerNodes(
            GameAssets.tutorialBox.moveTo(tutorialGuideBoxPosition),
            Text(
              model.tutorial.head.text1,
              tutorialGuideBoxPosition.x + 5,
              tutorialGuideBoxPosition.y + 10,
              1,
              GameAssets.fontKey
            ),
            Text(
              model.tutorial.head.text2,
              tutorialGuideBoxPosition.x + 5,
              tutorialGuideBoxPosition.y + 25,
              1,
              GameAssets.fontKey
            ),
            Text(
              model.tutorial.head.continue,
              tutorialGuideBoxPosition.x + 5,
              tutorialGuideBoxPosition.y + 40,
              1,
              GameAssets.fontKey
            )
          )
    Outcome(fragment)
  }

  // The footer instructions
  def controlInstructions(model: PlayModel): String =
    model.maze.kind match {
      case a if a == flipKind => "Arrow keys and F / buttons above"
      case _                  => "Arrow keys / buttons above"
    }

  def drawPlayer(model: PlayModel, time: Seconds): Group = {
    val position = if (model.playerMoves.isEmpty) (model.position) else model.playerMoves.head.from
    val stepCompletion =
      if (model.playerMoves.isEmpty) 0.0 else (time - model.playerMoves.head.started).toDouble / stepTime.toDouble
    val offsetX = if (model.playerMoves.isEmpty) 0.0 else stepCompletion * model.playerMoves.head.dx
    val offsetY = if (model.playerMoves.isEmpty) 0.0 else stepCompletion * model.playerMoves.head.dy
    val graphic = if (model.playerMoves.isEmpty) GameAssets.player else model.playerMoves.head.image

    if (!model.extended)
      Group(place(position, model.maze, graphic, offsetX, offsetY))
    else
      Group(
        place(position, model.maze, GameAssets.playerTop, offsetX, offsetY),
        place(model.position.moveBy(0, 1), model.maze, GameAssets.playerBottom)
      )
  }

  def drawMovingBoulders(model: PlayModel, time: Seconds): Group =
    Group(model.boulderMoves.map { mover =>
      val position       = mover.head.from
      val stepCompletion = (time - mover.head.started).toDouble / stepTime.toDouble
      val offsetX        = stepCompletion * mover.head.dx
      val offsetY        = stepCompletion * mover.head.dy

      place(
        position,
        model.maze,
        boulder,
        offsetX,
        offsetY
      )
    }.toList)

  def drawDiamond(model: PlayModel): Group =
    if (model.diamondTaken && !reachingDiamond(model)) Group(List.empty)
    else Group(place(model.maze.diamond, model.maze, GameAssets.diamond))

  /** Draw the walls on the right hand side of the level (not included in the level spec). */
  def drawRightWall(model: PlayModel): Group =
    Group(
      (0 until model.maze.height).map(y => place(GridPoint(model.maze.width, y), model.maze, GameAssets.wall)).toList
    )

  /** Draw the ceiling at the top of the level (not included in the level spec). */
  def drawCeiling(model: PlayModel): Group =
    Group((0 until model.maze.width).map(x => place(GridPoint(x, -1), model.maze, GameAssets.floor)).toList)
}
