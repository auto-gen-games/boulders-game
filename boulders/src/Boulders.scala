import GameAssets._
import PlayModel.play
import Settings._
import ViewLogic._
import indigo._
import indigo.scenes.{Scene, SceneName}
import indigoextras.ui.Button

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object Boulders extends IndigoGame[GameViewport, ReferenceData, Model, ViewModel] {

  /** The initial game model starts with the tutorial level, and with no levels marked completed. */
  def initialModel(startupData: ReferenceData): Model =
    Model("base", play(startupData.tutorial, startupData.guide), Set.empty)

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
    NonEmptyList(StartScene, LevelsScene, new GlobalisedScene(PlayScene), SuccessScene)

  def initialScene(bootData: GameViewport): Option[SceneName] =
    Some(StartScene.name)

  /** Load and decode the level specs on startup. */
  def setup(
      bootData: GameViewport,
      assetCollection: AssetCollection,
      dice: Dice
  ): Startup[StartupErrors, ReferenceData] = {
    val specs =
      levelSpecs
        .map { case (kind, name) => kind -> assetCollection.findTextDataByName(name).map(Level.decodeLevels(kind, _)) }
        .filter(_._2.isDefined)
        .view
        .mapValues(_.get)
        .toMap
    val result = for {
      spriteAnim <- GameAssets.loadAnimation(assetCollection, dice, highlightJSON, highlightBox, Depth(0))
      level      <- assetCollection.findTextDataByName(tutorialSpec).map(Level.levelFromCode("base", -1, _))
      guide      <- assetCollection.findTextDataByName(tutorialGuide).map(TutorialGuideLine.loadGuide)
    } yield Startup
      .Success(ReferenceData(bootData, level, specs, guide, spriteAnim.sprite))
      .addAnimations(spriteAnim.animations)

    result.getOrElse(Startup.Failure.withErrors("Could not load or parse game data"))
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
    val levelNumberButtons: Map[String, List[Button]] =
      gameTypes.map(kind => kind -> levelButtons(startupData.levels(kind).size)).toMap
    val tutorialButtons: Map[String, Button] =
      gameTypes.map(kind => kind -> createButton("button-base", tutorialLevelPosition, TutorialButtonEvent)).toMap
    val kindButtons: RadioButton =
      gameTypeButton

    val levelSceneButtons: LevelsSceneButtons =
      LevelsSceneButtons(levelNumberButtons, tutorialButtons, kindButtons)
    val playSceneButtons: PlaySceneButtons =
      PlaySceneButtons(Map("base" -> baseControls, "flip" -> flipControls), List(backButton, replayButton))
    val successSceneButtons: List[Button] =
      List(forwardButton, backButton, replayButton)

    ViewModel(levelSceneButtons, playSceneButtons, successSceneButtons)
  }
}
