import indigo.Outcome
import indigo.shared.input.Mouse
import indigoextras.ui._

object ButtonUpdate {
  def updateButton(button: Button, mouse: Mouse): Outcome[Button] =
    button.update(mouse)

  def updateRadio(button: RadioButtonGroup, mouse: Mouse): Outcome[RadioButtonGroup] =
    button.update(mouse)

  def updateList[V](updater: (V, Mouse) => Outcome[V])(buttons: List[V], mouse: Mouse): Outcome[List[V]] =
    buttons.map(updater(_, mouse)).sequence

  def updateMapValue[K, V](
      key: K,
      updater: (V, Mouse) => Outcome[V]
  )(collection: Map[K, V], mouse: Mouse): Outcome[Map[K, V]] =
    updater(collection(key), mouse).map(value => collection + (key -> value))

  def update2Tuple[V1, V2](
      updater1: (V1, Mouse) => Outcome[V1],
      updater2: (V2, Mouse) => Outcome[V2]
  )(item1: V1, item2: V2, mouse: Mouse): Outcome[(V1, V2)] =
    updater1(item1, mouse).merge(updater2(item2, mouse)) { case (v1, v2) => (v1, v2) }

  def update3Tuple[V1, V2, V3](
      updater1: (V1, Mouse) => Outcome[V1],
      updater2: (V2, Mouse) => Outcome[V2],
      updater3: (V3, Mouse) => Outcome[V3]
  )(item1: V1, item2: V2, item3: V3, mouse: Mouse): Outcome[(V1, V2, V3)] =
    update2Tuple(updater1, updater2)(item1, item2, mouse).merge(updater3(item3, mouse)) {
      case (v12, v3) => (v12._1, v12._2, v3)
    }
}
