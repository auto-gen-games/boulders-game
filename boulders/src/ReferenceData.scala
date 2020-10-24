import indigo._

/** The start-up data is the viewport (not used at the moment but kept in case useful later) and the
  * list of loaded levels.
  */
case class ReferenceData(
    viewport: GameViewport,
    tutorial: Level,
    levels: Map[LevelKind, Vector[Level]],
    guide: Vector[TutorialGuideLine],
    highlight: Sprite
)
