package ui

import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.ButtonDesign
import be.doeraene.webcomponents.ui5.configkeys.LinkTarget

object HelpWindowsIsBroken {

  def apply(): HtmlElement =
    val helpModalOpenerBus = new EventBus[Boolean]

    val theBrowserType = browserType

    div(
      Text(
        "Au secours ! Les images sont toutes déformées ! ",
        Link(
          _.events.onClick.preventDefault.mapTo(true) --> helpModalOpenerBus.writer,
          "On va vous aider."
        )
      ),
      Dialog(
        _.open        <-- helpModalOpenerBus.events,
        _.slots.header := Title.h3("Comment réparer les images ?"),
        div(
          Text("Si les images vous apparaissent un peu comme l'image ci-dessous, pas de panique !"),
          img(
            src         := "/dictee-the-game/assets/menus-ui/deformed-images.png",
            alt         := "Les images sont déformées",
            maxWidth.px := 200
          ),
          Text("Suivez les étapes suivantes en fonction de votre navigateur:"),
          div(
            height.px := 400,
            TabContainer(
              width.px := 800,
              _.tab(
                _.text     := "Chrome",
                _.selected := (theBrowserType == BrowserType.Chrome),
                div(
                  Text(
                    "Rendez-vous dans les configurations système de Chrome (entrez ",
                    kbd("chrome://settings/system"),
                    " dans la barre de votre navigateur) et désactivez le 'Utiliser l'accélération graphique si disponible'. Puis relancer. Voir ci-dessous."
                  ),
                  img(
                    src         := "/dictee-the-game/assets/menus-ui/deformed-images-solution-chrome.png",
                    alt         := "Solution pour Chrome",
                    maxWidth.px := 400
                  )
                )
              ),
              _.tab(
                _.text     := "Edge",
                _.selected := (theBrowserType == BrowserType.Edge),
                div(
                  Text(
                    "Rendez-vous dans les configurations système de Edge (entrez ",
                    kbd("edge://settings/system/manageSystem"),
                    " dans la barre de votre navigateur) et désactivez l'accélération graphique. Puis relancer. Voir ci-dessous."
                  ),
                  img(
                    src         := "/dictee-the-game/assets/menus-ui/deformed-images-solution-edge.png",
                    alt         := "Solution pour Edge",
                    maxWidth.px := 400
                  )
                )
              ),
              _.tab(
                _.text     := "Firefox",
                _.selected := (theBrowserType == BrowserType.Firefox),
                div(
                  Text(
                    "Malheureusement, nous n'avons pas de solution pour Firefox :(. Essayez avec Chrome ou Edge et utilisez une des solutions pour ces navigateurs."
                  )
                )
              ),
              _.tab(
                _.text     := "Autre",
                _.selected := (theBrowserType == BrowserType.Unknown),
                div(
                  Text(
                    "Essayez Chrome ou Edge et utilisez une des solutions pour ceux-là."
                  )
                )
              )
            )
          ),
          div(width.px := 800)
        ),
        _.slots.footer := Bar(
          _.slots.endContent := Button(
            _.design := ButtonDesign.Transparent,
            "Fermer",
            _.events.onClick.mapTo(false) --> helpModalOpenerBus.writer
          )
        )
      )
    )

}
