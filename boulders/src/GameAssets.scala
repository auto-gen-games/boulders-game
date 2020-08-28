import Settings.{gridSquareSize, levelBoxSize}
import indigo._
import indigoextras.ui.ButtonAssets

/** Assets: graphics, level specs (text), and font (copied from Snake demo) */
object GameAssets {
  val smallFontName = AssetName ("smallFontName")
  val playerImage = AssetName ("playerImage")
  val playerBottomImage = AssetName ("playerBottomImage")
  val playerTopImage = AssetName ("playerTopImage")
  val leftPush = AssetName ("leftPushImage")
  val rightPush = AssetName ("rightPushImage")
  val falling = AssetName ("fallingImage")
  val boulderImage = AssetName ("boulderImage")
  val exitImage = AssetName ("exitImage")
  val diamondImage = AssetName ("diamondImage")
  val wallImage = AssetName ("wallImage")
  val floorImage = AssetName ("floorImage")
  val numberBoxImage = AssetName ("numberBoxImage")
  val numberOverImage = AssetName ("numberOverImage")
  val numberDownImage = AssetName ("numberDownImage")
  val infoBoxImage = AssetName ("infoBoxImage")
  val backBoxImage = AssetName ("backBoxImage")
  val retryBoxImage = AssetName ("retryBoxImage")
  val levelSpecs = AssetName ("levelSpecs")

  val playerMaterial: Material.Textured = Material.Textured (playerImage)
  val playerBottomMaterial: Material.Textured = Material.Textured (playerBottomImage)
  val playerTopMaterial: Material.Textured = Material.Textured (playerTopImage)
  val boulderMaterial: Material.Textured = Material.Textured (boulderImage)
  val exitMaterial: Material.Textured = Material.Textured (exitImage)
  val diamondMaterial: Material.Textured = Material.Textured (diamondImage)
  val wallMaterial: Material.Textured = Material.Textured (wallImage)
  val floorMaterial: Material.Textured = Material.Textured (floorImage)
  val levelNumberMaterial: Material.Textured = Material.Textured (numberBoxImage)
  val levelNumberOverMaterial: Material.Textured = Material.Textured (numberOverImage)
  val levelNumberDownMaterial: Material.Textured = Material.Textured (numberDownImage)
  val infoBoxMaterial: Material.Textured = Material.Textured (infoBoxImage)
  val backBoxMaterial: Material.Textured = Material.Textured (backBoxImage)
  val retryBoxMaterial: Material.Textured = Material.Textured (retryBoxImage)

  val player = Graphic (0, 0, gridSquareSize, gridSquareSize, 2, playerMaterial)
  val playerBottom = Graphic (0, 0, gridSquareSize, gridSquareSize, 2, playerBottomMaterial)
  val playerTop = Graphic (0, 0, gridSquareSize, gridSquareSize, 2, playerTopMaterial)
  val boulder = Graphic (0, 0, gridSquareSize, gridSquareSize, 2, boulderMaterial)
  val wall = Graphic (0, 0, gridSquareSize, gridSquareSize, 2, wallMaterial)
  val floor = Graphic (0, 0, gridSquareSize, gridSquareSize, 2, floorMaterial)
  val diamond = Graphic (0, 0, gridSquareSize, gridSquareSize, 2, diamondMaterial)
  val exit = Graphic (0, 0, gridSquareSize, gridSquareSize, 2, exitMaterial)
  val levelNumber = Graphic (0, 0, levelBoxSize, levelBoxSize, 2, levelNumberMaterial)
  val levelNumberOver = Graphic (0, 0, levelBoxSize, levelBoxSize, 2, levelNumberOverMaterial)
  val levelNumberDown = Graphic (0, 0, levelBoxSize, levelBoxSize, 2, levelNumberDownMaterial)
  val infoBox = Graphic (0, 0, gridSquareSize, levelBoxSize, 2, infoBoxMaterial)
  val backBox = Graphic (0, 0, gridSquareSize, levelBoxSize, 2, backBoxMaterial)
  val retryBox = Graphic (0, 0, gridSquareSize, levelBoxSize, 2, retryBoxMaterial)

  val levelButtonGraphic: ButtonAssets =
    ButtonAssets (up = levelNumber, over = levelNumberOver, down = levelNumberDown)

