package ui

import io.circe.Codec
import org.scalajs.dom

class LocalStorage {

  def store[T](key: String, t: T)(using encoder: Codec[T]): Unit =
    dom.window.localStorage.setItem(key, encoder.apply(t).spaces2)

  def retrieve[T](key: String)(using Codec[T]): Option[T] =
    for
      encoded <- Option(dom.window.localStorage.getItem(key))
      t       <- io.circe.parser.decode[T](encoded).toOption
    yield t

}
