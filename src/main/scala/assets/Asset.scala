package assets

import urldsl.language.dummyErrorImpl.*
import urldsl.language.PathSegment

import scala.language.implicitConversions
import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport
import scala.util.Try
import gamelogic.config.Background

class Asset private (
    val path: PathSegment[Unit, ?],
    val name: String,
    val width: Int,
    val height: Int,
    val grid: (Int, Int)
) {

  override final def equals(obj: Any): Boolean = obj match {
    case that: Asset => this.name == that.name
    case _           => false
  }

  override final def hashCode(): Int = name.hashCode()

  val assetName = indigo.AssetName(name)

  val size: indigo.Size    = indigo.Size(width, height)
  val center: indigo.Point = indigo.Point(width / 2, height / 2)

  def scaleTo(targetSize: indigo.Size): indigo.Vector2 =
    indigo.Vector2(targetSize.width / width.toDouble, targetSize.height / height.toDouble)
  def scaleTo(targetSize: Double): indigo.Vector2 =
    indigo.Vector2(targetSize / width, targetSize / height)

  def asIndigoAssetType: indigo.AssetType = indigo.AssetType.Image(
    assetName,
    indigo.AssetPath(pathStr)
  )

  def indigoBitmap       = indigo.Material.Bitmap(assetName)
  def indigoImageEffects = indigo.Material.ImageEffects(assetName)

  def indigoGraphic(
      position: indigo.Point,
      maybeTint: Option[indigo.RGBA],
      rotation: indigo.Radians,
      targetSize: indigo.Size
  ) = indigo
    .Graphic(
      indigo.Rectangle(size),
      maybeTint match
        case None       => indigoBitmap
        case Some(tint) => indigoImageEffects.withTint(tint)
    )
    .withPosition(position)
    .withRef(center)
    .withRotation(rotation)
    .withScale(scaleTo(targetSize))

  val gridSize   = indigo.Size(size.width / grid._1, size.height / grid._2)
  val gridCount  = grid._1 * grid._2
  val gridCenter = gridSize.toPoint / 2

  def gridIndigoGraphic(
      position: indigo.Point,
      targetSize: indigo.Size,
      gridCoord: (Int, Int)
  ) = indigo
    .Graphic(
      indigo.Rectangle(
        indigo.Point(gridCoord._1 * gridSize._1, gridCoord._2 * gridSize._2),
        indigo.Size(gridSize._1, gridSize._2)
      ),
      indigoBitmap
    )
    .withPosition(position)
    .withCrop(
      indigo.Rectangle(
        indigo.Point(gridCoord._1 * gridSize._1, gridCoord._2 * gridSize._2),
        indigo.Size(gridSize._1, gridSize._2)
      )
    )
    .withRef(gridCenter)
    .withScale(indigo.Vector2(targetSize.width.toDouble / gridSize._1, targetSize.height.toDouble / gridSize._2))

  def animatedGridIndigoGraphic(
      position: indigo.Point,
      targetSize: indigo.Size,
      time: indigo.Seconds,
      fps: indigo.FPS
  ) =
    val gridCellIndex = ((time.toDouble - math.floor(time.toDouble)) * fps.toDouble).toInt % gridCount
    val gridCoord     = (gridCellIndex % grid._1, gridCellIndex / grid._1)
    gridIndigoGraphic(
      position,
      targetSize,
      gridCoord
    )

  private lazy val pathStr = "/" ++ path.createPart()

}

object Asset {

  private val _allAssets = mutable.Set.empty[Asset]

  def all: Set[Asset] = _allAssets.toSet

  private def apply(path: PathSegment[Unit, ?], width: Int, height: Int, grid: (Int, Int) = (1, 1)): Asset = {
    val asset = new Asset(
      path,
      path.createSegments().last.content.reverse.dropWhile(_ != '.').tail.reverse,
      width,
      height,
      grid
    )
    _allAssets += asset
    asset
  }

  private val basePath = root / "dictee-the-game" / "assets"

  object ingame {
    private val assetpack = basePath / "assetpack"

    object parallax {
      private val parallaxP = assetpack / "parallax"

      object forest {
        private val forestP = parallaxP / "forest"

