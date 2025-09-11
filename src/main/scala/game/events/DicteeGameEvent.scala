package game.events

import gamelogic.Complex

sealed trait DicteeGameEvent extends indigo.GlobalEvent

object DicteeGameEvent {

  case class GameStarts()                 extends DicteeGameEvent
  case class EnemyDied(position: Complex) extends DicteeGameEvent
  case class PlayerWasHit()               extends DicteeGameEvent
  case class PlayerDied()                 extends DicteeGameEvent

}
