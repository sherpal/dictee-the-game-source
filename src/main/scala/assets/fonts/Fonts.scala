package assets.fonts

import indigo.*
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Fonts(glyphsInfo: Map[(Fonts.AllowedColor, Fonts.AllowedSize), (String, GlyphInfo)]) {

  val fontsInfo: Map[(Fonts.AllowedColor, Fonts.AllowedSize), FontInfo] = (for {
    size      <- Fonts.allowedSizes
    color     <- Fonts.allowedColors
    glyphInfo <- glyphsInfo.get(color, size).map(_._2)
  } yield (((color, size), Fonts.fontInfoFromGlyphInfo(glyphInfo)): (
      (Fonts.AllowedColor, Fonts.AllowedSize),
      FontInfo
  ))).toMap

  val fontImages = for {
    (key, (imageData, _)) <- glyphsInfo
    assetName = Fonts.assetNames(key._1, key._2)
  } yield AssetType.Image(assetName, AssetPath(imageData))

  def fontImage(colour: Fonts.AllowedColor, size: Fonts.AllowedSize) =
    val (imageData, _) = glyphsInfo((colour, size))
    AssetType.Image(Fonts.assetNames(colour, size), AssetPath(imageData))

}

object Fonts {

  type AllowedSize  = 8 | 12 | 16 | 20 | 42
  type AllowedColor = "black" | "white" | "green" | "red"

  val xs: AllowedSize  = 8
  val s: AllowedSize   = 12
  val m: AllowedSize   = 16
  val l: AllowedSize   = 20
  val xxl: AllowedSize = 42

  def white: AllowedColor = "white"
  def red: AllowedColor   = "red"
  def black: AllowedColor = "black"
  def green: AllowedColor = "green"

  val fontName = "Quicksand"

  def allGlyphFontData(alphabet: Iterable[Char]) = Future
    .sequence(
      for {
        color <- allowedColors
        size  <- allowedSizes
        key: (AllowedColor, AllowedSize) = (color, size)
      } yield createGlyphFontData(quicksand, key._2, key._1, alphabet.toVector.distinct.sorted).map(
        (data, _, glyphInfo) => key -> (data, glyphInfo)
      )
    )
    .map(_.toMap)
    .map(Fonts(_))

  private def fontInfoFromGlyphInfo(glyphInfo: GlyphInfo): FontInfo = {
    val key = fontKeys(
      glyphInfo.color.asInstanceOf[AllowedColor],
      glyphInfo.fontSize.asInstanceOf[AllowedSize]
    )

    FontInfo(
      key,
      glyphInfo.position
        .find(_.char == '?')
        .map(position => FontChar(position.char.toString, position.x, position.y, position.width, position.height))
        .get,
      glyphInfo.position.map(position =>
        FontChar(position.char.toString, position.x, position.y, position.width, position.height)
      )*
    ).makeCaseSensitive(sensitive = true)

  }

  val allowedSizes: js.Array[AllowedSize]   = js.Array(8, 12, 16, 20, 42)
  val allowedColors: js.Array[AllowedColor] = js.Array("black", "white", "green", "red")

  val fontKeys: Map[(AllowedColor, AllowedSize), FontKey] = (for {
    size  <- allowedSizes
    color <- allowedColors
  } yield (((color, size), FontKey(s"the-font-$color-$size")): (
      (AllowedColor, AllowedSize),
      FontKey
  ))).toMap

  val assetNames: Map[(AllowedColor, AllowedSize), AssetName] = (
    for {
      size  <- allowedSizes
      color <- allowedColors
    } yield ((color, size), AssetName(s"$fontName-$color-$size")): (
        (AllowedColor, AllowedSize),
        AssetName
    )
  )
    .toMap[(AllowedColor, AllowedSize), AssetName]

}
