package ui

import com.raquo.laminar.api.L.*
import be.doeraene.webcomponents.ui5.*
import be.doeraene.webcomponents.ui5.configkeys.LinkTarget
import be.doeraene.webcomponents.ui5.configkeys.ListMode

object About {

  def apply(): HtmlElement = div(
    Text(
      "Le code source est disponible ",
      Link("ici", _.href := "https://github.com/sherpal/dictee-the-game-source", _.target := LinkTarget._blank),
      "."
    ),
    UList(
      _.selectionMode := ListMode.None,
      _.item(
        "Le décor et les vaisseaux viennent de ",
        Link(
          _.href   := "https://bongseng.itch.io/parallax-forest-desert-sky-moon",
          _.target := LinkTarget._blank,
          "ceci"
        ),
        "."
      ),
      _.item(
        "Les coeurs de vie viennent de ",
        Link(
          _.href   := "https://redreeh.itch.io/pixelhearts-16x16",
          _.target := LinkTarget._blank,
          "ceci"
        ),
        "."
      ),
      _.item(
        "Les coupes de victoire ont été générées via ChatGPT."
      )
    )
  )

}
