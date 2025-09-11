package game.scenes.endgame

import assets.fonts.Fonts
import game.IndigoModel
import game.IndigoViewModel
import game.scenes.ingame.InGameScene
import game.scenes.ingame.InGameScene.InGameModel
import gamelogic.config.GameConfiguration
import indigo.*
import indigo.scenes.*
import gamelogic.GameState
import gamelogic.config.GameDifficulty
import indigo.scenes.SceneEvent.JumpTo
import game.events.DicteeGameEvent

class EndGameScene(
    fonts: Fonts
) extends Scene[GameConfiguration, IndigoModel, IndigoViewModel] {

  override def name: SceneName = EndGameScene.name

  override def modelLens: Lens[IndigoModel, SceneModel] =
    Lens(_.inGameState, _.withInGameState(_))

  override def viewModelLens: Lens[IndigoViewModel, SceneViewModel] = Lens(identity, (_, a) => a)

  override def eventFilters: EventFilters = EventFilters.AllowAll

  override def subSystems: Set[SubSystem[IndigoModel]] = Set.empty

  override def updateModel(
      context: SceneContext[GameConfiguration],
      model: InGameModel
  ): GlobalEvent => Outcome[InGameModel] = {
    case MouseEvent.Click(position)
        if !model.state.hasWon && retryButtonRectangle(context.frame.screenSize).contains(position) =>
      Outcome(model).addGlobalEvents(
        JumpTo(InGameScene.name),
        DicteeGameEvent.GameStarts()
      )
    case MouseEvent.Click(position)
        if !model.state.hasWon && retryChangeSettingsButtonRectangle(context.frame.screenSize).contains(position) =>
      org.scalajs.dom.window.location.reload()
      Outcome(model)
    case MouseEvent.Click(position)
        if model.state.hasWon && replayButtonRectangle(context.frame.screenSize).contains(position) =>
      org.scalajs.dom.window.location.reload()
      Outcome(model)
    case _ => Outcome(model)
  }

  override def updateViewModel(
      context: SceneContext[GameConfiguration],
      model: InGameModel,
      viewModel: IndigoViewModel
  ): GlobalEvent => Outcome[IndigoViewModel] = _ => Outcome(viewModel)

  def winningPresent(context: SceneContext[GameConfiguration], gameState: GameState): Outcome[SceneUpdateFragment] =
    val endgame = assets.Asset.endgame

    val trophyAsset = context.context.startUpData.difficulty match
      case GameDifficulty.Rookie          => endgame.trophy
      case GameDifficulty.P2B             => endgame.rubyTrophy
      case GameDifficulty.Parent          => endgame.rubyGemsTrophy
      case GameDifficulty.MadameCharlotte => endgame.rubyPlentyGemsTrophy

    Outcome(
      SceneUpdateFragment(
        Batch(
          trophyAsset.indigoGraphic(
            context.frame.screenSize.toPoint / 2,
            None,
            Radians.zero,
            Size(
              trophyAsset.width * context.frame.screenSize.height / trophyAsset.height,
              context.frame.screenSize.height
            )
          ),
          Text(
            "Bravo !",
            Fonts.fontKeys(Fonts.black, Fonts.xxl),
            Material.Bitmap(Fonts.assetNames(Fonts.black, Fonts.xxl))
          ).withAlignment(TextAlignment.Center)
            .withPosition(context.frame.screenSize.toPoint / Point(2, 1) + Point(0, -2 * Fonts.xxl)),
          Shape
            .Box(
              replayButtonRectangle(context.frame.screenSize),
              Fill.Color(RGBA.White)
            ),
          Text(
            "Rejoue",
            Fonts.fontKeys(Fonts.black, Fonts.l),
            Material.Bitmap(Fonts.assetNames(Fonts.black, Fonts.l))
          ).withAlignment(TextAlignment.Center)
            .withPosition(replayButtonRectangle(context.frame.screenSize).position + Point(50, 0))
        )
      )
    )

  def losingPresent(context: SceneContext[GameConfiguration], gameState: GameState): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        Batch(
          Text(
            "Oups ! Tu as perdu !",
            Fonts.fontKeys(Fonts.white, Fonts.xxl),
            Material.Bitmap(Fonts.assetNames(Fonts.white, Fonts.xxl))
          ).withAlignment(TextAlignment.Center).withPosition(context.frame.screenSize.toPoint / 2),
          Text(
            s"Tu as réussi à détruire ${gameState.score} ennemi${if gameState.score > 1 then "s"
            else ""}. Il t'en fallait encore ${GameState.scoreToWin - gameState.score} pour gagner...",
            Fonts.fontKeys(Fonts.white, Fonts.l),
            Material.Bitmap(Fonts.assetNames(Fonts.white, Fonts.l))
          ).withAlignment(TextAlignment.Center)
            .withPosition(context.frame.screenSize.toPoint / 2 + Point(0, Fonts.xxl)),
          Shape
            .Box(
              retryButtonRectangle(context.frame.screenSize),
              Fill.Color(RGBA.White)
            ),
          Text(
            "Réessaie",
            Fonts.fontKeys(Fonts.black, Fonts.l),
            Material.Bitmap(Fonts.assetNames(Fonts.black, Fonts.l))
          ).withAlignment(TextAlignment.Center)
            .withPosition(retryButtonRectangle(context.frame.screenSize).position + Point(50, 0)),
          Shape
            .Box(
              retryChangeSettingsButtonRectangle(context.frame.screenSize),
              Fill.Color(RGBA.White)
            ),
          Text(
            "Autre Config",
            Fonts.fontKeys(Fonts.black, Fonts.l),
            Material.Bitmap(Fonts.assetNames(Fonts.black, Fonts.l))
          ).withAlignment(TextAlignment.Center)
            .withPosition {
              val rect = retryChangeSettingsButtonRectangle(context.frame.screenSize)
              rect.position + Point(rect.width / 2, 0)
            }
        )
      )
    )

  def retryButtonRectangle(screenSize: Size) =
    Rectangle(screenSize.toPoint / 2 + Point(-50, Fonts.xxl + Fonts.l + 15), Size(100, Fonts.l + 2))
  def retryChangeSettingsButtonRectangle(screenSize: Size) =
    retryButtonRectangle(screenSize) + Rectangle(Point(-25, Fonts.l + 4), Size(50, 0))

  def replayButtonRectangle(screenSize: Size) =
    Rectangle(
      screenSize.toPoint / Point(2, 1) + Point(-50, -Fonts.xxl + 3),
      Size(100, Fonts.l + 2)
    )

  override def present(
      context: SceneContext[GameConfiguration],
      model: InGameModel,
      viewModel: IndigoViewModel
  ): Outcome[SceneUpdateFragment] =
    if model.state.hasWon then winningPresent(context, model.state)
    else losingPresent(context, model.state)

  type SceneModel     = InGameScene.InGameModel
  type SceneViewModel = IndigoViewModel

}

object EndGameScene {

  val name = SceneName("end game")

}
