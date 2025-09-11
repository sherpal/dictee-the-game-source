package game

import gamelogic.GameState
import indigo.*
import gamelogic.Complex

import scala.scalajs.js

class IndigoViewModel(
    val state: GameState,
    val previouslyKilledShips: js.Array[IndigoViewModel.KilledShipInfo],
    val lastPlayerHit: Option[Seconds]
) extends js.Object {
  def copy(
      state: GameState = state,
      previouslyKilledShips: js.Array[IndigoViewModel.KilledShipInfo] = previouslyKilledShips,
      lastPlayerHit: Option[Seconds] = lastPlayerHit
  ): IndigoViewModel = IndigoViewModel(state, previouslyKilledShips, lastPlayerHit)

  def withGameState(updatedState: GameState): IndigoViewModel =
    copy(state = updatedState)

  def shipIsKilled(time: Seconds, position: Complex): IndigoViewModel = copy(
    previouslyKilledShips =
      previouslyKilledShips.filter(time - _.time <= Seconds(5.0)) :+ IndigoViewModel.KilledShipInfo(time, position)
  )

  def playerIsHit(time: Seconds): IndigoViewModel = copy(lastPlayerHit = Some(time))
}

object IndigoViewModel {

  case class KilledShipInfo(time: Seconds, position: Complex)

}
