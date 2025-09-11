package gamelogic.entities

import gamelogic.Complex

final case class Enemy(
    position: Complex,
    speed: Complex,
    words: Vector[String],
    currentLetters: Vector[Char],
    amountOfKilledWords: Int,
    spawnTime: Double
) {
  def rotation = speed.arg

  def isDead = amountOfKilledWords >= words.length

  def killWord = copy(amountOfKilledWords = amountOfKilledWords + 1)

  def nextWords = words.drop(amountOfKilledWords)

  def nextWordIsKilled = nextWords.headOption.exists(word => currentLetters.mkString == word)

  def hitByChar(c: Char) = {
    val newCurrentLettersCandidate = currentLetters :+ c
    if nextWords.headOption.exists(word => word.startsWith(newCurrentLettersCandidate.mkString)) then
      val newLetterAdded = copy(currentLetters = newCurrentLettersCandidate)
      if newLetterAdded.nextWordIsKilled then
        copy(currentLetters = Vector.empty, amountOfKilledWords = amountOfKilledWords + 1)
      else newLetterAdded
    else this
  }
  def nextPosition(deltaTime: Double) = position + speed * deltaTime

  def move(deltaTime: Double) = copy(position = nextPosition(deltaTime))

  def hasHitPlayer(playerPos: Complex): Boolean =
    (position - playerPos).modulus2 < 100
}

object Enemy {

  def init(position: Complex, speed: Complex, spawnTime: Double, words: Vector[String]): Enemy =
    Enemy(position, speed, words, currentLetters = Vector(), amountOfKilledWords = 0, spawnTime = spawnTime)

}
