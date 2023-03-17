package com.adaptionsoft.games.trivia.runner

import com.adaptionsoft.games.uglytrivia.Game
import com.adaptionsoft.games.uglytrivia.console.SystemConsole
import java.util.*

object GameRunner {
    private var notAWinner: Boolean = false

    fun runGame(
        game: Game,
        shouldUseRandom: Boolean = true,
        firstRoll: Int = -1,
        secondRoll: Int = -1,
        shouldStopAfterNumberOfQuestions: Int = 50
    ) {
        var counter = 0
        val rand = Random()

        do {
            if (shouldUseRandom) {
                game.roll(rand.nextInt(5) + 1)
                notAWinner = if (rand.nextInt(9) == 7) {
                    game.wrongAnswer()
                } else {
                    game.wasCorrectlyAnswered()
                }
            } else {
                game.roll(firstRoll)
                notAWinner = if (secondRoll == 7) {
                    game.wrongAnswer()
                } else {
                    game.wasCorrectlyAnswered()
                }
                counter++
            }
        } while (notAWinner && counter < shouldStopAfterNumberOfQuestions)
    }
}

fun main(args: Array<String>) {
    val aGame = Game()

    aGame.add("Chet")
    aGame.add("Pat")
    aGame.add("Sue")

    GameRunner.runGame(aGame)
}
