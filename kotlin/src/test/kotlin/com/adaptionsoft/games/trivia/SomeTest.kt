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
        var counter = 0
        val console = SpyConsole()
        val game = Game(console, shouldReplaceRockByTechno = true).apply {
            add(arrayListOf(Player("Gatien"), Player("Gatien")))
        }
        gameRunner.runGame(game)
        // On rejoue la partie car vu que l'on joue avec de l'aléatoire, il est possible que la catégorie ne soit pas jouée
        while (counter < 5 && !console.getContent().contains("The category is TECHNO")) {
            counter--
            gameRunner.replay()
        }
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
        gameRunner.runGame(game, shouldUseRandom = false, hasCorrectAnswered = true, shouldGoInPrison = false)
        assert(console.getContent().contains("Gatien now has 1 Gold Coins"))
        assert(!console.getContent().contains("Gatien now has 2 Gold Coins"))
        assert(console.getContent().contains("Gatien now has 3 Gold Coins"))
        assert(!console.getContent().contains("Gatien now has 4 Gold Coins"))
        assert(!console.getContent().contains("Gatien now has 5 Gold Coins"))
        assert(console.getContent().contains("Gatien now has 6 Gold Coins"))
    }
    //endregion

    //region PLAYER LOOSING CHOOSE NEXT CATEGORY [FEATURE #5]
    @Test
    fun `player should choose next category when loosing`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player(name = "Gatien", nextCategoryWhenLoosing = Game.Category.GEO), Player(name = "Louis")))
        }
        gameRunner.runGame(game, shouldUseRandom = false, shouldGoInPrison = true, hasCorrectAnswered = false)
        assert(console.getContent().contains("Question was incorrectly answeredGatien was sent to the penalty boxLouis is the current playerThey have rolled a 2Louis's new location is 2The category is GEO"))
    }
    //endregion

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

    //region CHANCES THAT A PLAYER GETS OUT OF PRISON MUST BE 1/X WHERE X IS TIMES HE GOT IN JAIL [FEATURE #7]
    @Test
    fun `player has 1 divide by X chance to exit jail when X is times he got in jail`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player(name = "Gatien"), Player(name = "Louis")))
        }
        // On force une partie où les joueurs restent en prison et ne font que des mauvaises réponses
        gameRunner.runGame(game, shouldUseRandom = false, shouldGoInPrison = true, hasCorrectAnswered = false)

        // On vérifie que Gatien a 1/1 chance de quitter la prison
        assert(console.getContent().contains("Gatien has 1/1 chance to exit jail"))
        val afterFirstChanceToExitJail = console.getContent().split("Gatien has 1/1 chance to exit jail")[1]
        assert(!afterFirstChanceToExitJail.contains("Gatien has 1/1 chance to exit jail"))

        // Puis 1/2
        assert(afterFirstChanceToExitJail.contains("Gatien has 1/2 chance to exit jail"))
        val afterSecondChanceToExitJail = console.getContent().split("Gatien has 1/2 chance to exit jail")[1]
        assert(!afterSecondChanceToExitJail.contains("Gatien has 1/2 chance to exit jail"))

        // Et enfin 1/3
        assert(afterFirstChanceToExitJail.contains("Gatien has 1/3 chance to exit jail"))
        val afterThirdChanceToExitJail = console.getContent().split("Gatien has 1/3 chance to exit jail")[1]
        assert(!afterThirdChanceToExitJail.contains("Gatien has 1/3 chance to exit jail"))
    }
    //endregion

    //region GAME CONTINUES WITH REMAINING PLAYERS UNTIL A LEADERBOARD OF THREE PLAYERS IS COMPLETED [FEATURE #8]
    @Test
    fun `game should continue while leaderboard is not completed`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player(name = "Gatien"), Player(name = "Louis"), Player(name = "Arthur"), Player(name = "Antoine")))
        }
        gameRunner.runGame(game, shouldUseRandom = false, shouldGoInPrison = false)
        val afterFirstWinning = console.getContent().split("Gatien win")[1]
        assert(!afterFirstWinning.contains("Gatien"))
        val afterSecondWinning = afterFirstWinning.split("Louis win")[1]
        assert(!afterSecondWinning.contains("Louis"))
        val afterThirdWinning = afterSecondWinning.split("Arthur win")[1]
        assert(!afterThirdWinning.contains("Arthur"))
    }
    //endregion

    //region EVERY TURN SPENT IN JAIL INCREASES CHANCES THAT PLAYER GOES OUT BY 10% [FEATURE #9]
    @Test
    fun `after each turn spent in jail, player should have 10 more percent to goes out`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player(name = "Gatien"), Player(name = "Louis")))
        }

        // On force une partie où les joueurs restent en prison et ne font que des mauvaises réponses
        gameRunner.runGame(game, shouldUseRandom = false, shouldGoInPrison = true, hasCorrectAnswered = false)

        // On vérifie que Gatien a 10% de chance supplémentaire de quitter la prison
        assert(console.getContent().contains("Gatien has 10% more chance to exit jail with 1 turn in jail"))
        val afterFirstChanceToExitJail = console.getContent().split("Gatien has 10% more chance to exit jail with 1 turn in jail")[1]
        assert(!afterFirstChanceToExitJail.contains("Gatien has 10% more chance to exit jail with 1 turn in jail"))

        // Puis 20%
        assert(afterFirstChanceToExitJail.contains("Gatien has 20% more chance to exit jail with 2 turn in jail"))
        val afterSecondChanceToExitJail = console.getContent().split("Gatien has 20% more chance to exit jail with 2 turn in jail")[1]
        assert(!afterSecondChanceToExitJail.contains("Gatien has 20% more chance to exit jail with 2 turn in jail"))

        // Et enfin 30%
        assert(afterFirstChanceToExitJail.contains("Gatien has 30% more chance to exit jail with 3 turn in jail"))
        val afterThirdChanceToExitJail = console.getContent().split("Gatien has 30% more chance to exit jail with 3 turn in jail")[1]
        assert(!afterThirdChanceToExitJail.contains("Gatien has 30% more chance to exit jail with 3 turn in jail"))
    }
    //endregion

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

    //region NUMBER OF CELLS IN PRISON IS NOW CONFIGURABLE [FEATURE #11]
    @Test
    fun `number of cells should be default to unlimited`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player(name = "Gatien"), Player(name = "Louis")))
        }
        gameRunner.runGame(game)
        assert(!console.getContent().contains("is getting out penalty box because of number of places in jail"))
    }

    @Test
    fun `if number of cells in jail is limited, latest player in jail is freed`() {
        val console = SpyConsole()
        val game = Game(console, cellsInJail = 2).apply {
            add(arrayListOf(Player(name = "Gatien"), Player(name = "Louis"), Player(name = "Arthur")))
        }
        gameRunner.runGame(game, shouldUseRandom = false, shouldGoInPrison = true, hasCorrectAnswered = false)
        assert(console.getContent().contains("Arthur was sent to the penalty boxGatien is getting out penalty box because of number of places in jail"))
    }
    //endregion

    //region EXPANSION PACK [FEATURE #12]
    @Test
    fun `when NOT using expansion pack, specific categories MUST NOT be played`() {
        val console = SpyConsole()
        val game = Game(console).apply {
            add(arrayListOf(Player("Gatien"), Player("Gatien")))
        }
        gameRunner.runGame(game)
        assert(!console.getContent().contains("The category is RAP"))
        assert(!console.getContent().contains("The category is PHILO"))
        assert(!console.getContent().contains("The category is LITTERATURE"))
        assert(!console.getContent().contains("The category is GEO"))
        assert(!console.getContent().contains("The category is PEOPLE"))
    }

    @Test
    fun `when using expansion pack, specific categories must be played`() {
        val console = SpyConsole()
        val game = Game(console, shouldUseExpansionPack = true).apply {
            add(arrayListOf(Player("Gatien"), Player("Gatien")))
        }
        gameRunner.runGame(
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
        assert(console.getContent().contains("The category is PEOPLE"))
    }
    //endregion
}
