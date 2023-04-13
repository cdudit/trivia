package com.adaptionsoft.games.trivia

import com.adaptionsoft.games.trivia.runner.GameRunner
import com.adaptionsoft.games.uglytrivia.Game
import com.adaptionsoft.games.uglytrivia.Player
import com.adaptionsoft.games.uglytrivia.errors.PlayersNumberError
import com.adaptionsoft.games.uglytrivia.console.SpyConsole
import com.adaptionsoft.games.uglytrivia.errors.CouldNotReplayGameError
import com.adaptionsoft.games.uglytrivia.errors.MinimalGoldRequiredNotEnoughError
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random

class SomeTest {
    private var gameRunner = GameRunner

    @BeforeEach
    fun reset() {
        gameRunner.reset()
    }

    //region PLAYERS NUMBER [BUG #1 #2]
    @Test
    fun `game should run if player are 2 or more and 6 or less`() {
        val game = Game().apply {
            add(arrayListOf(Player("Gatien"), Player("Gatien")))
        }
        assertDoesNotThrow {
            gameRunner.runGame(game)
        }
    }

    @Test
    fun `game should not run if player are less than 2`() {
        assertThrows<PlayersNumberError> {
            val game = Game().apply {
                add(arrayListOf(Player("Gatien")))
            }
            gameRunner.runGame(game)
        }
    }

    @Test
    fun `game should not run if player are more than 6`() {
        assertThrows<PlayersNumberError> {
            val game = Game().apply {
                add(arrayListOf(Player("Gatien"), Player("Gatien"), Player("Gatien"), Player("Gatien"), Player("Gatien"), Player("Gatien"), Player("Gatien"), Player("Gatien")))
            }
            gameRunner.runGame(game)
        }
    }
    //endregion

