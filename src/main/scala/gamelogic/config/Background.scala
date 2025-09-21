package gamelogic.config

import assets.fonts.Fonts
import io.circe.Codec

enum Background derives Codec {
  case Forest
  case Desert
  case Sky
  case Moon
  case Ocean

  def textColour: Fonts.AllowedColor = this match
    case Forest => Fonts.black
    case Desert => Fonts.black
    case Sky    => Fonts.black
    case Moon   => Fonts.white
    case Ocean  => Fonts.black

}
