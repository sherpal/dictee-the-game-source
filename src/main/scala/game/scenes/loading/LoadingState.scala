package game.scenes.loading

enum LoadingState {
  case NotStarted()
  case InProgress(percentage: Int)
  case Error(key: String, message: String)
}
