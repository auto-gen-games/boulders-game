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

  /** The initial game model starts with the tutorial level, and with no levels marked completed. */
  def initialModel(startupData: ReferenceData): Model = {
    val completedLevels = levelKinds.map(kind => (kind -> Set[Int]())).toMap
    val defaultPlay     = play(startupData.tutorial(baseKind), startupData.guide(baseKind))
    Model(baseKind, defaultPlay, completedLevels, ReplayModel(defaultPlay, List.empty, Seconds.zero))
  }

  /** Copied from the Snake demo, loading assets and the viewport. */
  def boot(flags: Map[String, String]): BootResult[GameViewport] = {
    val assetPath: String = flags.getOrElse("baseUrl", "")
    val config = GameConfig(
      viewport = GameViewport(Settings.viewportWidth, Settings.viewportHeight),
      frameRate = 60,
      clearColor = ClearColor.Black,
      magnification = Settings.magnificationLevel
    )

    BootResult(config, config.viewport)
      .withAssets(GameAssets.assets(assetPath))
      .withFonts(GameAssets.fontInfo)
  }

  /** Three scenes: start screen, levels choice, game play screen */
  def scenes(bootData: GameViewport): NonEmptyList[Scene[ReferenceData, Model, ViewModel]] =
    NonEmptyList(StartScene, LevelsScene, new GlobalisedScene(PlayScene), SuccessScene, ReplayScene)

  def initialScene(bootData: GameViewport): Option[SceneName] =
    Some(StartScene.name)

  /** Load and decode the level specs on startup. */
  def setup(
      bootData: GameViewport,
      assetCollection: AssetCollection,
      dice: Dice
  ): Startup[ReferenceData] = {
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

    result.getOrElse(Startup.Failure("Could not load or parse game data"))
  }

  def initialViewModel(startupData: ReferenceData, model: Model): ViewModel = {
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

    ViewModel(levelSceneButtons, playSceneButtons, successSceneButtons, backButton)
  }
}
