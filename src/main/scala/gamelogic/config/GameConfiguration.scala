package gamelogic.config

import io.circe.Codec

final case class GameConfiguration(
    dictionary: Vector[String],
    difficulty: GameDifficulty,
    background: Background
) derives Codec {

  val enemySpeedMultiplier: Double = difficulty match
    case GameDifficulty.Rookie          => 1.0
    case GameDifficulty.P2B             => 2.0
    case GameDifficulty.Parent          => 10.0
    case GameDifficulty.MadameCharlotte => 40.0

  def trimEmptyWords: GameConfiguration = copy(dictionary = dictionary.map(_.trim).filterNot(_.isEmpty).distinct)

  def withRandomBackground(maybeChosen: Option[Background]): GameConfiguration =
    val chosenBackground = maybeChosen.getOrElse(Background.values(scala.util.Random.nextInt(Background.values.length)))
    copy(background = chosenBackground)

}
