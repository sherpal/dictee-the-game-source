package game

import indigo.*
import assets.fonts.Fonts
import gamelogic.GameState
import indigo.scenes.SceneName
import indigo.scenes.Scene
import game.scenes.loading.LoadingScene
import gamelogic.config.GameConfiguration
import game.scenes.ingame.InGameScene
import assets.Asset
import game.scenes.endgame.EndGameScene

import scala.scalajs.js

class TheGame(fonts: Fonts, config: GameConfiguration)
    extends IndigoGame[GameConfiguration, GameConfiguration, IndigoModel, IndigoViewModel] {

  type BootData    = GameConfiguration
  type StartUpData = GameConfiguration
  type Model       = IndigoModel
  type ViewModel   = IndigoViewModel

  override def scenes(bootData: BootData): NonEmptyBatch[Scene[StartUpData, Model, ViewModel]] =
    NonEmptyBatch[Scene[StartUpData, Model, ViewModel]](
      LoadingScene(fonts, bootData.background),
      InGameScene(fonts),
      EndGameScene(fonts)
    )

  override def initialScene(bootData: GameConfiguration): Option[SceneName] = Option.empty

  override def eventFilters: EventFilters =
    EventFilters.BlockAll

  override def boot(flags: Map[String, String]): Outcome[BootResult[BootData, Model]] = {
    val width  = flags("width").toInt
    val height = flags("height").toInt
    Outcome(
      BootResult(
        GameConfig(width, height).withClearColor(RGBA.Zero),
        config
      )
        .withFonts(fonts.fontsInfo.values.toSet)
        .withAssets(fonts.fontImage(Fonts.white, Fonts.m))
    )
  }

  override def setup(
      bootData: GameConfiguration,
      assetCollection: AssetCollection,
      dice: Dice
  ): Outcome[Startup[GameConfiguration]] =
    Outcome(Startup.Success(bootData))

  override def initialModel(startupData: StartUpData): Outcome[Model] =
    Outcome(IndigoModel.initial)

  override def initialViewModel(startupData: StartUpData, model: Model): Outcome[ViewModel] =
    Outcome(IndigoViewModel(model.inGameState.state, js.Array(), None))

  override def updateModel(context: Context[StartUpData], model: Model): GlobalEvent => Outcome[Model] = _ =>
    Outcome(model)

  override def updateViewModel(
      context: Context[StartUpData],
      model: Model,
      viewModel: ViewModel
  ): GlobalEvent => Outcome[ViewModel] = _ => Outcome(viewModel)

  override def present(
      context: Context[StartUpData],
      model: Model,
      viewModel: ViewModel
  ): Outcome[SceneUpdateFragment] = Outcome(SceneUpdateFragment.empty)

}
