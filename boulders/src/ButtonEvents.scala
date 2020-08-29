import indigo.GlobalEvent

case class LevelButtonEvent (level: Int) extends GlobalEvent
case object InfoButtonEvent extends GlobalEvent
case object BackButtonEvent extends GlobalEvent
case object ReplayButtonEvent extends GlobalEvent
