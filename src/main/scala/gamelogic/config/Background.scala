package gamelogic.config

import io.circe.Codec

enum Background derives Codec {
  case Forest
  case Desert
  case Sky
}