        val forestBack     = Asset(forestP / "forest_back.png", 3800, 2400)
        val forestLong     = Asset(forestP / "forest_long.png", 3800, 1200)
        val forestMid      = Asset(forestP / "forest_mid.png", 3800, 2400)
        val forestMoon     = Asset(forestP / "forest_moon.png", 3800, 2400)
        val forestMountain = Asset(forestP / "forest_mountain.png", 3800, 2400)
        val forestShort    = Asset(forestP / "forest_short.png", 3800, 2400)
        val forestSky      = Asset(forestP / "forest_sky.png", 1900, 1200)

      }

      object desert {
        private val desertP = parallaxP / "desert"

        val desertCloud     = Asset(desertP / "desert_cloud.png", 1900, 1000)
        val desertDuneFront = Asset(desertP / "desert_dunefrontt.png", 3800, 1000)
        val desertDuneMid   = Asset(desertP / "desert_dunemid.png", 1900, 1000)
        val desertMoon      = Asset(desertP / "desert_moon.png", 3800, 2400)
        val desertMountain  = Asset(desertP / "desert_mountain.png", 3800, 1000)
        val desertSky       = Asset(desertP / "desert_sky.png", 1900, 1000)
      }

      object skies {
        private val skiesP = parallaxP / "skies"

        private def a(name: String) = Asset(skiesP / ("sky_" ++ name ++ ".png"), 1900, 1000)

        val skyBackMountain  = a("back_mountain")
        val skyCloudFloor2   = a("cloud_floor_2")
        val skyCloudFloor    = a("cloud_floor")
        val skyCloudSingle   = a("cloud_single")
        val skyClouds        = a("clouds")
        val skyFrontCloud    = a("front_cloud")
        val skyFrontMountain = a("front_mountain")
        val skyMoon          = Asset(skiesP / "sky_moon.png", 3800, 2400)
        val skySky           = a("sky")
      }

      object moon {
        private val moonP = parallaxP / "moon"

        private def a(name: String, width: Int) = Asset(moonP / ("moon_" ++ name ++ ".png"), width, 1000)

        val moonBack  = a("back", 3800)
        val moonEarth = a("earth", 3800)
        val moonFloor = a("floor", 1900)
        val moonFront = a("front", 3800)
        val moonMid   = a("mid", 3800)
        val moonSky   = a("sky", 1900)
      }

      object ocean {
        private val oceanP = parallaxP / "Ocean"

        private def a(name: String) = Asset(oceanP / (name ++ ".png"), 3800, 1200)

        val oceanSkyAndSun    = a("0 ocean sky and sun")
        val oceanSea          = a("1 ocean sea")
        val oceanSunLight     = a("2 ocean sun light")
        val oceanClouds       = a("3 ocean clouds")
        val oceanBackMountain = a("4 ocean back mountain")
        val oceanSand         = a("5 ocean sand")
        val oceanWave         = a("6 ocean wave")

      }

    }

    object playership {
      private val playershipP = assetpack / "player-ship"

      val sprite_player_spaceship_up_down = Asset(playershipP / "sprite_player_spaceship_up_down.png", 2450, 150)
    }

    object enemies {
      private val enemiesP = assetpack / "enemies"

      val zigZag = Asset(enemiesP / "enemy_zig_zag-sheet.png", 1448, 214, (4, 1))
      val enemyZ = Asset(enemiesP / "enemy-z.png", 67, 55)

      val explosion = Asset(enemiesP / "explosion-sheet.png", 2800, 300, (7, 1))
    }

    object ui {
      private val uiP = basePath / "ui"

      object health {
        private val healthP = uiP / "health"

        val redHearts = Asset(healthP / "red-hearts.png", 48, 16, grid = (3, 1))
      }
    }
  }

  object endgame {
    private val charlesP = basePath / "charles"

    val trophy               = Asset(charlesP / "emerald-trophy.png", 1024, 1536)
    val rubyTrophy           = Asset(charlesP / "ruby-trophy.png", 1024, 1536)
    val rubyGemsTrophy       = Asset(charlesP / "ruby-gems.png", 1024, 1536)
    val rubyPlentyGemsTrophy = Asset(charlesP / "ruby-plenty-gems.png", 1024, 1536)
  }
  endgame

  val parallax = ingame.parallax

  def initializeBackground(background: Background): Unit = background match
    case Background.Forest =>
      ingame.parallax.forest
      ()
    case Background.Desert =>
      ingame.parallax.desert
      ()
    case Background.Sky =>
      ingame.parallax.skies
      ()
    case Background.Moon =>
      ingame.parallax.moon
      ()
    case Background.Ocean =>
      ingame.parallax.ocean
      ()

  val playership = ingame.playership

  ingame.ui.health

  ingame.enemies

}
