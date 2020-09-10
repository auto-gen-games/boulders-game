import indigo._
import indigo.scenes._

/**
  * An event returned by a sub-scene to a globalised scene saying that an attribute of the parent model
  * or view model should be modified, defined by a lens for the attribute and a mapping for the modification.
  */
case class ModifyEvent[Model, Attribute](lens: Lens[Model, Attribute], modification: Attribute => Attribute)
    extends GlobalEvent

/**
  * A wrapper around a scene (a sub-scene) allowing the sub-scene to mainly work with just a part of the
  * model and/or view model but return modifications to the parent model via events.
  *
  * @param subscene The wrapped sub-scene
  * @tparam ReferenceData The start-up data object
  * @tparam Model The global model
  * @tparam ViewModel The global view model
  * @tparam SubModel The part of the model used by the subscene
  * @tparam SubView The part of the view model used by the subscene
  */
class GlobalisedScene[ReferenceData, Model, ViewModel, SubModel, SubView](
    subscene: Scene[ReferenceData, Model, ViewModel] { type SceneModel = SubModel; type SceneViewModel = SubView }
) extends Scene[ReferenceData, Model, ViewModel] {
  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val name: SceneName                                = subscene.name
  val modelLens: Lens[Model, SceneModel]             = Lens.keepLatest
  val viewModelLens: Lens[ViewModel, SceneViewModel] = Lens.keepLatest
  val eventFilters: EventFilters                     = EventFilters.Default
  val subSystems: Set[SubSystem]                     = Set.empty

  def updateModel(context: FrameContext[ReferenceData], model: Model): GlobalEvent => Outcome[Model] =
    event =>
      processModifyEvents(
        subscene
          .updateModel(context, subscene.modelLens.get(model))(event)
          .map(submodel => subscene.modelLens.set(model, submodel))
      )

  def updateViewModel(
      context: FrameContext[ReferenceData],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] =
    event =>
      processModifyEvents(
        subscene
          .updateViewModel(context, subscene.modelLens.get(model), subscene.viewModelLens.get(viewModel))(event)
          .map(subviewmodel => subscene.viewModelLens.set(viewModel, subviewmodel))
      )

  def present(
      context: FrameContext[ReferenceData],
      model: Model,
      viewModel: ViewModel
  ): SceneUpdateFragment =
    subscene
      .present(context, subscene.modelLens.get(model), subscene.viewModelLens.get(viewModel))

  def processModifyEvents[M](outcome: Outcome[M]): Outcome[M] = {
    val newModel = outcome.globalEvents.foldLeft(outcome.state) {
      case (model, event) =>
        event match {
          case e: ModifyEvent[M, _] => e.lens.modify(model, e.modification)
          case _                    => model
        }
    }
    outcome
      .mapState(_ => newModel)
      .mapGlobalEvents(_.filter {
        case _: ModifyEvent[_, _] => false
        case _                    => true
      })
  }
}