  def assets (baseUrl: String): Set[AssetType] =
    Set (
      AssetType.Image (smallFontName, AssetPath (baseUrl + "assets/boxy_font_small.png")),
      AssetType.Image (playerImage, AssetPath (baseUrl + "assets/player.png")),
      AssetType.Image (playerBottomImage, AssetPath (baseUrl + "assets/player-bottom.png")),
      AssetType.Image (playerTopImage, AssetPath (baseUrl + "assets/player-top.png")),
      AssetType.Image (boulderImage, AssetPath (baseUrl + "assets/boulder.png")),
      AssetType.Image (exitImage, AssetPath (baseUrl + "assets/exit.png")),
      AssetType.Image (diamondImage, AssetPath (baseUrl + "assets/diamond.png")),
      AssetType.Image (wallImage, AssetPath (baseUrl + "assets/wall.png")),
      AssetType.Image (floorImage, AssetPath (baseUrl + "assets/floor.png")),
      AssetType.Image (numberBoxImage, AssetPath (baseUrl + "assets/level-number.png")),
      AssetType.Image (numberOverImage, AssetPath (baseUrl + "assets/level-number-over.png")),
      AssetType.Image (numberDownImage, AssetPath (baseUrl + "assets/level-number-down.png")),
      AssetType.Image (infoBoxImage, AssetPath (baseUrl + "assets/info.png")),
      AssetType.Image (backBoxImage, AssetPath (baseUrl + "assets/back.png")),
      AssetType.Image (retryBoxImage, AssetPath (baseUrl + "assets/retry.png")),
      AssetType.Text (levelSpecs, AssetPath (baseUrl + "assets/levels.txt"))
    )

  val fontKey: FontKey = FontKey ("boxy font")

  val fontInfo: FontInfo =
    FontInfo (fontKey, Material.Textured(smallFontName), 320, 230, FontChar("?", 47, 26, 11, 12))
      .addChar(FontChar("A", 2, 39, 10, 12))
      .addChar(FontChar("B", 14, 39, 9, 12))
      .addChar(FontChar("C", 25, 39, 10, 12))
      .addChar(FontChar("D", 37, 39, 9, 12))
      .addChar(FontChar("E", 49, 39, 9, 12))
      .addChar(FontChar("F", 60, 39, 9, 12))
      .addChar(FontChar("G", 72, 39, 9, 12))
      .addChar(FontChar("H", 83, 39, 9, 12))
      .addChar(FontChar("I", 95, 39, 5, 12))
      .addChar(FontChar("J", 102, 39, 9, 12))
      .addChar(FontChar("K", 113, 39, 10, 12))
      .addChar(FontChar("L", 125, 39, 9, 12))
      .addChar(FontChar("M", 136, 39, 13, 12))
      .addChar(FontChar("N", 2, 52, 11, 12))
      .addChar(FontChar("O", 15, 52, 10, 12))
      .addChar(FontChar("P", 27, 52, 9, 12))
      .addChar(FontChar("Q", 38, 52, 11, 12))
      .addChar(FontChar("R", 51, 52, 10, 12))
      .addChar(FontChar("S", 63, 52, 9, 12))
      .addChar(FontChar("T", 74, 52, 11, 12))
      .addChar(FontChar("U", 87, 52, 10, 12))
      .addChar(FontChar("V", 99, 52, 9, 12))
      .addChar(FontChar("W", 110, 52, 13, 12))
      .addChar(FontChar("X", 125, 52, 9, 12))
      .addChar(FontChar("Y", 136, 52, 11, 12))
      .addChar(FontChar("Z", 149, 52, 10, 12))
      .addChar(FontChar("0", 2, 13, 10, 12))
      .addChar(FontChar("1", 13, 13, 7, 12))
      .addChar(FontChar("2", 21, 13, 9, 12))
      .addChar(FontChar("3", 33, 13, 9, 12))
      .addChar(FontChar("4", 44, 13, 9, 12))
      .addChar(FontChar("5", 56, 13, 9, 12))
      .addChar(FontChar("6", 67, 13, 9, 12))
      .addChar(FontChar("7", 79, 13, 9, 12))
      .addChar(FontChar("8", 90, 13, 10, 12))
      .addChar(FontChar("9", 102, 13, 9, 12))
      .addChar(FontChar("?", 47, 26, 11, 12))
      .addChar(FontChar("!", 2, 0, 6, 12))
      .addChar(FontChar(".", 143, 0, 6, 12))
      .addChar(FontChar(",", 124, 0, 8, 12))
      .addChar(FontChar("-", 133, 0, 9, 12))
      .addChar(FontChar(" ", 112, 13, 12, 12))
      .addChar(FontChar("[", 2, 65, 7, 12))
      .addChar(FontChar("]", 21, 65, 7, 12))
      .addChar(FontChar("(", 84, 0, 7, 12))
      .addChar(FontChar(")", 93, 0, 7, 12))
      .addChar(FontChar("\\", 11, 65, 8, 12))
      .addChar(FontChar("/", 150, 0, 9, 12))
      .addChar(FontChar(":", 2, 26, 5, 12))
      .addChar(FontChar("_", 42, 65, 9, 12))

}
