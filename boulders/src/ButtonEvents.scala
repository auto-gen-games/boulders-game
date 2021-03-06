import indigo.GlobalEvent

case class LevelTypeButtonEvent(index: Int) extends GlobalEvent
case class LevelButtonEvent(level: Int)     extends GlobalEvent
case object TutorialButtonEvent             extends GlobalEvent
trait PlaySceneButtonEvent                  extends GlobalEvent

case object LeftButtonEvent    extends PlaySceneButtonEvent
case object ExtendButtonEvent  extends PlaySceneButtonEvent
case object RightButtonEvent   extends PlaySceneButtonEvent
case object FlipButtonEvent    extends PlaySceneButtonEvent
case object BackButtonEvent    extends PlaySceneButtonEvent
case object ReplayButtonEvent  extends PlaySceneButtonEvent
case object UndoButtonEvent    extends PlaySceneButtonEvent
case object SpaceContinueEvent extends PlaySceneButtonEvent
case object ForwardButtonEvent extends PlaySceneButtonEvent
