sealed trait Indicator
case object NoIndicator extends Indicator
case object PlayerIndicator extends Indicator
case object DiamondIndicator extends Indicator
case object ExitIndicator extends Indicator
case object LeftIndicator extends Indicator
case object RightIndicator extends Indicator
case object ExtendIndicator extends Indicator
case object ReplayIndicator extends Indicator

case class TutorialGuideLine (indicator: Indicator, text1: String, text2: String, continue: String, enabled: Set[PlaySceneButtonEvent])

object TutorialGuideLine {
  def loadGuide (guide: String): Vector[TutorialGuideLine] =
    guide.split ('\n').filter (_.trim.nonEmpty).map (loadLine).toVector

  def loadLine (line: String): TutorialGuideLine = {
    val parts = line.split (";")
    val indicator = parts (0).charAt (0) match {
      case 'P' => PlayerIndicator
      case 'D' => DiamondIndicator
      case 'X' => ExitIndicator
      case '<' => LeftIndicator
      case '>' => RightIndicator
      case '^' => ExtendIndicator
      case 'R' => ReplayIndicator
      case _ => NoIndicator
    }
    val continue = indicator match {
      case NoIndicator => "Press space to continue"
      case PlayerIndicator => "Press space to continue"
      case DiamondIndicator => "Press space to continue"
      case ExitIndicator => "Press space to continue"
      case LeftIndicator => "Click/press left"
      case RightIndicator => "Click/press right"
      case ExtendIndicator => "Click extend/press up"
      case ReplayIndicator => "Click replay button"
    }
    val enabled: Set[PlaySceneButtonEvent] = indicator match {
      case NoIndicator => Set (BackButtonEvent, SpaceContinueEvent)
      case PlayerIndicator => Set (BackButtonEvent, SpaceContinueEvent)
      case DiamondIndicator => Set (BackButtonEvent, SpaceContinueEvent)
      case ExitIndicator => Set (BackButtonEvent, SpaceContinueEvent)
      case LeftIndicator => Set (BackButtonEvent, LeftButtonEvent)
      case RightIndicator => Set (BackButtonEvent, RightButtonEvent)
      case ExtendIndicator => Set (BackButtonEvent, ExtendButtonEvent)
      case ReplayIndicator => Set (BackButtonEvent, ReplayButtonEvent)
    }
    val text1 = if (parts.size > 1) parts (1) else ""
    val text2 = if (parts.size > 2) parts (2) else ""
    TutorialGuideLine (indicator, text1, text2, continue, enabled)
  }
}