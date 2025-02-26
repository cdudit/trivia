package com.adaptionsoft.games.trivia.runner

import com.adaptionsoft.games.uglytrivia.Game
import com.adaptionsoft.games.uglytrivia.Player
import com.adaptionsoft.games.uglytrivia.console.SystemConsole
import com.adaptionsoft.games.uglytrivia.errors.CouldNotReplayGameError
import java.util.*

object GameRunner {
    private var lastGame: Game? = null
    private var lastShouldUseRandom: Boolean = true
    private var lastShouldGoInPrison: Boolean = true
    private var lastHasCorrectAnswered: Boolean = true
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
                lastShouldGoInPrison,
                lastHasCorrectAnswered,
                lastShouldStopAfterNumberOfQuestions
            )
        }
    }

    fun runGame(
        game: Game,
        shouldUseRandom: Boolean = true,
        shouldGoInPrison: Boolean = true,
        hasCorrectAnswered: Boolean = true, // = shouldStayInPrison,
        shouldStopAfterNumberOfQuestions: Int = 50
    ) {
        lastGame = game
        lastShouldUseRandom = shouldUseRandom
        lastShouldGoInPrison = shouldGoInPrison
        lastHasCorrectAnswered = hasCorrectAnswered
        lastShouldStopAfterNumberOfQuestions = shouldStopAfterNumberOfQuestions

        var counter = 0
        val rand = Random()

        do {
            if (shouldUseRandom) {
                if (game.currentPlayerIsInJail) {
                    game.roll(rand.nextInt(game.getChancesToExitJail()))
                }
                if (!game.currentPlayerIsInJail) {
                    game.roll(rand.nextInt(5) + 1)
                }
                if (rand.nextInt(9) == 7) {
                    game.wrongAnswer()
                } else {
                    game.wasCorrectlyAnswered()
                }
            } else {
                if (hasCorrectAnswered) {
                    game.roll(1)
                } else {
                    game.roll(2)
                }
                val secondRoll = if (shouldGoInPrison) 7 else 2
                if (secondRoll == 7) {
                    game.wrongAnswer()
                } else {
                    game.wasCorrectlyAnswered()
                }
            }
            counter++
        } while (!game.isLeaderboardComplete && counter < shouldStopAfterNumberOfQuestions)
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
