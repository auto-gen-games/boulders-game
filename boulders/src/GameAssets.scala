import Level.levelKinds
import Settings._
import indigo._
import indigo.json.Json
import indigo.platform.assets.AssetCollection
import indigoextras.ui.{Button, ButtonAssets, RadioButton, RadioButtonGroup}

/** Assets: graphics, level specs (text), and font (copied from Snake demo) */
object GameAssets {
  val imageFiles = Set(
    "boulder",
    "boxy_font_small",
    "button-base",
    "diamond",
    "exit",
    "floor",
    "highlight-sheet",
    "left-push",
    "player-bottom",
    "player-sprite",
    "player-top",
    "right-push",
    "tick",
    "tutorial-box",
    "wall"
  )
  val buttonFiles = Set("back-button", "control-arrows", "replay-button", "undo-button", "type-button")
  val textFiles = Set(
    "levels-base",
    "levels-flip",
    "levels-pipe",
    "tutorial-level-base",
    "tutorial-level-flip",
    "tutorial-guide-base",
    "tutorial-guide-flip"
  )
  val jsonFiles  = Set("highlight")
  val audioFiles = Set("rolling")

  def assets(baseUrl: String): Set[AssetType] =
    imageFiles.map(file => AssetType.Image(AssetName(file), AssetPath(baseUrl + s"assets/$file.png"))) ++
      buttonFiles.map(file => AssetType.Image(AssetName(file), AssetPath(baseUrl + s"assets/$file.png"))) ++
      textFiles.map(file => AssetType.Text(AssetName(file), AssetPath(baseUrl + s"assets/$file.txt"))) ++
      audioFiles.map(file => AssetType.Audio(AssetName(file), AssetPath(baseUrl + s"assets/$file.mp3"))) ++
      jsonFiles.map(file => AssetType.Text(AssetName(file), AssetPath(baseUrl + s"assets/$file.json")))

  val materials: Map[String, Material.Textured] =
    imageFiles.map(image => (image, Material.Textured(AssetName(image)))).toMap

  def graphic(asset: String, width: Int = cellSize, height: Int = cellSize): Graphic =
    Graphic(0, 0, width, height, 2, materials(asset))

  def possiblyTint(graphic: Graphic, tint: Option[RGBA]): Graphic =
    tint match {
      case Some(value) => graphic.withTint(value)
      case None        => graphic
    }

  /** Creates a button from the given graphics asset, at a position, and triggering an event */
  def createButton(
      assetName: String,
      position: Point,
      buttonEvent: GlobalEvent,
      row: Int = 0,
      width: Int = cellSize,
      height: Int = cellSize,
      flipped: Boolean = false,
      tint: Option[RGBA] = None
  ): Button = {
    val material = Material.Textured(AssetName(assetName))
    val buttonAssets = ButtonAssets(
      up = possiblyTint(
        Graphic(0, 0, width, height, 2, material).withCrop(0, row * height, width, height).flipHorizontal(flipped),
        tint
      ),
      over = possiblyTint(
        Graphic(0, 0, width, height, 2, material).withCrop(width, row * height, width, height).flipHorizontal(flipped),
        tint
      ),
      down = possiblyTint(
        Graphic(0, 0, width, height, 2, material)
          .withCrop(width * 2, row * height, width, height)
          .flipHorizontal(flipped),
        tint
      )
    )
    Button(
      buttonAssets = buttonAssets,
      bounds = Rectangle(position.x, position.y, width, height),
      depth = Depth(2)
    ).withUpActions(buttonEvent)
  }

  def createRadio(
      assetName: String,
      row: Int = 0,
      positions: List[Point],
      width: Int,
      height: Int,
      selectionEvent: Int => GlobalEvent,
      initialSelection: Int
  ): RadioButtonGroup = {
    val material = Material.Textured(AssetName(assetName))
    val buttonAssets = ButtonAssets(
      up = Graphic(0, 0, width, height, 2, material).withCrop(0, row * height, width, height),
      over = Graphic(0, 0, width, height, 2, material).withCrop(width, row * height, width, height),
      down = Graphic(0, 0, width, height, 2, material)
        .withCrop(width * 2, row * height, width, height)
    )
    val buttons = positions.map(pos => RadioButton(pos)).zipWithIndex.map {
      case (button, index) =>
        if (initialSelection == index)
          button.withSelectedActions(selectionEvent(index)).selected
        else
          button.withSelectedActions(selectionEvent(index)).deselected
    }
    RadioButtonGroup(buttonAssets, width, height)
      .withRadioButtons(buttons)
  }

  val levelSpecs: Map[LevelKind, AssetName] =
    levelKinds.map(kind => kind -> AssetName(s"levels-${kind.name}")).toMap
  val tutorialSpecs: Map[LevelKind, AssetName] =
    levelKinds.map(kind => kind -> AssetName(s"tutorial-level-${kind.name}")).toMap
  val tutorialGuides: Map[LevelKind, AssetName] =
    levelKinds.map(kind => kind -> AssetName(s"tutorial-guide-${kind.name}")).toMap
  val highlightBox  = AssetName("highlight-sheet")
  val highlightJSON = AssetName("highlight")

  val player          = graphic("player-sprite").withCrop(0, 0, cellSize, cellSize)
  val playerRight     = graphic("player-sprite").withCrop(cellSize, 0, cellSize, cellSize)
  val playerFalling   = graphic("player-sprite").withCrop(cellSize * 2, 0, cellSize, cellSize)
  val playerLeft      = playerRight.flipHorizontal(true)
  val playerBottom    = graphic("player-bottom")
  val playerTop       = graphic("player-top")
  val playerLeftPush  = graphic("left-push")
  val playerRightPush = graphic("right-push")
  val boulder         = graphic("boulder")
  val wall            = graphic("wall")
  val floor           = graphic("floor")
  val diamond         = graphic("diamond")
  val exit            = graphic("exit")
  val tutorialBox     = graphic("tutorial-box", 256, 64)
  val tick            = graphic("tick")

  def loadAnimation(
      assetCollection: AssetCollection,
      dice: Dice,
      jsonRef: AssetName,
      name: AssetName,
      depth: Depth
  ): Option[SpriteAndAnimations] = {
    val json = assetCollection.findTextDataByName(jsonRef)
    if (json.isEmpty) System.err.println("Could not load JSON")
    val aseprite = json.flatMap(Json.asepriteFromJson)
    if (aseprite.isEmpty) System.err.println("Could not parse JSON")
    val spriteAndAnimations = aseprite.flatMap(_.toSpriteAndAnimations(dice, name))
    if (spriteAndAnimations.isEmpty) System.err.println("Could not create animation")
    spriteAndAnimations.map(sas => sas.copy(sprite = sas.sprite.withDepth(depth)))
  }

  val fontKey: FontKey = FontKey("small font")

  val fontInfo: FontInfo =
    FontInfo(fontKey, Material.Textured(AssetName("boxy_font_small")), 320, 230, FontChar("?", 47, 26, 11, 12))
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
