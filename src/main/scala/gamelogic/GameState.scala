package gamelogic

import scala.scalajs.js
import gamelogic.entities.Enemy
import gamelogic.config.GameConfiguration
import indigo.shared.dice.Dice

final class GameState(
    val startTime: Double,
    val time: Double,
    val entities: js.Array[Enemy],
    val score: Int,
    val playerPosition: Complex,
    val playerLife: Int,
    val lastSpawnTime: Double,
    val lastEnemyDiedTime: Double
) extends js.Object {

  def copy(
      startTime: Double = startTime,
      time: Double = time,
      entities: js.Array[Enemy] = entities,
      score: Int = score,
      playerPosition: Complex = playerPosition,
      playerLife: Int = playerLife,
      lastSpawnTime: Double = lastSpawnTime,
      lastEnemyDiedTime: Double = lastEnemyDiedTime
  ): GameState =
    GameState(startTime, time, entities, score, playerPosition, playerLife, lastSpawnTime, lastEnemyDiedTime)

  def runningTime: indigo.Seconds = indigo.Seconds(time - startTime)

  def ended: Boolean = playerLife <= 0 || hasWon

  def hasWon: Boolean = score >= GameState.scoreToWin

  def increaseScore: GameState = copy(score = score + 1)

  def playerIsHit: GameState = copy(playerLife = playerLife - 1)

  def newEnemy(worldHeight: Double, worldRight: Complex, config: GameConfiguration, dice: Dice): Enemy = {
    val position   = Complex(worldRight.re, (dice.rollDouble * 2 - 1) * worldHeight / 2)
    val worldWidth = worldRight.re * 2

    val speedModulus = worldWidth / secondsToCrossScreen * (config.enemySpeedMultiplier.min(5.0))

    val speed = Complex.polar(speedModulus, (playerPosition - position).arg)

    // currently one word per enemy
    val word = config.dictionary(dice.roll(config.dictionary.length) - 1).toUpperCase()
    Enemy.init(position, speed, time, Vector(word))
  }

  def spawnNewEnemyIfNeeded(
      worldHeight: Double,
      worldRight: Complex,
      config: GameConfiguration,
      dice: Dice
  ): GameState = {
    val timeBetweenSpawns = secondsToCrossScreen / config.enemySpeedMultiplier // seconds

    if (time - lastSpawnTime > timeBetweenSpawns) || (time - lastEnemyDiedTime > 3.0 && entities.isEmpty) then

      val newEnemyEntity = newEnemy(worldHeight, worldRight, config, dice)

      copy(
        entities = entities :+ newEnemyEntity,
        lastSpawnTime = time
      )
    else this
  }

  def charIsHit(char: Char): GameState =
    copy(entities = entities.map(_.hitByChar(char)))

  def moveAllEnemies(deltaTime: Double): GameState =
    copy(entities = entities.map(_.move(deltaTime)))

  val secondsToCrossScreen = 30.0

}

object GameState {

  def initial: GameState =
    GameState(
      startTime = 0,
      time = 0,
      entities = js.Array(),
      score = 0,
      playerPosition = 0,
      playerLife = maxLife,
      lastSpawnTime = 0,
      lastEnemyDiedTime = 0
    )

  def scoreToWin = 30

  def maxLife = 3

}
