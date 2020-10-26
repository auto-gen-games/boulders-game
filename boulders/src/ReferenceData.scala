import indigo._

/** The start-up data is the viewport (not used at the moment but kept in case useful later) and the
  * list of loaded levels.
  */
case class ReferenceData(
    viewport: GameViewport,
    tutorial: Map[LevelKind, Level],
    levels: Map[LevelKind, Vector[Level]],
    guide: Map[LevelKind, Vector[TutorialGuideLine]],
    highlight: Sprite
) {
  def getLevel(kind: LevelKind, number: Int): Level =
    if (number < 0) tutorial(kind) else levels(kind)(number)
}
