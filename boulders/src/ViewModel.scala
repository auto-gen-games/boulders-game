import ButtonUpdate._
import indigo._
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.SceneGraphNodePrimitive
import indigoextras.ui._

case class ViewModel(
    levelSceneButtons: LevelsSceneButtons,
    playSceneButtons: PlaySceneButtons,
    successSceneButtons: List[Button]
)

case class LevelsSceneButtons(
    levelButtons: Map[String, List[Button]],
    tutorialButtons: Map[String, Button],
    typeButton: RadioButton
) {
  def draw(gameType: String): List[SceneGraphNodePrimitive] =
    levelButtons(gameType).map(_.draw) :+ typeButton.draw :+ tutorialButtons(gameType).draw

  def update(gameType: String, mouse: Mouse): Outcome[LevelsSceneButtons] =
    update3Tuple(
      updateMapValue(gameType, updateList(updateButton)),
      updateMapValue(gameType, updateButton),
      updateRadio
    )(levelButtons, tutorialButtons, typeButton, mouse).map {
      case (newLevelButtons, newTutorialButtons, newTypeButtons) =>
        LevelsSceneButtons(newLevelButtons, newTutorialButtons, newTypeButtons)
    }
}

case class PlaySceneButtons(controls: Map[String, List[Button]], navigation: List[Button]) {
  def draw(gameType: String): List[SceneGraphNodePrimitive] =
    controls(gameType).map(_.draw) ++ navigation.map(_.draw)

  def update(gameType: String, mouse: Mouse): Outcome[PlaySceneButtons] =
    update2Tuple(
      updateMapValue(gameType, updateList(updateButton)),
      updateList(updateButton)
    )(controls, navigation, mouse).map {
      case (newControls, newNavigation) =>
        PlaySceneButtons(newControls, newNavigation)
    }
}
