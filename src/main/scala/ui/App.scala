package ui

import com.raquo.laminar.api.L.*
import assets.fonts.Fonts
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.InputType
import game.TheGame
import gamelogic.config.GameConfiguration
import gamelogic.config.GameDifficulty
import be.doeraene.webcomponents.ui5.configkeys.ValueState
import be.doeraene.webcomponents.ui5.configkeys.ToastPlacement
import be.doeraene.webcomponents.ui5.configkeys.LinkTarget
import be.doeraene.webcomponents.ui5.configkeys.ButtonDesign
import gamelogic.config.Background

object App {

  val dictation1 = Vector(
    "colle",
    "stylo",
    "gomme",
    "ciseaux",
    "cahier",
    "cartable",
    "latte",
    "trousse",
    "crayon",
    "livre"
  )

  val dictation2 = Vector(
    "lundi",
    "mardi",
    "mercredi",
    "jeudi",
    "vendredi",
    "samedi",
    "dimanche"
  )

  val dictations = Vector(dictation1, dictation2)

  def apply(storage: LocalStorage)(using ExecutionContext): HtmlElement = {
    def fullAlphabet(dictionary: Vector[String]) =
      assets.fonts.alphabet ++ dictionary.flatten.flatMap(c => Vector(c.toLower, c.toUpper))

    val canvasId = "the-canvas"

    val startGameBus         = new EventBus[Unit]
    val gameStartedBus       = new EventBus[Boolean]
    val gameStartedSignal    = gameStartedBus.events.startWith(false)
    val aboutDialogOpenerBus = new EventBus[Unit]
    val aboutDialogCloseBus  = new EventBus[Unit]

    val defaultConfig = GameConfiguration(
      dictation1,
      GameDifficulty.Rookie,
      Background.Desert
    )

    val storedConfigKey = "dictee-stored-config"
    val initialConfig = storage
      .retrieve[GameConfiguration](storedConfigKey)
      .getOrElse(defaultConfig)
    val gameConfigVar = Var(initialConfig)

    def dictionaryWarning(dictionary: Vector[String]) =
      dictionary
        .find(word => word.trim.nonEmpty && word.contains(" "))
        .map(word => s"Les mots ne peuvent pas contenir d'espace, mais tu as mis `$word`.")
        .orElse(Option.when(dictionary.forall(_.trim.isEmpty))("Tu dois ajouter des mots à la liste"))

    val dictionaryWarningSignal = gameConfigVar.signal.map(_.dictionary).map(dictionaryWarning)

    val difficultySignal = gameConfigVar.signal.map(_.difficulty)
    val difficultyUpdater =
      gameConfigVar.updater[GameDifficulty]((config, difficulty) => config.copy(difficulty = difficulty))

    val dictionaryUpdater = gameConfigVar.updater[Vector[String]]((config, words) => config.copy(dictionary = words))

    val configHasErrorSignal = gameConfigVar.signal.map(config => dictionaryWarning(config.dictionary).nonEmpty)

    val difficultyMap = GameDifficulty.values.map { difficulty =>
      val display = difficulty match
        case GameDifficulty.Rookie          => s"🔰 Rookie"
        case GameDifficulty.P2B             => s"🧒 P2B"
        case GameDifficulty.Parent          => s"👴👵 Parents"
        case GameDifficulty.MadameCharlotte => s"🙂 Madame Charlotte"
      display -> difficulty
    }.toVector

    val launchErrorBus = new EventBus[Unit]

    div(
      Title.h1("Entraine-toi à la dictée !"),
      div(
        display <-- gameStartedSignal.map(if _ then "none" else "block"),
        Form(
          _.headerText := "Choisis tes mots et ta difficulté",
          _.layout     := "S1 M1 L1 XL1",
          _.labelSpan  := "S12 M12 L12 XL12",
          _.item(
            div(
              Label("Difficulté")
            ),
            Text(
              Select(
                difficultyMap.map((display, difficulty) =>
                  Select.option(
                    display,
                    _.selected <-- difficultySignal.map(_ == difficulty),
                    _.value     := display
                  )
                ),
                _.events.onChange
                  .map(_.detail.selectedOption.maybeValue.get)
                  .map(difficultyMap.toMap.apply(_)) --> difficultyUpdater
              ),
              Label(
                marginLeft := "0.5em",
                child.text <-- difficultySignal.map {
                  case GameDifficulty.Rookie          => "Découvre le jeu et ton clavier."
                  case GameDifficulty.P2B             => "Prépare-toi pour ta dictée."
                  case GameDifficulty.Parent          => "Tu penses que tes parents peuvent réussir ?"
                  case GameDifficulty.MadameCharlotte => "Personne ne peut battre Madame Charlotte."
                }
              )
            )
          ),
          _.item(
            div(
              Label("Les mots pour le jeu"),
              Text("Mets un mot par ligne dans la liste à droite, ils seront utilisés pour détruire tes enemis !"),
              Text(
                paddingTop    := "0.5em",
                paddingBottom := "0.5em",
                "Astuce: pour découvrir ton clavier, tu peux mettre les lettres de l'",
                Link(
                  "alphabet",
                  _.events.onClick.preventDefault
                    .mapTo(assets.fonts.alphabetLetters.map(_.toString)) --> dictionaryUpdater
                ),
                "."
              ),
              dictations.zipWithIndex.map((dictation, index) =>
                Text(
                  "Entraine toi sur la ",
                  Link(s"dictée ${index + 1}", _.events.onClick.preventDefault.mapTo(dictation) --> dictionaryUpdater),
                  "."
                )
              ),
              Text(
                paddingTop := "0.5em",
                "Essaie ",
                Link(
                  "toutes les dictées",
                  _.events.onClick.preventDefault.mapTo(dictations.flatten) --> dictionaryUpdater
                ),
                " en même temps !"
              )
            ),
            Text(
              TextArea(
                _.rows           := 15,
                _.growingMaxRows := 20,
                _.growing        := true,
                _.value         <-- gameConfigVar.signal.map(_.dictionary.mkString("\n")),
                _.events.onChange.mapToValue.map(_.split("\n").toVector) --> dictionaryUpdater,
                _.valueState <-- dictionaryWarningSignal.map {
                  case None    => ValueState.None
                  case Some(_) => ValueState.Negative
                },
                _.slots.valueStateMessage := div(
                  child.text <-- dictionaryWarningSignal.map(_.mkString)
                )
              )
            )
          )
        ),
        Bar(
          _.slots.startContent := Button(
            "Lance le jeu !",
            _.events.onClick.mapToUnit --> startGameBus.writer,
            _.disabled <-- gameStartedSignal
          ),
          _.slots.endContent := Button(
            _.design := ButtonDesign.Transparent,
            "Réinitialiser",
            _.events.onClick.mapTo(defaultConfig) --> gameConfigVar.writer
          ),
          _.slots.endContent := Button(
            _.design := ButtonDesign.Transparent,
            _.events.onClick.mapToUnit --> aboutDialogOpenerBus.writer,
            "À propos"
          )
        )
      ),
      div(
        div(
          idAttr       := canvasId,
          maxHeight.px := 720,
          maxWidth.px  := 1000
        ),
        child.maybe <-- gameStartedSignal.map(_ && isWindows).map(Option.when(_)(HelpWindowsIsBroken()))
      ),
      startGameBus.events.sample(gameConfigVar.signal).withCurrentValueOf(configHasErrorSignal).collect {
        case (config, false) => config
      } --> Observer[GameConfiguration] { config =>
        gameStartedBus.writer.onNext(true)
        Fonts.allGlyphFontData(fullAlphabet(config.dictionary)).onComplete {
          case Failure(exception) => throw exception
          case Success(fonts) =>
            storage.store(storedConfigKey, config)

            val theGame = TheGame(fonts, config.trimEmptyWords.withRandomBackground())

            theGame.launch(
              canvasId,
              "width"  -> "1000",
              "height" -> "720"
            )
        }
      },
      startGameBus.events.sample(configHasErrorSignal).filter(identity).mapToUnit --> launchErrorBus.writer,
      Toast(
        _.open     <-- launchErrorBus.events.mapTo(true),
        _.placement := ToastPlacement.BottomCenter,
        "Il y a des erreurs dans la configuration du jeu, cherche les trucs rouges"
      ),
      Dialog(
        _.open <-- aboutDialogOpenerBus.events.mapTo(true),
        _.closeFromEvents(aboutDialogCloseBus.events),
        _.headerText := "À propos",
        About(),
        _.slots.footer := Bar(
          _.slots.endContent := Button(
            _.design := ButtonDesign.Transparent,
            "Fermer",
            _.events.onClick.mapToUnit --> aboutDialogCloseBus.writer
          )
        )
      )
    )

  }

}
