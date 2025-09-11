package game

import scala.scalajs.js
import game.scenes.loading.LoadingState
import indigo.Seconds
import gamelogic.GameState
import game.scenes.ingame.InGameScene

class IndigoModel(
    val inGameState: InGameScene.InGameModel,
    val loadingState: LoadingState
) extends js.Object {

  inline def withLoadingState(loadingStateUpdate: LoadingState): IndigoModel =
    IndigoModel(inGameState, loadingStateUpdate)

  inline def modifyLoadingState(fn: LoadingState => LoadingState): IndigoModel =
    withLoadingState(fn(loadingState))

  inline def withInGameState(newInGameState: InGameScene.InGameModel): IndigoModel =
    IndigoModel(newInGameState, loadingState)

}

object IndigoModel {
  def initial: IndigoModel =
    IndigoModel(
      InGameScene.initialModel,
      LoadingState.NotStarted()
    )
}