    //region STAY IN PRISON [BUG #3]
    @Test
    fun `should stay in prison`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player("Arthur"), Player("Arthur")))
        }
        gameRunner.runGame(game, shouldUseRandom = false, shouldGoInPrison = true, hasCorrectAnswered = false)
        assert(!console.getContent().contains("Arthur is getting out of the penalty box"))
    }

    @Test
    fun `should quit prison`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player("Arthur"), Player("Arthur")))
        }
        gameRunner.runGame(game, shouldUseRandom = false, shouldGoInPrison = true, hasCorrectAnswered = true)
        assert(console.getContent().contains("Arthur is getting out of the penalty box"))
    }
    //endregion

    //region QUESTIONS DISTRIBUTION SHOULD BE PROPORTIONAL [BUG #4]
    @Test
    fun `questions distribution should be proportional`() {
        val game = Game().apply {
            add(arrayListOf(Player(name = "Gatien"), Player(name = "Gatien")))
        }

        // Récupération des catégories utilisées (en fonction de l'expansion pack)
        val array = game.getQuestionDistribution().filter { it.second > 0 }

        // On vérifie que le nombre de question de la catégorie courante est égal à celui de la prochaine
        array.forEachIndexed { index, pair ->
            if (index + 1 < array.size) {
                println(pair.toString() + " == " + array[index + 1].toString())
                assertEquals(pair.second, array[index + 1].second)
            }
        }
    }
    //endregion

    //region INFINITE QUESTIONS [BUG #5]
    @Test
    fun `number of questions should be infinite`() {
        val game = Game().apply {
            add(arrayListOf(Player("Gatien"), Player("Gatien")))
        }
        val random = Random.nextInt(10, 100)

        assertDoesNotThrow {
            gameRunner.runGame(
                game,
                shouldUseRandom = false,
                hasCorrectAnswered = false,
                shouldGoInPrison = true,
                shouldStopAfterNumberOfQuestions = random
            )
        }

        println("\nNumber of roll: $random\n")
    }
    //endregion

    //region FEATURE REPLACE ROCK BY TECHNO [FEATURE #1]
    @Test
    fun `without replacing category is rock and not techno`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player("Gatien"), Player("Gatien")))
        }
        gameRunner.runGame(game)
        assert(console.getContent().contains("The category is ROCK"))
        assert(!console.getContent().contains("The category is TECHNO"))
    }

    @Test
    fun `should replace rock by techno`() {
        val console = SpyConsole()
        val game = Game(console, shouldReplaceRockByTechno = true).apply {
            add(arrayListOf(Player("Gatien"), Player("Gatien")))
        }
        gameRunner.runGame(game)
        assert(console.getContent().contains("The category is TECHNO"))
        assert(!console.getContent().contains("The category is ROCK"))
    }
    //endregion

    //region PLAYER CAN USE JOKER [FEATURE #2]
    @Test
    fun `player can use joker, doesn't answer and doesn't win gold`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player("Gatien"), Player("Louis", hasJoker = true)))
        }
        gameRunner.runGame(game)
        assert(console.getContent().contains("Louis uses his jokerGatien is the current player"))
    }
    //endregion

    //region DOES NOT WANT TO ANSWER [FEATURE #3]
    @Test
    fun `player doesn't want to answer the question and leave the game`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player("Louis"), Player("Gatien", doesNotWantToAnswer = true)))
        }
        gameRunner.runGame(game)
        assert(console.getContent().contains("Gatien doesn't want to answer and left the game"))
        val afterPlayerLeft = console.getContent().split("Gatien doesn't want to answer and left the game")[1]
        assert(!afterPlayerLeft.contains("Gatien"))
    }
    //endregion

    //region CORRECT ANSWERS IN A ROW [FEATURE #4]
    @Test
    fun `player earns X GP when giving X correct answers in a row`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player(name = "Gatien"), Player(name = "Louis")))
        }
        GameRunner.runGame(game, shouldUseRandom = false, hasCorrectAnswered = true, shouldGoInPrison = false)
        assert(console.getContent().contains("Gatien now has 1 Gold Coins"))
        assert(!console.getContent().contains("Gatien now has 2 Gold Coins"))
        assert(console.getContent().contains("Gatien now has 3 Gold Coins"))
        assert(!console.getContent().contains("Gatien now has 4 Gold Coins"))
        assert(!console.getContent().contains("Gatien now has 5 Gold Coins"))
        assert(console.getContent().contains("Gatien now has 6 Gold Coins"))
    }
    //endregion

    //TODO Feature 5

    //region MINIMAL GOLD REQUIRED [FEATURE #6]
    @Test
    fun `game with less than 6 gold should throw`() {
        assertThrows<MinimalGoldRequiredNotEnoughError> {
            val game = Game(minimalGoldRequired = 5).apply {
                add(arrayListOf(Player("Gatien"), Player("Gatien")))
            }
            gameRunner.runGame(game)
        }
    }

    @Test
    fun `game with 6 gold should not throw`() {
        assertDoesNotThrow {
            val game = Game(minimalGoldRequired = 6).apply {
                add(arrayListOf(Player("Gatien"), Player("Gatien")))
            }
            gameRunner.runGame(game)
        }
    }

    @Test
    fun `game more than 6 gold should not throw`() {
        val console = SpyConsole()
        assertDoesNotThrow {
            val game = Game(console, minimalGoldRequired = 7).apply {
                add(arrayListOf(Player("Gatien"), Player("Gatien")))
            }
            gameRunner.runGame(game)
        }
    }
    //endregion

    //TODO Feature 7

    //TODO Feature 8

    //TODO Feature 9

    //region COULD REPLAY GAME [FEATURE #10]
    @Test
    fun `cannot replay game if no game has been played`() {
        assertThrows<CouldNotReplayGameError> {
            gameRunner.replay()
        }
    }

    @Test
    fun `game can be replayed`() {
        val game = Game().apply {
            add(arrayListOf(Player("Gatien"), Player("Gatien")))
        }
        val defaultParameters = game.getParameters()
        gameRunner.runGame(game)
        assertDoesNotThrow {
            gameRunner.replay()
        }
        assertEquals(defaultParameters, game.getParameters())
    }
    //endregion

    //TODO Feature 11

    //region EXPANSION PACK [FEATURE #12]
    @Test
    fun `when NOT using expansion pack, specific categories MUST NOT be played`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player("Gatien"), Player("Gatien")))
        }
        GameRunner.runGame(game)
        assert(!console.getContent().contains("The category is RAP"))
        assert(!console.getContent().contains("The category is PHILO"))
        assert(!console.getContent().contains("The category is LITTERATURE"))
        assert(!console.getContent().contains("The category is GEO"))
    }

    @Test
    fun `when using expansion pack, specific categories must be played`() {
        val console = SpyConsole()
        val game = Game(console, shouldUseExpansionPack = true).apply {
            add(arrayListOf(Player("Gatien"), Player("Gatien")))
        }
        GameRunner.runGame(
            game,
            shouldUseRandom = false,
            hasCorrectAnswered = true,
            shouldGoInPrison = true,
            shouldStopAfterNumberOfQuestions = 100
        )
        assert(console.getContent().contains("The category is RAP"))
        assert(console.getContent().contains("The category is PHILO"))
        assert(console.getContent().contains("The category is LITTERATURE"))
        assert(console.getContent().contains("The category is GEO"))
    }
    //endregion

    //region PLAYERS EARN JOKER AFTER MULTIPLE TIMES IN JAIL [FEATURE #16]
    @Test
    fun `player should earn joker after 3 times in jail`() {
        val console = SpyConsole()
        val player = Player("Louis")
        val game = Game(console).apply {
            add(arrayListOf(player, Player("Gatien")))
        }
        assert(!player.hasJoker)
        GameRunner.runGame(game, shouldUseRandom = false, hasCorrectAnswered = true, shouldGoInPrison = true)
        assert(player.timesGotInPrison >= 3 && console.getContent().contains("${player.name} uses his joker"))
    }
    //endregion
}
