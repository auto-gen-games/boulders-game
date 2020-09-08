import GameAssets._
import Settings._
import ViewLogic._
import indigo._
import indigo.scenes.{Scene, SceneName}
import indigoextras.ui.Button
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("IndigoGame")
object Boulders extends IndigoGame[GameViewport, StartupData, Model, ViewModel] {

  /** The initial game model contains the loaded levels and starts with the tutorial level. */
  def initialModel(startupData: StartupData): Model =
    Model(
      startupData.tutorial,
      startupData.levels,
      PlayModel.play(startupData.tutorial, startupData.guide, startupData.highlightAnimation),
      startupData.guide,
      startupData.highlightAnimation
    )

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
  def scenes(bootData: GameViewport): NonEmptyList[Scene[StartupData, Model, ViewModel]] =
    NonEmptyList(StartScene, LevelsScene, PlayScene)

  def initialScene(bootData: GameViewport): Option[SceneName] =
    Some(StartScene.name)

  /** Load and decode the level specs on startup. */
  def setup(
      bootData: GameViewport,
      assetCollection: AssetCollection,
      dice: Dice
  ): Startup[StartupErrors, StartupData] = {
    val result = for {
      spriteAnim <- GameAssets.loadAnimation(assetCollection, dice, highlightJSON, highlightBox, Depth(0))
      level      <- assetCollection.findTextDataByName(tutorialSpec).map(Level.levelFromCode(-1, _))
      specs      <- assetCollection.findTextDataByName(levelSpecs).map(Level.decodeLevels)
      guide      <- assetCollection.findTextDataByName(tutorialGuide).map(TutorialGuideLine.loadGuide)
    } yield Startup
      .Success(StartupData(bootData, level, specs, guide, spriteAnim.sprite))
      .addAnimations(spriteAnim.animations)

    result.getOrElse(Startup.Failure.withErrors("Could not load or parse game data"))
  }

  def initialViewModel(startupData: StartupData, model: Model): ViewModel = {
    val leftButton: Button =
      createButton("control-arrows", leftControlPosition, LeftButtonEvent, row = 0)
    val extendButton: Button =
      createButton("control-arrows", extendControlPosition, ExtendButtonEvent, row = 1)
    val rightButton: Button =
      createButton("control-arrows", rightControlPosition, RightButtonEvent, row = 2)
    val backButton: Button =
      createButton("back-button", backBoxPosition, BackButtonEvent)
    val replayButton: Button =
      createButton("replay-button", replayBoxPosition, ReplayButtonEvent)
    val playSceneButtons: List[Button] =
      List(leftButton, extendButton, rightButton, backButton, replayButton)

    ViewModel(levelButtons(model.levels.size), playSceneButtons)
  }
}
