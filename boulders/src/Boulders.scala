import GameAssets._
import Level.{baseKind, flipKind, levelKinds}
import PlayModel.play
import Settings._
import ViewLogic._
import indigo._
import indigo.scenes.{Scene, SceneName}
import indigoextras.ui.{Button, RadioButtonGroup}

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object Boulders extends IndigoGame[GameViewport, ReferenceData, Model, ViewModel] {
  val eventFilters: EventFilters = EventFilters.AllowAll

  /** The initial game model starts with the tutorial level, and with no levels marked completed. */
  def initialModel(startupData: ReferenceData): Outcome[Model] = {
    val completedLevels = levelKinds.map(kind => (kind -> Set[Int]())).toMap
    val defaultPlay     = play(startupData.tutorial(baseKind), startupData.guide(baseKind))
    Outcome(Model(baseKind, defaultPlay, completedLevels, ReplayModel(defaultPlay, List.empty, Seconds.zero)))
  }

  /** Copied from the Snake demo, loading assets and the viewport. */
  def boot(flags: Map[String, String]): Outcome[BootResult[GameViewport]] = {
    val assetPath: String = flags.getOrElse("baseUrl", "")
    val config = GameConfig(
      viewport = GameViewport(Settings.viewportWidth, Settings.viewportHeight),
      frameRate = 60,
      clearColor = RGBA.Black,
      magnification = Settings.magnificationLevel
    )

    Outcome(
      BootResult(config, config.viewport)
        .withAssets(GameAssets.assets(assetPath))
        .withFonts(GameAssets.fontInfo)
    )
  }

  /** Three scenes: start screen, levels choice, game play screen */
  def scenes(bootData: GameViewport): NonEmptyList[Scene[ReferenceData, Model, ViewModel]] =
    NonEmptyList(StartScene, LevelsScene, PlayScene, SuccessScene, ReplayScene)

  def initialScene(bootData: GameViewport): Option[SceneName] =
    Some(StartScene.name)

  /** Load and decode the level specs on startup. */
  def setup(
      bootData: GameViewport,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[ReferenceData]] = {
    def mapKindAssets[Result](
        kindMap: Map[LevelKind, AssetName],
        toValue: (LevelKind, String) => Result
    ): Map[LevelKind, Result] =
      kindMap
        .map { case (kind, name) => kind -> assetCollection.findTextDataByName(name).map(toValue(kind, _)) }
        .filter(_._2.isDefined)
        .view
        .mapValues(_.get)
        .toMap

    val specs     = mapKindAssets(levelSpecs, Level.decodeLevels)
    val tutorials = mapKindAssets(tutorialSpecs, Level.levelFromCode(_, -1, _))
    val guides    = mapKindAssets(tutorialGuides, TutorialGuideLine.loadGuide)
    val result = for {
      spriteAnim <- GameAssets.loadAnimation(assetCollection, dice, highlightJSON, highlightBox, Depth(0))
    } yield Startup
      .Success(ReferenceData(bootData, tutorials, specs, guides, spriteAnim.sprite))
      .addAnimations(spriteAnim.animations)

    Outcome(result.getOrElse(Startup.Failure("Could not load or parse game data")))
  }

  def initialViewModel(startupData: ReferenceData, model: Model): Outcome[ViewModel] = {
    val leftButton: Button =
      createButton("control-arrows", leftControlPosition, LeftButtonEvent, row = 0)
    val extendButton: Button =
      createButton("control-arrows", extendControlPosition, ExtendButtonEvent, row = 1)
    val rightButton: Button =
      createButton("control-arrows", rightControlPosition, RightButtonEvent, row = 2)
    val flipButton: Button =
      createButton("control-arrows", flipControlPosition, FlipButtonEvent, row = 3)
    val baseControls: List[Button] =
      List(leftButton, extendButton, rightButton)
    val flipControls: List[Button] =
      List(leftButton, extendButton, rightButton, flipButton)
    val backButton: Button =
      createButton("back-button", backBoxPosition, BackButtonEvent)
    val forwardButton: Button =
      createButton("back-button", forwardBoxPosition, ForwardButtonEvent, flipped = true)
    val replayButton: Button =
      createButton("replay-button", replayBoxPosition, ReplayButtonEvent)
    val levelNumberButtons: Map[LevelKind, List[Button]] =
      levelKinds.map(kind => kind -> levelButtons(kind, startupData.levels(kind).size)).toMap
    val tutorialButtons: Map[LevelKind, Button] =
      levelKinds
        .map(kind =>
          kind -> createButton(
            "button-base",
            tutorialLevelPosition,
            TutorialButtonEvent,
            tint = Some(levelKindTints(kind))
          )
        )
        .toMap
    val kindButtons: RadioButtonGroup =
      gameTypeButton

    val levelSceneButtons: LevelsSceneButtons =
      LevelsSceneButtons(levelNumberButtons, tutorialButtons, kindButtons)
    val playSceneButtons: PlaySceneButtons =
      PlaySceneButtons(Map(baseKind -> baseControls, flipKind -> flipControls), List(backButton, replayButton))
    val successSceneButtons: List[Button] =
      List(forwardButton, backButton, replayButton)

    Outcome(ViewModel(levelSceneButtons, playSceneButtons, successSceneButtons, backButton))
  }

  def updateModel(context: FrameContext[ReferenceData], model: Model): GlobalEvent => Outcome[Model] = {
    case SolvedLevel(kind, number) =>
      Outcome(model.copy(completed = model.completed + (kind -> (model.completed(kind) + number))))
    case ReplayEvent(replayModel) =>
      Outcome(model.copy(replay = replayModel))
    case _ => Outcome(model)
  }

  def updateViewModel(
      context: FrameContext[ReferenceData],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    _ => Outcome(viewModel)

  def present(
      context: FrameContext[ReferenceData],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] = Outcome(SceneUpdateFragment.empty)
}
