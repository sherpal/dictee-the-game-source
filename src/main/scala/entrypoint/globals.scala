package entrypoint

import com.raquo.laminar.api.L._
import org.scalajs.dom
import scala.concurrent.ExecutionContext.Implicits.global
import assets.fonts.Fonts
import scala.util.Failure
import scala.util.Success
import ui.App
import ui.LocalStorage

@main def run(): Unit = {
  println("Hello, world!")

  val storage = LocalStorage()

  render(dom.document.getElementById("root"), App(storage))

}
