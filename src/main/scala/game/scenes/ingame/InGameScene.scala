package game.scenes.ingame

import scala.scalajs.js
import gamelogic.GameState
import indigo.*
import indigo.scenes.*
import assets.fonts.Fonts
import gamelogic.config.GameConfiguration
import game.IndigoModel
import game.IndigoViewModel
import assets.Asset
import assets.Asset.parallax
import game.events.DicteeGameEvent
import indigo.scenes.SceneEvent.JumpTo
import game.scenes.endgame.EndGameScene
import scala.scalajs.js.JSConverters.*
import game.gameutils.gameToLocal
import gamelogic.Complex
import gamelogic.config.Background

class InGameScene(
    fonts: Fonts
) extends Scene[GameConfiguration, IndigoModel, IndigoViewModel] {
  type SceneModel     = InGameScene.InGameModel
  type SceneViewModel = IndigoViewModel

  override def name: SceneName = InGameScene.name

  override def modelLens: Lens[IndigoModel, SceneModel] =
    Lens(_.inGameState, _.withInGameState(_))

  override def viewModelLens: Lens[IndigoViewModel, SceneViewModel] = Lens(identity, (_, a) => a)

  override def eventFilters: EventFilters = EventFilters.AllowAll

  override def subSystems: Set[SubSystem[IndigoModel]] = Set.empty

  override def updateModel(
      context: SceneContext[GameConfiguration],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {
    case FrameTick =>
      val deltaTime = context.frame.time.delta.toDouble
      val newState =
        model.state.copy(time = model.state.time + deltaTime, playerPosition = -context.frame.screenSize.width / 2 + 20)

      val withNewEnemy = newState.spawnNewEnemyIfNeeded(
        worldHeight = context.frame.screenSize.height.toDouble,
        worldRight = context.frame.screenSize.width / 2,
        config = context.context.startUpData,
        dice = context.frame.dice
      )

      val withEnemiesMoved = withNewEnemy.moveAllEnemies(deltaTime)

      val (playerHits, stillThere) =
        withEnemiesMoved.entities.partition(_.hasHitPlayer(withEnemiesMoved.playerPosition))

      Outcome(InGameScene.InGameModel(withEnemiesMoved.copy(entities = stillThere))).addGlobalEvents(
        Batch(playerHits.map(_ => DicteeGameEvent.PlayerWasHit()))
      )
    case kbd: KeyboardEvent.KeyUp =>
      val char = kbd.key.key.toUpperCase

      if char.length == 1 then
        val state          = model.state
        val withEnemiesHit = state.charIsHit(char.head)

        val (deadEnemies, aliveEnemies) = withEnemiesHit.entities.partition(_.isDead)
        Outcome(
          model.withState(
            withEnemiesHit.copy(
              entities = aliveEnemies,
              lastEnemyDiedTime =
                if deadEnemies.nonEmpty then context.frame.time.running.toDouble else withEnemiesHit.lastEnemyDiedTime
            )
          )
        ).addGlobalEvents(
          Batch(deadEnemies.map(enemy => DicteeGameEvent.EnemyDied(enemy.position)))
        )
      else Outcome(model)
    case DicteeGameEvent.EnemyDied(position) =>
      val withNewScore = model.state.increaseScore
      if withNewScore.ended then Outcome(model.withState(withNewScore)).addGlobalEvents(JumpTo(EndGameScene.name))
      else Outcome(model.withState(withNewScore))
    case DicteeGameEvent.PlayerWasHit() =>
      val withPlayerHit = model.state.playerIsHit
      if withPlayerHit.ended then Outcome(model.withState(withPlayerHit)).addGlobalEvents(JumpTo(EndGameScene.name))
      else Outcome(model.withState(withPlayerHit))
    case DicteeGameEvent.GameStarts() =>
      Outcome(
        InGameScene.initialModel.modifyState(
          _.copy(startTime = context.frame.time.running.toDouble, time = context.frame.time.running.toDouble)
        )
      )
    case _ => Outcome(model)
  }

  override def updateViewModel(
      context: SceneContext[GameConfiguration],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] = {
    case DicteeGameEvent.EnemyDied(position) =>
      Outcome(viewModel.shipIsKilled(context.frame.time.running, position))
    case DicteeGameEvent.PlayerWasHit() =>
      Outcome(viewModel.playerIsHit(context.frame.time.running))
    case _ => Outcome(viewModel.withGameState(model.state))
  }

  def background(chosenBackground: Background, time: Seconds, screenSize: Size): Batch[SceneNode] = {

    /** Uses twice the same asset (that coincides on both edges) to create a parallax effect.
      *
      * The speed is in pixels per second.
      *
      * The "first" occurrence goes from 0 to its width, the second one from -width to 0.
      *
      * @param speed
      * @param width
      * @return
      */
    def makeParallax(asset: Asset, speed: Double) =
      val assetWidthInWorldCoords = asset.width * screenSize.height / asset.height
      val firstAssetPosition      = assetWidthInWorldCoords + (time.toDouble * (-speed)).toInt % assetWidthInWorldCoords
      val secondAssetPosition     = firstAssetPosition - assetWidthInWorldCoords

      Batch(
        asset.indigoGraphic(
          screenSize.toPoint / 2 + Point(firstAssetPosition, 0),
          None,
          Radians.zero,
          Size(assetWidthInWorldCoords, screenSize.height)
        ),
        asset.indigoGraphic(
          screenSize.toPoint / 2 + Point(secondAssetPosition + 1, 0),
          None,
          Radians.zero,
          Size(assetWidthInWorldCoords, screenSize.height)
        )
      )

    chosenBackground match
      case Background.Forest =>
        import Asset.ingame.parallax.forest.*
        makeParallax(forestSky, 10) ++
          makeParallax(forestMountain, 40) ++
          makeParallax(forestBack, 70) ++
          makeParallax(forestMid, 100) ++
          makeParallax(forestShort, 140) ++
          makeParallax(forestMoon, 1)
      case Background.Desert =>
        import Asset.ingame.parallax.desert.*
        makeParallax(desertSky, 10) ++
          makeParallax(desertCloud, 30) ++
          makeParallax(desertMountain, 50) ++
          makeParallax(desertDuneMid, 80)
      case Background.Sky =>
        import Asset.ingame.parallax.skies.*
        makeParallax(skySky, 0) ++
          makeParallax(skyClouds, 10) ++
          makeParallax(skyBackMountain, 20) ++
          makeParallax(skyCloudSingle, 50) ++
          makeParallax(skyFrontMountain, 40) ++
          makeParallax(skyCloudFloor2, 60) ++
          makeParallax(skyCloudFloor, 80)
  }

  def ship(screenSize: Size, lastPlayerHit: Option[Seconds], time: Seconds) = {
    val asset = Asset.playership.sprite_player_spaceship_up_down

    val material = lastPlayerHit.map(time.toDouble - _.toDouble).filter(_ < 2.0) match
      case None => Asset.playership.sprite_player_spaceship_up_down.indigoBitmap
      case Some(delta) =>
        Asset.playership.sprite_player_spaceship_up_down.indigoImageEffects.withAlpha(
          (1 + math.cos(3 * delta * math.Pi)) / 2
        )

    val clip =
      Clip(
        asset.width / 7,
        asset.height,
        ClipSheet(7, FPS(10)),
        ClipPlayMode.default,
        material
      ).withPosition(game.gameutils.gameToLocal(-screenSize.width / 2)(Rectangle(screenSize)))
        .withRef(Point(0, asset.height / 2))
        .withScale(Vector2(0.3))

    Batch[SceneNode](clip)
  }

  def drawEnemies(state: GameState, screenSize: Size) = {
    import game.gameutils.gameToLocal
    import assets.fonts.Fonts

    val asset = assets.Asset.ingame.enemies.zigZag

    Batch(
      state.entities.flatMap { enemy =>
        val position = gameToLocal(enemy.position)(Rectangle(screenSize))

        val wordPosition = position + Point(-20, -50)

        js.Array(
          Clip(asset.gridSize, ClipSheet(asset.grid._1, FPS(10)), ClipPlayMode.default, asset.indigoBitmap)
            .withPosition(position)
            .withRef(asset.gridCenter)
            .withRotation(Radians(-enemy.rotation) + Radians.PI)
            .withScale(Vector2(0.2)),
          Text(
            enemy.words.headOption.mkString,
            Fonts.fontKeys(Fonts.black, Fonts.l),
            Material.Bitmap(Fonts.assetNames(Fonts.black, Fonts.l))
          )
            .withPosition(wordPosition)
            .withAlignment(TextAlignment.Left),
          Text(
            enemy.currentLetters.mkString,
            Fonts.fontKeys(Fonts.red, Fonts.l),
            Material.Bitmap(Fonts.assetNames(Fonts.red, Fonts.l))
          )
            .withPosition(wordPosition)
            .withAlignment(TextAlignment.Left)
        )
      }
    )
  }

  def drawExplosions(
      killedShips: js.Array[IndigoViewModel.KilledShipInfo],
      time: Seconds,
      screenSize: Size
  ): Batch[SceneNode] =
    val asset                      = Asset.ingame.enemies.explosion
    val explosionTime              = Seconds(0.5)
    val fps                        = FPS(asset.gridCount * (1 / explosionTime.toDouble).toInt)
    def gamePosition(pos: Complex) = gameToLocal(pos)(Rectangle(screenSize))

    Batch(
      killedShips
        .filter(time - _.time <= explosionTime)
        .map(info =>
          asset.animatedGridIndigoGraphic(
            gamePosition(info.position),
            Size(200, 150),
            time,
            fps
          )
        )
    )

  def displayPlayerInfo(
      state: GameState,
      screenSize: Size
  ) = {
    def text(content: String) =
      Text(content, Fonts.fontKeys(Fonts.white, Fonts.l), Material.Bitmap(Fonts.assetNames(Fonts.white, Fonts.l)))

    val redHearts = Asset.ingame.ui.health.redHearts

    def filledHeart(position: Point, targetSize: Size) = redHearts.gridIndigoGraphic(
      position,
      targetSize,
      (0, 0)
    )

    def emptyHeart(position: Point, targetSize: Size) = redHearts.gridIndigoGraphic(
      position,
      targetSize,
      (2, 0)
    )

    val heartSize = Size(32)

    val emptyHearts =
      (0 until GameState.maxLife)
        .map(index => emptyHeart(Point(index * heartSize.width, 0) + heartSize.toPoint / 2, heartSize))
        .toJSArray
    val filledHearts =
      (0 until state.playerLife)
        .map(index => filledHeart(Point(index * heartSize.width, 0) + heartSize.toPoint / 2, heartSize))
        .toJSArray

    val runningTime = state.runningTime.toDouble
    val minutes     = math.floor(runningTime / 60)
    val seconds     = math.floor(runningTime - minutes * 60)

    Batch(emptyHearts) ++ Batch(filledHearts) ++
      Batch(
        text(s"Score: ${state.score}").withPosition(Point(5, 5 + heartSize.height)),
        text(s"Temps: $minutes:${String.format("%02d", seconds)}")
          .withAlignment(TextAlignment.Right)
          .withPosition(Point(screenSize.width, 0) + Point(-5, 5))
      )
  }

  override def present(
      context: SceneContext[GameConfiguration],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      SceneUpdateFragment(
        background(context.startUpData.background, context.frame.time.running, context.frame.screenSize) ++
          ship(context.frame.screenSize, viewModel.lastPlayerHit, context.frame.time.running) ++
          drawExplosions(viewModel.previouslyKilledShips, context.frame.time.running, context.frame.screenSize) ++
          drawEnemies(model.state, context.frame.screenSize) ++
          displayPlayerInfo(model.state, context.frame.screenSize)
      )
    )
}

object InGameScene {

  class InGameModel(val state: GameState) extends js.Object {
    inline def withState(newState: GameState): InGameModel = InGameModel(newState)

    inline def modifyState(f: GameState => GameState): InGameModel =
      withState(f(state))
  }

  def initialModel: InGameModel = InGameModel(
    GameState.initial
  )

  val name = SceneName("in game")

}
