import indigo.{EventFilters, SubSystem}
import indigo.scenes.{Lens, Scene, SceneName}

abstract class StandardScene extends Scene[StartupData, Model, ViewModel] {
  type SceneModel     = Model
  type SceneViewModel = ViewModel

  val modelLens: Lens[Model, Model]             = Lens.keepLatest
  val viewModelLens: Lens[ViewModel, ViewModel] = Lens.keepLatest
  val eventFilters: EventFilters                = EventFilters.Default
  val subSystems: Set[SubSystem]                = Set()
}
