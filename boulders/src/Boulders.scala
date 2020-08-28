import indigo._
import indigo.scenes.{Scene, SceneName}
import scala.scalajs.js.annotation.JSExportTopLevel
import ViewLogic._

@JSExportTopLevel("IndigoGame")
object Boulders extends IndigoGame[GameViewport, StartupData, Model, ViewModel] {
  /** The initial game model is the list of loaded level specifications and a default level layout. */
  def initialModel (startupData: StartupData): Model =
    Model (startupData.levels, PlayModel.uninitiated)

  /** Copied from the Snake demo, loading assets and the viewport. */
  def boot (flags: Map[String, String]): BootResult[GameViewport] = {
    val assetPath: String = flags.getOrElse ("baseUrl", "")
    val config = GameConfig (
        viewport = GameViewport (Settings.viewportWidth, Settings.viewportHeight),
        frameRate = 60,
        clearColor = ClearColor.Black,
        magnification = Settings.magnificationLevel
      )

    BootResult (config, config.viewport)
      .withAssets (GameAssets.assets (assetPath))
      .withFonts (GameAssets.fontInfo)
  }

  /** Four scenes: start screen, levels choice, game play screen, and instructions page */
  def scenes (bootData: GameViewport): NonEmptyList[Scene[StartupData, Model, ViewModel]] =
    NonEmptyList (StartScene, LevelsScene, PlayScene, InstructionsScene)

  def initialScene (bootData: GameViewport): Option[SceneName] =
    Some (StartScene.name)

  /** Load and decode the level specs on startup. */
  def setup (bootData: GameViewport, assetCollection: AssetCollection, dice: Dice): Startup[StartupErrors, StartupData] =
    Startup.Success (StartupData (bootData,
      assetCollection.findTextDataByName (GameAssets.levelSpecs).map (Level.decodeLevels).getOrElse (Vector.empty)))

  def initialViewModel (startupData: StartupData, model: Model): ViewModel =
    ViewModel (createLevelButtons (model))
}
