package game.scenes.loading

import indigo.*
import indigo.scenes.*
import indigoextras.subsystems.*
import game.scenes.loading.LoadingScene.StartupData
import indigo.scenes.SceneEvent.Next

import scala.scalajs.js
import game.scenes.loading.LoadingState.NotStarted
import game.scenes.loading.LoadingState.InProgress
import indigo.scenes.SceneEvent.JumpTo
import assets.fonts.Fonts
import game.IndigoModel
import game.IndigoViewModel
import game.scenes.ingame.InGameScene
import gamelogic.config.GameConfiguration
import assets.Asset
import game.events.DicteeGameEvent
import gamelogic.config.Background

/** This is the indigo scene when we start the game.
  */
class LoadingScene(
    fonts: Fonts,
    chosenBackground: Background
) extends Scene[GameConfiguration, IndigoModel, IndigoViewModel] {

  Asset.initializeBackground(chosenBackground)

  val fontColour = Fonts.white
  val fontSize   = Fonts.m

  val fontKey       = Fonts.fontKeys(fontColour, fontSize)
  val fontAssetName = Fonts.assetNames(fontColour, fontSize)

  type SceneModel     = LoadingState
  type SceneViewModel = Unit

  override def subSystems: Set[SubSystem[IndigoModel]] =
    Set(AssetBundleLoader[IndigoModel])

  override def present(
      context: SceneContext[GameConfiguration],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    val text = model match
      case NotStarted()           => "On se prépare..."
      case InProgress(100)        => "Tout est chargé, on finalise les détails..."
      case InProgress(percentage) => s"Chargement des images... $percentage %"
      case game.scenes.loading.LoadingState.Error(key, message) =>
        s"Outch, something went wrong: $key $message"

    Outcome(
      SceneUpdateFragment(
        Text(
          text,
          fontKey,
          Material.Bitmap(fontAssetName)
        )
          .withPosition(Point(10, 100))
      )
    )

  override def updateModel(
      context: SceneContext[GameConfiguration],
      loadingState: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {
    case FrameTick =>
      loadingState match
        case LoadingState.NotStarted() =>
          Outcome(LoadingState.InProgress(0))
            .addGlobalEvents(
              AssetBundleLoaderEvent.Load(
                BindingKey("Loading"),
                fonts.fontImages.toSet ++
                  Asset.all.map(_.asIndigoAssetType)
              )
            )
        case _ => Outcome(loadingState)

    case assetLoadingEvent: AssetBundleLoaderEvent =>
      assetLoadingEvent match
        case indigoextras.subsystems.AssetBundleLoaderEvent.Load(key, assets) =>
          Outcome(loadingState)
        case AssetBundleLoaderEvent.Retry(key) =>
          Outcome(loadingState)
        case AssetBundleLoaderEvent.Started(key) =>
          Outcome(loadingState)
        case AssetBundleLoaderEvent.LoadProgress(key, percent, completed, total) =>
          Outcome(LoadingState.InProgress(percent))
        case AssetBundleLoaderEvent.Success(key) =>
          Outcome(LoadingState.InProgress(100)).addGlobalEvents(JumpTo(InGameScene.name), DicteeGameEvent.GameStarts())
        case AssetBundleLoaderEvent.Failure(key, message) =>
          Outcome(LoadingState.Error(key.toString, message))

    case _ =>
      Outcome(loadingState)
  }

  override def eventFilters: EventFilters = EventFilters.AllowAll

  override def viewModelLens: Lens[IndigoViewModel, SceneViewModel] =
    Lens(_ => (), (viewModel, _) => viewModel)

  override def name: SceneName = SceneName("Loading")

  override def modelLens: Lens[IndigoModel, SceneModel] =
    Lens(_.loadingState, _.withLoadingState(_))

  override def updateViewModel(
      context: SceneContext[GameConfiguration],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] = _ => Outcome(())

}

object LoadingScene {
  type StartupData = Unit
}
