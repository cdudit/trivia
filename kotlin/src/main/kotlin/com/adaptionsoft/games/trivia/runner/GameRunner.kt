package com.adaptionsoft.games.trivia.runner

import com.adaptionsoft.games.uglytrivia.Game
import com.adaptionsoft.games.uglytrivia.Player
import com.adaptionsoft.games.uglytrivia.console.SystemConsole
import com.adaptionsoft.games.uglytrivia.errors.CouldNotReplayGameError
import java.util.*

object GameRunner {
    private var notAWinner: Boolean = false

    private var lastGame: Game? = null
    private var lastShouldUseRandom: Boolean = true
    private var lastFirstRoll: Int = -1
    private var lastSecondRoll: Int = -1
    private var lastShouldStopAfterNumberOfQuestions: Int = 50

    fun reset() {
        lastGame = null
    }

    fun replay() {
        if (lastGame == null) {
            throw CouldNotReplayGameError()
        }
        lastGame?.let {
            runGame(
                it,
                lastShouldUseRandom,
                lastFirstRoll,
                lastSecondRoll,
                lastShouldStopAfterNumberOfQuestions
            )
        }
    }

    fun runGame(
        game: Game,
        shouldUseRandom: Boolean = true,
        firstRoll: Int = -1,
        secondRoll: Int = -1,
        shouldStopAfterNumberOfQuestions: Int = 50
    ) {
        lastGame = game
        lastShouldUseRandom = shouldUseRandom
        lastFirstRoll = firstRoll
        lastSecondRoll = secondRoll
        lastShouldStopAfterNumberOfQuestions = shouldStopAfterNumberOfQuestions

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
    val game = Game()
    game.add(arrayListOf(
        Player("Chet"),
        Player("Pat"),
        Player("Sue"),
    ))
    GameRunner.runGame(game)
}
