package gamelogic.config

import io.circe.Codec

enum GameDifficulty derives Codec {
  case Rookie
  case P2B
  case Parent
  case MadameCharlotte
}
