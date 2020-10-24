import ButtonUpdate._
import indigo._
import indigo.shared.input.Mouse
import indigo.shared.scenegraph.SceneGraphNodePrimitive
import indigoextras.ui._

case class ViewModel(
    levelSceneButtons: LevelsSceneButtons,
    playSceneButtons: PlaySceneButtons,
    successSceneButtons: List[Button],
    replaySceneButton: Button
)

case class LevelsSceneButtons(
    levelButtons: Map[LevelKind, List[Button]],
    tutorialButtons: Map[LevelKind, Button],
    typeButton: RadioButtonGroup
) {
  def draw(levelKind: LevelKind): List[SceneGraphNodePrimitive] =
    levelButtons(levelKind).map(_.draw) :+ typeButton.draw :+ tutorialButtons(levelKind).draw

  def update(levelKind: LevelKind, mouse: Mouse): Outcome[LevelsSceneButtons] =
    update3Tuple(
      updateMapValue(levelKind, updateList(updateButton)),
      updateMapValue(levelKind, updateButton),
      updateRadio
    )(levelButtons, tutorialButtons, typeButton, mouse).map {
      case (newLevelButtons, newTutorialButtons, newTypeButtons) =>
        LevelsSceneButtons(newLevelButtons, newTutorialButtons, newTypeButtons)
    }
}

case class PlaySceneButtons(controls: Map[LevelKind, List[Button]], navigation: List[Button]) {
  def draw(levelKind: LevelKind): List[SceneGraphNodePrimitive] =
    controls(levelKind).map(_.draw) ++ navigation.map(_.draw)

  def update(levelKind: LevelKind, mouse: Mouse): Outcome[PlaySceneButtons] =
    update2Tuple(
      updateMapValue(levelKind, updateList(updateButton)),
      updateList(updateButton)
    )(controls, navigation, mouse).map {
      case (newControls, newNavigation) =>
        PlaySceneButtons(newControls, newNavigation)
    }
}
