package assets.fonts

final case class GlyphInfo(
    imageWidth: Int,
    imageHeight: Int,
    fontSize: Int,
    color: String,
    position: Vector[GlyphInfo.GlyphPosition]
)

object GlyphInfo {

  case class GlyphPosition(char: Char, x: Int, y: Int, width: Int, height: Int)

}
