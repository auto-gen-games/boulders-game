import indigo.shared.Outcome
import indigo.shared.datatypes.{Depth, Point, Rectangle}
import indigo.shared.events.GlobalEvent
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.Group
import indigoextras.ui.{ButtonAssets, ButtonState}

final case class RadioButton(
    buttonAssets: ButtonAssets,
    bounds: List[Rectangle],
    depth: Depth,
    selected: Option[Int],
    over: Option[Int],
    onUnselected: Int => List[GlobalEvent],
    onSelected: Int => List[GlobalEvent],
    onHoverOver: Int => List[GlobalEvent],
    onHoverOut: Int => List[GlobalEvent]
) {
  def state(option: Int): ButtonState =
    if (selected.contains(option)) ButtonState.Down
    else if (over.contains(option)) ButtonState.Over
    else ButtonState.Up

  def update(mouse: Mouse): Outcome[RadioButton] = {
    val nowOver: Int = bounds.indexWhere(_.isPointWithin(mouse.position))

    if (nowOver >= 0) {
      val nowSelected: Option[Int] = if (mouse.mousePressed) Some(nowOver) else selected
      Outcome(this.copy(over = Some(nowOver), selected = nowSelected))
        .addGlobalEvents(changeOverEvents(Some(nowOver)))
        .addGlobalEvents(changeSelectedEvents(nowSelected))
    } else
      Outcome(this.copy(over = None)).addGlobalEvents(changeOverEvents(None))
  }

  private def changeOverEvents(nowOver: Option[Int]): List[GlobalEvent] =
    (over, nowOver) match {
      case (None, Some(newOption)) =>
        onHoverOver(newOption)
      case (Some(oldOption), None) =>
        onHoverOut(oldOption)
      case (Some(oldOption), Some(newOption)) if oldOption != newOption =>
        onHoverOut(oldOption) ++ onHoverOver(newOption)
      case _ =>
        List.empty
    }

  private def changeSelectedEvents(nowSelected: Option[Int]): List[GlobalEvent] =
    (selected, nowSelected) match {
      case (None, Some(newOption)) =>
        onSelected(newOption)
      case (Some(oldOption), None) =>
        onUnselected(oldOption)
      case (Some(oldOption), Some(newOption)) if oldOption != newOption =>
        onUnselected(oldOption) ++ onSelected(newOption)
      case _ =>
        List.empty
    }

  def draw: Group =
    Group(bounds.zipWithIndex.map {
      case (optionBounds, optionIndex) =>
        state(optionIndex) match {
          case ButtonState.Up =>
            buttonAssets.up.moveTo(optionBounds.position).withDepth(depth)

          case ButtonState.Over =>
            buttonAssets.over.moveTo(optionBounds.position).withDepth(depth)

          case ButtonState.Down =>
            buttonAssets.down.moveTo(optionBounds.position).withDepth(depth)
        }
    })

  def withUnselectedAction(action: Int => List[GlobalEvent]): RadioButton =
    this.copy(onUnselected = action)

  def withSelectedAction(action: Int => List[GlobalEvent]): RadioButton =
    this.copy(onSelected = action)

  def withHoverOverAction(action: Int => List[GlobalEvent]): RadioButton =
    this.copy(onHoverOver = action)

  def withHoverOutAction(action: Int => List[GlobalEvent]): RadioButton =
    this.copy(onHoverOut = action)
}

object RadioButton {
  def apply(buttonAssets: ButtonAssets, bounds: List[Rectangle], depth: Depth, selected: Option[Int]): RadioButton =
    RadioButton(
      buttonAssets = buttonAssets,
      bounds = bounds,
      depth = depth,
      selected = selected,
      over = None,
      onUnselected = _ => Nil,
      onSelected = _ => Nil,
      onHoverOut = _ => Nil,
      onHoverOver = _ => Nil
    )

  def apply(
      buttonAssets: ButtonAssets,
      positions: List[Point],
      width: Int,
      height: Int,
      depth: Depth,
      selected: Option[Int]
  ): RadioButton =
    apply(buttonAssets, positions.map(pos => Rectangle(pos.x, pos.y, width, height)), depth, selected)
}
